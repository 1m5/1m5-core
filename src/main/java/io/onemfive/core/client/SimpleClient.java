package io.onemfive.core.client;

import io.onemfive.core.MessageProducer;
import io.onemfive.data.ServiceCallback;
import io.onemfive.data.Envelope;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple client for making requests to SC services.
 *
 * @author objectorange
 */
final class SimpleClient implements Client {

    private Map<Long,ServiceCallback> claimCheck;
    private Long id;
    private MessageProducer producer;

    SimpleClient(Long id, MessageProducer producer) {
        this.id = id;
        this.producer = producer;
        this.claimCheck = new HashMap<>();
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
        System.out.println("SimpleClient sending to service bus message channel");
        e.setHeader(Envelope.CLIENT,id);
        producer.send(e);

        // Save callback for later retrieval (Correlation)
        claimCheck.put(e.getId(), cb);
    }

    public void notify(Envelope e) {
        System.out.println("SimpleClient sending to ServiceCallback");
        Long id = e.getId();
        ServiceCallback cb = claimCheck.get(id);
        cb.reply(e);
        claimCheck.remove(e.getId());
    }

}
