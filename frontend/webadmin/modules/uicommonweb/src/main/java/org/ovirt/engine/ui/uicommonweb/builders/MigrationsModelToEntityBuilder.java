package org.ovirt.engine.ui.uicommonweb.builders;

import org.ovirt.engine.core.common.businessentities.HasMigrationOptions;
import org.ovirt.engine.core.common.migration.ParallelMigrationsType;
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

        buildParallelMigrations(source, destination);
    }

    private Integer sourceToDestinationMigrations(S source, ParallelMigrationsType parallelMigrationsType) {
        if (parallelMigrationsType == null) {
            return null;
        }
        switch (parallelMigrationsType) {
            case AUTO:
                return ParallelMigrationsType.AUTO.getValue();
            case AUTO_PARALLEL:
                return ParallelMigrationsType.AUTO_PARALLEL.getValue();
            case DISABLED:
                return ParallelMigrationsType.DISABLED.getValue();
            default:
                return source.getCustomParallelMigrations().getEntity();
        }
    }

    private void buildParallelMigrations(S source, D destination) {
        // null value is not applicable for clusters but it should never occur there
        final ParallelMigrationsType parallelMigrationsType = source.getParallelMigrationsType().getSelectedItem();
        destination.setParallelMigrations(sourceToDestinationMigrations(source, parallelMigrationsType));
    }
}
