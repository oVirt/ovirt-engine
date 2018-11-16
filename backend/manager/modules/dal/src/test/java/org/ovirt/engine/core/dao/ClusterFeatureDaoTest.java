package org.ovirt.engine.core.dao;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.AdditionalFeature;
import org.ovirt.engine.core.common.businessentities.SupportedAdditionalClusterFeature;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.compat.Guid;

public class ClusterFeatureDaoTest extends BaseDaoTestCase<ClusterFeatureDao> {
    private static final Guid EXISTING_CLUSTER = FixturesTool.CLUSTER_RHEL6_ISCSI;

    private static final Guid NEW_SUPPORTED_FEATURE_1 = new Guid("00000000-0000-0000-0000-000000000004");
    private static final Guid NEW_SUPPORTED_FEATURE_2 = new Guid("00000000-0000-0000-0000-000000000005");

    @Test
    public void testGetClusterFeaturesForVersionAndCategory() {
        verifyFeaturesReturned(dao.getClusterFeaturesForVersionAndCategory("4.3", ApplicationMode.VirtOnly),
                Arrays.asList("TEST_FEATURE_1", "TEST_FEATURE_3", "TEST_FEATURE_4", "TEST_FEATURE_5"));
        verifyFeaturesReturned(dao.getClusterFeaturesForVersionAndCategory("4.3", ApplicationMode.GlusterOnly),
                Arrays.asList("TEST_FEATURE_2", "TEST_FEATURE_3", "TEST_FEATURE_4", "TEST_FEATURE_5"));
        verifyFeaturesReturned(dao.getClusterFeaturesForVersionAndCategory("4.3", ApplicationMode.AllModes),
                Arrays.asList("TEST_FEATURE_1", "TEST_FEATURE_2", "TEST_FEATURE_3", "TEST_FEATURE_4", "TEST_FEATURE_5"));
    }

    private void verifyFeaturesReturned(Set<AdditionalFeature> featuresFromDb, List<String> featuresExpdected) {
        assertNotNull(featuresFromDb, "Failed to retrive additional features for the version and category");
        assertEquals(featuresFromDb.size(), featuresExpdected.size(), "Failed to retrive correct set of features for the given version and category");
        for (AdditionalFeature feature : featuresFromDb) {
            assertThat("Wrong feature returned from DB", featuresExpdected, hasItem(feature.getName()));
        }
    }

    @Test
    public void testGetAllByClusterId() {
        Set<SupportedAdditionalClusterFeature> featuresSupportedInCluster = dao.getAllByClusterId(EXISTING_CLUSTER);
        List<String> expectedFeatures = Arrays.asList("TEST_FEATURE_1", "TEST_FEATURE_2", "TEST_FEATURE_3");
        assertNotNull(featuresSupportedInCluster, "Failed to retrive supported additional features for the cluster");
        assertThat("Failed to retrive correct set of features for the given version and category",
                featuresSupportedInCluster, hasSize(expectedFeatures.size()));
        for (SupportedAdditionalClusterFeature supportedFeatures : featuresSupportedInCluster) {
            assertThat("Wrong feature returned from DB",
                    expectedFeatures, hasItem(supportedFeatures.getFeature().getName()));
        }
    }

    private SupportedAdditionalClusterFeature buildSupportedFeature(Guid featureId, Guid clusterId, boolean enabled) {
        SupportedAdditionalClusterFeature supportedAdditionalClusterFeature = new SupportedAdditionalClusterFeature();
        supportedAdditionalClusterFeature.setClusterId(clusterId);
        supportedAdditionalClusterFeature.setEnabled(enabled);
        supportedAdditionalClusterFeature.setFeature(new AdditionalFeature());
        supportedAdditionalClusterFeature.getFeature().setId(featureId);
        return supportedAdditionalClusterFeature;
    }

    @Test
    public void testAddSupportedClusterFeature() {
        Set<SupportedAdditionalClusterFeature> previouslySupportedFeatures =
                dao.getAllByClusterId(EXISTING_CLUSTER);
        dao.save(buildSupportedFeature(NEW_SUPPORTED_FEATURE_1, EXISTING_CLUSTER, true));
        Set<SupportedAdditionalClusterFeature> supportedFeatures =
                dao.getAllByClusterId(EXISTING_CLUSTER);
        assertThat(supportedFeatures, hasSize(previouslySupportedFeatures.size() + 1));
    }

    @Test
    public void testAddAllSupportedClusterFeature() {
        Set<SupportedAdditionalClusterFeature> previouslySupportedFeatures =
                dao.getAllByClusterId(EXISTING_CLUSTER);
        Set<SupportedAdditionalClusterFeature> newFeatures = new HashSet<>();
        newFeatures.add(buildSupportedFeature(NEW_SUPPORTED_FEATURE_1, EXISTING_CLUSTER, true));
        newFeatures.add(buildSupportedFeature(NEW_SUPPORTED_FEATURE_2, EXISTING_CLUSTER, true));
        dao.saveAll(newFeatures);
        Set<SupportedAdditionalClusterFeature> supportedFeatures =
                dao.getAllByClusterId(EXISTING_CLUSTER);
        assertThat("Failed to add all the supported feature",
                supportedFeatures, hasSize(previouslySupportedFeatures.size() + 2));
    }

    @Test
    public void testUpdateSupportedClusterFeature() {
        Set<SupportedAdditionalClusterFeature> features =
                dao.getAllByClusterId(EXISTING_CLUSTER);

        // Lets stop after testing the first feature.
        features.stream().findFirst().ifPresent(feature -> {
            feature.setEnabled(false);
            dao.update(feature);
        });

        Set<SupportedAdditionalClusterFeature> newFeatureSet =
                dao.getAllByClusterId(EXISTING_CLUSTER);
        assertEquals(new HashSet<>(features), newFeatureSet, "Failed to update the feature set");
    }
}
