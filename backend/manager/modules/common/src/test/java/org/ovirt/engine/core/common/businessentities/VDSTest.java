package org.ovirt.engine.core.common.businessentities;

import static org.junit.Assert.assertEquals;

import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.ovirt.engine.core.compat.Guid;

/**
 * A test case for the {@link VDS} class.
 */
@RunWith(Theories.class)
public class VDSTest {

    @DataPoints
    public static VDS[] data() {
        VDS vds1 = new VDS();

        VDS vds2 = new VDS();
        vds2.setId(Guid.newGuid());

        VDS vds3 = new VDS();
        vds3.setId(Guid.newGuid());
        vds3.setClusterId(Guid.newGuid());

        return new VDS[] { vds1, vds2, vds3 };
    }

    @Theory
    public void testClone(VDS vds) {
        VDS cloned = vds.clone();
        assertEquals("clones not equal", vds, cloned);
        assertEquals("clones do not have equal hashCodes", vds.hashCode(), cloned.hashCode());
    }
}
