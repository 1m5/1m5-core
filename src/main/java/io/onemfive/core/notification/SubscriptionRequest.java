package io.onemfive.core.notification;

import io.onemfive.data.EventMessage;
import io.onemfive.data.Subscription;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public class SubscriptionRequest {

    private EventMessage.Type type;
    private String filter;
    private Subscription subscription;

    public SubscriptionRequest(EventMessage.Type type, Subscription subscription) {
        this.type = type;
        this.subscription = subscription;
    }

    public SubscriptionRequest(EventMessage.Type type, String filter, Subscription subscription) {
        this.type = type;
        this.filter = filter;
        this.subscription = subscription;
    }

    public EventMessage.Type getType() {
        return type;
    }

    public String getFilter() {
        return filter;
    }

    public Subscription getSubscription() {
        return subscription;
    }
}
