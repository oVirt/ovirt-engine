package org.ovirt.engine.core.bll;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.GetVmOvfByVmIdParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmDeviceDao;

@MockitoSettings(strictness = Strictness.LENIENT)
public class GetVmOvfByVmIdQueryTest extends AbstractUserQueryTest<GetVmOvfByVmIdParameters, GetVmOvfByVmIdQuery<GetVmOvfByVmIdParameters>> {

    @Mock
    private VmDao vmDao;

    @Mock
    private SnapshotDao snapshotDao;

    @Mock
    private VmDeviceDao vmDeviceDao;

    @Mock
    private VmDeviceUtils vmDeviceUtils;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        doReturn("config").when(getQuery()).generateOvfConfig(any(), anyBoolean());
    }

    private VM createVm(Guid existingVmId, long dbGeneration) {
        VM vm = new VM();
        vm.setId(existingVmId);

        vm.setDbGeneration(dbGeneration);
        return vm;
    }

    @Test
    public void dbGenerationNotAsParametersDbGen() {
        Guid id = Guid.newGuid();
        long dbGeneration = 5;
        VM vm = createVm(id, dbGeneration);
        doReturn(vm).when(vmDao).get(any(), any(), anyBoolean());
        when(getQueryParameters().getRequiredGeneration()).thenReturn(dbGeneration - 1);
        GetVmOvfByVmIdQuery query = getQuery();
        query.execute();
        verify(query, never()).generateOvfConfig(any(), anyBoolean());
        assertFalse(query.getQueryReturnValue().getSucceeded());
        assertNull(query.getQueryReturnValue().getReturnValue());
    }

    @Test
    public void dbGenerationSameAsParametersDbGen() {
        Guid id = Guid.newGuid();
        long dbGeneration = 5;
        VM vm = createVm(id, dbGeneration);
        doReturn(vm).when(vmDao).get(any(), any(), anyBoolean());
        when(getQueryParameters().getRequiredGeneration()).thenReturn(dbGeneration);
        GetVmOvfByVmIdQuery query = getQuery();
        query.execute();
        verify(query, times(1)).generateOvfConfig(any(), anyBoolean());
        assertTrue(query.getQueryReturnValue().getSucceeded());
        assertNotNull(query.getQueryReturnValue().getReturnValue());
    }
}
