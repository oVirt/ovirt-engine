package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.GetVmOvfByVmIdParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDao;

@RunWith(MockitoJUnitRunner.class)
public class GetVmOvfByVmIdQueryTest extends AbstractUserQueryTest<GetVmOvfByVmIdParameters, GetVmOvfByVmIdQuery<GetVmOvfByVmIdParameters>> {

    @Mock
    private VmDao vmDao;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        setUpDaoMocks();
        doReturn("config").when(getQuery()).generateOvfConfig(any(VM.class));
    }

    private VM createVm(Guid existingVmId, long dbGeneration) {
        VM vm = new VM();
        vm.setId(existingVmId);

        vm.setDbGeneration(dbGeneration);
        return vm;
    }

    private void setUpDaoMocks() {
        doReturn(vmDao).when(getQuery()).getVmDao();
    }

    @Test
    public void dbGenerationNotAsParametersDbGen() {
        Guid id = Guid.newGuid();
        long dbGeneration = 5;
        VM vm = createVm(id, dbGeneration);
        doReturn(vm).when(vmDao).get(any(Guid.class), any(Guid.class), anyBoolean());
        when(getQueryParameters().getRequiredGeneration()).thenReturn(dbGeneration - 1);
        GetVmOvfByVmIdQuery query = getQuery();
        query.execute();
        verify(query, never()).generateOvfConfig(any(VM.class));
        assertFalse(query.getQueryReturnValue().getSucceeded());
        assertNull(query.getQueryReturnValue().getReturnValue());
    }

    @Test
    public void dbGenerationSameAsParametersDbGen() {
        Guid id = Guid.newGuid();
        long dbGeneration = 5;
        VM vm = createVm(id, dbGeneration);
        doReturn(vm).when(vmDao).get(any(Guid.class), any(Guid.class), anyBoolean());
        when(getQueryParameters().getRequiredGeneration()).thenReturn(dbGeneration);
        GetVmOvfByVmIdQuery query = getQuery();
        query.execute();
        verify(query, times(1)).generateOvfConfig(any(VM.class));
        assertTrue(query.getQueryReturnValue().getSucceeded());
        assertNotNull(query.getQueryReturnValue().getReturnValue());
    }
}
