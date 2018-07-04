package io.onemfive.core.client;

import io.onemfive.core.MessageProducer;
import io.onemfive.data.ServiceCallback;
import io.onemfive.data.Envelope;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * A simple client for making requests to SC services.
 *
 * @author objectorange
 */
final class SimpleClient implements Client {

    private static final Logger LOG = Logger.getLogger(SimpleClient.class.getName());

    private Map<Long,ServiceCallback> claimCheck;
    private Long id;
    private MessageProducer producer;
    private List<ClientStatusListener> clientStatusListeners = new ArrayList<>();

    SimpleClient(Long id, MessageProducer producer) {
        this.id = id;
        this.producer = producer;
        this.claimCheck = new HashMap<>();
    }

    void updateClientStatus(ClientAppManager.Status status) {
        for(ClientStatusListener l : clientStatusListeners) {
            l.clientStatusChanged(status);
        }
    }

    @Override
    public Long getId() {
        return id;
    }


    @Override
    public void request(Envelope e) {
        producer.send(e);
    }

    @Override
    public void request(Envelope e, ServiceCallback cb) {
        LOG.finer("Sending to service bus message channel");
        e.setClient(id);
        producer.send(e);
        // Save callback for later retrieval using envelope id for correlation
        claimCheck.put(e.getId(), cb);
    }

    @Override
    public void notify(Envelope e) {
        LOG.finer("Sending to ServiceCallback");
        ServiceCallback cb = claimCheck.get(e.getId());
        cb.reply(e);
        claimCheck.remove(e.getId());
    }

    @Override
    public void registerClientStatusListener(ClientStatusListener listener) {
        clientStatusListeners.add(listener);
    }
}
