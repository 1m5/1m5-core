package io.onemfive.core.admin;

import io.onemfive.core.*;
import io.onemfive.core.bus.ServiceBus;
import io.onemfive.core.bus.ServiceNotAccessibleException;
import io.onemfive.core.bus.ServiceNotSupportedException;
import io.onemfive.core.bus.ServiceRegisteredException;
import io.onemfive.data.util.DLC;
import io.onemfive.data.Envelope;
import io.onemfive.data.Route;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Manages the bus and its services including auto-install of new services,
 * auto-updates, and auto-uninstalls.
 *
 * Supports registering services by client applications.
 *
 * @author objectorange
 */
public class AdminService extends BaseService {

    private static final Logger LOG = Logger.getLogger(AdminService.class.getName());

    public static final String OPERATION_REGISTER_SERVICES = "REGISTER_SERVICES";
    public static final String OPERATION_LIST_SERVICES = "LIST_SERVICES";

    private ServiceBus serviceBus;

    public AdminService(MessageProducer producer, ServiceStatusListener serviceStatusListener) {
        super(producer, serviceStatusListener);
        serviceBus = (ServiceBus)producer;
    }

    @Override
    public void handleDocument(Envelope e) {
        Route route = e.getRoute();
        switch(route.getOperation()) {
            case OPERATION_REGISTER_SERVICES:{registerServices(e);break;}
            case OPERATION_LIST_SERVICES:{listServices(e);break;}
            default: deadLetter(e);
        }
    }

    private void registerServices(Envelope e) {
        List<Class> servicesToRegister = (List<Class>)DLC.getEntity(e);
        LOG.info("Services to register: "+servicesToRegister);
        Properties p = (Properties)DLC.getData(Properties.class, e);
        Map<String,List<ServiceStatusObserver>> serviceStatusObservers = (Map<String,List<ServiceStatusObserver>>)DLC.getData(ServiceStatusObserver.class, e);
        List<ServiceStatusObserver> observers;
        for(Class c : servicesToRegister) {
            try {
                // Look for observers
                if(serviceStatusObservers!=null && serviceStatusObservers.get(c.getName())!=null) {
                    observers = serviceStatusObservers.get(c.getName());
                } else {
                    observers = null;
                }
                // Register the Service
                serviceBus.register(c, p, observers);
            } catch (ServiceNotAccessibleException e1) {
                DLC.addException(e1, e);
            } catch (ServiceNotSupportedException e1) {
                DLC.addException(e1, e);
            } catch (ServiceRegisteredException e1) {
                DLC.addException(e1, e);
            }
        }
    }

    private void listServices(Envelope e) {
        DLC.addEntity(serviceBus.listServices(), e);
    }

    @Override
    public boolean start(Properties properties) {
        super.start(properties);
        LOG.info("Starting...");
        updateStatus(ServiceStatus.STARTING);

        updateStatus(ServiceStatus.RUNNING);
        LOG.info("Started.");
        return true;
    }

    @Override
    public boolean shutdown() {
        super.shutdown();
        LOG.info("Shutting down...");
        updateStatus(ServiceStatus.SHUTTING_DOWN);

        updateStatus(ServiceStatus.SHUTDOWN);
        LOG.info("Shutdown");
        return true;
    }

    @Override
    public boolean gracefulShutdown() {
        return shutdown();
    }
}
