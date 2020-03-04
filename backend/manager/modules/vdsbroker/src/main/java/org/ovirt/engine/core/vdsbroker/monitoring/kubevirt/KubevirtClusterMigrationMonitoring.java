package org.ovirt.engine.core.vdsbroker.monitoring.kubevirt;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.KubevirtProviderProperties;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.vdsbroker.kubevirt.KubevirtAuditUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.informer.ResourceEventHandler;
import io.kubernetes.client.informer.SharedIndexInformer;
import io.kubernetes.client.informer.SharedInformerFactory;
import io.kubernetes.client.util.CallGeneratorParams;
import kubevirt.io.KubevirtApi;
import kubevirt.io.V1VirtualMachineInstance;
import kubevirt.io.V1VirtualMachineInstanceList;
import kubevirt.io.V1VirtualMachineInstanceMigration;
import kubevirt.io.V1VirtualMachineInstanceMigrationList;

public class KubevirtClusterMigrationMonitoring {
    private static final Logger log = LoggerFactory.getLogger(KubevirtClusterMigrationMonitoring.class);

    private KubevirtApi api;
    private AuditLogDirector auditLogDirector;
    private Provider<KubevirtProviderProperties> provider;

    private Map<KubeResourceId, V1VirtualMachineInstanceMigration> vmToMigration;

    public KubevirtClusterMigrationMonitoring(ApiClient client, AuditLogDirector auditLogDirector,
            Provider<KubevirtProviderProperties> provider) {
        this.auditLogDirector = auditLogDirector;
        this.provider = provider;
        vmToMigration = new ConcurrentHashMap<>();
        api = new KubevirtApi(client);
    }

    public V1VirtualMachineInstanceMigration getMigrationForVm(KubeResourceId vmId) {
        return vmToMigration.get(vmId);
    }

    public void monitor(SharedInformerFactory sharedInformerFactory) {
        SharedIndexInformer<V1VirtualMachineInstanceMigration> migrationInformer =
                sharedInformerFactory.sharedIndexInformerFor(
                        (CallGeneratorParams params) -> {
                            return api.listVirtualMachineInstanceMigrationForAllNamespacesCall(
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
                        V1VirtualMachineInstanceMigration.class,
                        V1VirtualMachineInstanceMigrationList.class);

        migrationInformer.addEventHandler(
                new ResourceEventHandler<V1VirtualMachineInstanceMigration>() {
                    @Override
                    public synchronized void onAdd(V1VirtualMachineInstanceMigration vmim) {
                        if (vmim.getStatus() == null || vmim.getStatus().getPhase() == null) {
                            vmToMigration.put(
                                    new KubeResourceId(vmim.getMetadata().getNamespace(), vmim.getSpec().getVmiName()),
                                    vmim);
                            log.info("vmim {} (namespace {}) added!",
                                    vmim.getMetadata().getName(),
                                    vmim.getMetadata().getNamespace());
                            return;
                        }
                        switch (vmim.getStatus().getPhase()) {
                        case "Succeeded":
                        case "Failed":
                            break;
                        default:
                            vmToMigration.put(
                                    new KubeResourceId(vmim.getMetadata().getNamespace(), vmim.getSpec().getVmiName()),
                                    vmim);
                            log.info("vmim {} (namespace {}) added!",
                                    vmim.getMetadata().getName(),
                                    vmim.getMetadata().getNamespace());
                        }
                    }

                    @Override
                    public synchronized void onUpdate(V1VirtualMachineInstanceMigration oldVmim,
                            V1VirtualMachineInstanceMigration newVmim) {
                        KubeResourceId vmId = new KubeResourceId(newVmim.getMetadata().getNamespace(),
                                newVmim.getSpec().getVmiName());
                        V1VirtualMachineInstanceMigration migration = vmToMigration.get(vmId);
                        if (migration == null) {
                            return;
                        }
                        if (!migration.getMetadata().getName().equals(newVmim.getMetadata().getName())) {
                            return;
                        }
                        if (oldVmim.getStatus() != null
                                && !Objects.equals(oldVmim.getStatus().getPhase(), newVmim.getStatus().getPhase())) {
                            switch (newVmim.getStatus().getPhase()) {
                            case "Succeeded":
                                vmToMigration.remove(vmId);
                                auditMigrationSucceeded(newVmim);
                                break;
                            case "Failed":
                                vmToMigration.remove(vmId);
                                auditMigrationFailed(newVmim);
                                break;
                            default:
                            }
                        }
                    }

                    @Override
                    public synchronized void onDelete(V1VirtualMachineInstanceMigration vmim,
                            boolean deletedFinalStateUnknown) {
                        KubeResourceId vmId =
                                new KubeResourceId(vmim.getMetadata().getNamespace(), vmim.getSpec().getVmiName());
                        V1VirtualMachineInstanceMigration migration = vmToMigration.get(vmId);
                        if (migration == null) {
                            return;
                        }
                        if (migration.getMetadata().getName().equals(vmim.getMetadata().getName())) {
                            vmToMigration.remove(vmId);
                        }
                    }
                });
    }

    private void auditMigrationSucceeded(V1VirtualMachineInstanceMigration vmim) {
        final AuditLogable event = createMigrationEvent(vmim, true);
        auditLogDirector.log(event, AuditLogType.VM_MIGRATION_DONE);
    }

    private void auditMigrationFailed(V1VirtualMachineInstanceMigration vmim) {
        final AuditLogable event = createMigrationEvent(vmim, false);
        event.addCustomValue("DueToMigrationError", " ");
        auditLogDirector.log(event, AuditLogType.VM_MIGRATION_FAILED);
    }

    private AuditLogable createMigrationEvent(V1VirtualMachineInstanceMigration vmim, boolean includeDestination) {
        final AuditLogable event = new AuditLogableImpl();

        // TODO: set the VM to correlate the audit message with
        // event.setVmId(dbVm.getId());

        event.setVmName(vmim.getSpec().getVmiName());
        V1VirtualMachineInstance vmi = getVmi(vmim);
        if (vmi != null && vmi.getStatus().getMigrationState() != null) {
            event.setVdsName(vmi.getStatus().getMigrationState().getSourceNode());
            if (includeDestination) {
                event.addCustomValue("DestinationVdsName", vmi.getStatus().getMigrationState().getTargetNode());
            }
        }
        return event;
    }

    private V1VirtualMachineInstance getVmi(V1VirtualMachineInstanceMigration vmim) {
        V1VirtualMachineInstanceList vmis;
        try {
            vmis = api.listNamespacedVirtualMachineInstance(
                    vmim.getMetadata().getNamespace(),
                    null,
                    "metadata.name=" + vmim.getSpec().getVmiName(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null);
        } catch (ApiException e) {
            KubevirtAuditUtils.auditAuthorizationIssues(e, auditLogDirector, provider);
            log.error("failed to query VM {} (namespace {}): {}",
                    vmim.getSpec().getVmiName(),
                    vmim.getMetadata().getNamespace(),
                    e.getMessage());
            return null;
        }
        if (vmis.getItems().size() != 1) {
            return null;
        }
        return vmis.getItems().get(0);
    }

}
