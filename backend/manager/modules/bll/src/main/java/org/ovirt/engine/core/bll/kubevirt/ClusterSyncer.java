package org.ovirt.engine.core.bll.kubevirt;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.dao.VmTemplateDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.models.V1Node;
import io.kubernetes.client.models.V1NodeList;
import io.kubernetes.client.models.V1PersistentVolumeClaim;
import io.kubernetes.client.models.V1PersistentVolumeClaimList;
import kubevirt.io.KubevirtApi;
import kubevirt.io.V1VirtualMachineList;

/**
 * ClusterSyncer syncs the status of ovirt-engine with the current state of kubevirt cluster
 */
public class ClusterSyncer {
    private static final Logger log = LoggerFactory.getLogger(ClusterSyncer.class);

    @Inject
    private VdsStaticDao vdsStaticDao;

    @Inject
    private VmStaticDao vmStaticDao;

    @Inject
    private HostUpdater hostUpdater;

    @Inject
    private VmUpdater vmUpdater;

    @Inject
    private DiskUpdater diskUpdater;

    @Inject
    private DiskImageDao diskImageDao;

    @Inject
    private VmTemplateDao templateDao;

    @Inject
    private TemplateUpdater templateUpdater;

    /**
     * Sync act by the following sequence to maintain data integrity:
     * <li>Remove VMs that exist on engine and were not reported by kubevirt</li>
     * <li>Remove Hosts that exist on engine and were not reported by kubevirt</li>
     * <li>Add Hosts that were reported by kubevirt and don't exist on engine</li>
     * <li>Add VMs that were reported by kubevirt and don't exist on engine</li>
     *
     * @param client
     *            The client object of kubevirt provider
     * @param clusterId
     *            The identifier of kubevirt cluster
     */
    public void sync(ApiClient client, Guid clusterId) {
        V1VirtualMachineList kubevirtVms = getKubevirtVms(client, clusterId);
        if (kubevirtVms == null) {
            return;
        }

        V1NodeList kubevirtHosts = getKubevirtNodes(client, clusterId);
        if (kubevirtHosts == null) {
            return;
        }

        V1PersistentVolumeClaimList kubevirtDisks = getKubevirtDisks(client, clusterId);
        if (kubevirtDisks == null) {
            return;
        }

        List<DiskImage> engineDisks = diskImageDao.getAllForStorageDomain(clusterId);
        removeUnreportedDisks(kubevirtDisks, engineDisks);

        List<VmStatic> engineVms = vmStaticDao.getAllByCluster(clusterId);
        removeUnreportedVms(kubevirtVms, engineVms);

        List<VdsStatic> engineHosts = vdsStaticDao.getAllForCluster(clusterId);
        removeUnreportedHosts(kubevirtHosts, engineHosts);

        addMissingDisks(clusterId, kubevirtDisks, engineDisks);
        addMissingHosts(clusterId, kubevirtHosts, engineHosts);
        addMissingVms(clusterId, kubevirtVms, engineVms);

        // we currently don't correlate the kubevirt templates with VMs so we can
        // simply remove them all and then get them back so we don't follow the
        // pattern for hosts, VMs and disks above.
        List<VmTemplate> templates = templateDao.getAllForCluster(clusterId);
        templates.forEach(templateUpdater::removeFromDB);
    }

    private void addMissingVms(Guid clusterId, V1VirtualMachineList kubevirtVms, List<VmStatic> engineVms) {
        List<Pair> engineVmsNamespace =
                engineVms.stream().map(v -> new Pair(v.getName(), v.getNamespace())).collect(Collectors.toList());
        kubevirtVms.getItems()
                .stream()
                .filter(v -> !engineVmsNamespace.contains(new Pair(v.getMetadata().getName(),
                        v.getMetadata().getNamespace())))
                .forEach(v -> vmUpdater.addVm(v, clusterId));
    }

    private void addMissingHosts(Guid clusterId, V1NodeList kubevirtHosts, List<VdsStatic> engineHosts) {
        // add hosts that were reported from kubevirt but don't exist on ovirt-engine
        List<String> engineHostNames = engineHosts.stream().map(VdsStatic::getHostName).collect(Collectors.toList());
        List<V1Node> hostsToAdd =
                kubevirtHosts.getItems()
                        .stream()
                        .filter(h -> !engineHostNames.contains(hostUpdater.getHostName(h)))
                        .collect(Collectors.toList());

        hostsToAdd.forEach(h -> hostUpdater.addHost(h, clusterId));
    }

