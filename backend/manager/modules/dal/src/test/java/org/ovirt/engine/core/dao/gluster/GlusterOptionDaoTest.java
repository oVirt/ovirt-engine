package org.ovirt.engine.core.dao.gluster;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeOptionEntity;
import org.ovirt.engine.core.common.constants.gluster.GlusterConstants;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseDAOTestCase;

public class GlusterOptionDaoTest extends BaseDAOTestCase {
    private static final Guid EXISTING_VOL_ID = new Guid("0c3f45f6-3fe9-4b35-a30c-be0d1a835ea8");
    private static final Guid EXISTING_OPTION_ID = new Guid("9e6a606e-9a7a-4398-9b13-3ad4777abfba");
    private static final String OPTION_AUTH_REJECT = "auth.reject";
    private static final String OPTION_AUTH_REJECT_VALUE = "192.168.1.123";
    private static final String OPTION_AUTH_ALLOW_VALUE_NEW = "192.168.1.321";
    private static final String OPTION_AUTH_ALLOW_VALUE_ALL = "*";

    private GlusterOptionDao dao;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        dao = dbFacade.getGlusterOptionDao();
    }

    @Test
    public void testSaveAndGetById() {
        GlusterVolumeOptionEntity newOption = new GlusterVolumeOptionEntity(EXISTING_VOL_ID, OPTION_AUTH_REJECT, OPTION_AUTH_REJECT_VALUE);
        dao.save(newOption);

        GlusterVolumeOptionEntity retrievedOption = dao.getById(newOption.getId());
        assertNotNull(retrievedOption);
        assertEquals(newOption, retrievedOption);
    }

    @Test
    public void testUpdateVolumeOption() {
        GlusterVolumeOptionEntity optionAuthAllow = dao.getById(EXISTING_OPTION_ID);
        assertEquals(GlusterConstants.OPTION_AUTH_ALLOW, optionAuthAllow.getKey());
        assertEquals(OPTION_AUTH_ALLOW_VALUE_ALL, optionAuthAllow.getValue());

        dao.updateVolumeOption(optionAuthAllow.getId(), OPTION_AUTH_ALLOW_VALUE_NEW);

        GlusterVolumeOptionEntity retrievedOption = dao.getById(EXISTING_OPTION_ID);
        assertNotNull(retrievedOption);
        assertEquals(OPTION_AUTH_ALLOW_VALUE_NEW, retrievedOption.getValue());
    }

    @Test
    public void testRemoveVolumeOption() {
        assertNotNull(dao.getById(EXISTING_OPTION_ID));
        dao.removeVolumeOption(EXISTING_OPTION_ID);
        assertNull(dao.getById(EXISTING_OPTION_ID));
    }
}
