package org.ovirt.engine.core.bll;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.action.ImportVmParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.BusinessEntitySnapshotDAO;
import org.ovirt.engine.core.dao.StorageDomainDAO;
import org.ovirt.engine.core.dao.StorageDomainStaticDAO;
import org.ovirt.engine.core.dao.VdsGroupDAO;
import org.ovirt.engine.core.dao.VmDAO;
import org.ovirt.engine.core.dao.VmStaticDAO;
import org.ovirt.engine.core.dao.VmStatisticsDAO;
import org.ovirt.engine.core.dao.VmTemplateDAO;

public class TestHelperImportVmCommand extends ImportVmCommand {

    private static final long serialVersionUID = 1L;

    public TestHelperImportVmCommand(final ImportVmParameters p) {
        super(p);
    }

    @Override
    public boolean IsDomainActive(final Guid g1, final NGuid g2) {
        return true;
    }

    @Override
    protected BusinessEntitySnapshotDAO getBusinessEntitySnapshotDAO() {
        return null;
    }

    @Override
    protected boolean CheckTemplateInStorageDomain() {
        return true;
    }

    @Override
    protected VmDAO getVmDAO() {
        final VmDAO d = mock(VmDAO.class);
        when(d.get(any(Guid.class))).thenReturn(null);
        return d;
    }

    @Override
    public VmStaticDAO getVmStaticDAO() {
        final VmStaticDAO d = mock(VmStaticDAO.class);
        when(d.get(any(Guid.class))).thenReturn(null);
        return d;
    }

    @Override
    protected VmStatisticsDAO getVmStatisticsDAO() {
        final VmStatisticsDAO d = mock(VmStatisticsDAO.class);
        when(d.get(any(Guid.class))).thenReturn(null);
        return d;
    }

    @Override
    public storage_pool getStoragePool() {
        return new storage_pool();
    }

    @Override
    protected VdsGroupDAO getVdsGroupDAO() {
        VdsGroupDAO d = mock(VdsGroupDAO.class);
        List<VDSGroup> list = new ArrayList<VDSGroup>();
        VDSGroup g = new VDSGroup();
        g.setId(getParameters().getVdsGroupId());
        Version v = new Version("2.2");
        g.setcompatibility_version(v);
        list.add(g);
        when(d.getAllForStoragePool(any(Guid.class))).thenReturn(list);
        when(d.get(any(Guid.class))).thenReturn(g);
        return d;
    }

    @Override
    public StorageDomainDAO getStorageDomainDAO() {
        final storage_domains sd = new storage_domains();
        sd.setstorage_domain_type(StorageDomainType.ImportExport);
        sd.setstatus(StorageDomainStatus.Active);
        final StorageDomainDAO d = mock(StorageDomainDAO.class);
        when(d.getForStoragePool(any(Guid.class), any(Guid.class))).thenReturn(sd);
        return d;
    }

    @Override
    protected StorageDomainStaticDAO getStorageDomainStaticDAO() {
        StorageDomainStaticDAO d = mock(StorageDomainStaticDAO.class);
        return d;
    }

    @Override
    public storage_domains getStorageDomain() {
        storage_domains sd = new storage_domains();
        sd.setstatus(StorageDomainStatus.Active);
        sd.setavailable_disk_size(10);
        return sd;
    }

    @Override
    public BackendInternal getBackend() {
        BackendInternal backend = mock(BackendInternal.class);
        when(backend.runInternalQuery(eq(VdcQueryType.GetVmsFromExportDomain), any(VdcQueryParametersBase.class))).thenReturn(createVmQueryResult());
        when(backend.runInternalQuery(eq(VdcQueryType.IsVmWithSameNameExist), any(VdcQueryParametersBase.class))).thenReturn(createDuplicateResult());
        return backend;
    }

    @Override
    public VmTemplateDAO getVmTemplateDAO() {
        VmTemplateDAO d = mock(VmTemplateDAO.class);
        when(d.get(any(Guid.class))).thenReturn(new VmTemplate());
        return d;
    }

    private VdcQueryReturnValue createVmQueryResult() {
        final VdcQueryReturnValue v = new VdcQueryReturnValue();
        List<VM> list = new ArrayList<VM>();
        list.add(createVM());
        v.setReturnValue(list);
        v.setSucceeded(true);
        return v;
    }

    private VdcQueryReturnValue createDuplicateResult() {
        final VdcQueryReturnValue v = new VdcQueryReturnValue();
        v.setReturnValue(Boolean.FALSE);
        v.setSucceeded(true);
        return v;
    }

    protected VM createVM() {
        final VM v = new VM();
        v.setId(getParameters().getVm().getId());
        v.setDiskSize(2);
        return v;
    }
}