    private void removeUnreportedHosts(V1NodeList kubevirtHosts, List<VdsStatic> engineHosts) {
        // remove hosts that exist on engine but weren't reported from kubevirt
        Set<String> kubevirtHostNames =
                kubevirtHosts.getItems().stream().map(hostUpdater::getHostName).collect(Collectors.toSet());

        List<Guid> hostsToDelete = engineHosts.stream()
                .filter(h -> !kubevirtHostNames.contains(h.getHostName()))
                .map(VdsStatic::getId)
                .collect(Collectors.toList());
        hostsToDelete.forEach(hostUpdater::removeHost);
    }

    private V1NodeList getKubevirtNodes(ApiClient client, Guid clusterId) {
        CoreV1Api api = new CoreV1Api(client);
        try {
            return api.listNode(Boolean.FALSE.toString(), null, null, null, null, null, null, Boolean.FALSE);
        } catch (ApiException e) {
            log.error("Failed to communicate with kubevirt cluster " + clusterId + " due to: " + e.getMessage());
            log.debug("Exception", e);
            return null;
        }
    }

    private void removeUnreportedVms(V1VirtualMachineList kubevirtVms, List<VmStatic> engineVms) {
        Set<Pair> kubevirtVmsNamespaced = kubevirtVms.getItems()
                .stream()
                .map(v -> new Pair(v.getMetadata().getName(), v.getMetadata().getNamespace()))
                .collect(Collectors.toSet());

        // remove vms that exist on engine but weren't reported by kubevirt
        engineVms.stream()
                .filter(v -> !kubevirtVmsNamespaced.contains(new Pair(v.getName(), v.getNamespace())))
                .map(VmBase::getId)
                .forEach(vmUpdater::removeVm);
    }

    private V1VirtualMachineList getKubevirtVms(ApiClient client, Guid clusterId) {
        KubevirtApi kubevirtApi = new KubevirtApi(client);
        try {
            return kubevirtApi.listVirtualMachineForAllNamespaces(null, null, null, null, null, null, null, null);
        } catch (ApiException e) {
            log.error("Failed to communicate with kubevirt cluster " + clusterId + " due to: " + e.getMessage());
            log.debug("Exception", e);
            return null;
        }
    }

    private V1PersistentVolumeClaimList getKubevirtDisks(ApiClient client, Guid clusterId) {
        CoreV1Api api = new CoreV1Api(client);
        try {
            return api.listPersistentVolumeClaimForAllNamespaces(null, null, null, null, null, null, null, null);
        } catch (ApiException e) {
            log.error("Failed to communicate with kubevirt cluster " + clusterId + " due to: " + e.getMessage());
            log.debug("Exception", e);
            return null;
        }
    }

    private void addMissingDisks(Guid clusterId,
            V1PersistentVolumeClaimList kubevirtDisks,
            List<DiskImage> engineDisks) {
        List<PVCDisk> enginePVCDisks = engineDisks.stream().map(PVCDisk::new).collect(Collectors.toList());
        List<V1PersistentVolumeClaim> disksToAdd =
                kubevirtDisks.getItems()
                        .stream()
                        .filter(pvc -> enginePVCDisks.stream().noneMatch(DiskUpdater.equals(pvc)))
                        .collect(Collectors.toList());

        disksToAdd.forEach(d -> diskUpdater.addDisk(d, clusterId));
    }

    private void removeUnreportedDisks(V1PersistentVolumeClaimList kubevirtDisks, List<DiskImage> engineDisks) {
        List<PVCDisk> enginePVCDisks = engineDisks.stream().map(PVCDisk::new).collect(Collectors.toList());
        List<PVCDisk> disksToDelete = enginePVCDisks.stream()
                .filter(disk -> kubevirtDisks.getItems().stream().noneMatch(DiskUpdater.equals(disk)))
                .collect(Collectors.toList());
        disksToDelete.forEach(diskUpdater::removeFromDB);
    }

}
