package org.ovirt.engine.api.restapi.resource;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.api.model.QuotaClusterLimit;
import org.ovirt.engine.api.model.QuotaClusterLimits;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaCluster;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendQuotaClusterLimitsResourceTest extends AbstractBackendBaseTest {

    private static final int CPU_NUMBER = 40;
    private static final int VIRTUAL_CPU_USAGE = 70;
    protected static final Guid QUOTA_ID = GUIDS[0];
    protected static final Guid DATACENTER_ID = GUIDS[1];
    protected static final Guid CLUSTER_ID_1 = GUIDS[2];
    protected static final Guid CLUSTER_ID_2 = GUIDS[3];

    protected BackendQuotaClusterLimitsResource collection;

    @Test
    public void testListGlobalLimit() throws Exception {
        Quota quota = getQuota();
        quota.setGlobalQuotaCluster(getClusterGlobalCpuLimit());
        setUpGetEntityExpectations(quota);
        control.replay();
        QuotaClusterLimits clusterLimits = collection.list();
        assertClusterLimitsFound(clusterLimits, 1);
        QuotaClusterLimit clusterLimit = clusterLimits.getQuotaClusterLimits().get(0);
        assertEquals(20, clusterLimit.getVcpuLimit().intValue());
    }

    @Test
    public void testListNonGlobalLimit() throws Exception {
        Quota quota = getQuota();
        List<QuotaCluster> clusterLimits = new LinkedList<>();
        QuotaCluster clusterLimit1 = new QuotaCluster();
        clusterLimit1.setVirtualCpu(CPU_NUMBER);
        clusterLimit1.setClusterId(CLUSTER_ID_1);
        QuotaCluster clusterLimit2 = new QuotaCluster();
        clusterLimit2.setVirtualCpuUsage(VIRTUAL_CPU_USAGE);
        clusterLimit2.setClusterId(CLUSTER_ID_2);
        clusterLimits.add(clusterLimit1);
        clusterLimits.add(clusterLimit2);
        quota.setQuotaClusters(clusterLimits);
        setUpGetEntityExpectations(quota);
        control.replay();
        QuotaClusterLimits list = collection.list();
        assertClusterLimitsFound(list, 2);
        for (QuotaClusterLimit clusterLimit: list.getQuotaClusterLimits()) {
            if (clusterLimit.getCluster().getId().equals(CLUSTER_ID_1.toString())) {
                assertEquals(CPU_NUMBER, clusterLimit.getVcpuLimit().longValue());
            }
            if (clusterLimit.getCluster().getId().equals(CLUSTER_ID_2.toString())) {
                assertTrue(clusterLimit.getVcpuUsage() == VIRTUAL_CPU_USAGE);
            }
        }

    }

    private void assertClusterLimitsFound(QuotaClusterLimits clusterLimits, int resultsNum) {
        assertNotNull(clusterLimits);
        assertNotNull(clusterLimits.getQuotaClusterLimits());
        assertEquals(clusterLimits.getQuotaClusterLimits().size(), resultsNum);
    }

    private QuotaCluster getClusterGlobalCpuLimit() {
        QuotaCluster clusterGlobalLimit = new QuotaCluster();
        clusterGlobalLimit.setQuotaId(GUIDS[0]);
        clusterGlobalLimit.setClusterId(CLUSTER_ID_1);
        clusterGlobalLimit.setVirtualCpu(20);
        return clusterGlobalLimit;
    }

    protected org.ovirt.engine.core.common.businessentities.Quota getQuota() {
        org.ovirt.engine.core.common.businessentities.Quota quota =
                new org.ovirt.engine.core.common.businessentities.Quota();
        quota.setId(QUOTA_ID);
        quota.setStoragePoolId(DATACENTER_ID);
        return quota;
    }

    private void setUpGetEntityExpectations(Quota quota) throws Exception {
        setUpGetEntityExpectations(VdcQueryType.GetQuotaByQuotaId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { QUOTA_ID },
                quota);
    }

    @Override
    protected void init() {
        collection = new BackendQuotaClusterLimitsResource(QUOTA_ID);
        collection.setMappingLocator(mapperLocator);
        initBackendResource(collection);
    }
}
