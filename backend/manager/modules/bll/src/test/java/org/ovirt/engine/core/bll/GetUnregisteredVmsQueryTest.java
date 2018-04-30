package org.ovirt.engine.core.bll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.storage.ovfstore.OvfHelper;
import org.ovirt.engine.core.common.businessentities.OvfEntityData;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.storage.FullEntityOvfData;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.UnregisteredOVFDataDao;
import org.ovirt.engine.core.utils.ovf.OvfReaderException;

/** A test case for the {@link GetUnregisteredVmsQuery} class. */
@MockitoSettings(strictness = Strictness.LENIENT)
public class GetUnregisteredVmsQueryTest extends AbstractQueryTest<IdQueryParameters, GetUnregisteredVmsQuery<? extends IdQueryParameters>> {
    @Mock
    private UnregisteredOVFDataDao unregisteredOVFDataDaoMock;

    Guid storageDomainId = Guid.newGuid();
    VmEntityType entityType = VmEntityType.VM;
    Guid newVmGuid = Guid.newGuid();
    VMStatus vmStatus = VMStatus.Up;
    Guid newVmGuid2 = Guid.newGuid();
    VMStatus vmStatus2 = VMStatus.Down;

    @Mock
    private OvfHelper ovfHelperMock;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        mockQueryParameters();
        setUpQueryEntities();
    }

    @Test
    public void testExecuteQueryGetAllEntitiesCommand() {
        getQuery().executeQueryCommand();

        @SuppressWarnings("unchecked")
        List<VM> result = getQuery().getQueryReturnValue().getReturnValue();
        assertEquals(2, result.size(), "Wrong number of VMs in result");
        result.forEach(vm -> {
            assertTrue(vm.getId().equals(newVmGuid) ? vm.getStatus() == vmStatus : vm.getStatus() == vmStatus2);
        });
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
        ovfEntityData.setStatus(vmStatus);
        List<OvfEntityData> expectedResultQuery1 = new ArrayList<>();
        expectedResultQuery1.add(ovfEntityData);
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
        ovfEntityData2.setStatus(vmStatus2);
        expectedResult.add(ovfEntityData2);
        List<OvfEntityData> expectedResultQuery2 = new ArrayList<>();
        expectedResultQuery2.add(ovfEntityData);

        // Mock the Daos
        when(unregisteredOVFDataDaoMock.getAllForStorageDomainByEntityType(storageDomainId, entityType)).thenReturn(expectedResult);
        when(unregisteredOVFDataDaoMock.getByEntityIdAndStorageDomain(newVmGuid2, storageDomainId)).thenReturn(expectedResultQuery2);
        when(unregisteredOVFDataDaoMock.getByEntityIdAndStorageDomain(newVmGuid, storageDomainId)).thenReturn(expectedResultQuery1);

        // Mock OVF
        when(ovfHelperMock.readVmFromOvf(ovfData)).thenReturn(new FullEntityOvfData(vmReturnForOvf));
        when(ovfHelperMock.readVmFromOvf(ovfData2)).thenReturn(new FullEntityOvfData(vmReturnForOvf2));
    }
}
