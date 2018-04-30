package org.ovirt.engine.core.bll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDao;

public class GetVmsPinnedToHostQueryTest extends AbstractUserQueryTest<IdQueryParameters, GetVmsPinnedToHostQuery<IdQueryParameters>> {

    @Mock
    private VmDao vmDao;

    @Mock
    private VmHandler vmHandler;

    @Test
    public void testExecuteQuery() {
        VM expectedResult = new VM();
        expectedResult.setId(Guid.newGuid());

        IdQueryParameters paramsMock = getQueryParameters();
        when(paramsMock.getId()).thenReturn(Guid.newGuid());

        when(vmDao.getAllPinnedToHost(any(Guid.class))).thenReturn(Collections.singletonList(expectedResult));
        getQuery().executeQueryCommand();

        List<VM> result = getQuery().getQueryReturnValue().getReturnValue();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(expectedResult, result.get(0), "Wrong VM returned");
    }

}
