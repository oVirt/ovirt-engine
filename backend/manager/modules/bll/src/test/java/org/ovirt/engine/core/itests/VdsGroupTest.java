package org.ovirt.engine.core.itests;

import org.junit.Test;
import static org.junit.Assert.*;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;

import java.util.Collection;

public class VdsGroupTest extends AbstractBackendTest {

    @Test
    public void getAllVdsGroups() {
        VdcQueryReturnValue value = backend
                .runInternalQuery(VdcQueryType.GetAllVdsGroups, new VdcQueryParametersBase());
        assertTrue(value.getSucceeded());
        assertFalse(((Collection) value.getReturnValue()).isEmpty());
    }

}
