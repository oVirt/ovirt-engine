package org.ovirt.engine.core.vdsbroker.monitoring;
import java.io.IOException;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.dao.provider.ProviderDao;
import org.ovirt.engine.core.vdsbroker.KubevirtUtils;
import org.ovirt.engine.core.vdsbroker.VdsManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.kubernetes.client.ApiException;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.models.V1Node;

public class KubevirtNodesMonitoring implements HostMonitoringInterface {
    private static Logger log = LoggerFactory.getLogger(KubevirtNodesMonitoring.class);

    private ProviderDao providerDao;

    private CoreV1Api api;
    private VdsManager vdsManager;

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

    private VdsDynamic createVdsDynamic(V1Node node) {
        Map<String, String> labels = node.getMetadata().getLabels();
        VdsDynamic dynamic = new VdsDynamic();
        dynamic.setId(vdsManager.getVdsId());
        dynamic.setStatus("true".equals(labels.get("kubevirt.io/schedulable")) ? VDSStatus.Up : VDSStatus.Error);
        dynamic.setCpuThreads(node.getStatus().getCapacity().getOrDefault("cpu", new Quantity("0")).getNumber().intValue());
        dynamic.setPhysicalMemMb(4809);
        dynamic.setKernelVersion(node.getStatus().getNodeInfo().getKernelVersion());
        return dynamic;
    }

    @Override
    public void refresh() {
        String name = vdsManager.getVdsName();
        log.debug("refreshing host {}", name);
        VdsDynamic vdsDynamic = getDynamicInfo(name);
        vdsManager.updateDynamicData(vdsDynamic);
    }

    @Override
    public void afterRefreshTreatment() {
    }
}
