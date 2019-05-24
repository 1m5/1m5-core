package io.onemfive.core;

import io.onemfive.core.infovault.InfoVaultDB;
import io.onemfive.data.*;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * A base for all Services to provide a common framework for them.
 *
 * @author objectorange
 */
public abstract class BaseService implements MessageConsumer, Service, LifeCycle {

    private static final Logger LOG = Logger.getLogger(BaseService.class.getName());

    protected boolean orchestrator = false;
    protected MessageProducer producer;
    protected InfoVaultDB infoVaultDB;
    private File serviceDirectory;

    private ServiceStatus serviceStatus;
    private List<ServiceStatusListener> serviceStatusListeners = new ArrayList<>();

    public BaseService() {

    }

    public BaseService(MessageProducer producer, ServiceStatusListener listener) {
        if(listener != null)
            serviceStatusListeners.add(listener);
        this.producer = producer;
    }

    public ServiceStatus getServiceStatus() {
        return serviceStatus;
    }

    public void registerServiceStatusListener(ServiceStatusListener listener) {
        serviceStatusListeners.add(listener);
    }

    public void unregisterServiceStatusListener(ServiceStatusListener listener) {
        serviceStatusListeners.remove(listener);
    }

    protected void updateStatus(ServiceStatus serviceStatus) {
        this.serviceStatus = serviceStatus;
        if(serviceStatusListeners != null) {
            for(ServiceStatusListener l : serviceStatusListeners) {
                l.serviceStatusChanged(this.getClass().getName(), serviceStatus);
            }
        }
    }

    public MessageProducer getProducer() {
        return producer;
    }

    public void setProducer(MessageProducer producer) {
        this.producer = producer;
    }

    @Override
    public final boolean receive(Envelope envelope) {
        LOG.finer("Envelope received by service. Handling...");
        if(envelope.getMessage() instanceof DocumentMessage)
            handleDocument(envelope);
        else if(envelope.getMessage() instanceof EventMessage)
            handleEvent(envelope);
        else if(envelope.getMessage() instanceof CommandMessage)
            runCommand(envelope);
        else
            handleHeaders(envelope);
        // If not orchestrator, always return a reply.
        // If orchestrator, it will determine if a reply should be sent.
        if(!orchestrator) {
            reply(envelope);
        }
        return true;
    }

    protected final void deadLetter(Envelope envelope) {
        LOG.warning("Can't route envelope:"+envelope);
    }

    protected final void endRoute(Envelope envelope) {
        LOG.fine("End of route and no client to return to:"+envelope);
    }

    @Override
    public void handleDocument(Envelope envelope) {LOG.warning(this.getClass().getName()+" has not implemented handleDocument().");}

    @Override
    public void handleEvent(Envelope envelope) {LOG.warning(this.getClass().getName()+" has not implemented handleEvent().");}

    @Override
    public void handleCommand(Envelope envelope) {LOG.warning(this.getClass().getName()+" has not implemented handleCommand().");}

    @Override
    public void handleHeaders(Envelope envelope) {LOG.warning(this.getClass().getName()+" has not implemented handleHeaders().");}

    /**
     * Supports synchronous high-priority calls from ServiceBus and asynchronous low-priority calls from receive()
     * @param envelope
     */
    final void runCommand(Envelope envelope) {
        LOG.finer("Running command by service...");
        CommandMessage m = (CommandMessage)envelope.getMessage();
        switch(m.getCommand()) {
            case Shutdown: {shutdown();break;}
            case Restart: {restart();break;}
            case Start: {
                Properties p = (Properties)envelope.getHeader(Properties.class.getName());
                start(p);
            }
        }
    }

    protected final void reply(Envelope envelope) {
        LOG.finest("Sending reply to service bus...");
        int maxAttempts = 30;
        int attempts = 0;
        // Create new Envelope instance with same ID, Headers, and Message so that Message Channel sees it as a different envelope.
        Envelope newEnvelope = Envelope.envelopeFactory(envelope);
        // Don't set if the orchestration service
        if(!orchestrator) {
            Route route = envelope.getRoute();
            if(route != null) route.setRouted(true);
        }
        while(!producer.send(newEnvelope) && ++attempts <= maxAttempts) {
            synchronized (this) {
                try {
                    this.wait(100);
                } catch (InterruptedException e) {}
            }
        }
    }

    public final File getServiceDirectory() {
        return serviceDirectory;
    }

    @Override
    public boolean start(Properties p) {
        if(p==null) {
            LOG.severe("Properties for start are required.");
            return false;
        }
        infoVaultDB = OneMFiveAppContext.getInstance().getInfoVaultDB();
        String baseStr = p.getProperty("1m5.dir.base");
        File servicesFolder = new File(baseStr + "/services");
        if(!servicesFolder.exists() && !servicesFolder.mkdir()) {
            LOG.severe("Unable to create services directory: " + baseStr + "/services");
            return false;
        }
        try {
            p.setProperty("1m5.dir.services",servicesFolder.getCanonicalPath());
        } catch (IOException e) {
            LOG.warning(e.getLocalizedMessage());
        }
        String serviceDirectoryPath = null;
        try {
            serviceDirectoryPath = servicesFolder.getCanonicalPath()+"/"+this.getClass().getName();
        } catch (IOException e) {
            LOG.warning(e.getLocalizedMessage());
            return false;
        }
        serviceDirectory = new File(serviceDirectoryPath);
        if(!serviceDirectory.exists() && !serviceDirectory.mkdir()) {
            LOG.severe("Unable to create service directory: " + serviceDirectoryPath);
            return false;
        } else {
            p.setProperty("1m5.dir.services."+this.getClass().getName(), serviceDirectoryPath);
        }
        return true;
    }

    @Override
    public boolean pause() {
        return false;
    }

    @Override
    public boolean unpause() {
        return false;
    }

    @Override
    public boolean restart() {
        return false;
    }

    @Override
    public boolean shutdown() {
        if(infoVaultDB != null && infoVaultDB.getStatus() == InfoVaultDB.Status.Running)
            infoVaultDB.teardown();
        return true;
    }

    @Override
    public boolean gracefulShutdown() {
        return true;
    }

}
