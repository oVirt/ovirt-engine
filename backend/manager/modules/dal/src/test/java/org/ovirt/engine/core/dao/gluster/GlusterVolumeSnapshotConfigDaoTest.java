package org.ovirt.engine.core.dao.gluster;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotConfig;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseDaoTestCase;

public class GlusterVolumeSnapshotConfigDaoTest extends BaseDaoTestCase {

    private static final Guid CLUSTER_ID = new Guid("ae956031-6be2-43d6-bb8f-5191c9253314");
    private static final Guid VOLUME_ID = new Guid("0c3f45f6-3fe9-4b35-a30c-be0d1a835ea8");
    private static final String PARAM_NAME_1 = "param1";
    private static final String PARAM_NAME_2 = "param2";
    private static final String PARAM_NAME_3 = "param3";
    private static final String NEW_PARAM_NAME = "new_param";

    private GlusterVolumeSnapshotConfig existingConfig1;
    private GlusterVolumeSnapshotConfig existingConfig2;
    private GlusterVolumeSnapshotConfig existingConfig3;
    private GlusterVolumeSnapshotConfig newConfig;
    private GlusterVolumeSnapshotConfigDao dao;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        dao = dbFacade.getGlusterVolumeSnapshotConfigDao();
        existingConfig1 = dao.getConfigByVolumeIdAndName(CLUSTER_ID, VOLUME_ID, PARAM_NAME_1);
        existingConfig2 = dao.getConfigByVolumeIdAndName(CLUSTER_ID, VOLUME_ID, PARAM_NAME_2);
        existingConfig3 = dao.getConfigByClusterIdAndName(CLUSTER_ID, PARAM_NAME_3);
    }

    @Test
    public void testSaveAndGetByVolumeId() {
        GlusterVolumeSnapshotConfig config =
                dao.getConfigByVolumeIdAndName(CLUSTER_ID, VOLUME_ID, NEW_PARAM_NAME);
        assertNull(config);

        newConfig = insertTestConfig();
        config = dao.getConfigByVolumeIdAndName(CLUSTER_ID, VOLUME_ID, NEW_PARAM_NAME);

        assertNotNull(config);
        assertEquals(newConfig, config);
    }

    private GlusterVolumeSnapshotConfig insertTestConfig() {
        GlusterVolumeSnapshotConfig config = new GlusterVolumeSnapshotConfig();
        config.setClusterId(CLUSTER_ID);
        config.setVolumeId(VOLUME_ID);
        config.setParamName(NEW_PARAM_NAME);
        config.setParamValue("new_value");
        dao.save(config);

        return config;
    }

    @Test
    public void testGetGlusterVolumeSnapshotConfigByClusterId() {
        List<GlusterVolumeSnapshotConfig> configs = dao.getConfigByClusterId(CLUSTER_ID);

        assertTrue(configs != null);
        assertTrue(configs.size() == 3);
        assertTrue(configs.contains(existingConfig1));
    }

    @Test
    public void testGetGlusterVolumeSnapshotConfigByVolumeId() {
        List<GlusterVolumeSnapshotConfig> configs =
                dao.getConfigByVolumeId(CLUSTER_ID, VOLUME_ID);

        assertTrue(configs != null);
        assertTrue(configs.size() == 2);
        assertTrue(configs.contains(existingConfig1));
    }

    @Test
    public void testGetGlusterVolumeSnapshotConfigByClusterIdAndName() {
        GlusterVolumeSnapshotConfig config =
                dao.getConfigByClusterIdAndName(CLUSTER_ID, PARAM_NAME_3);

        assertNotNull(config);
        assertEquals(config, existingConfig3);
    }

    @Test
    public void testGetGlusterVolumeSnapshotConfigByVolumeIdAndName() {
        GlusterVolumeSnapshotConfig config =
                dao.getConfigByVolumeIdAndName(CLUSTER_ID, VOLUME_ID, PARAM_NAME_2);

        assertNotNull(config);
        assertEquals(config, existingConfig2);
    }

    @Test
    public void testGetAllWithQuery() {
        List<GlusterVolumeSnapshotConfig> configs =
                dao.getAllWithQuery("select * from gluster_volume_snapshot_config");

        assertTrue(configs != null);
        assertTrue(configs.size() == 3);
    }

    @Test
    public void testUpdateByClusterIdAndName() {
        GlusterVolumeSnapshotConfig config = dao.getConfigByClusterIdAndName(CLUSTER_ID, PARAM_NAME_3);

        assertNotNull(config);
        assertEquals(config.getParamValue(), "value3");

        dao.updateConfigByClusterIdAndName(CLUSTER_ID, PARAM_NAME_3, "new_value");

        GlusterVolumeSnapshotConfig modifiedConfig = dao.getConfigByClusterIdAndName(CLUSTER_ID, PARAM_NAME_3);
        assertNotNull(modifiedConfig);
        assertEquals(modifiedConfig.getParamValue(), "new_value");
    }

    @Test
    public void testUpdateByVolumeIdAndName() {
        GlusterVolumeSnapshotConfig config = dao.getConfigByVolumeIdAndName(CLUSTER_ID, VOLUME_ID, PARAM_NAME_1);

        assertNotNull(config);
        assertEquals(config.getParamValue(), "value1");

        dao.updateConfigByVolumeIdAndName(CLUSTER_ID, VOLUME_ID, PARAM_NAME_1, "new_value");

        GlusterVolumeSnapshotConfig modifiedConfig =
                dao.getConfigByVolumeIdAndName(CLUSTER_ID, VOLUME_ID, PARAM_NAME_1);
        assertNotNull(modifiedConfig);
        assertEquals(modifiedConfig.getParamValue(), "new_value");
    }
}
