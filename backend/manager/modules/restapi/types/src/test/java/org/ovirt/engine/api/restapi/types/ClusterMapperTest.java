package org.ovirt.engine.api.restapi.types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.api.model.ErrorHandling;
import org.ovirt.engine.api.model.MigrateOnError;
import org.ovirt.engine.api.model.RngSource;
import org.ovirt.engine.api.model.SerialNumberPolicy;
import org.ovirt.engine.core.common.businessentities.Cluster;

public class ClusterMapperTest extends AbstractInvertibleMappingTest<org.ovirt.engine.api.model.Cluster, Cluster, Cluster> {
    public ClusterMapperTest() {
        super(org.ovirt.engine.api.model.Cluster.class, Cluster.class, Cluster.class);
    }

    @Override
    protected org.ovirt.engine.api.model.Cluster postPopulate(org.ovirt.engine.api.model.Cluster model) {
        ErrorHandling errorHandling = new ErrorHandling();
        errorHandling.setOnError(MigrateOnError.DO_NOT_MIGRATE);
        model.setErrorHandling(errorHandling);
        model.getSerialNumber().setPolicy(SerialNumberPolicy.CUSTOM);
        model.getRequiredRngSources().getRequiredRngSources().clear();
        model.getRequiredRngSources().getRequiredRngSources().add(RngSource.RANDOM);
        return model;
    }

    @Override
    protected void verify(org.ovirt.engine.api.model.Cluster model, org.ovirt.engine.api.model.Cluster transform) {
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
        assertEquals(model.getMacPool().getId(), transform.getMacPool().getId());
    }

    private Mapper<org.ovirt.engine.api.model.Cluster, Cluster> getMapper() {
        MappingLocator mappingLocator = new MappingLocator();
        mappingLocator.populate();
        return mappingLocator.getMapper(org.ovirt.engine.api.model.Cluster.class, Cluster.class);
    }

    @Test
    public void thpDefaultFalseTest() {
        org.ovirt.engine.api.model.Cluster cluster = new org.ovirt.engine.api.model.Cluster();
        cluster.setVersion(new org.ovirt.engine.api.model.Version() {
            {
                setMajor(2);
                setMinor(0);
            }
        });
        Cluster transform = getMapper().map(cluster, null);
        assertFalse(transform.getTransparentHugepages());
    }

    @Test
    public void thpDefaultTrueTest() {
        org.ovirt.engine.api.model.Cluster cluster = new org.ovirt.engine.api.model.Cluster();
        cluster.setVersion(new org.ovirt.engine.api.model.Version() {
            {
                setMajor(3);
                setMinor(0);
            }
        });
        Cluster transform = getMapper().map(cluster, null);
        assertTrue(transform.getTransparentHugepages());
    }
}
