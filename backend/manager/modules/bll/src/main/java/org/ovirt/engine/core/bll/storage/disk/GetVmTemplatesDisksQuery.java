package org.ovirt.engine.core.bll.storage.disk;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.storage.BaseDisk;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetVmTemplatesDisksQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    public GetVmTemplatesDisksQuery(P parameters) {
        super(parameters);
    }

    public GetVmTemplatesDisksQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        List<Disk> disks = getTemplateDisks();
        for (Disk disk : disks) {
            disk.setDiskVmElements(Collections.singletonList(getDiskVmElement(disk)));
        }
        getQueryReturnValue().setReturnValue(disks);
    }

    protected List<Disk> getTemplateDisks() {
        return DbFacade.getInstance()
                .getDiskDao()
                .getAllForVm(getParameters().getId(), getUserID(), getParameters().isFiltered());
    }

    private DiskVmElement getDiskVmElement(BaseDisk disk) {
        return getDbFacade().getDiskVmElementDao().get(new VmDeviceId(disk.getId(), getParameters().getId()));
    }
}
