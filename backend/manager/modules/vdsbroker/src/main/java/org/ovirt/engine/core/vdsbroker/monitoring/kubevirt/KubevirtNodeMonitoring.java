package org.ovirt.engine.core.vdsbroker.monitoring.kubevirt;

import org.ovirt.engine.core.common.businessentities.KubevirtProviderProperties;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.vdsbroker.VdsManager;
import org.ovirt.engine.core.vdsbroker.kubevirt.KubevirtAuditUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.informer.ResourceEventHandler;
import io.kubernetes.client.informer.SharedIndexInformer;
import io.kubernetes.client.informer.SharedInformerFactory;
import io.kubernetes.client.models.V1Node;
import io.kubernetes.client.models.V1NodeList;
import io.kubernetes.client.util.CallGeneratorParams;
import kubevirt.io.KubevirtApi;
import kubevirt.io.V1VirtualMachineInstance;
import kubevirt.io.V1VirtualMachineInstanceList;

public class KubevirtNodeMonitoring {

    private static Logger log = LoggerFactory.getLogger(KubevirtNodesMonitoring.class);

    private final VdsManager vdsManager;
    private final Provider<KubevirtProviderProperties> provider;
    private final CoreV1Api api;
    private final KubevirtApi kubevirtApi;
    private final HostDynamicDataUpdater hostDynamicDataUpdater;

    public KubevirtNodeMonitoring(VdsManager vdsManager, ApiClient client, Provider<KubevirtProviderProperties> provider) {
        this.vdsManager = vdsManager;
        this.provider = provider;
        api = new CoreV1Api(client);
        kubevirtApi = new KubevirtApi(client);
        hostDynamicDataUpdater = new HostDynamicDataUpdater(vdsManager);
    }

    public void monitorNodeVmsUpdates(SharedInformerFactory sharedInformerFactory) {
        initializeDynamicData();

        SharedIndexInformer<V1VirtualMachineInstance> vmisInformer =
                sharedInformerFactory.sharedIndexInformerFor(
                        (CallGeneratorParams params) -> {
                            return kubevirtApi.listVirtualMachineInstanceForAllNamespacesCall(
                                    null,
                                    null,
                                    null,
                                    getNodeLabelSelector(),
                                    null,
                                    params.resourceVersion,
                                    params.timeoutSeconds,
                                    params.watch,
                                    null,
                                    null);
                        },
                        V1VirtualMachineInstance.class,
                        V1VirtualMachineInstanceList.class);

        vmisInformer.addEventHandler(
                new ResourceEventHandler<V1VirtualMachineInstance>() {
                    @Override
                    public void onAdd(V1VirtualMachineInstance vmi) {
                        // On VMI creation we'll update the vm count for the host
                        updateVmCountForHost();
                    }

                    @Override
                    public void onUpdate(V1VirtualMachineInstance oldVmi, V1VirtualMachineInstance newVmi) {
                        // TODO: Consider evaluating VMInstance status and update VmActive based on the status.
                    }

                    @Override
                    public void onDelete(V1VirtualMachineInstance vmi, boolean deletedFinalStateUnknown) {
                        // On VMI deletion we'll update the vm count for the host
                        updateVmCountForHost();
                    }
                });
    }

    private void initializeDynamicData() {
        try {
            V1Node v1Node = api.readNode(vdsManager.getVdsHostname(), null, null, null);
            hostDynamicDataUpdater.updateHosDynamicData(v1Node);
        } catch (ApiException e) {
            KubevirtAuditUtils.auditAuthorizationIssues(e, vdsManager.getAuditLogDirector(), provider);
            log.error("Failed to read status of node {}: {}", vdsManager.getVdsHostname(), e.getMessage());
            log.debug("Exception", e);
            VdsDynamic vdsDynamic = vdsManager.getCopyVds().getDynamicData();
            vdsDynamic.setId(vdsManager.getVdsId());
            vdsDynamic.setStatus(VDSStatus.NonResponsive);
            vdsManager.updateDynamicData(vdsDynamic);
        }
    }

    public void monitorNodeUpdates(SharedInformerFactory sharedInformerFactory) {
        SharedIndexInformer<V1Node> nodeInformer =
                sharedInformerFactory.sharedIndexInformerFor(
                        (CallGeneratorParams params) -> {
                            return api.listNodeCall(
                                    null,
                                    null,
                                    null,
                                    getNodeLabelSelector(),
                                    null,
                                    params.resourceVersion,
                                    params.timeoutSeconds,
                                    params.watch,
                                    null,
                                    null);
                        },
                        V1Node.class,
                        V1NodeList.class);

        nodeInformer.addEventHandler(
                new ResourceEventHandler<V1Node>() {
                    @Override
                    public void onAdd(V1Node node) {
                        /**
                         * Ignored. this node was already added.
                         */
                    }

                    @Override
                    public void onUpdate(V1Node oldNode, V1Node newNode) {
                        hostDynamicDataUpdater.updateHosDynamicData(newNode);
                    }

                    @Override
                    public void onDelete(V1Node node, boolean deletedFinalStateUnknown) {
                        /**
                         * Ignored. Deletion of node is handled by {@code ClusterMonitoring} via {@code NodesMonitoring}
                         */
                    }
                });
    }

    private String getNodeLabelSelector() {
        return "kubevirt.io/nodeName=" + vdsManager.getVdsHostname();
    }

    private void updateVmCountForHost() {
        // TODO: Acquire lock for monitoring lock during this update
        try {
            V1VirtualMachineInstanceList vmis =
                    kubevirtApi.listVirtualMachineInstanceForAllNamespaces(
                            null,
                            null,
                            null,
                            getNodeLabelSelector(),
                            null,
                            null,
                            null,
                            null);

            VdsDynamic dynamicData = vdsManager.getCopyVds().getDynamicData();
            dynamicData.setVmCount(vmis.getItems().size());
            dynamicData.setVmActive(vmis.getItems().size());
            vdsManager.updateDynamicData(dynamicData);
        } catch (ApiException e) {
            KubevirtAuditUtils.auditAuthorizationIssues(e, vdsManager.getAuditLogDirector(), provider);
            log.error("Failed to update dynamic data for host {}: {}", vdsManager.getVdsName(), e.getMessage());
            log.debug("Exception", e);
        }
    }
}
