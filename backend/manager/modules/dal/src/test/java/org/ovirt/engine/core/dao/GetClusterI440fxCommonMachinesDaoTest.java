package org.ovirt.engine.core.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.compat.Guid;

/** Tests {@link VdsDao#getClusterI440fxCommonMachines(Guid)}. */
public class GetClusterI440fxCommonMachinesDaoTest extends BaseDaoTestCase<VdsDao> {

    /** No vds rows — empty result. */
    private static final Guid EMPTY_CLUSTER =
            new Guid("e14f0f1c-0000-0000-0000-000000000001");

    /** Two hosts, both advertising pc-i440fx-rhel10.0.0. */
    private static final Guid UNIFORM_CLUSTER =
            new Guid("a0000000-0000-0000-0000-000000000001");

    /** host-A: rhel10.0.0 + rhel10.2.0; host-B: rhel10.0.0 only. Intersection = {rhel10.0.0}. */
    private static final Guid PARTIAL_CLUSTER =
            new Guid("a0000000-0000-0000-0000-000000000002");

    @Inject
    private VdsDao vdsDao;

    @Test
    public void emptyClusterReturnsEmptyList() {
        List<String> result = vdsDao.getClusterI440fxCommonMachines(EMPTY_CLUSTER);
        assertTrue(result.isEmpty(), "Cluster with no hosts must return empty list");
    }

    @Test
    public void uniformClusterReturnsSingleCommonType() {
        List<String> result = vdsDao.getClusterI440fxCommonMachines(UNIFORM_CLUSTER);
        assertEquals(1, result.size(), "Both hosts share exactly one i440fx type");
        assertEquals("pc-i440fx-rhel10.0.0", result.get(0));
    }

    @Test
    public void partialClusterReturnsOnlyIntersection() {
        // host-A has rhel10.0.0 + rhel10.2.0; host-B has rhel10.0.0 only.
        // Union would be {rhel10.0.0, rhel10.2.0}; intersection is {rhel10.0.0}.
        List<String> result = vdsDao.getClusterI440fxCommonMachines(PARTIAL_CLUSTER);
        assertEquals(1, result.size(),
                "Only the type present on BOTH hosts must be returned, not the union");
        assertEquals("pc-i440fx-rhel10.0.0", result.get(0));
    }

    @Test
    public void nonExistentClusterReturnsEmptyList() {
        List<String> result = vdsDao.getClusterI440fxCommonMachines(
                new Guid("00000000-0000-0000-0000-000000000000"));
        assertTrue(result.isEmpty());
    }
}
