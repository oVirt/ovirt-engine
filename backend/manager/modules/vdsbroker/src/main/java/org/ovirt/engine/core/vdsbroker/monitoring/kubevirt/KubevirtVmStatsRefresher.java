package org.ovirt.engine.core.vdsbroker.monitoring.kubevirt;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.ovirt.engine.core.common.businessentities.GraphicsInfo;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.KubevirtProviderProperties;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmStatistics;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.dao.provider.ProviderDao;
import org.ovirt.engine.core.utils.OsRepositoryImpl;
import org.ovirt.engine.core.vdsbroker.VdsManager;
import org.ovirt.engine.core.vdsbroker.kubevirt.KubevirtAuditUtils;
import org.ovirt.engine.core.vdsbroker.kubevirt.KubevirtUtils;
import org.ovirt.engine.core.vdsbroker.kubevirt.PrometheusClient;
import org.ovirt.engine.core.vdsbroker.kubevirt.PrometheusUrlResolver;
import org.ovirt.engine.core.vdsbroker.monitoring.PollVmStatsRefresher;
import org.ovirt.engine.core.vdsbroker.monitoring.VdsmVm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.kubernetes.client.ApiException;
import io.kubernetes.client.models.V1ObjectMeta;
import io.kubernetes.client.models.V1OwnerReference;
import kubevirt.io.KubevirtApi;
import kubevirt.io.V1Features;
import kubevirt.io.V1VirtualMachineInstance;
import kubevirt.io.V1VirtualMachineInstanceGuestOSInfo;
import kubevirt.io.V1VirtualMachineInstanceList;
import kubevirt.io.V1VirtualMachineInstanceMigration;
import kubevirt.io.V1VirtualMachineInstanceMigrationState;
import kubevirt.io.V1VirtualMachineInstanceStatus;

public class KubevirtVmStatsRefresher extends PollVmStatsRefresher {
    private static Logger log = LoggerFactory.getLogger(KubevirtVmStatsRefresher.class);

    @Inject
    private ProviderDao providerDao;
    @Inject
    private KubevirtMigrationMonitoring migrationMonitoring;
    @Inject
    private VmStaticDao vmStaticDao;
    @Inject
    private AuditLogDirector auditLogDirector;
    @Inject
    private PrometheusUrlResolver prometheusUrlResolver;

    private KubevirtApi api;
    private PrometheusClient prometheusClient;

    private int refreshIteration;

    public KubevirtVmStatsRefresher(VdsManager vdsManager) {
        super(vdsManager);
    }

    @Override
    protected long getRefreshRate() {
        return VMS_REFRESH_RATE;
    }

    private KubevirtApi getKubevirtApi() throws IOException {
        if (api == null) {
            Provider provider = providerDao.get(vdsManager.getClusterId());
            api = KubevirtUtils.getKubevirtApi(provider);
        }
        return api;
    }

    private PrometheusClient getPrometheusClient() {
        if (prometheusClient == null) {
            Provider<KubevirtProviderProperties> provider =
                    (Provider<KubevirtProviderProperties>) providerDao.get(vdsManager.getClusterId());
            prometheusClient = PrometheusClient.create(provider, prometheusUrlResolver);
        }
        return prometheusClient;
    }

    @Override
    protected VDSReturnValue getAllVmStats() {
        VDSReturnValue returnValue = new VDSReturnValue();
        try {
            V1VirtualMachineInstanceList result = listVMIs(vdsManager.getVdsName());
            log.debug("got:\n{}", result);
            List<V1VirtualMachineInstance> vmis = result.getItems();
            List<VdsmVm> vms = vmis.stream()
                    .map(this::toVdsmVm)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            returnValue.setReturnValue(vms);
            returnValue.setSucceeded(true);
        } catch (Exception e) {
            if (ApiException.class.isInstance(e)) {
                KubevirtAuditUtils.auditAuthorizationIssues((ApiException) e, auditLogDirector,
                        providerDao.get(vdsManager.getClusterId()));
            }
            log.error("failed to retrieve kubevirt VMs for node {}: {}",
                    vdsManager.getVdsName(),
                    ExceptionUtils.getRootCauseMessage(e));
            log.debug("Exception", e);
        }
        return returnValue;
    }

