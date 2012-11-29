package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.action.ImportVmParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool;

public class TestHelperImportVmCommand extends ImportVmCommand {

    private static final long serialVersionUID = 1L;

    public TestHelperImportVmCommand(final ImportVmParameters p) {
        super(p);
    }

    @Override
    protected boolean validateNoDuplicateVm() {
        return true;
    }

    @Override
    public storage_pool getStoragePool() {
        return new storage_pool();
    }

    @Override
    protected boolean validateVdsCluster() {
        return true;
    }

    @Override
    protected boolean validateUsbPolicy() {
        return true;
    }

    @Override
    protected storage_domains getSourceDomain() {
        storage_domains sd = new storage_domains();
        sd.setstorage_domain_type(StorageDomainType.ImportExport);
        sd.setstatus(StorageDomainStatus.Active);
        return sd;
    }

    @Override
    public VmTemplate getVmTemplate() {
        return new VmTemplate();
    }

    @Override
    protected boolean canAddVm() {
        return true;
    }

    @Override
    protected List<VM> getVmsFromExportDomain() {
        return Collections.singletonList(createVM());
    }

    protected VM createVM() {
        final VM v = new VM();
        v.setId(getParameters().getVm().getId());
        v.setDiskSize(2);
        return v;
    }
}
