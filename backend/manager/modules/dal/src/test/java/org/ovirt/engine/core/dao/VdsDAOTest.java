package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;

public class VdsDAOTest extends BaseDAOTestCase {
    private static final Guid EXISTING_VDS_ID = new Guid("afce7a39-8e8c-4819-ba9c-796d316592e7");

    private static final String IP_ADDRESS = "192.168.122.17";
    private VdsDAO dao;
    private VDS existingVds;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        dao = prepareDAO(dbFacade.getVdsDAO());
        existingVds = dao.get(EXISTING_VDS_ID);
    }

    /**
     * Ensures that retrieving with an invalid ID returns null.
     */
    @Test
    public void testGetWithInvalidId() {
        VDS result = dao.get(NGuid.NewGuid());

        assertNull(result);
    }

    /**
     * Ensures that retrieving VDS by ID works as expected.
     */
    @Test
    public void testGet() {
        VDS result = dao.get(existingVds.getId());

        assertCorrectGetResult(result);
    }

    /**
     * Ensures that retrieving VDS by ID works as expected for a privileged user.
     */
    @Test
    public void testGetWithPermissionsPrivilegedUser() {
        VDS result = dao.get(existingVds.getId(), PRIVILEGED_USER_ID, true);

        assertCorrectGetResult(result);
    }

    /**
     * Ensures that retrieving VDS by ID works as expected with filtering disabled for an unprivileged user.
     */
    @Test
    public void testGetWithPermissionsDisabledUnprivilegedUser() {
        VDS result = dao.get(existingVds.getId(), UNPRIVILEGED_USER_ID, false);

        assertCorrectGetResult(result);
    }

    /**
     * Ensures that no VDS is retrieved for an unprivileged user.
     */
    @Test
    public void testGetWithPermissionsUnprivilegedUser() {
        VDS result = dao.get(existingVds.getId(), UNPRIVILEGED_USER_ID, true);

        assertNull(result);
    }

    /**
     * Ensures that an empty collection is returned.
     */
    @Test
    public void testGetAllWithNameUsingInvalidName() {
        List<VDS> result = dao.getAllWithName("farkle");

        assertIncorrectGetResult(result);
    }

    /**
     * Asserts the result from {@link VdsDAO#get(NGuid)} is correct.
     * @param result
     */
    private void assertCorrectGetResult(VDS result) {
        assertNotNull(result);
        assertEquals(existingVds, result);
    }

    /**
     * Asserts the result from a call to {@link VdsDAO#get(NGuid)}
     * that isn't supposed to return any data is indeed empty.
     * @param result
     */
    private static void assertIncorrectGetResult(List<VDS> result) {
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures the right set of objects are returned with the given name.
     */
    @Test
    public void testGetAllWithName() {
        List<VDS> result = dao.getAllWithName(existingVds.getvds_name());

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (VDS vds : result) {
            assertEquals(existingVds.getvds_name(), vds.getvds_name());
        }
    }

    /**
     * Ensures that the right set of VDS instances are returned for the given hostname.
     */
    @Test
    public void testGetAllForHostname() {
        List<VDS> result = dao.getAllForHostname(existingVds.gethost_name());

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (VDS vds : result) {
            assertEquals(existingVds.gethost_name(), vds.gethost_name());
        }
    }

    /**
     * Ensures that the right set of VDS instances are returned.
     */
    @Test
    public void testGetAllWithIpAddress() {
        List<VDS> result = dao.getAllWithIpAddress(IP_ADDRESS);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    /**
     * Ensures the right set of VDS instances are returned.
     */
    @Test
    public void testGetAllWithUniqueId() {
        List<VDS> result = dao.getAllWithUniqueId(existingVds.getUniqueId());

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (VDS vds : result) {
            assertEquals(existingVds.getUniqueId(), vds.getUniqueId());
        }
    }

    /**
     * Ensures that an empty collection is returned if the type is not present.
     */
    @Test
    public void testGetAllOfTypeWithUnrepresentedType() {
        List<VDS> result = dao.getAllOfType(VDSType.oVirtNode);

        assertIncorrectGetResult(result);
    }

    /**
     * Ensures that all of the right instances for the given type.
     */
    @Test
    public void testGetAllOfType() {
        List<VDS> result = dao.getAllOfType(VDSType.VDS);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (VDS vds : result) {
            assertEquals(VDSType.VDS, vds.getvds_type());
        }
    }

    /**
     * Ensures that an empty collection is returned.
     */
    @Test
    public void testGetAllOfTypesWithUnrepresentedTypes() {
        List<VDS> result = dao
                .getAllOfTypes(new VDSType[] { VDSType.oVirtNode });

        assertIncorrectGetResult(result);
    }

    /**
     * Ensures that all of the right instances for the given types.
     */
    @Test
    public void testGetAllOfTypes() {
        List<VDS> result = dao.getAllOfTypes(new VDSType[] { VDSType.VDS });

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (VDS vds : result) {
            assertEquals(VDSType.VDS, vds.getvds_type());
        }
    }

    /**
     * Ensures the API works as expected.
     */
    @Test
    public void testGetAllForVdsGroupWithoutMigrating() {
        List<VDS> result = dao.getAllForVdsGroupWithoutMigrating(existingVds
                .getvds_group_id());

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (VDS vds : result) {
            assertEquals(existingVds.getvds_group_id(), vds.getvds_group_id());
        }
    }

    /**
     * Ensures that all VDS instances are returned.
     */
    @Test
    public void testGetAll() {
        List<VDS> result = dao.getAll();

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    /**
     * Ensures that all VDS related to the VDS group supplied.
     */
    @Test
    public void testGetAllForVdsGroup() {
        List<VDS> result = dao.getAllForVdsGroup(existingVds.getvds_group_id());

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (VDS vds : result) {
            assertEquals(existingVds.getvds_group_id(), vds.getvds_group_id());
        }
    }

    /**
     * Ensures that the VDS instances are returned according to spm priority
     */
    @Test
    public void testGetListForSpmSelection() {
        final Guid STORAGE_POOL_ID = new Guid("6d849ebf-755f-4552-ad09-9a090cda105d");
        List<VDS> result = dao.getListForSpmSelection(STORAGE_POOL_ID);
        assertTrue(result.get(0).getVdsSpmPriority() >= result.get(1).getVdsSpmPriority());
    }
}
