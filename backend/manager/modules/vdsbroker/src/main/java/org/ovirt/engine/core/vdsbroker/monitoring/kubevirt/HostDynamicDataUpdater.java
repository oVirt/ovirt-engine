package org.ovirt.engine.core.vdsbroker.monitoring.kubevirt;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.utils.kubevirt.Units;
import org.ovirt.engine.core.vdsbroker.VdsManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.custom.QuantityFormatter;
import io.kubernetes.client.models.V1Node;
import io.kubernetes.client.models.V1NodeCondition;
import io.kubernetes.client.models.V1NodeSystemInfo;

public class HostDynamicDataUpdater {

    private static Logger log = LoggerFactory.getLogger(HostDynamicDataUpdater.class);

    // list of node conditions as specified in https://kubernetes.io/docs/concepts/architecture/nodes/#condition
    private static Set<String> NODE_CONDITIONS =
            Set.of("MemoryPressure", "DiskPressure", "PIDPressure", "NetworkUnavailable");

    private final VdsManager vdsManager;

    public HostDynamicDataUpdater(VdsManager vdsManager) {
        this.vdsManager = vdsManager;
    }

    public void updateHosDynamicData(V1Node node) {
        VdsDynamic dynamicData = vdsManager.getCopyVds().getDynamicData();
        updateVdsDynamic(node, dynamicData);
        vdsManager.updateDynamicData(dynamicData);
    }

    private void updateVdsDynamic(V1Node node, VdsDynamic dynamic) {
        Map<String, String> labels = node.getMetadata().getLabels();
        if ("true".equals(labels.get("kubevirt.io/schedulable"))) {
            dynamic.setStatus(VDSStatus.Up);
        } else {
            dynamic.setStatus(VDSStatus.NonOperational);
            dynamic.setNonOperationalReason(NonOperationalReason.KUBEVIRT_NOT_SCHEDULABLE);
            logUnmetConditions(node);
        }
        dynamic.setCpuThreads(node.getStatus()
                .getCapacity()
                .getOrDefault("cpu", new Quantity("0"))
                .getNumber()
                .intValue());

        Map<String, Quantity> capacity = node.getStatus().getCapacity();
        dynamic.setCpuThreads(capacity.getOrDefault("cpu", new Quantity("0")).getNumber().intValue());
        Quantity memory = capacity.getOrDefault("memory", new Quantity("0"));
        dynamic.setPhysicalMemMb(Units.parse(new QuantityFormatter().format(memory)));
        V1NodeSystemInfo nodeInfo = node.getStatus().getNodeInfo();
        dynamic.setKernelVersion(nodeInfo.getKernelVersion());
        dynamic.setBootUuid(nodeInfo.getBootID());
        dynamic.setHostOs(nodeInfo.getOperatingSystem());
        dynamic.setHardwareUUID(nodeInfo.getSystemUUID());
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
}
