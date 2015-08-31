package org.ovirt.engine.core.dao;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.ovirt.engine.core.compat.Guid;


public class SupportedHostFeatureDaoTest extends BaseDaoTestCase {

    private static final Guid EXISTING_HOST_ID_1 = new Guid("afce7a39-8e8c-4819-ba9c-796d316592e7");

    private SupportedHostFeatureDao dao;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        dao = dbFacade.getSupportedHostFeatureDao();
    }

    @Test
    public void testGetSupportedHostFeaturesByHostId() {
        Set<String> featuresSupported = dao.getSupportedHostFeaturesByHostId(EXISTING_HOST_ID_1);
        assertNotNull("Failed to retrive supported addtional features in the host", featuresSupported);
        Set<String> expectedFeatures = new HashSet<>(Arrays.asList("TEST_FEATURE_1", "TEST_FEATURE_2"));
        assertEquals("Failed to retrive supported addtional features in the host",
                expectedFeatures,
                featuresSupported);
    }

    @Test
    public void testAddSupportedHostFeature() {
        String newFeature = "NEW_FEATURE_1";
        dao.addSupportedHostFeature(EXISTING_HOST_ID_1, newFeature);
        Set<String> featuresSupported = dao.getSupportedHostFeaturesByHostId(EXISTING_HOST_ID_1);
        assertThat("Failed to add the feature", featuresSupported, hasItem(newFeature));
    }

    @Test
    public void testAddAllSupportedHostFeature() {
        Set<String> newFatures = new HashSet<>(Arrays.asList("NEW__FEATURE_1", "NEW__FEATURE_2", "NEW__FEATURE_3"));
        dao.addAllSupportedHostFeature(EXISTING_HOST_ID_1, newFatures);
        Set<String> featuresSupported = dao.getSupportedHostFeaturesByHostId(EXISTING_HOST_ID_1);
        assertTrue("Failed to add the feature", featuresSupported.containsAll(newFatures));
    }

    @Test
    public void testRemoveAllSupportedHostFeature() {
        Set<String> featuresSupported = dao.getSupportedHostFeaturesByHostId(EXISTING_HOST_ID_1);
        assertFalse(featuresSupported.isEmpty());
        dao.removeAllSupportedHostFeature(EXISTING_HOST_ID_1, featuresSupported);
        Set<String> featuresSupportedAfterDeletion = dao.getSupportedHostFeaturesByHostId(EXISTING_HOST_ID_1);
        assertTrue("Failed to remove addtional supported features", featuresSupportedAfterDeletion.isEmpty());
    }
}
