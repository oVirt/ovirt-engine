package org.ovirt.engine.core.common.action;

import javax.validation.Valid;

import org.ovirt.engine.core.common.businessentities.EventSubscriber;

public class EventSubscriptionParametesBase extends ActionParametersBase {
    private static final long serialVersionUID = -6988075041053848616L;

    public EventSubscriptionParametesBase(EventSubscriber eventSubscriber, String domain) {
        setEventSubscriber(eventSubscriber);
        setDomain(domain);
    }

    @Valid
    private EventSubscriber privateEventSubscriber;

    public EventSubscriber getEventSubscriber() {
        return privateEventSubscriber;
    }

    private void setEventSubscriber(EventSubscriber value) {
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
