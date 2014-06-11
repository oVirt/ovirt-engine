package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.OvfEntityData;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.UnregisteredOVFDataDAO;
import org.ovirt.engine.core.utils.ovf.OvfReaderException;

/** A test case for the {@link GetUnregisteredVmsQuery} class. */
public class GetUnregisteredVmsQueryTest extends AbstractQueryTest<IdQueryParameters, GetUnregisteredVmsQuery<? extends IdQueryParameters>> {

    Guid storageDomainId = Guid.newGuid();
    VmEntityType entityType = VmEntityType.VM;
    Guid newVmGuid = Guid.newGuid();
    Guid newVmGuid2 = Guid.newGuid();

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        mockQueryParameters();
        setUpQueryEntities();
    }

    @Test
    public void testExecuteQueryGetAllEntitiesCommand() throws OvfReaderException {
        getQuery().executeQueryCommand();

        @SuppressWarnings("unchecked")
        List<VM> result = getQuery().getQueryReturnValue().getReturnValue();
        assertEquals("Wrong number of VMs in result", 2, result.size());
    }

    private void mockQueryParameters() {
        // Mock the Query Parameters
        when(getQueryParameters().getId()).thenReturn(storageDomainId);
    }

    private void setUpQueryEntities() throws OvfReaderException {
        // Set up the expected result
        VM vmReturnForOvf = new VM();
        vmReturnForOvf.setId(newVmGuid);
        vmReturnForOvf.setName("Name");
        String ovfData = new String("OVF data for the first VM");
        OvfEntityData ovfEntityData =
                new OvfEntityData(vmReturnForOvf.getId(),
                        vmReturnForOvf.getName(),
                        VmEntityType.VM,
                        null,
                        null,
                        storageDomainId,
                        ovfData,
                        null);
        List<OvfEntityData> expectedResult = new ArrayList<>();
        expectedResult.add(ovfEntityData);
        VM vmReturnForOvf2 = new VM();
        vmReturnForOvf2.setId(newVmGuid2);
        vmReturnForOvf2.setName("Name2");
        String ovfData2 = new String("OVF data for the second VM");
        OvfEntityData ovfEntityData2 =
                new OvfEntityData(vmReturnForOvf2.getId(),
                        vmReturnForOvf2.getName(),
                        VmEntityType.VM,
                        null,
                        null,
                        storageDomainId,
                        ovfData2,
                        null);
        expectedResult.add(ovfEntityData2);

        // Mock the DAOs
        UnregisteredOVFDataDAO unregisteredOVFDataDAOMock = mock(UnregisteredOVFDataDAO.class);
        when(getDbFacadeMockInstance().getUnregisteredOVFDataDao()).thenReturn(unregisteredOVFDataDAOMock);
        when(unregisteredOVFDataDAOMock.getAllForStorageDomainByEntityType(storageDomainId, entityType)).thenReturn(expectedResult);
        when(unregisteredOVFDataDAOMock.getByEntityIdAndStorageDomain(newVmGuid2, storageDomainId)).thenReturn(ovfEntityData2);
        when(unregisteredOVFDataDAOMock.getByEntityIdAndStorageDomain(newVmGuid, storageDomainId)).thenReturn(ovfEntityData);

        // Mock OVF
        OvfHelper ovfHelperMock = mock(OvfHelper.class);
        when(getQuery().getOvfHelper()).thenReturn(ovfHelperMock);
        when(ovfHelperMock.readVmFromOvf(ovfData)).thenReturn(vmReturnForOvf);
        when(ovfHelperMock.readVmFromOvf(ovfData2)).thenReturn(vmReturnForOvf2);
    }
}
