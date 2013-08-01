package org.ovirt.engine.core.bll.scheduling.external;

public class ExternalSchedulerFactory {
    private final static ExternalSchedulerBrokerImpl instance = new ExternalSchedulerBrokerImpl();

    public static ExternalSchedulerBroker getInstance() {
        return instance;
    }
}
