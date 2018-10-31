package io.onemfive.core.notification;

import io.onemfive.core.BaseService;
import io.onemfive.core.MessageProducer;
import io.onemfive.core.ServiceStatus;
import io.onemfive.core.ServiceStatusListener;
import io.onemfive.core.util.AppThread;
import io.onemfive.data.*;
import io.onemfive.data.util.DLC;

import java.util.*;
import java.util.logging.Logger;

/**
 * Provides notifications of publishing events for subscribers.
 *
 * TODO: Replace callbacks with service calls to improve scalability and thread contention
 *
 * @author objectorange
 */
public class NotificationService extends BaseService {

    private static final Logger LOG = Logger.getLogger(NotificationService.class.getName());

    /**
     * To subscribe to EventMessages, send a SubscriptionRequest as a DocumentMessage to Service using
     * OPERATION_SUBSCRIBE as operation. SubscriptionRequest must specify EventMessage.Type and optionally a Filter.
     *
     * Filters available for each EventMessage.Type:
     *
     * EMAIL: Internal filtering automatic based on end user's owned DIDs.
     * EXCEPTION: Internal filtering automatically; Client exceptions can be subscribed to by Clients (not yet implemented).
     * ERROR: No filters supported
     * STATUS_SENSOR: String representing full name of Sensor class, e.g. io.onemfive.core.sensors.i2p.I2PSensor
     * STATUS_SERVICE: String representing full name of Service class, e.g. io.onemfive.core.sensors.SensorService
     * STATUS_BUS: No filters supported
     * STATUS_CLIENT: No filters supported
     * TEXT: Can filter by name if provided. For I2P messages, the name is the sender's base64 encoded key.
     *
     */
    public static final String OPERATION_SUBSCRIBE = "SUBSCRIBE";
    public static final String OPERATION_UNSUBSCRIBE = "UNSUBSCRIBE";
    /**
     * To publish an EventMessage, ensure the Envelope contains one.
     */
    public static final String OPERATION_PUBLISH = "PUBLISH";

    private Map<String,Map<String,List<Subscription>>> subscriptions;

    public NotificationService(MessageProducer producer, ServiceStatusListener serviceStatusListener) {
        super(producer, serviceStatusListener);
    }

    @Override
    public void handleDocument(Envelope e) {
        Route r = e.getRoute();
        String operation = r.getOperation();
        switch(operation) {
            case OPERATION_SUBSCRIBE:{subscribe(e);break;}
            case OPERATION_UNSUBSCRIBE:{unsubscribe(e);break;}
            default: deadLetter(e);
        }
    }

    @Override
    public void handleEvent(Envelope e) {
        Route r = e.getRoute();
        String operation = r.getOperation();
        switch(operation) {
            case OPERATION_PUBLISH:{publish(e);break;}
            default: deadLetter(e);
        }
    }

    private void subscribe(Envelope e) {
        LOG.info("Received subscribe request...");
        SubscriptionRequest r = (SubscriptionRequest)DLC.getData(SubscriptionRequest.class,e);
        LOG.info("Subscription for type: "+r.getType().name());
        Map<String,List<Subscription>> s = subscriptions.get(r.getType().name());
        if(r.getFilter() == null) {
            LOG.info("With no filters.");
            s.get(null).add(r.getSubscription());
        } else {
            LOG.info("With filter: "+r.getFilter());
            if(s.get(r.getFilter()) == null)
                s.put(r.getFilter(), new ArrayList<Subscription>());
            s.get(r.getFilter()).add(r.getSubscription());
        }
        LOG.info("Subscription added.");
    }

    private void unsubscribe(Envelope e) {
        LOG.info("Received unsubscribe request...");
        SubscriptionRequest r = (SubscriptionRequest)DLC.getData(SubscriptionRequest.class,e);
        Map<String,List<Subscription>> s = subscriptions.get(r.getType().name());
        if(r.getFilter() == null) {
            s.get(null).remove(r.getSubscription());
        } else {
            s.get(r.getFilter()).remove(r.getSubscription());
        }
        LOG.info("Subscription removed.");
    }

    private void publish(final Envelope e) {
        LOG.info("Received publish request...");
        List<Subscription> toNotify = new ArrayList<>();
        EventMessage m = (EventMessage)e.getMessage();
        LOG.info("For type: "+m.getType());
        Map<String,List<Subscription>> s = subscriptions.get(m.getType());
        if(s.size() == 0) {
            LOG.info("No subscriptions for type: "+m.getType());
            return;
        }
        final List<Subscription> subs = s.get(null);
        if(subs.size() == 0) {
            LOG.info("No subscriptions without filters.");
        } else {
            toNotify.addAll(subs);
        }
        LOG.info("With name to filter on: " + m.getName());
        final List<Subscription> filteredSubs = s.get(m.getName());
        if(filteredSubs.size() == 0) {
            LOG.info("No subscriptions for filter: "+m.getName());
        } else {
            toNotify.addAll(filteredSubs);
        }
        LOG.info("Notifying "+toNotify.size()+" subscriber(s) of event...");
        // Directly notify in separate thread
        for(final Subscription sub: toNotify) {
            // TODO: Move to WorkerThreadPool to control CPU usage
            new AppThread(new Runnable() {
                @Override
                public void run() {
                    sub.notifyOfEvent(e);
                }
            }).start();
        }
    }

    private Map<String,List<Subscription>> buildNewMap() {
        List<Subscription> l = new ArrayList<>();
        Map<String,List<Subscription>> m = new HashMap<>();
        m.put(null,l);
        return m;
    }

    @Override
    public boolean start(Properties properties) {
        super.start(properties);
        LOG.info("Starting...");
        updateStatus(ServiceStatus.STARTING);

        subscriptions = new HashMap<>();
        // For each EventMessage.Type, set a HashMap<String,Subscription>
        // and add a null filtered list for Subscriptions with no filters.

        subscriptions.put(EventMessage.Type.EMAIL.name(), buildNewMap());
        subscriptions.put(EventMessage.Type.EXCEPTION.name(), buildNewMap());
        subscriptions.put(EventMessage.Type.ERROR.name(), buildNewMap());
        subscriptions.put(EventMessage.Type.STATUS_BUS.name(), buildNewMap());
        subscriptions.put(EventMessage.Type.STATUS_CLIENT.name(), buildNewMap());
        subscriptions.put(EventMessage.Type.STATUS_DID.name(), buildNewMap());
        subscriptions.put(EventMessage.Type.STATUS_SENSOR.name(), buildNewMap());
        subscriptions.put(EventMessage.Type.STATUS_SERVICE.name(), buildNewMap());
        subscriptions.put(EventMessage.Type.TEXT.name(), buildNewMap());

        updateStatus(ServiceStatus.RUNNING);
        LOG.info("Started.");
        return true;
    }

    @Override
    public boolean shutdown() {
        super.shutdown();
        LOG.info("Shutting down....");
        updateStatus(ServiceStatus.SHUTTING_DOWN);

        updateStatus(ServiceStatus.SHUTDOWN);
        LOG.info("Shutdown");
        return true;
    }

    @Override
    public boolean gracefulShutdown() {
        // TODO:
        return shutdown();
    }
}
