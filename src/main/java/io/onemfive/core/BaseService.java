package io.onemfive.core;

import io.onemfive.core.infovault.InfoVault;
import io.onemfive.data.*;

import java.util.Properties;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public abstract class BaseService implements MessageConsumer, Service, LifeCycle {

    protected boolean orchestrator = false;
    private MessageProducer producer;
    protected InfoVault infoVault;

    public BaseService() {
        infoVault = InfoVault.getInstance();
    }

    public BaseService(MessageProducer producer) {
        infoVault = InfoVault.getInstance();
        this.producer = producer;
    }

    public MessageProducer getProducer() {
        return producer;
    }

    public void setProducer(MessageProducer producer) {
        this.producer = producer;
    }

    @Override
    public boolean receive(Envelope envelope) {
        System.out.println(BaseService.class.getSimpleName()+": Envelope received by service. Handling...");
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
        System.out.println("Can't route envelope:"+envelope);
    }

    protected final void endRoute(Envelope envelope) {
        System.out.println("End of route and no client to return to:"+envelope);
    }

    @Override
    public void handleDocument(Envelope envelope) {System.out.println(this.getClass().getName()+" has not implemented handleDocument().");}

    @Override
    public void handleEvent(Envelope envelope) {System.out.println(this.getClass().getName()+" has not implemented handleEvent().");}

    @Override
    public void handleCommand(Envelope envelope) {System.out.println(this.getClass().getName()+" has not implemented handleCommand().");}

    @Override
    public void handleHeaders(Envelope envelope) {System.out.println(this.getClass().getName()+" has not implemented handleHeaders().");}

    /**
     * Supports synchronous high-priority calls from ServiceBus and asynchronous low-priority calls from receive()
     * @param envelope
     */
    final void runCommand(Envelope envelope) {
        System.out.println("Running command by service...");
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
        System.out.println(BaseService.class.getSimpleName()+": Sending reply to service bus...");
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

    @Override
    public boolean start(Properties properties) {
        return false;
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
        return false;
    }

    @Override
    public boolean gracefulShutdown() {
        return false;
    }
}
