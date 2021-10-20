package org.ovirt.engine.ui.uicommonweb.builders;

import org.ovirt.engine.core.common.businessentities.HasMigrationOptions;
import org.ovirt.engine.ui.uicommonweb.models.ModelWithMigrationsOptions;

public class MigrationsModelToEntityBuilder<S extends ModelWithMigrationsOptions, D extends HasMigrationOptions> extends BaseSyncBuilder<S, D> {

    private boolean includePolicy;

    public MigrationsModelToEntityBuilder(boolean includePolicy) {
        this.includePolicy = includePolicy;
    }

    @Override
    public void build(S source, D destination) {
        destination.setAutoConverge(source.getAutoConverge().getSelectedItem());
        destination.setMigrateCompressed(source.getMigrateCompressed().getSelectedItem());

        if (includePolicy && source.getMigrationPolicies().getSelectedItem() != null) {
            destination.setMigrationPolicyId(source.getMigrationPolicies().getSelectedItem().getId());
        }
        destination.setMigrateEncrypted(source.getMigrateEncrypted().getSelectedItem());
    }
}
