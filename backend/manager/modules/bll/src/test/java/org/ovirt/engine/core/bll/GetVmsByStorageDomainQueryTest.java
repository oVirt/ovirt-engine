package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDao;

public class GetVmsByStorageDomainQueryTest extends AbstractQueryTest<IdQueryParameters, GetVmsByStorageDomainQuery<IdQueryParameters>> {
    VmDao vmDao = mock(VmDao.class);
    Guid domainId = Guid.newGuid();
    GetVmsByStorageDomainQuery<IdQueryParameters> query;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        params = getQueryParameters();
        when(getDbFacadeMockInstance().getVmDao()).thenReturn(vmDao);
        when(params.getId()).thenReturn(domainId);
        query = getQuery();
    }

    @Test
    public void testExecuteQueryCommandNoDisks() {
        List<VM> vmsOfDomain = new ArrayList<>();
        VM vm1 = mock(VM.class);
        VM vm2 = mock(VM.class);
        vmsOfDomain.add(vm1);
        vmsOfDomain.add(vm2);

        when(vmDao.getAllForStorageDomain(domainId)).thenReturn(vmsOfDomain);

        VdcQueryReturnValue returnValue = new VdcQueryReturnValue();
        returnValue.setSucceeded(true);
        List<DiskImage> diskImages = new ArrayList<>();
        returnValue.setReturnValue(diskImages);

        doReturn(returnValue).when(query).getAllDisksByStorageDomain(domainId);
        query.executeQueryCommand();

        List<VM> vms = query.getQueryReturnValue().getReturnValue();
        assertEquals(2, vms.size());
    }

    @Test
    public void testExecuteQueryCommandNoVms() {
        List<VM> vmsOfDomain = new ArrayList<>();

        when(vmDao.getAllForStorageDomain(domainId)).thenReturn(vmsOfDomain);

        VdcQueryReturnValue returnValue = new VdcQueryReturnValue();
        returnValue.setSucceeded(true);
        List<DiskImage> diskImages = new ArrayList<>();
        returnValue.setReturnValue(diskImages);

        doReturn(returnValue).when(query).getAllDisksByStorageDomain(domainId);
        query.executeQueryCommand();

        List<VM> vms = query.getQueryReturnValue().getReturnValue();
        assertEquals(0, vms.size());
    }

    @Test
    public void testExecuteQueryCommandSharedDisks() {
        List<VM> vmsOfDomain = new ArrayList<>();
        VM vm1 = new VM();
        vm1.setName("vm1");
        VM vm2 = new VM();
        vm2.setName("vm2");

        vmsOfDomain.add(vm1);
        vmsOfDomain.add(vm2);

        when(vmDao.getAllForStorageDomain(domainId)).thenReturn(vmsOfDomain);

        VdcQueryReturnValue returnValue = new VdcQueryReturnValue();
        returnValue.setSucceeded(true);
        List<DiskImage> diskImages = new ArrayList<>();
        DiskImage d1 = mock(DiskImage.class);
        when(d1.isShareable()).thenReturn(true);
        ArrayList<String> vmNames = new ArrayList<>();
        vmNames.add("vm1");
        vmNames.add("vm2");
        when(d1.getVmNames()).thenReturn(vmNames);
        //floating disk
        DiskImage d2 = mock(DiskImage.class);
        when(d2.isShareable()).thenReturn(false);
        when(d2.getVmNames()).thenReturn(null);

        diskImages.add(d1);
        diskImages.add(d2);

        returnValue.setReturnValue(diskImages);

        doReturn(returnValue).when(query).getAllDisksByStorageDomain(domainId);
        query.executeQueryCommand();

        List<VM> vms = query.getQueryReturnValue().getReturnValue();
        assertEquals(2, vms.size());
        assertEquals(1, vms.get(0).getDiskList().size());
        assertEquals(1, vms.get(1).getDiskList().size());
    }
}
