package org.ovirt.engine.core.bll.scheduling.external;

public class ExternalSchedulerFactory {
    private static final ExternalSchedulerBrokerImpl instance = new ExternalSchedulerBrokerImpl();

    public static ExternalSchedulerBroker getInstance() {
        return instance;
    }
}
