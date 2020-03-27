package org.ovirt.engine.core.bll.kubevirt;

import org.ovirt.engine.core.compat.Guid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.kubernetes.client.ApiClient;
import io.kubernetes.client.informer.ResourceEventHandler;
import io.kubernetes.client.informer.SharedIndexInformer;
import io.kubernetes.client.informer.SharedInformerFactory;
import io.kubernetes.client.util.CallGeneratorParams;
import kubevirt.io.KubevirtApi;
import kubevirt.io.V1VirtualMachine;
import kubevirt.io.V1VirtualMachineList;

public class VMsMonitoring {
    private static final Logger log = LoggerFactory.getLogger(VMsMonitoring.class);

    private Guid clusterId;
    private KubevirtApi api;
    private final VmUpdater vmUpdater;

    public VMsMonitoring(ApiClient client, Guid clusterId, VmUpdater vmUpdater) {
        this.clusterId = clusterId;
        this.vmUpdater = vmUpdater;
        api = new KubevirtApi(client);
    }

    public void monitor(SharedInformerFactory sharedInformerFactory) {
        SharedIndexInformer<V1VirtualMachine> vmsInformer =
                sharedInformerFactory.sharedIndexInformerFor(
                        (CallGeneratorParams params) -> {
                            return api.listVirtualMachineForAllNamespacesCall(
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
                        V1VirtualMachine.class,
                        V1VirtualMachineList.class);

        vmsInformer.addEventHandler(
                new ResourceEventHandler<V1VirtualMachine>() {
                    @Override
                    public void onAdd(V1VirtualMachine vm) {
                        if (vmUpdater.addVm(vm, clusterId)) {
                            log.info("vm {} added!", vm.getMetadata().getName());
                        }
                    }

                    @Override
                    public void onUpdate(V1VirtualMachine oldVm, V1VirtualMachine newVm) {
                        if (vmUpdater.updateVM(oldVm, newVm, clusterId)) {
                            log.info("vm {} updated!", newVm.getMetadata().getName());
                        }
                    }

                    @Override
                    public void onDelete(V1VirtualMachine vm, boolean deletedFinalStateUnknown) {
                        // TODO: replace vm identification by namespace and name
                        if (vmUpdater.removeVm(new Guid(vm.getMetadata().getUid()))) {
                            log.info("vm {} removed!", vm.getMetadata().getUid());
                        }
                    }
                });
    }
}
