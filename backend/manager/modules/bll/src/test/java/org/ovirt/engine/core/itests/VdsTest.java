package org.ovirt.engine.core.itests;

import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.queries.GetVdsByTypeParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.businessentities.VDS;

import java.util.Collection;

@Ignore
public class VdsTest extends AbstractBackendTest {

    @Test
    public void fenceVds() {
        VdcQueryReturnValue value = backend.runInternalQuery(VdcQueryType.GetVdsByType, new GetVdsByTypeParameters(
                VDSType.VDS));
        VDS vds = ((Collection<VDS>) value.getReturnValue()).iterator().next();
        assertNotNull(vds);
    }

}
