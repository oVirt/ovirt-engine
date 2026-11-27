package org.ovirt.engine.core.common.migration;

import org.ovirt.engine.core.compat.Guid;

public class NoMigrationPolicy extends MigrationPolicy {

    public static final Guid ID = Guid.Empty;

    public NoMigrationPolicy() {
        // not externalized here - like the policies from vdc_options, the frontend translates
        // the name and description by policy id (see LocalizedMigrationPolicies)
        setName("Legacy"); //$NON-NLS-1$
        setDescription("Legacy behavior of 3.6 version, vdsm.conf overrides are still applied. The guest agent hook mechanism is disabled."); //$NON-NLS-1$
        setConfig(new NoConvergenceConfig());
        // the ultimate default
        setMaxMigrations(2);
        setId(ID);
    }

}
