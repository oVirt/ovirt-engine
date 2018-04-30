package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.QuotaClusterLimit;
import org.ovirt.engine.api.model.QuotaClusterLimits;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaCluster;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendQuotaClusterLimitsResourceTest extends AbstractBackendBaseTest {

    private static final int CPU_NUMBER = 40;
    private static final int VIRTUAL_CPU_USAGE = 70;
    protected static final Guid QUOTA_ID = GUIDS[0];
    protected static final Guid DATACENTER_ID = GUIDS[1];
    protected static final Guid CLUSTER_ID_1 = GUIDS[2];
    protected static final Guid CLUSTER_ID_2 = GUIDS[3];

    protected BackendQuotaClusterLimitsResource collection;

    @Test
    public void testListGlobalLimit() {
        Quota quota = getQuota();
        quota.setGlobalQuotaCluster(getClusterGlobalCpuLimit());
        setUpGetEntityExpectations(quota);
        QuotaClusterLimits clusterLimits = collection.list();
        assertClusterLimitsFound(clusterLimits, 1);
        QuotaClusterLimit clusterLimit = clusterLimits.getQuotaClusterLimits().get(0);
        assertEquals(20, clusterLimit.getVcpuLimit().intValue());
    }

    @Test
    public void testListNonGlobalLimit() {
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
        QuotaClusterLimits list = collection.list();
        assertClusterLimitsFound(list, 2);
        for (QuotaClusterLimit clusterLimit: list.getQuotaClusterLimits()) {
            if (clusterLimit.getCluster().getId().equals(CLUSTER_ID_1.toString())) {
                assertEquals(CPU_NUMBER, clusterLimit.getVcpuLimit().longValue());
            }
            if (clusterLimit.getCluster().getId().equals(CLUSTER_ID_2.toString())) {
                assertEquals(VIRTUAL_CPU_USAGE, (int) clusterLimit.getVcpuUsage());
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

    private void setUpGetEntityExpectations(Quota quota) {
        setUpGetEntityExpectations(QueryType.GetQuotaByQuotaId,
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
