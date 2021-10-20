package org.ovirt.engine.ui.uicommonweb.models;

import org.ovirt.engine.core.common.migration.MigrationPolicy;

public interface ModelWithMigrationsOptions {

    ListModel<Boolean> getAutoConverge();

    ListModel<Boolean> getMigrateCompressed();

    ListModel<Boolean> getMigrateEncrypted();

    ListModel<MigrationPolicy> getMigrationPolicies();
}
