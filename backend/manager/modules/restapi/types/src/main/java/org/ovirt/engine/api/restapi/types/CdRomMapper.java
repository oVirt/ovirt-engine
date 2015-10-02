package org.ovirt.engine.api.restapi.types;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.model.Cdrom;
import org.ovirt.engine.api.model.File;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.compat.Guid;

public class CdRomMapper {

    public static final Guid CDROM_ID = Guid.Empty;

    @Mapping(from = Cdrom.class, to = VM.class)
    public static VM map(Cdrom model, VM template) {
        VM entity = template != null ? template : new VM();
        if (model.isSetFile() && model.getFile().isSetId()) {
            entity.getStaticData().setIsoPath(model.getFile().getId());
        }
        return entity;
    }

    @Mapping(from = VM.class, to = Cdrom.class)
    public static Cdrom map(VM entity, Cdrom template) {
        Cdrom model = template != null ? template : new Cdrom();
        model.setId(CDROM_ID.toString());
        if (!StringUtils.isEmpty(entity.getStaticData().getIsoPath())) {
            model.setFile(new File());
            model.getFile().setId(entity.getStaticData().getIsoPath());
        }
        return model;
    }

    /**
     * Bi-directional Cdrom->VmTemplate not required as
     * template device collections are always read-only
     */
    @Mapping(from = VmTemplate.class, to = Cdrom.class)
    public static Cdrom map(VmTemplate entity, Cdrom template) {
        Cdrom model = template != null ? template : new Cdrom();
        model.setId(CDROM_ID.toString());
        if (!StringUtils.isEmpty(entity.getIsoPath())) {
            model.setFile(new File());
            model.getFile().setId(entity.getIsoPath());
        }
        return model;
    }
}
