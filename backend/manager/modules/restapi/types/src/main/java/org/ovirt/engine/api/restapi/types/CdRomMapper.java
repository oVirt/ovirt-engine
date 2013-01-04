package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.CdRom;
import org.ovirt.engine.api.model.File;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;

public class CdRomMapper {

    public static final Guid CDROM_ID = Guid.Empty;

    @Mapping(from = CdRom.class, to = VM.class)
    public static VM map(CdRom model, VM template) {
        VM entity = template != null ? template : new VM();
        if (model.isSetFile() && model.getFile().isSetId()) {
            entity.getStaticData().setIsoPath(model.getFile().getId());
        }
        return entity;
    }

    @Mapping(from = VM.class, to = CdRom.class)
    public static CdRom map(VM entity, CdRom template) {
        CdRom model = template != null ? template : new CdRom();
        model.setId(CDROM_ID.toString());
        if (!StringHelper.isNullOrEmpty(entity.getStaticData().getIsoPath())) {
            model.setFile(new File());
            model.getFile().setId(entity.getStaticData().getIsoPath());
        }
        return model;
    }

    /**
     * Bi-directional CdRom->VmTemplate not required as
     * template device collections are always read-only
     */
    @Mapping(from = VmTemplate.class, to = CdRom.class)
    public static CdRom map(VmTemplate entity, CdRom template) {
        CdRom model = template != null ? template : new CdRom();
        model.setId(CDROM_ID.toString());
        if (!StringHelper.isNullOrEmpty(entity.getIsoPath())) {
            model.setFile(new File());
            model.getFile().setId(entity.getIsoPath());
        }
        return model;
    }
}
