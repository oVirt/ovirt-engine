package org.ovirt.engine.core.bll.kubevirt;

import org.ovirt.engine.core.compat.Guid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.kubernetes.client.ApiClient;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.informer.ResourceEventHandler;
import io.kubernetes.client.informer.SharedIndexInformer;
import io.kubernetes.client.informer.SharedInformerFactory;
import io.kubernetes.client.models.V1PersistentVolumeClaim;
import io.kubernetes.client.models.V1PersistentVolumeClaimList;
import io.kubernetes.client.util.CallGeneratorParams;

public class DisksMonitoring {
    private static final Logger log = LoggerFactory.getLogger(DisksMonitoring.class);

    private Guid clusterId;
    private CoreV1Api api;
    private final DiskUpdater diskUpdater;

    public DisksMonitoring(ApiClient client, Guid clusterId, DiskUpdater diskUpdater) {
        this.clusterId = clusterId;
        this.diskUpdater = diskUpdater;
        api = new CoreV1Api(client);
    }

    public void monitor(SharedInformerFactory sharedInformerFactory) {
        SharedIndexInformer<V1PersistentVolumeClaim> disksInformer =
                sharedInformerFactory.sharedIndexInformerFor(
                        (CallGeneratorParams params) -> {
                            return api.listPersistentVolumeClaimForAllNamespacesCall(
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
                        V1PersistentVolumeClaim.class,
                        V1PersistentVolumeClaimList.class);

        disksInformer.addEventHandler(
                new ResourceEventHandler<V1PersistentVolumeClaim>() {
                    @Override
                    public void onAdd(V1PersistentVolumeClaim pvc) {
                        if (diskUpdater.addDisk(pvc, clusterId)) {
                            log.info("pvc {} (namespace {}) added!",
                                    pvc.getMetadata().getName(),
                                    pvc.getMetadata().getNamespace());
                        }
                    }

                    @Override
                    public void onUpdate(V1PersistentVolumeClaim oldPvc, V1PersistentVolumeClaim newPvc) {
                        // nothing interesting to do with that at the moment
                    }

                    @Override
                    public void onDelete(V1PersistentVolumeClaim pvc, boolean deletedFinalStateUnknown) {
                        if (diskUpdater.removeDisk(pvc, clusterId)) {
                            log.info("pvc {} (namespace {}) deleted!",
                                    pvc.getMetadata().getName(),
                                    pvc.getMetadata().getNamespace());
                        }
                    }
                });
    }
}
