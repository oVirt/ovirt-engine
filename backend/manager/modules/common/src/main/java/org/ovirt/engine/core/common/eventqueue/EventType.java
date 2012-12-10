package org.ovirt.engine.core.common.eventqueue;

public enum EventType {
    RECONSTRUCT,
    DOMAINFAILOVER,
    DOMAINNOTOPERATIONAL,
    VDSSTOARGEPROBLEMS,
    DOMAINMONITORING,
    VDSCLEARCACHE;
}
