package org.ovirt.engine.api.restapi.types;

import org.junit.Test;
import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.ErrorHandling;
import org.ovirt.engine.api.model.MigrateOnError;
import org.ovirt.engine.api.model.SchedulingPolicy;
import org.ovirt.engine.api.model.SchedulingPolicyType;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VdsSelectionAlgorithm;
import org.ovirt.engine.core.compat.Guid;

public class ClusterMapperTest extends AbstractInvertibleMappingTest<Cluster, VDSGroup, VDSGroup> {

    public ClusterMapperTest() {
        super(Cluster.class, VDSGroup.class, VDSGroup.class);
    }

    @Override
    protected Cluster postPopulate(Cluster model) {
        model.getSchedulingPolicy().setPolicy(MappingTestHelper.shuffle(SchedulingPolicyType.class).value());
        ErrorHandling errorHandling = new ErrorHandling();
        errorHandling.setOnError(MappingTestHelper.shuffle(MigrateOnError.class).value());
        model.setErrorHandling(errorHandling);
        SchedulingPolicy policy = new SchedulingPolicy();
        policy.setPolicy("power_saving");
        model.setSchedulingPolicy(policy);
        return model;
    }

    @Override
    protected void verify(Cluster model, Cluster transform) {
        assertNotNull(transform);
        assertEquals(model.getName(), transform.getName());
        assertEquals(model.getId(), transform.getId());
        assertEquals(model.getDescription(), transform.getDescription());
        assertNotNull(transform.getCpu());
        assertEquals(model.getCpu().getId(), transform.getCpu().getId());
        assertNotNull(transform.getDataCenter());
        assertEquals(model.getDataCenter().getId(), transform.getDataCenter().getId());
        assertNotNull(transform.getSchedulingPolicy());
        assertEquals(model.getSchedulingPolicy().getPolicy(), transform.getSchedulingPolicy().getPolicy());
        assertEquals(model.getErrorHandling().getOnError(), transform.getErrorHandling().getOnError());
        assertNotNull(transform.getMemoryPolicy());
        assertNotNull(transform.getMemoryPolicy().getTransparentHugepages());
        assertEquals(transform.getMemoryPolicy().getTransparentHugepages().isEnabled(), transform.getMemoryPolicy()
                .getTransparentHugepages()
                .isEnabled());
        assertEquals(model.isVirtService(), transform.isVirtService());
        assertEquals(model.isGlusterService(), transform.isGlusterService());
        assertEquals(model.isTunnelMigration(), transform.isTunnelMigration());
    }

    private Mapper<Cluster, VDSGroup> getMapper() {
        MappingLocator mappingLocator = new MappingLocator();
        mappingLocator.populate();
        return mappingLocator.getMapper(Cluster.class, VDSGroup.class);
    }

    @Test
    public void thpDefaultFalseTest() {
        Cluster cluster = new Cluster();
        cluster.setVersion(new org.ovirt.engine.api.model.Version() {
            {
                setMajor(2);
                setMinor(0);
            }
        });
        VDSGroup transform = getMapper().map(cluster, null);
        assertEquals(transform.getTransparentHugepages(), false);
    }

    @Test
    public void thpDefaultTrueTest() {
        Cluster cluster = new Cluster();
        cluster.setVersion(new org.ovirt.engine.api.model.Version() {
            {
                setMajor(3);
                setMinor(0);
            }
        });
        VDSGroup transform = getMapper().map(cluster, null);
        assertEquals(transform.getTransparentHugepages(), true);
    }

    @Test
    public void testSchedulingPolicyNone() {
        Cluster cluster = new Cluster();
        SchedulingPolicy policy = new SchedulingPolicy();
        policy.setPolicy("None");
        cluster.setSchedulingPolicy(policy);
        VDSGroup transform = getMapper().map(cluster, null);
        assertNotNull(transform.getselection_algorithm());
        assertEquals(transform.getselection_algorithm(), VdsSelectionAlgorithm.None);
        transform.setId(Guid.Empty);
        cluster = ClusterMapper.map(transform, cluster);
        assertNull(cluster.getSchedulingPolicy().getPolicy());
    }
}
