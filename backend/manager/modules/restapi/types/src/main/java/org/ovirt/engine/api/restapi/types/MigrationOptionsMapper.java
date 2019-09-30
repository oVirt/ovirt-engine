package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.InheritableBoolean;
import org.ovirt.engine.api.model.MigrationOptions;
import org.ovirt.engine.api.model.MigrationPolicy;
import org.ovirt.engine.core.common.businessentities.HasMigrationOptions;
import org.ovirt.engine.core.compat.Guid;

public class MigrationOptionsMapper {

    @Mapping(from = HasMigrationOptions.class, to = MigrationOptions.class)
    public static MigrationOptions map(HasMigrationOptions entity, MigrationOptions template) {
        if (template == null) {
            template = new MigrationOptions();
        }
        template.setAutoConverge(mapToInheritableBoolean(entity.getAutoConverge()));
        template.setCompressed(mapToInheritableBoolean(entity.getMigrateCompressed()));
        template.setEncrypted(mapToInheritableBoolean(entity.getMigrateEncrypted()));

        if (entity.getMigrationPolicyId() != null) {
            MigrationPolicy policy = template.getPolicy();
            if (policy == null) {
                policy = new MigrationPolicy();
                template.setPolicy(policy);
            }
            policy.setId(entity.getMigrationPolicyId().toString());
        }

        return template;
    }

    public static void copyMigrationOptions(MigrationOptions model, HasMigrationOptions entity) {
        if (model.isSetAutoConverge()) {
            entity.setAutoConverge(mapFromInheritableBoolean(model.getAutoConverge()));
        }

        if (model.isSetCompressed()) {
            entity.setMigrateCompressed(mapFromInheritableBoolean(model.getCompressed()));
        }

        if (model.isSetEncrypted()) {
            entity.setMigrateEncrypted(mapFromInheritableBoolean(model.getEncrypted()));
        }

        if (model.isSetPolicy()) {
            if (model.getPolicy().isSetId()) {
                entity.setMigrationPolicyId(Guid.createGuidFromString(model.getPolicy().getId()));
            } else {
                entity.setMigrationPolicyId(null);
            }
        }
    }

    private static InheritableBoolean mapToInheritableBoolean(Boolean value) {
        return InheritableBooleanMapper.map(value);
    }

    public static Boolean mapFromInheritableBoolean(InheritableBoolean value) {
        return InheritableBooleanMapper.map(value);
    }
}
