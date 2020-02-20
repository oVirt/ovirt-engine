package org.ovirt.engine.core.bll.kubevirt;

import org.ovirt.engine.core.compat.Guid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.kubernetes.client.ApiClient;
import io.kubernetes.client.informer.ResourceEventHandler;
import io.kubernetes.client.informer.SharedIndexInformer;
import io.kubernetes.client.informer.SharedInformerFactory;
import io.kubernetes.client.util.CallGeneratorParams;
import kubevirt.io.K8sCniCncfIoV1Api;
import kubevirt.io.V1NetworkAttachmentDefinition;
import kubevirt.io.V1NetworkAttachmentDefinitionList;

public class NetworksMonitoring {
    private static final Logger log = LoggerFactory.getLogger(NetworksMonitoring.class);

    private Guid clusterId;
    private K8sCniCncfIoV1Api cniApi;
    private final NetworkUpdater networkUpdater;

    public NetworksMonitoring(ApiClient client, Guid clusterId, NetworkUpdater networkUpdater) {
        this.clusterId = clusterId;
        this.networkUpdater = networkUpdater;
        cniApi = new K8sCniCncfIoV1Api(client);
    }

    public void monitor(SharedInformerFactory sharedInformerFactory) {
        SharedIndexInformer<V1NetworkAttachmentDefinition> networksInformer =
                sharedInformerFactory.sharedIndexInformerFor(
                        (CallGeneratorParams params) -> {
                            return cniApi.listV1NetworkAttachmentDefinitionForAllNamespacesCall(
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    params.resourceVersion,
                                    params.timeoutSeconds,
                                    params.watch,
                                    null,
                                    null);
                        },
                        V1NetworkAttachmentDefinition.class,
                        V1NetworkAttachmentDefinitionList.class);

        networksInformer.addEventHandler(
                new ResourceEventHandler<>() {
                    @Override
                    public void onAdd(V1NetworkAttachmentDefinition network) {
                        if (networkUpdater.addNetwork(network, clusterId)) {
                            log.info("network {} added!", network.getMetadata().getName());
                        }
                    }

                    @Override
                    public void onUpdate(V1NetworkAttachmentDefinition oldNet, V1NetworkAttachmentDefinition newNet) {
                        log.debug("{} => {} network updated!",
                                oldNet.getMetadata().getName(),
                                newNet.getMetadata().getName());
                        // TODO: consider updating the network
                    }

                    @Override
                    public void onDelete(V1NetworkAttachmentDefinition network, boolean deletedFinalStateUnknown) {
                        if (networkUpdater.removeNetwork(NetworkUpdater.getNetworkName(network), clusterId)) {
                            log.info("network {} removed!", network.getMetadata().getName());
                        }
                    }
                });
    }
}
