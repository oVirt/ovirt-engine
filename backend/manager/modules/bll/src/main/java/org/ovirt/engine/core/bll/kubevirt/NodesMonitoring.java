package org.ovirt.engine.core.bll.kubevirt;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.kubernetes.client.ApiClient;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.informer.ResourceEventHandler;
import io.kubernetes.client.informer.SharedIndexInformer;
import io.kubernetes.client.informer.SharedInformerFactory;
import io.kubernetes.client.models.V1Node;
import io.kubernetes.client.models.V1NodeList;
import io.kubernetes.client.util.CallGeneratorParams;

public class NodesMonitoring {
    private static final Logger log = LoggerFactory.getLogger(NodesMonitoring.class);

    private Guid clusterId;
    private CoreV1Api api;
    private VdsStaticDao vdsStaticDao;
    private final HostUpdater hostUpdater;

    public NodesMonitoring(ApiClient client,
            Guid clusterId,
            VdsStaticDao vdsStaticDao,
            HostUpdater hostUpdater) {
        this.clusterId = clusterId;
        this.vdsStaticDao = vdsStaticDao;
        this.hostUpdater = hostUpdater;
        api = new CoreV1Api(client);
    }

    public void monitor(SharedInformerFactory sharedInformerFactory) {
        SharedIndexInformer<V1Node> nodeInformer =
                sharedInformerFactory.sharedIndexInformerFor(
                        (CallGeneratorParams params) -> {
                            return api.listNodeCall(
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
                        V1Node.class,
                        V1NodeList.class);

        nodeInformer.addEventHandler(
                new ResourceEventHandler<V1Node>() {
                    @Override
                    public void onAdd(V1Node node) {
                        if (hostUpdater.addHost(node, clusterId)) {
                            log.info("node {} added!", node.getMetadata().getName());
                        }
                    }

                    @Override
                    public void onUpdate(V1Node oldNode, V1Node newNode) {
                        /**
                         * Skip host updates. This functionality is achieved by {@code KubevirtNodesMonitoring} and by
                         * {@code KubevirtHostConnectionRefresher}
                         */
                    }

                    @Override
                    public void onDelete(V1Node node, boolean deletedFinalStateUnknown) {
                        if (hostUpdater.removeHost(node, clusterId)) {
                            log.info("node {} deleted!", node.getMetadata().getName());
                        }
                    }
                });
    }
}
