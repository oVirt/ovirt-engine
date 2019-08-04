package org.ovirt.engine.core.bll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.storage.FullEntityOvfData;
import org.ovirt.engine.core.common.queries.GetUnregisteredEntityQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.UnregisteredOVFDataDao;
import org.ovirt.engine.core.utils.ovf.OvfReaderException;

/**
 * A test case for the {@link GetUnregisteredVmQuery} class.
 */
@MockitoSettings(strictness = Strictness.LENIENT)
public class GetUnregisteredVmQueryTest
        extends AbstractQueryTest<GetUnregisteredEntityQueryParameters, GetUnregisteredVmQuery<GetUnregisteredEntityQueryParameters>> {
    @Mock
    private UnregisteredOVFDataDao unregisteredOVFDataDaoMock;

    private Guid storageDomainId = Guid.newGuid();
    private Guid entityId = Guid.newGuid();
    private VM vmReturnForOvf;

    @Mock
    private OvfHelper ovfHelperMock;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        mockQueryParameters();
    }

    @Test
    public void testExecuteQueryGetUnregisteredVm() throws OvfReaderException {
        setUpVm();
        setUpQueryEntities();
        getQuery().executeQueryCommand();

        @SuppressWarnings("unchecked")
        VM result = getQuery().getQueryReturnValue().getReturnValue();
        assertEquals(vmReturnForOvf, result, "Wrong VM entity");
    }

    @Test
    public void testExecuteQueryGetUnregisteredVmNotFound() {
        setUpQueryEntitiesNotFound();
        getQuery().executeQueryCommand();

        @SuppressWarnings("unchecked")
        VM result = getQuery().getQueryReturnValue().getReturnValue();
        assertNull(result, "Wrong VM entity");
    }

    private void mockQueryParameters() {
        // Mock the Query Parameters
        when(getQueryParameters().getStorageDomainId()).thenReturn(storageDomainId);
        when(getQueryParameters().getEntityId()).thenReturn(entityId);
    }

    private void setUpVm() {
        vmReturnForOvf = new VM();
        vmReturnForOvf.setId(Guid.newGuid());
        vmReturnForOvf.setName("Name");
    }

    private void setUpQueryEntities() throws OvfReaderException {
        // Set up the expected result

        String ovfData = "OVF data for VM";
        OvfEntityData ovfEntityData =
                new OvfEntityData(vmReturnForOvf.getId(),
                        vmReturnForOvf.getName(),
                        VmEntityType.VM,
                        null,
                        null,
                        storageDomainId,
                        ovfData,
                        null);
        List<OvfEntityData> expectedResultQuery = new ArrayList<>();
        expectedResultQuery.add(ovfEntityData);

        // Mock the Daos
        when(unregisteredOVFDataDaoMock.getByEntityIdAndStorageDomain(entityId, storageDomainId)).thenReturn(
                expectedResultQuery);

        // Mock OVF
        when(ovfHelperMock.readVmFromOvf(ovfData)).thenReturn(new FullEntityOvfData(vmReturnForOvf));
    }

    private void setUpQueryEntitiesNotFound() {
        // Mock the Daos
        when(unregisteredOVFDataDaoMock.getByEntityIdAndStorageDomain(entityId, storageDomainId)).thenReturn(
                new ArrayList<>());

    }
}
