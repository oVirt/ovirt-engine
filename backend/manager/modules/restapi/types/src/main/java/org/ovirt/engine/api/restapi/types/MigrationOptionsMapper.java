package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.InheritableBoolean;
import org.ovirt.engine.api.model.MigrationOptions;
import org.ovirt.engine.core.common.businessentities.HasMigrationOptions;

public class MigrationOptionsMapper {

    @Mapping(from = HasMigrationOptions.class, to = MigrationOptions.class)
    public static MigrationOptions map(HasMigrationOptions entity, MigrationOptions template) {
        if (template == null) {
            template = new MigrationOptions();
        }
        template.setAutoConverge(mapToInheritableBoolean(entity.getAutoConverge()));
        template.setCompressed(mapToInheritableBoolean(entity.getMigrateCompressed()));

        return template;
    }

    public static void copyMigrationOptions(MigrationOptions model, HasMigrationOptions entity) {
        if (model.isSetAutoConverge()) {
            entity.setAutoConverge(mapFromInheritableBoolean(model.getAutoConverge()));
        }

        if (model.isSetCompressed()) {
            entity.setMigrateCompressed(mapFromInheritableBoolean(model.getCompressed()));
        }
    }

    private static String mapToInheritableBoolean(Boolean value) {
        return InheritableBooleanMapper.map(value).value();
    }

    public static Boolean mapFromInheritableBoolean(String value) {
        return InheritableBooleanMapper.map(InheritableBoolean.fromValue(value));
    }
}
