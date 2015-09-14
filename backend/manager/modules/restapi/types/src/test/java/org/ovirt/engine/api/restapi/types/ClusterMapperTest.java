package org.ovirt.engine.api.restapi.types;

import org.junit.Test;
import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.ErrorHandling;
import org.ovirt.engine.api.model.MigrateOnError;
import org.ovirt.engine.api.model.RngSource;
import org.ovirt.engine.api.model.SerialNumberPolicy;
import org.ovirt.engine.core.common.businessentities.VDSGroup;

public class ClusterMapperTest extends AbstractInvertibleMappingTest<Cluster, VDSGroup, VDSGroup> {
    public ClusterMapperTest() {
        super(Cluster.class, VDSGroup.class, VDSGroup.class);
    }

    @Override
    protected Cluster postPopulate(Cluster model) {
        ErrorHandling errorHandling = new ErrorHandling();
        errorHandling.setOnError(MappingTestHelper.shuffle(MigrateOnError.class).value());
        model.setErrorHandling(errorHandling);
        model.getSerialNumber().setPolicy(SerialNumberPolicy.CUSTOM.value());
        model.getRequiredRngSources().getRequiredRngSources().clear();
        model.getRequiredRngSources().getRequiredRngSources().add(RngSource.RANDOM.name());
        return model;
    }

    @Override
    protected void verify(Cluster model, Cluster transform) {
        assertNotNull(transform);
        assertEquals(model.getName(), transform.getName());
        assertEquals(model.getId(), transform.getId());
        assertEquals(model.getDescription(), transform.getDescription());
        assertEquals(model.getComment(), transform.getComment());
        assertNotNull(transform.getCpu());
        assertEquals(model.getCpu().getType(), transform.getCpu().getType());
        assertNotNull(transform.getDataCenter());
        assertEquals(model.getDataCenter().getId(), transform.getDataCenter().getId());
        assertNotNull(transform.getSchedulingPolicy());
        assertEquals(model.getSchedulingPolicy().getId(), transform.getSchedulingPolicy().getId());
        assertEquals(model.getErrorHandling().getOnError(), transform.getErrorHandling().getOnError());
        assertNotNull(transform.getMemoryPolicy());
        assertNotNull(transform.getMemoryPolicy().getTransparentHugepages());
        assertEquals(transform.getMemoryPolicy().getTransparentHugepages().isEnabled(), transform.getMemoryPolicy()
                .getTransparentHugepages()
                .isEnabled());
        assertEquals(model.isVirtService(), transform.isVirtService());
        assertEquals(model.isGlusterService(), transform.isGlusterService());
        assertEquals(model.isTunnelMigration(), transform.isTunnelMigration());
        assertEquals(model.isTrustedService(), transform.isTrustedService());
        assertEquals(model.isBallooningEnabled(), transform.isBallooningEnabled());

        assertEquals(model.getKsm().isEnabled(), transform.getKsm().isEnabled());
        assertEquals(model.getKsm().isMergeAcrossNodes(), transform.getKsm()
                .isMergeAcrossNodes());

        assertEquals(model.getDisplay().getProxy(), transform.getDisplay().getProxy());
        assertEquals(model.getSerialNumber().getPolicy(), transform.getSerialNumber().getPolicy());
        assertEquals(model.getSerialNumber().getValue(), transform.getSerialNumber().getValue());
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
}
