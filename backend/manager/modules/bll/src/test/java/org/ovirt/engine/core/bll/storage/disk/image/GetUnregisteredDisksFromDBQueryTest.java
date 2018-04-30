package org.ovirt.engine.core.bll.storage.disk.image;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.AbstractQueryTest;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.storage.UnregisteredDisk;
import org.ovirt.engine.core.common.businessentities.storage.UnregisteredDiskId;
import org.ovirt.engine.core.common.queries.IdAndBooleanQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.UnregisteredDisksDao;

public class GetUnregisteredDisksFromDBQueryTest extends AbstractQueryTest<IdAndBooleanQueryParameters, GetUnregisteredDisksFromDBQuery<? extends IdAndBooleanQueryParameters>> {

    Guid storageDomainId = Guid.newGuid();
    Guid newDiskId = Guid.newGuid();
    Guid newDiskId2 = Guid.newGuid();

    @Mock
    private UnregisteredDisksDao unregisteredDisksDaoMock;

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
        assertEquals(1, result.size(), "Wrong number of Disks in result");
    }

    private void mockQueryParameters() {
        // Mock the Query Parameters
        when(getQueryParameters().getId()).thenReturn(storageDomainId);
        when(getQueryParameters().isFilterResult()).thenReturn(true);
    }

    private void setUpQueryEntities() {
        // Set up the expected result
        UnregisteredDisk unregistedDisk = new UnregisteredDisk();
        unregistedDisk.setId(new UnregisteredDiskId(newDiskId, storageDomainId));
        unregistedDisk.setDiskAlias("DiskAlias");
        unregistedDisk.setDescription("DiskDescription");
        ArrayList<VmBase> vms = new ArrayList<>();
        VmBase vm = new VmBase();
        vm.setId(Guid.newGuid());
        vm.setName("FirstVM");
        vms.add(vm);
        unregistedDisk.setVms(vms);

        UnregisteredDisk unregistedDisk2 = new UnregisteredDisk();
        unregistedDisk2.setId(new UnregisteredDiskId(newDiskId2, storageDomainId));
        unregistedDisk2.setDiskAlias("DiskAlias2");
        unregistedDisk2.setDescription("DiskDescription2");

        List<UnregisteredDisk> expectedResultFromDB = new ArrayList<>();
        expectedResultFromDB.add(unregistedDisk);
        expectedResultFromDB.add(unregistedDisk2);

        // Mock the Daos
        when(unregisteredDisksDaoMock.getByDiskIdAndStorageDomainId(null, storageDomainId)).thenReturn(expectedResultFromDB);
    }
}
