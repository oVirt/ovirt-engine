package org.ovirt.engine.core.dao.gluster;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotConfig;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseDaoTestCase;

public class GlusterVolumeSnapshotConfigDaoTest extends BaseDaoTestCase<GlusterVolumeSnapshotConfigDao> {

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

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
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

        assertNotNull(configs);
        assertEquals(3, configs.size());
        assertTrue(configs.contains(existingConfig1));
    }

    @Test
    public void testGetGlusterVolumeSnapshotConfigByVolumeId() {
        List<GlusterVolumeSnapshotConfig> configs =
                dao.getConfigByVolumeId(CLUSTER_ID, VOLUME_ID);

        assertNotNull(configs);
        assertEquals(2, configs.size());
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

        assertNotNull(configs);
        assertEquals(3, configs.size());
    }

    @Test
    public void testUpdateByClusterIdAndName() {
        GlusterVolumeSnapshotConfig config = dao.getConfigByClusterIdAndName(CLUSTER_ID, PARAM_NAME_3);

        assertNotNull(config);
        assertEquals("value3", config.getParamValue());

        dao.updateConfigByClusterIdAndName(CLUSTER_ID, PARAM_NAME_3, "new_value");

        GlusterVolumeSnapshotConfig modifiedConfig = dao.getConfigByClusterIdAndName(CLUSTER_ID, PARAM_NAME_3);
        assertNotNull(modifiedConfig);
        assertEquals("new_value", modifiedConfig.getParamValue());
    }

    @Test
    public void testUpdateByVolumeIdAndName() {
        GlusterVolumeSnapshotConfig config = dao.getConfigByVolumeIdAndName(CLUSTER_ID, VOLUME_ID, PARAM_NAME_1);

        assertNotNull(config);
        assertEquals("value1", config.getParamValue());

        dao.updateConfigByVolumeIdAndName(CLUSTER_ID, VOLUME_ID, PARAM_NAME_1, "new_value");

        GlusterVolumeSnapshotConfig modifiedConfig =
                dao.getConfigByVolumeIdAndName(CLUSTER_ID, VOLUME_ID, PARAM_NAME_1);
        assertNotNull(modifiedConfig);
        assertEquals("new_value", modifiedConfig.getParamValue());
    }
}
