package org.ovirt.engine.core.bll.storage.disk;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.storage.BaseDisk;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.DiskVmElementDao;

public class GetVmTemplatesDisksQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private DiskDao diskDao;

    @Inject
    private DiskVmElementDao diskVmElementDao;

    public GetVmTemplatesDisksQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        List<Disk> disks = getTemplateDisks();
        getQueryReturnValue().setReturnValue(disks);
    }

    protected List<Disk> getTemplateDisks() {
        List<Disk> disks = diskDao.getAllForVm(getParameters().getId(), getUserID(), getParameters().isFiltered());
        disks.forEach(disk -> disk.setDiskVmElements(Collections.singletonList(getDiskVmElement(disk))));
        return disks;
    }

    private DiskVmElement getDiskVmElement(BaseDisk disk) {
        return diskVmElementDao.get(new VmDeviceId(disk.getId(), getParameters().getId()));
    }
}
