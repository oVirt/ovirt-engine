package org.ovirt.engine.ui.uicommonweb.builders;

import org.ovirt.engine.core.common.businessentities.HasMigrationOptions;
import org.ovirt.engine.ui.uicommonweb.models.ModelWithMigrationsOptions;

public class MigrationsEntityToModelBuilder<S extends HasMigrationOptions, D extends ModelWithMigrationsOptions> extends BaseSyncBuilder<S, D> {

    @Override
    public void build(S source, D destination) {
        // the setting of migration policy ID is coupled with loading the
        // policies into the list, it is handled by model separately
        destination.getAutoConverge().setSelectedItem(source.getAutoConverge());
        destination.getMigrateCompressed().setSelectedItem(source.getMigrateCompressed());
        destination.getMigrateEncrypted().setSelectedItem(source.getMigrateEncrypted());
    }
}
