package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.Snapshot;
import org.ovirt.engine.core.common.businessentities.DiskImage;

public class SnapshotMapper {
    @Mapping(from = DiskImage.class, to = Snapshot.class)
    public static Snapshot map(DiskImage entity, Snapshot template) {
        Snapshot model = template != null ? template : new Snapshot();
        model.setId(entity.getvm_snapshot_id().toString());
        if (entity.getdescription() != null) {
            model.setDescription(entity.getdescription());
        }
        if (entity.getlast_modified_date() != null) {
            model.setDate(DateMapper.map(entity.getlast_modified_date(), null));
        }
        return model;
    }
}
