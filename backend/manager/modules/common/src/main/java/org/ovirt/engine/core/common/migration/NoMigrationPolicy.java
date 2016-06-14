package org.ovirt.engine.core.common.migration;

import org.ovirt.engine.core.compat.Guid;

public class NoMigrationPolicy extends MigrationPolicy {

    public static final Guid ID = Guid.Empty;

    public NoMigrationPolicy() {
        // no need to externalize it - all the others are inside the vdc_options untranslatable
        // so it would look strange to have only one translated
        setName("Legacy"); //$NON-NLS-1$
        setDescription("Legacy behavior of 3.6 version, vdsm.conf overrides are still applied. The guest agent hook mechanism is disabled."); //$NON-NLS-1$
        setConfig(new NoConvergenceConfig());
        // the ultimate default
        setMaxMigrations(2);
        setId(ID);
    }

}
