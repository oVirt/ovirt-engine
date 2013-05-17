package org.ovirt.engine.core.bll;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.action.ImportVmTemplateParameters;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.StorageDomainDynamic;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.queries.DiskImageList;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BusinessEntitySnapshotDAO;
import org.ovirt.engine.core.dao.StorageDomainDAO;
import org.ovirt.engine.core.dao.StorageDomainStaticDAO;
import org.ovirt.engine.core.dao.VmTemplateDAO;

public class TestHelperImportVmTemplateCommand extends ImportVmTemplateCommand {

    public TestHelperImportVmTemplateCommand(final ImportVmTemplateParameters p) {
        super(p);
    }

    @Override
    protected BusinessEntitySnapshotDAO getBusinessEntitySnapshotDAO() {
        return null;
    }

    @Override
    public StorageDomainDAO getStorageDomainDAO() {
        final StorageDomain destination = new StorageDomain();
        destination.setStorageDomainType(StorageDomainType.Data);
        destination.setStatus(StorageDomainStatus.Active);

        final StorageDomainDAO d = mock(StorageDomainDAO.class);
        when(d.getForStoragePool(any(Guid.class), any(Guid.class))).thenReturn(destination);
        StorageDomainDynamic dy = new StorageDomainDynamic();
        dy.setAvailableDiskSize(10);
        dy.setUsedDiskSize(0);
        destination.setStorageDynamicData(dy);
        return d;
    }

    @Override
    protected StorageDomainStaticDAO getStorageDomainStaticDAO() {
        StorageDomainStaticDAO d = mock(StorageDomainStaticDAO.class);
        when(d.get(any(Guid.class))).thenReturn(new StorageDomainStatic());
        return d;
    }

    @Override
    public VmTemplateDAO getVmTemplateDAO() {
        VmTemplateDAO d = mock(VmTemplateDAO.class);
        return d;
    }

    @Override
    public StoragePool getStoragePool() {
        return new StoragePool();
    }

    @Override
    public BackendInternal getBackend() {
        BackendInternal backend = mock(BackendInternal.class);
        when(backend.runInternalQuery(eq(VdcQueryType.GetTemplatesFromExportDomain), any(VdcQueryParametersBase.class))).thenReturn(createDiskImageQueryResult());
        return backend;
    }

    @Override
    protected StorageDomain getSourceDomain() {
        StorageDomain source = new StorageDomain();
        source.setStorageDomainType(StorageDomainType.ImportExport);
        source.setStatus(StorageDomainStatus.Active);
        return source;
    }

    @Override
    protected boolean isVmTemplateWithSameNameExist() {
        return false;
    }

    @Override
    protected boolean validateNoDuplicateDiskImages(Iterable<DiskImage> images) {
        return true;
    }

    private static VdcQueryReturnValue createDiskImageQueryResult() {
        final VdcQueryReturnValue v = new VdcQueryReturnValue();
        Map<VmTemplate, DiskImageList> m = new HashMap<VmTemplate, DiskImageList>();
        VmTemplate t = new VmTemplate();
        DiskImage i = new DiskImage();
        i.setActualSizeInBytes(2);
        ArrayList<DiskImage> ial = new ArrayList<DiskImage>();
        ial.add(i);
        DiskImageList il = new DiskImageList(ial);
        m.put(t, il);
        v.setReturnValue(m);
        v.setSucceeded(true);
        return v;
    }
}
