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

public class SupportedHostFeatureDaoTest extends BaseDaoTestCase {

    private SupportedHostFeatureDao dao;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        dao = dbFacade.getSupportedHostFeatureDao();
    }

    @Test
    public void testGetSupportedHostFeaturesByHostId() {
        Set<String> featuresSupported = dao.getSupportedHostFeaturesByHostId(FixturesTool.HOST_ID);
        assertNotNull("Failed to retrive supported addtional features in the host", featuresSupported);
        Set<String> expectedFeatures = new HashSet<>(Arrays.asList("TEST_FEATURE_1", "TEST_FEATURE_2"));
        assertEquals("Failed to retrive supported addtional features in the host",
                expectedFeatures,
                featuresSupported);
    }

    @Test
    public void testAddSupportedHostFeature() {
        String newFeature = "NEW_FEATURE_1";
        dao.addSupportedHostFeature(FixturesTool.HOST_ID, newFeature);
        Set<String> featuresSupported = dao.getSupportedHostFeaturesByHostId(FixturesTool.HOST_ID);
        assertThat("Failed to add the feature", featuresSupported, hasItem(newFeature));
    }

    @Test
    public void testAddAllSupportedHostFeature() {
        Set<String> newFatures = new HashSet<>(Arrays.asList("NEW__FEATURE_1", "NEW__FEATURE_2", "NEW__FEATURE_3"));
        dao.addAllSupportedHostFeature(FixturesTool.HOST_ID, newFatures);
        Set<String> featuresSupported = dao.getSupportedHostFeaturesByHostId(FixturesTool.HOST_ID);
        assertTrue("Failed to add the feature", featuresSupported.containsAll(newFatures));
    }

    @Test
    public void testRemoveAllSupportedHostFeature() {
        Set<String> featuresSupported = dao.getSupportedHostFeaturesByHostId(FixturesTool.HOST_ID);
        assertFalse(featuresSupported.isEmpty());
        dao.removeAllSupportedHostFeature(FixturesTool.HOST_ID, featuresSupported);
        Set<String> featuresSupportedAfterDeletion = dao.getSupportedHostFeaturesByHostId(FixturesTool.HOST_ID);
        assertTrue("Failed to remove addtional supported features", featuresSupportedAfterDeletion.isEmpty());
    }
}
