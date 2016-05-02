package org.ovirt.engine.core.common.migration;

import java.io.Serializable;

import org.ovirt.engine.core.common.businessentities.Nameable;
import org.ovirt.engine.core.compat.Guid;

public class MigrationPolicy implements Serializable, Nameable {

    private Guid id;

    private String name;

    private String description;

    private int maxMigrations;

    private boolean autoConvergence;

    private boolean migrationCompression;

    private boolean enableGuestEvents;

    private ConvergenceConfig config;

    public MigrationPolicy() {}

    public MigrationPolicy(Guid id, String name, String description, ConvergenceConfig config) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.config = config;
    }

    public Guid getId() {
        return id;
    }

    public void setId(Guid id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getMaxMigrations() {
        return maxMigrations;
    }

    public void setMaxMigrations(int maxMigrations) {
        this.maxMigrations = maxMigrations;
    }

    public ConvergenceConfig getConfig() {
        return config;
    }

    public void setConfig(ConvergenceConfig config) {
        this.config = config;
    }

    public boolean isAutoConvergence() {
        return autoConvergence;
    }

    public void setAutoConvergence(boolean autoConvergence) {
        this.autoConvergence = autoConvergence;
    }

    public boolean isMigrationCompression() {
        return migrationCompression;
    }

    public void setMigrationCompression(boolean migrationCompression) {
        this.migrationCompression = migrationCompression;
    }

    public boolean isEnableGuestEvents() {
        return enableGuestEvents;
    }

    public void setEnableGuestEvents(boolean enableGuestEvents) {
        this.enableGuestEvents = enableGuestEvents;
    }
}
