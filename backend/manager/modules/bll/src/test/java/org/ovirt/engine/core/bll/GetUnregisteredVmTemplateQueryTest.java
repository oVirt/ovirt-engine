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
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage.FullEntityOvfData;
import org.ovirt.engine.core.common.queries.GetUnregisteredEntityQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.UnregisteredOVFDataDao;
import org.ovirt.engine.core.utils.ovf.OvfReaderException;

/**
 * A test case for the {@link GetUnregisteredVmTemplateQuery} class.
 */
@MockitoSettings(strictness = Strictness.LENIENT)
public class GetUnregisteredVmTemplateQueryTest
        extends AbstractQueryTest<GetUnregisteredEntityQueryParameters, GetUnregisteredVmTemplateQuery<GetUnregisteredEntityQueryParameters>> {
    @Mock
    private UnregisteredOVFDataDao unregisteredOVFDataDaoMock;

    private Guid storageDomainId = Guid.newGuid();
    private Guid entityId = Guid.newGuid();
    private VmTemplate vmTemplateReturnForOvf;

    @Mock
    private OvfHelper ovfHelperMock;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        mockQueryParameters();
    }

    @Test
    public void testExecuteQueryGetUnregisteredTemplate() throws OvfReaderException {
        setUpTemplate();
        setUpQueryEntities();
        getQuery().executeQueryCommand();

        @SuppressWarnings("unchecked")
        VmTemplate result = getQuery().getQueryReturnValue().getReturnValue();
        assertEquals(vmTemplateReturnForOvf, result, "Wrong Template entity");
    }

    @Test
    public void testExecuteQueryGetUnregisteredTemplateNotFound() {
        setUpQueryEntitiesNotFound();
        getQuery().executeQueryCommand();

        @SuppressWarnings("unchecked")
        VmTemplate result = getQuery().getQueryReturnValue().getReturnValue();
        assertNull(result, "Wrong Template entity");
    }

    private void mockQueryParameters() {
        // Mock the Query Parameters
        when(getQueryParameters().getStorageDomainId()).thenReturn(storageDomainId);
        when(getQueryParameters().getEntityId()).thenReturn(entityId);
    }

    private void setUpTemplate() {
        vmTemplateReturnForOvf = new VmTemplate();
        vmTemplateReturnForOvf.setId(Guid.newGuid());
        vmTemplateReturnForOvf.setName("Name");
    }

    private void setUpQueryEntities() throws OvfReaderException {
        // Set up the expected result

        String ovfData = "OVF data for Template";
        OvfEntityData ovfEntityData =
                new OvfEntityData(vmTemplateReturnForOvf.getId(),
                        vmTemplateReturnForOvf.getName(),
                        VmEntityType.TEMPLATE,
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
        when(ovfHelperMock.readVmTemplateFromOvf(ovfData)).thenReturn(new FullEntityOvfData(vmTemplateReturnForOvf));
    }

    private void setUpQueryEntitiesNotFound() {
        // Mock the Daos
        when(unregisteredOVFDataDaoMock.getByEntityIdAndStorageDomain(entityId, storageDomainId)).thenReturn(
                new ArrayList<>());

    }
}
