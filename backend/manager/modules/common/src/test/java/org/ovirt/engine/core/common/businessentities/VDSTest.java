package org.ovirt.engine.core.common.businessentities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.ovirt.engine.core.compat.Guid;

/**
 * A test case for the {@link VDS} class.
 */
public class VDSTest {

    public static Stream<VDS> vdsCloned() {
        VDS vds1 = new VDS();

        VDS vds2 = new VDS();
        vds2.setId(Guid.newGuid());

        VDS vds3 = new VDS();
        vds3.setId(Guid.newGuid());
        vds3.setClusterId(Guid.newGuid());

        return Stream.of(vds1, vds2, vds3);
    }

    @ParameterizedTest
    @MethodSource
    public void vdsCloned(VDS vds) {
        VDS cloned = vds.clone();
        assertEquals(vds, cloned, "clones not equal");
        assertEquals(vds.hashCode(), cloned.hashCode(), "clones do not have equal hashCodes");
    }
}
