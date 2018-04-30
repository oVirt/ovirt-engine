package org.ovirt.engine.core.dao;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

public class SupportedHostFeatureDaoTest extends BaseDaoTestCase<SupportedHostFeatureDao> {
    @Test
    public void testGetSupportedHostFeaturesByHostId() {
        Set<String> featuresSupported = dao.getSupportedHostFeaturesByHostId(FixturesTool.HOST_ID);
        assertNotNull(featuresSupported, "Failed to retrive supported addtional features in the host");
        Set<String> expectedFeatures = new HashSet<>(Arrays.asList("TEST_FEATURE_1", "TEST_FEATURE_2"));
        assertEquals(expectedFeatures, featuresSupported, "Failed to retrive supported addtional features in the host");
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
        assertTrue(featuresSupported.containsAll(newFatures), "Failed to add the feature");
    }

    @Test
    public void testRemoveAllSupportedHostFeature() {
        Set<String> featuresSupported = dao.getSupportedHostFeaturesByHostId(FixturesTool.HOST_ID);
        assertFalse(featuresSupported.isEmpty());
        dao.removeAllSupportedHostFeature(FixturesTool.HOST_ID, featuresSupported);
        Set<String> featuresSupportedAfterDeletion = dao.getSupportedHostFeaturesByHostId(FixturesTool.HOST_ID);
        assertTrue(featuresSupportedAfterDeletion.isEmpty(), "Failed to remove addtional supported features");
    }
}