    private VdsmVm toVdsmVm(V1VirtualMachineInstance vmi) {
        V1ObjectMeta metadata = vmi.getMetadata();
        List<V1OwnerReference> ownerReferences = metadata.getOwnerReferences();
        if (ownerReferences == null) {
            return null;
        }
        Optional<V1OwnerReference> ownerReference = ownerReferences.stream()
                .filter(ow -> "VirtualMachine".equals(ow.getKind()))
                .findFirst();
        if (!ownerReference.isPresent()) {
            return null;
        }
        VmDynamic vmDynamic = new VmDynamic();
        V1VirtualMachineInstanceStatus status = vmi.getStatus();
        vmDynamic.setIp(status.getInterfaces().get(0).getIpAddress());
        String phase = status.getPhase();
        if (phase == null) {
            return null;
        }
        switch (phase) {
        case "Running":
            vmDynamic.setStatus(VMStatus.Up);
            break;
        case "Scheduling":
        case "Scheduled":
            vmDynamic.setStatus(VMStatus.WaitForLaunch);
            break;
        case "Failed":
        case "Succeeded":
            vmDynamic.setStatus(VMStatus.PoweringDown);
            break;
        default:
            log.error("got invalid status: {}", phase);
            return null;
        }

        updateGuestOsInfo(vmDynamic, vmi);

        V1VirtualMachineInstanceMigration migration = migrationMonitoring.getMigration(
                vdsManager.getClusterId(),
                new KubeResourceId(metadata));
        if (migration != null) {
            V1VirtualMachineInstanceMigrationState migrationState = status.getMigrationState();
            if (migrationState == null ||
                    mayBePreviousMigration(migrationState) ||
                    vdsManager.getVdsName().equals(migrationState.getSourceNode())) {
                vmDynamic.setStatus(VMStatus.MigratingFrom);
            }
            // we don't set the migrating-to-vds field as we rely on kubevirt migration object
            // TODO: this would be the place to update migration progress
        }

        vmDynamic.setRunOnVds(vdsManager.getVdsId());
        vmDynamic.setId(new Guid((String) ownerReference.get().getUid()));
        Double timestamp = (double) System.currentTimeMillis();
        VdsmVm vm = new VdsmVm(timestamp).setVmDynamic(vmDynamic);

        V1Features features = vmi.getSpec().getDomain().getFeatures();
        if (features != null) {
            vmDynamic.setAcpiEnable(features.getAcpi() != null && features.getAcpi().isEnabled());
        }

        if (!Boolean.FALSE.equals(vmi.getSpec().getDomain().getDevices().isAutoattachGraphicsDevice())) {
            GraphicsInfo graphicsInfo = new GraphicsInfo();
            // we have to set some port for the DAO to set VNC
            graphicsInfo.setPort(-1);
            vmDynamic.getGraphicsInfos().put(GraphicsType.VNC, graphicsInfo);
        }

        if (isStatistics()) {
            vm = appendStatistics(vm, vmi);
        }

        return vm;
    }

    private void updateGuestOsInfo(VmDynamic vmDynamic, V1VirtualMachineInstance vmi) {
        V1VirtualMachineInstanceGuestOSInfo guestOSInfo = vmi.getStatus().getGuestOSInfo();

        // guestOSInfo might be empty, therefore we'll skip it if neither OS ID nor name are reported
        if (guestOSInfo != null && (guestOSInfo.getId() != null || guestOSInfo.getName() != null)) {
            if (StringUtils.isNumeric(guestOSInfo.getId())) {
                int osId = Integer.parseInt(guestOSInfo.getId());
                if (OsRepositoryImpl.INSTANCE.getOsIds().contains(osId)) {
                    vmDynamic.setGuestOs(OsRepositoryImpl.INSTANCE.getOsName(osId));
                    vmDynamic.setGuestOsArch(OsRepositoryImpl.INSTANCE.getArchitectureFromOS(osId));
                } else {
                    vmDynamic.setGuestOs(guestOSInfo.getName());
                }
            }

            vmDynamic.setGuestOsKernelVersion(guestOSInfo.getKernelVersion());
            vmDynamic.setGuestOsVersion(guestOSInfo.getVersion());
        }
    }

    private boolean mayBePreviousMigration(V1VirtualMachineInstanceMigrationState migrationState) {
        // previously failed to migrate the vm out of this node
        if (Boolean.TRUE.equals(migrationState.isFailed()) &&
                vdsManager.getVdsName().equals(migrationState.getSourceNode())) {
            return true;
        }
        // previously migrated the vm to this node
        if (Boolean.TRUE.equals(migrationState.isCompleted()) &&
                !Boolean.TRUE.equals(migrationState.isFailed()) &&
                vdsManager.getVdsName().equals(migrationState.getTargetNode())) {
            return true;
        }
        return false;
    }

    private VdsmVm appendStatistics(VdsmVm vm, V1VirtualMachineInstance vmi) {
        VmStatistics statistics = new VmStatistics();
        statistics.setId(vm.getId());
        DateTime creationTimestampDate = vmi.getMetadata().getCreationTimestamp();
        if (creationTimestampDate != null) {
            DateTime now = DateTime.now();
            Seconds seconds = Seconds.secondsBetween(creationTimestampDate, now);
            statistics.setElapsedTime((double) seconds.getSeconds());
        }
        PrometheusClient promClient = getPrometheusClient();
        if (promClient != null) {
            // FIXME: Kubevirt currently have only kubevirt_vmi_vcpu_seconds, which is total CPU time,
            // so we are setting it here only as system time, which is wrong.
            statistics.setCpuSys(
                    promClient.getVmiCpuUsage(vmi.getMetadata().getName(), vmi.getMetadata().getNamespace())
            );
        }

        return vm.setVmStatistics(statistics)
                .setDiskStatistics(Collections.emptyList())
                .setVmJobs(Collections.emptyList());
    }

