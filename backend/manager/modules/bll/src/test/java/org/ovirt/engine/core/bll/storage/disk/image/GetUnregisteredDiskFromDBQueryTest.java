package org.ovirt.engine.core.bll.storage.disk.image;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.AbstractQueryTest;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.storage.UnregisteredDisk;
import org.ovirt.engine.core.common.businessentities.storage.UnregisteredDiskId;
import org.ovirt.engine.core.common.queries.GetUnregisteredEntityQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.UnregisteredDisksDao;

public class GetUnregisteredDiskFromDBQueryTest extends AbstractQueryTest<GetUnregisteredEntityQueryParameters, GetUnregisteredDiskFromDBQuery<GetUnregisteredEntityQueryParameters>> {

    private Guid storageDomainId = Guid.newGuid();
    private Guid newDiskId = Guid.newGuid();
    private UnregisteredDisk unregisteredDisk;

    @Mock
    private UnregisteredDisksDao unregisteredDisksDaoMock;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        mockQueryParameters();
    }

    @Test
    public void testExecuteQueryGetEntityCommand() {
        setUpQueryEntities();
        getQuery().executeQueryCommand();

        @SuppressWarnings("unchecked")
        UnregisteredDisk result = getQuery().getQueryReturnValue().getReturnValue();
        assertEquals(unregisteredDisk, result, "Wrong unregistered disk entity");
    }

    @Test
    public void testExecuteQueryGetUnregisteredDiskNotFound() {
        setUpQueryEntityNotFound();
        getQuery().executeQueryCommand();

        @SuppressWarnings("unchecked")
        UnregisteredDisk result = getQuery().getQueryReturnValue().getReturnValue();
        assertNull(result, "Wrong unregistered disk entity");
    }

    private void mockQueryParameters() {
        // Mock the Query Parameters
        when(getQueryParameters().getStorageDomainId()).thenReturn(storageDomainId);
        when(getQueryParameters().getEntityId()).thenReturn(newDiskId);
    }

    private void setUpQueryEntities() {
        // Set up the expected result
        unregisteredDisk = new UnregisteredDisk();
        unregisteredDisk.setId(new UnregisteredDiskId(newDiskId, storageDomainId));
        unregisteredDisk.setDiskAlias("DiskAlias");
        unregisteredDisk.setDescription("DiskDescription");
        ArrayList<VmBase> vms = new ArrayList<>();
        VmBase vm = new VmBase();
        vm.setId(Guid.newGuid());
        vm.setName("FirstVM");
        vms.add(vm);
        unregisteredDisk.setVms(vms);

        List<UnregisteredDisk> expectedResultFromDB = new ArrayList<>();
        expectedResultFromDB.add(unregisteredDisk);

        // Mock the Daos
        when(unregisteredDisksDaoMock.getByDiskIdAndStorageDomainId(newDiskId, storageDomainId)).thenReturn(expectedResultFromDB);
    }

    private void setUpQueryEntityNotFound() {
        // Mock the Daos
        when(unregisteredDisksDaoMock.getByDiskIdAndStorageDomainId(newDiskId, storageDomainId)).thenReturn(new ArrayList<>());
    }
}
