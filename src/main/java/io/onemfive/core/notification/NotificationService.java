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
    /**
     * To publish an EventMessage, ensure the Envelope contains one.
     */
    public static final String OPERATION_PUBLISH = "PUBLISH";

    private Map<EventMessage.Type,Map<String,List<Subscription>>> subscriptions;

    public NotificationService(MessageProducer producer, ServiceStatusListener serviceStatusListener) {
        super(producer, serviceStatusListener);
    }

    @Override
    public void handleDocument(Envelope e) {
        Route r = e.getRoute();
        String operation = r.getOperation();
        switch(operation) {
            case OPERATION_SUBSCRIBE:{subscribe(e);break;}
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
        Map<String,List<Subscription>> st = subscriptions.get(r.getType());
        List<Subscription> s;
        if(r.getFilter() == null) {
            s = st.get(null);
        } else {
            s = st.get(r.getFilter());
        }
        s.add(r.getSubscription());
        LOG.info("Subscription added.");
    }

    private void publish(final Envelope e) {
        LOG.info("Received publish request...");
        EventMessage m = (EventMessage)e.getMessage();
        Map<String,List<Subscription>> st = subscriptions.get(m.getType());
        List<Subscription> s = null;
        switch (m.getType()) {
            case EMAIL: {
                LOG.info("Publish Type is Email");
                s = st.get(null);
                break;
            }
            case ERROR: {
                s = st.get(null);
                break;
            }
            case EXCEPTION: {
                s = st.get(null);
                break;
            }
            case STATUS_BUS: {
                s = st.get(null);
                break;
            }
            case STATUS_CLIENT: {
                s = st.get(null);
                break;
            }
            case STATUS_SENSOR: {
                if(st.get(null).size() > 0) {
                    // Get Subscriptions for Sensors with no filtering
                    s = st.get(null);
                }
                if(m.getName() != null && !m.getName().isEmpty() && st.get(m.getName()) != null && st.get(m.getName()).size() > 0) {
                    // Get Subscriptions for Sensors filtered by provided name if present
                    if(s == null) {
                        // No unfiltered Subscriptions
                        s = st.get(m.getName());
                    } else {
                        // Add to unfiltered Subscriptions
                        s.addAll(st.get(m.getName()));
                    }
                }
                break;
            }
            case STATUS_SERVICE: {
                if(st.get(null).size() > 0) {
                    // Get Subscriptions for Services with no filtering
                    s = st.get(null);
                }
                if(m.getName() != null && !m.getName().isEmpty() && st.get(m.getName()) != null && st.get(m.getName()).size() > 0) {
                    // Get Subscriptions for Services filtered by provided name if present
                    if(s == null) {
                        // No unfiltered Subscriptions
                        s = st.get(m.getName());
                    } else {
                        // Add to unfiltered Subscriptions
                        s.addAll(st.get(m.getName()));
                    }
                }
                break;
            }
        }

        if(s != null) {
            for(final Subscription sub : s) {
                LOG.info("Notifying subscription of event...");
                // Directly notify in separate thread
                // TODO: Move to WorkerThreadPool to control CPU usage
                new AppThread(new Runnable() {
                    @Override
                    public void run() {
                        sub.notifyOfEvent(e);
                    }
                }).start();
            }
        }
    }

    @Override
    public boolean start(Properties properties) {
        LOG.info("Starting...");
        updateStatus(ServiceStatus.STARTING);

        subscriptions = new HashMap<>();
        // For each EventMessage.Type, set a HashMap<String,List<Subscription>>
        // and add a null filtered list for Subscriptions with no filters.
        subscriptions.put(EventMessage.Type.EMAIL, new HashMap<String, List<Subscription>>());
        subscriptions.get(EventMessage.Type.EMAIL).put(null,new ArrayList<Subscription>());
        subscriptions.put(EventMessage.Type.EXCEPTION, new HashMap<String, List<Subscription>>());
        subscriptions.get(EventMessage.Type.EXCEPTION).put(null,new ArrayList<Subscription>());
        subscriptions.put(EventMessage.Type.ERROR, new HashMap<String, List<Subscription>>());
        subscriptions.get(EventMessage.Type.ERROR).put(null,new ArrayList<Subscription>());
        subscriptions.put(EventMessage.Type.STATUS_BUS, new HashMap<String, List<Subscription>>());
        subscriptions.get(EventMessage.Type.STATUS_BUS).put(null,new ArrayList<Subscription>());
        subscriptions.put(EventMessage.Type.STATUS_CLIENT, new HashMap<String, List<Subscription>>());
        subscriptions.get(EventMessage.Type.STATUS_CLIENT).put(null,new ArrayList<Subscription>());
        subscriptions.put(EventMessage.Type.STATUS_SENSOR, new HashMap<String, List<Subscription>>());
        subscriptions.get(EventMessage.Type.STATUS_SENSOR).put(null,new ArrayList<Subscription>());
        subscriptions.put(EventMessage.Type.STATUS_SERVICE, new HashMap<String, List<Subscription>>());
        subscriptions.get(EventMessage.Type.STATUS_SERVICE).put(null,new ArrayList<Subscription>());

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
