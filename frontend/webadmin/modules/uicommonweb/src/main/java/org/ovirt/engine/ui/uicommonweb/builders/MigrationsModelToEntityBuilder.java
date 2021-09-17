package org.ovirt.engine.ui.uicommonweb.builders;

import org.ovirt.engine.core.common.businessentities.HasMigrationOptions;
import org.ovirt.engine.ui.uicommonweb.models.ModelWithMigrationsOptions;
import org.ovirt.engine.ui.uicommonweb.models.ParallelMigrationsType;

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

        buildParallelMigrations(source, destination);
    }

    private void buildParallelMigrations(S source, D destination) {
        // null value is not applicable for clusters but it should never occur there
        final ParallelMigrationsType parallelMigrationsType = source.getParallelMigrationsType().getSelectedItem();
        final Integer parallelMigrations =
            parallelMigrationsType == null ? null :
            parallelMigrationsType == ParallelMigrationsType.AUTO ? -2 :
            parallelMigrationsType == ParallelMigrationsType.AUTO_PARALLEL ? -1 :
            parallelMigrationsType == ParallelMigrationsType.DISABLED ? 0 :
            source.getCustomParallelMigrations().getEntity();
        destination.setParallelMigrations(parallelMigrations);
    }
}
