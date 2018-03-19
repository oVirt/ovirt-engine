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
            model.setSnapshotType(map(entity.getType(), null));
        }
        model.setPersistMemorystate(entity.containsMemory());
        return model;
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType.class, to = SnapshotType.class)
    public static SnapshotType map(org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType snapshotType, SnapshotType template) {
        switch (snapshotType) {
        case ACTIVE:
            return SnapshotType.ACTIVE;
        case PREVIEW:
            return SnapshotType.PREVIEW;
        case REGULAR:
            return SnapshotType.REGULAR;
        case STATELESS:
            return SnapshotType.STATELESS;
        default:
            return null;
        }
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotStatus.class, to = SnapshotStatus.class)
    public static SnapshotStatus map(org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotStatus snapshotStatus, SnapshotStatus template) {
        switch (snapshotStatus) {
        case IN_PREVIEW:
            return SnapshotStatus.IN_PREVIEW;
        case LOCKED:
            return SnapshotStatus.LOCKED;
        case OK:
            return SnapshotStatus.OK;
        default:
            return null;
        }
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.VM.class, to = Snapshot.class)
    public static Snapshot map(org.ovirt.engine.core.common.businessentities.VM entity, Snapshot template) {
        VmMapper.map(entity, template);
        return template;
    }

    public static Snapshot map(String configuration, ConfigurationType type, Snapshot snapshot) {
        snapshot.setInitialization(new Initialization());
        snapshot.getInitialization().setConfiguration(new Configuration());
        snapshot.getInitialization().getConfiguration().setData(configuration);
        snapshot.getInitialization().getConfiguration().setType(type);
        return snapshot;
    }
}
