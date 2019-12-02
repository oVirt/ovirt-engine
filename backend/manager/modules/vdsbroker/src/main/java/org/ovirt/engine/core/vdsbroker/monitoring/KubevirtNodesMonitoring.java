package org.ovirt.engine.core.vdsbroker.monitoring;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.businessentities.HugePage;
import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.common.businessentities.VdsStatistics;
import org.ovirt.engine.core.dao.provider.ProviderDao;
import org.ovirt.engine.core.vdsbroker.KubevirtUtils;
import org.ovirt.engine.core.vdsbroker.VdsManager;
import org.ovirt.engine.core.vdsbroker.vdsbroker.PrometheusClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.kubernetes.client.ApiException;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.models.V1Node;
import io.kubernetes.client.models.V1NodeCondition;

public class KubevirtNodesMonitoring implements HostMonitoringInterface {
    private static Logger log = LoggerFactory.getLogger(KubevirtNodesMonitoring.class);

    // list of node conditions as specified in https://kubernetes.io/docs/concepts/architecture/nodes/#condition
    private static Set<String> NODE_CONDITIONS =
            Set.of("MemoryPressure", "DiskPressure", "PIDPressure", "NetworkUnavailable");

    private ProviderDao providerDao;

    private CoreV1Api api;
    private VdsManager vdsManager;
    private PrometheusClient prometheusClient;

    public KubevirtNodesMonitoring(VdsManager vdsManager, ProviderDao providerDao) {
        this.vdsManager = vdsManager;
        this.providerDao = providerDao;
    }

    private VdsDynamic getDynamicInfo(String name) {
        V1Node node;
        try {
            node = readNodeStatus(name);
        } catch (ApiException | IOException e) {
            log.error("failed to read status of node {}", name);
            VdsDynamic vdsDynamic = new VdsDynamic();
            vdsDynamic.setId(vdsManager.getVdsId());
            vdsDynamic.setStatus(VDSStatus.NonResponsive);
            return vdsDynamic;
        }
        return createVdsDynamic(node);
    }

    private V1Node readNodeStatus(String name) throws ApiException, IOException {
        return getApi().readNodeStatus(name, null);
    }

    private CoreV1Api getApi() throws IOException {
        if (api == null) {
            Provider<?> provider = providerDao.get(vdsManager.getClusterId());
            api = KubevirtUtils.getCoreApi(provider);
        }
        return api;
    }

    private PrometheusClient getPrometheusClient() {
        if (prometheusClient == null) {
            Provider<?> provider = providerDao.get(vdsManager.getClusterId());
            prometheusClient = KubevirtUtils.create(provider);
        }
        return prometheusClient;
    }

    private VdsDynamic createVdsDynamic(V1Node node) {
        Map<String, String> labels = node.getMetadata().getLabels();
        VdsDynamic dynamic = new VdsDynamic();
        dynamic.setId(vdsManager.getVdsId());
        if ("true".equals(labels.get("kubevirt.io/schedulable"))) {
            dynamic.setStatus(VDSStatus.Up);
        } else {
            dynamic.setStatus(VDSStatus.NonOperational);
            dynamic.setNonOperationalReason(NonOperationalReason.KUBEVIRT_NOT_SCHEDULABLE);
            logUnmetConditions(node);
        }
        dynamic.setCpuThreads(node.getStatus().getCapacity().getOrDefault("cpu", new Quantity("0")).getNumber().intValue());
        dynamic.setPhysicalMemMb(4809);
        dynamic.setKernelVersion(node.getStatus().getNodeInfo().getKernelVersion());
        return dynamic;
    }

    private void logUnmetConditions(V1Node node) {
        // check nodes conditions for additional information
        List<V1NodeCondition> conditions = node.getStatus().getConditions();
        if (conditions != null) {
            String unmetConditions = conditions.stream()
                    .filter(c -> NODE_CONDITIONS.contains(c.getType()))
                    .filter(c -> Boolean.TRUE.toString().equals(c.getStatus().toLowerCase()))
                    .map(V1NodeCondition::getMessage)
                    .collect(Collectors.joining(", "));
            if (!unmetConditions.isEmpty()) {
                log.warn("KubeVirt node {} reports the following unmet conditions: {}",
                        node.getMetadata().getName(),
                        unmetConditions);
            }
        }
    }

    @Override
    public void refresh() {
        String name = vdsManager.getVdsName();
        log.debug("refreshing host {}", name);
        VdsDynamic vdsDynamic = getDynamicInfo(name);
        vdsManager.updateDynamicData(vdsDynamic);

        // Update statistics:
        PrometheusClient promClient = getPrometheusClient();
        if (vdsManager.isTimeToRefreshStatistics() && promClient != null) {
            VdsStatistics stat = new VdsStatistics();
            stat.setId(vdsManager.getVdsId());

            // memory
            stat.setUsageMemPercent(promClient.getNodeMemUsage(name));
            stat.setMemAvailable(promClient.getNodeMemAvailable(name));
            stat.setMemFree(promClient.getNodeMemFree(name));
            stat.setMemShared(promClient.getNodeMemShared(name));

            // swap
            stat.setSwapTotal(promClient.getNodeSwapTotalMb(name));
            stat.setSwapFree(promClient.getNodeSwapFreeMb(name));

            // huge pages
            stat.setAnonymousHugePages(promClient.getNodeAnonHugePages(name));
            stat.setHugePages(
                promClient.getNodeHugePages(name).stream()
                    .map(k -> new HugePage(k.getFirst(), k.getSecond()))
                    .collect(Collectors.toList())
            );

            // boot time
            stat.setBootTime(promClient.getNodeBootTime(name));

            // Cpu
            stat.setCpuIdle(promClient.getNodeCpuIdle(name));
            stat.setCpuSys(promClient.getNodeCpuSystem(name));
            stat.setCpuUser(promClient.getNodeCpuUser(name));
            stat.setCpuLoad(promClient.getNodeCpuLoad(name));
            stat.setUsageCpuPercent(promClient.getNodeCpuUsage(name));

            // TODO: KSM

            vdsManager.updateStatisticsData(stat);
        }
        vdsManager.afterRefreshTreatment(true);
    }

    @Override
    public void afterRefreshTreatment() {
    }

    /**
     * The lock acquired for kubevirt node refresh should be released at the end of the refresh execution.
     *
     * @return true for indicating the host monitoring should release the monitoring lock of the node.
     */
    @Override
    public boolean shouldReleaseMonitoringLockAfterRefresh() {
        return true;
    }
}
