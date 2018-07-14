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
 * @author objectorange
 */
public class NotificationService extends BaseService {

    private static final Logger LOG = Logger.getLogger(NotificationService.class.getName());

    /**
     * To subscribe to EventMessages, send a SubscriptionRequest as a DocumentMessage to Service using
     * OPERATION_SUBCRIBE as operation. SubscriptionRequest must specify EventMessage.Type and optionally a Filter.
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
     *
     */
    public static final String OPERATION_SUBSCRIBE = "SUBSCRIBE";
    public static final String OPERATION_UNSUBSCRIBE = "UNSUBSCRIBE";
    /**
     * To publish an EventMessage, ensure the Envelope contains one.
     */
    public static final String OPERATION_PUBLISH = "PUBLISH";

    private Subscription demoSub;

    private Map<String,Map<String,Subscription>> subscriptions;

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
        demoSub = r.getSubscription();
        LOG.info("Subscription for type: "+r.getType().name());
        Map<String,Subscription> s = subscriptions.get(r.getType().name());
        if(r.getFilter() == null) {
            // No filter, set default subscription list
            LOG.info("With no filters.");
            s.put(null,r.getSubscription());
        } else {
            LOG.info("With filter: "+r.getFilter());
            s.put(r.getFilter(),r.getSubscription());
        }
        LOG.info("Subscription added.");
    }

    private void unsubscribe(Envelope e) {
        LOG.info("Received unsubscribe request...");
        SubscriptionRequest r = (SubscriptionRequest)DLC.getData(SubscriptionRequest.class,e);
        Map<String,Subscription> s = subscriptions.get(r.getType().name());
        if(r.getFilter() == null) {
            // No filter, set default subscription list
            s.remove(null);
        } else {
            s.remove(r.getFilter());
        }
        LOG.info("Subscription removed.");
    }

    private void publish(final Envelope e) {
        LOG.info("Received publish request...");
        EventMessage m = (EventMessage)e.getMessage();
        LOG.info("For type: "+m.getType());
        Map<String,Subscription> s = subscriptions.get(m.getType());
        LOG.info("With name to filter on: "+m.getName());
        // Temporarily comment out for Demo as we wont be able to get Alice's keys after install
//        final Subscription sub = s.get(m.getName());
        final Subscription sub = demoSub;
        if(sub != null) {
            LOG.info("Notifying subscription of event...");
            // Directly notify in separate thread
            // TODO: Move to WorkerThreadPool to control CPU usage
            new AppThread(new Runnable() {
                @Override
                public void run() {
                    sub.notifyOfEvent(e);
                }
            }).start();
        } else {
            LOG.info("");
        }
    }

    @Override
    public boolean start(Properties properties) {
        LOG.info("Starting...");
        updateStatus(ServiceStatus.STARTING);

        subscriptions = new HashMap<>();
        // For each EventMessage.Type, set a HashMap<String,List<Subscription>>
        // and add a null filtered list for Subscriptions with no filters.
        subscriptions.put(EventMessage.Type.EMAIL.name(), new HashMap<String, Subscription>());
        subscriptions.put(EventMessage.Type.EXCEPTION.name(), new HashMap<String, Subscription>());
        subscriptions.put(EventMessage.Type.ERROR.name(), new HashMap<String, Subscription>());
        subscriptions.put(EventMessage.Type.STATUS_BUS.name(), new HashMap<String, Subscription>());
        subscriptions.put(EventMessage.Type.STATUS_CLIENT.name(), new HashMap<String, Subscription>());
        subscriptions.put(EventMessage.Type.STATUS_SENSOR.name(), new HashMap<String, Subscription>());
        subscriptions.put(EventMessage.Type.STATUS_SERVICE.name(), new HashMap<String, Subscription>());

        updateStatus(ServiceStatus.RUNNING);
        LOG.info("Started.");
        return true;
    }

    @Override
    public boolean shutdown() {
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
