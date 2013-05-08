package org.ovirt.engine.core.common.action;

import javax.validation.Valid;

import org.ovirt.engine.core.common.businessentities.event_subscriber;

public class EventSubscriptionParametesBase extends VdcActionParametersBase {
    private static final long serialVersionUID = -6988075041053848616L;

    public EventSubscriptionParametesBase(event_subscriber event_subscriber, String domain) {
        setEventSubscriber(event_subscriber);
        setDomain(domain);
    }

    @Valid
    private event_subscriber privateEventSubscriber;

    public event_subscriber getEventSubscriber() {
        return privateEventSubscriber;
    }

    private void setEventSubscriber(event_subscriber value) {
        privateEventSubscriber = value;
    }

    private String privateDomain;

    public String getDomain() {
        return privateDomain;
    }

    private void setDomain(String value) {
        privateDomain = value;
    }

    public EventSubscriptionParametesBase() {
    }
}
