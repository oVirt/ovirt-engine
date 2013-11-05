package org.ovirt.engine.core.common.businessentities;


public interface CachedEntity {
    public long getExpiration();

    public static final long LONG_LIVED_OBJECT = 5 * 60 * 1000;
    public static final long SHORT_LIVED_OBJECT = 1 * 60 * 1000;
}
