package org.ovirt.engine.ui.common.widget.renderer;

import org.ovirt.engine.core.common.migration.MigrationPolicy;
import org.ovirt.engine.ui.uicompat.MigrationPoliciesTranslator;

public class MigrationPolicyNameRenderer extends NullSafeRenderer<MigrationPolicy> {

    @Override
    protected String renderNullSafe(MigrationPolicy policy) {
        return MigrationPoliciesTranslator.getInstance().getName(policy);
    }
}
