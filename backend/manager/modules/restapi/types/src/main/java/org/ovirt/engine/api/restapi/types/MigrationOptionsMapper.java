package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.InheritableBoolean;
import org.ovirt.engine.api.model.MigrationOptions;
import org.ovirt.engine.api.model.MigrationPolicy;
import org.ovirt.engine.api.model.ParallelMigrationsPolicy;
import org.ovirt.engine.core.common.businessentities.HasMigrationOptions;
import org.ovirt.engine.core.common.migration.ParallelMigrationsType;
import org.ovirt.engine.core.compat.Guid;

public class MigrationOptionsMapper {

    @Mapping(from = HasMigrationOptions.class, to = MigrationOptions.class)
    public static MigrationOptions map(HasMigrationOptions entity, MigrationOptions template, boolean isCluster) {
        if (template == null) {
            template = new MigrationOptions();
        }
        template.setAutoConverge(mapToInheritableBoolean(entity.getAutoConverge()));
        template.setCompressed(mapToInheritableBoolean(entity.getMigrateCompressed()));
        template.setEncrypted(mapToInheritableBoolean(entity.getMigrateEncrypted()));

        ParallelMigrationsPolicy parallelMigrations = isCluster ? ParallelMigrationsPolicy.DISABLED
                : ParallelMigrationsPolicy.INHERIT;
        Integer customParallelMigrations = null;
        if (entity.getParallelMigrations() != null) {
            switch (entity.getParallelMigrations().intValue()) {
                case -2:
                    parallelMigrations = ParallelMigrationsPolicy.AUTO;
                    break;
                case -1:
                    parallelMigrations = ParallelMigrationsPolicy.AUTO_PARALLEL;
                    break;
                case 0:
                    parallelMigrations = ParallelMigrationsPolicy.DISABLED;
                    break;
                default:
                    parallelMigrations = ParallelMigrationsPolicy.CUSTOM;
                    customParallelMigrations = entity.getParallelMigrations();
            }
        }
        template.setParallelMigrationsPolicy(parallelMigrations);
        template.setCustomParallelMigrations(customParallelMigrations);

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

        if (model.isSetParallelMigrationsPolicy()) {
            Integer parallelMigrations = null;
            final Integer customParallelMigrations = model.getCustomParallelMigrations();
            switch (model.getParallelMigrationsPolicy()) {
                case INHERIT:
                    break;
                case AUTO:
                    parallelMigrations = ParallelMigrationsType.AUTO.getValue();
                    break;
                case AUTO_PARALLEL:
                    parallelMigrations = ParallelMigrationsType.AUTO_PARALLEL.getValue();
                    break;
                case DISABLED:
                    parallelMigrations = ParallelMigrationsType.DISABLED.getValue();
                    break;
                case CUSTOM:
                    if (customParallelMigrations != null && customParallelMigrations >= 1) {
                        parallelMigrations = customParallelMigrations;
                    } else {
                        // Artificial invalid value in case the (invalid) value collides with other
                        // policy values. This will be caught and reported nicely in the backend.
                        parallelMigrations = -Integer.MIN_VALUE;
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Unhandled parallel migration connections specification");
            }
            entity.setParallelMigrations(parallelMigrations);
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
