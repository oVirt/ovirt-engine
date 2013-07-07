package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.Configuration;
import org.ovirt.engine.api.model.ConfigurationType;
import org.ovirt.engine.api.model.Initialization;
import org.ovirt.engine.api.model.Snapshot;
import org.ovirt.engine.api.model.SnapshotStatus;
import org.ovirt.engine.api.model.SnapshotType;

public class SnapshotMapper {
    @Mapping(from = org.ovirt.engine.core.common.businessentities.Snapshot.class, to = Snapshot.class)
    public static Snapshot map(org.ovirt.engine.core.common.businessentities.Snapshot entity, Snapshot template) {
        Snapshot model = template != null ? template : new Snapshot();
        model.setId(entity.getId().toString());
        if (entity.getDescription() != null) {
            model.setDescription(entity.getDescription());
        }
        if (entity.getCreationDate() != null) {
            model.setDate(DateMapper.map(entity.getCreationDate(), null));
        }
        if (entity.getStatus() != null) {
            model.setSnapshotStatus(map(entity.getStatus(), null));
        }
        if (entity.getType() != null) {
            model.setType(map(entity.getType(), null));
        }
        if (entity.getMemoryVolume() != null) {
            model.setPersistMemorystate(!entity.getMemoryVolume().isEmpty());
        }
        return model;
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType.class, to = String.class)
    public static String map(org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType snapshotType, String template) {
        switch (snapshotType) {
        case ACTIVE:
            return SnapshotType.ACTIVE.value();
        case PREVIEW:
            return SnapshotType.PREVIEW.value();
        case REGULAR:
            return SnapshotType.REGULAR.value();
        case STATELESS:
            return SnapshotType.STATELESS.value();
        default:
            return null;
        }
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotStatus.class, to = String.class)
    public static String map(org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotStatus snapshotStatus, String template) {
        switch (snapshotStatus) {
        case IN_PREVIEW:
            return SnapshotStatus.IN_PREVIEW.value();
        case LOCKED:
            return SnapshotStatus.LOCKED.value();
        case OK:
            return SnapshotStatus.OK.value();
        default:
            return null;
        }
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.VM.class, to = Snapshot.class)
    public static Snapshot map(org.ovirt.engine.core.common.businessentities.VM entity, Snapshot template) {
        VmMapper.map(entity, template);
        return template;
    }

    public static Snapshot mapSnapshotConfiguration(String configuration, ConfigurationType type, Snapshot snapshot) {
        snapshot.setInitialization(new Initialization());
        snapshot.getInitialization().setConfiguration(new Configuration());
        snapshot.getInitialization().getConfiguration().setData(configuration);
        snapshot.getInitialization().getConfiguration().setType(type.value());
        return snapshot;
    }
}
