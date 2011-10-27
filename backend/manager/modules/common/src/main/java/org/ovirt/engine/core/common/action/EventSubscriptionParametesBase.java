package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.*;

import javax.validation.Valid;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "EventSubscriptionParametesBase")
public class EventSubscriptionParametesBase extends VdcActionParametersBase {
    private static final long serialVersionUID = -6988075041053848616L;

    public EventSubscriptionParametesBase(event_subscriber event_subscriber, String domain) {
        setEventSubscriber(event_subscriber);
        setDomain(domain);
    }

    @Valid
    @XmlElement(name = "EventSubscriber")
    private event_subscriber privateEventSubscriber;

    public event_subscriber getEventSubscriber() {
        return privateEventSubscriber;
    }

    private void setEventSubscriber(event_subscriber value) {
        privateEventSubscriber = value;
    }

    @XmlElement(name = "Domain")
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