    private V1VirtualMachineInstanceList listVMIs(String nodeName) throws ApiException, IOException {

        // A selector to restrict the list of returned objects by their fields. Defaults to everything.
        String fieldSelector = null;

        // A selector to restrict the list of returned objects by their labels. Defaults to everything.
        String labelSelector = "kubevirt.io/nodeName=" + nodeName;

        // Timeout for the list/watch call. This limits the duration of the call, regardless of any activity or
        // inactivity.
        Integer timeoutSeconds = 2;

        // Indicator if required a Watch for changes to the described resources and return them as a stream of add,
        // update, and remove notifications.
        Boolean watch = Boolean.FALSE;

        return getKubevirtApi().listVirtualMachineInstanceForAllNamespaces(null,
                null,
                null,
                labelSelector,
                null,
                null,
                timeoutSeconds,
                watch);
    }

    @Override
    public void poll() {
        super.poll();
        updateIteration();
    }

    @Override
    protected List<Pair<VmDynamic, VdsmVm>> matchVms(List<VdsmVm> vdsmVms) {
        Map<Guid, VmDynamic> dbVms = vmDynamicDao.getAllRunningForVds(vdsManager.getVdsId())
                .stream()
                .collect(Collectors.toMap(VmDynamic::getId, vm -> vm));
        StringBuilder logBuilder = log.isDebugEnabled() ? new StringBuilder() : null;
        List<Pair<VmDynamic, VdsmVm>> pairs = vdsmVms.stream()
                .map(vdsmVm -> {
                    if (logBuilder != null) {
                        logBuilder.append(String.format("%s:%s ",
                                vdsmVm.getVmDynamic().getId().toString().substring(0, 8),
                                vdsmVm.getVmDynamic().getStatus()));
                    }

                    VmDynamic dbVm = dbVms.remove(vdsmVm.getId());
                    if (dbVm == null) {
                        // the VM is not yet running in the database
                        dbVm = vmDynamicDao.get(vdsmVm.getId());
                    }
                    if (dbVm == null) {
                        // wait for the VM to be discovered
                        return null;
                    }
                    return new Pair<>(dbVm, vdsmVm);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // the remaining db vms with no corresponding vdsm vm are added accordingly
        dbVms.values().stream().filter(this::shouldNotDisappear).forEach(dbVm -> pairs.add(new Pair<>(dbVm, null)));

        if (logBuilder != null) {
            log.debug(logBuilder.toString());
        }

        return pairs;
    }

    private boolean shouldNotDisappear(VmDynamic vm) {
        VmStatic vmStatic = vmStaticDao.get(vm.getId());
        String namespace = vmStatic.getNamespace();
        String name = vmStatic.getName();
        if (isMigrating(namespace, name)) {
            return false;
        }
        if (isRunningElsewhere(namespace, name)) {
            return false;
        }
        return true;
    }

    private boolean isMigrating(String namespace, String name) {
        V1VirtualMachineInstanceMigration migration = migrationMonitoring.getMigration(
                vdsManager.getClusterId(),
                new KubeResourceId(namespace, name));
        return migration != null;
    }

    private boolean isRunningElsewhere(String namespace, String name) {
        V1VirtualMachineInstanceList vmis;
        try {
            vmis = api.listNamespacedVirtualMachineInstance(
                    namespace,
                    null,
                    "metadata.name=" + name,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null);
        } catch (ApiException e) {
            KubevirtAuditUtils.auditAuthorizationIssues(e, auditLogDirector, providerDao.get(vdsManager.getClusterId()));
            log.error("failed to query VM (name = {}, namespace = {}, cluster = {}): {}",
                    name,
                    namespace,
                    vdsManager.getClusterId(),
                    ExceptionUtils.getRootCauseMessage(e));
            log.debug("Exception", e);
            return false;
        }
        if (vmis.getItems().size() != 1) {
            return false;
        }
        V1VirtualMachineInstance vmi = vmis.getItems().get(0);
        return !vdsManager.getVdsName().equals(vmi.getStatus().getNodeName());
    }

    @Override
    protected boolean isStatistics() {
        return refreshIteration == 0;
    }

    private void updateIteration() {
        refreshIteration = (++refreshIteration) % NUMBER_VMS_REFRESHES_BEFORE_SAVE;
    }

    @Override
    protected Stream<VdsmVm> filterVmsToDevicesMonitoring(List<Pair<VmDynamic, VdsmVm>> polledVms) {
        return Stream.empty();
    }

    @Override
    public void stopMonitoring() {
        super.stopMonitoring();
        try {
            if (prometheusClient != null) {
                prometheusClient.close();
            }
        } catch (IOException e) {
            // ignore
        }
    }
}
