package org.ovirt.engine.ui.uicompat;

import java.util.MissingResourceException;

import org.ovirt.engine.core.common.migration.MigrationPolicy;

public class MigrationPoliciesTranslator  {

    private static final MigrationPoliciesTranslator INSTANCE = new MigrationPoliciesTranslator();

    private static final LocalizedMigrationPolicies migrationPolicies = ConstantsManager.getInstance().getMigrationPolicies();

    private MigrationPoliciesTranslator() {
    }

    public static MigrationPoliciesTranslator getInstance() {
        return INSTANCE;
    }

    public String getName(MigrationPolicy policy) {
        try {
            final String translatedName = migrationPolicies.getString(getNameKey(policy));
            return translatedName != null ? translatedName : policy.getName();
        } catch (MissingResourceException e) {
            // Silently ignore missing resource
            return policy.getName();
        }
    }

    public String getDescription(MigrationPolicy policy) {
        try {
            final String translatedName = migrationPolicies.getString(getDescriptionKey(policy));
            return translatedName != null ? translatedName : policy.getDescription();
        } catch (MissingResourceException e) {
            // Silently ignore missing resource
            return policy.getDescription();
        }
    }

    private String getNameKey(MigrationPolicy policy) {
        return getKey("name_", policy);//$NON-NLS-1$
    }

    private String getDescriptionKey(MigrationPolicy policy) {
        return getKey("description_", policy);//$NON-NLS-1$
    }

    private String getKey(String prefix, MigrationPolicy policy) {
        return prefix + policy.getId().toString().replace('-', '_');
    }
}
