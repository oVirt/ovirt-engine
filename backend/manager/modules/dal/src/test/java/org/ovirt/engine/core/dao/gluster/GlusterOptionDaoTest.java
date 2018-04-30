package org.ovirt.engine.core.dao.gluster;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeOptionEntity;
import org.ovirt.engine.core.common.constants.gluster.GlusterConstants;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseDaoTestCase;

public class GlusterOptionDaoTest extends BaseDaoTestCase<GlusterOptionDao> {
    private static final Guid EXISTING_VOL_ID = new Guid("0c3f45f6-3fe9-4b35-a30c-be0d1a835ea8");
    private static final Guid EXISTING_OPTION_ID = new Guid("9e6a606e-9a7a-4398-9b13-3ad4777abfba");
    private static final String OPTION_AUTH_REJECT = "auth.reject";
    private static final String OPTION_AUTH_REJECT_VALUE = "192.168.1.123";
    private static final String OPTION_AUTH_ALLOW_VALUE_NEW = "192.168.1.321";
    private static final String OPTION_AUTH_ALLOW_VALUE_ALL = "*";

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

    @Test
    public void testRemoveVolumeOptionMultiple() {
        List<GlusterVolumeOptionEntity> options = dao.getOptionsOfVolume(EXISTING_VOL_ID);
        assertEquals(2, options.size());

        List<Guid> idsToRemove = new ArrayList<>();
        idsToRemove.add(options.get(0).getId());
        idsToRemove.add(options.get(1).getId());
        dao.removeAll(idsToRemove);

        options = dao.getOptionsOfVolume(EXISTING_VOL_ID);
        assertTrue(options.isEmpty());
    }
}
