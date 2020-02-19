package org.ovirt.engine.core.bll.scheduling.pending;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.utils.HugePageUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.VdsManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tracking service for all pending resources. All writes are synchronized and ensure that the internal
 * structures are consistent. Reads are best effort operations and require external locking if absolute consistency
 * is needed.
 */
public class PendingResourceManager {
    private static final Logger log = LoggerFactory.getLogger(PendingResourceManager.class);

    // All internal structures have to be thread-safe for concurrent access
    private final Map<Guid, Set<PendingResource>> resourcesByHost = new ConcurrentHashMap<>();
    private final Map<Guid, Set<PendingResource>> resourcesByVm = new ConcurrentHashMap<>();
    private final Map<PendingResource, PendingResource> pendingResources = new ConcurrentHashMap<>();

    private final ResourceManager resourceManager;

    public PendingResourceManager() {
        resourceManager = null;
    }

    public PendingResourceManager(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    /**
     * Remove all pending resources associated with the VM.
     * @param vm VmStatic with valid getId()
     */
    public void clearVm(VmStatic vm) {
        Set<Guid> modifiedHosts;

        synchronized (this) {
            if (!resourcesByVm.containsKey(vm.getId())) {
                return;
            }

            log.debug("Clearing pending resources for VM {}", vm.getId());
            modifiedHosts = new HashSet<>();

            /* Remove all resources associated with the VM from the global set
             * and from the byHost index
             */
            resourcesByVm.compute(vm.getId(), (vmId, resources) -> {
                resources.stream()
                        .peek(pendingResources::remove)
                        .filter(r -> removeFromSetMap(resourcesByHost, r.getHost(), r))
                        .map(PendingResource::getHost)
                        .forEach(modifiedHosts::add);

                resources.clear();
                return resources;
            });
        }

        for (Guid hostId: modifiedHosts) {
            notifyHostManagers(hostId);
        }
    }

    public void clearVm(VM vm) {
        clearVm(vm.getStaticData());
    }

    /**
     * Remove all pending resources associated with the host.
     *
     * This is supposed to be used during maintenance flows to clear possible stale data.
     *
     * @param host VDS with valid getId()
     */
    public void clearHost(VDS host) {
        synchronized (this) {
            if (!resourcesByHost.containsKey(host.getId())) {
                return;
            }

            log.debug("Clearing pending resources for host {}", host.getId());

            /* Remove all resources associated with the host from the global set
             *  and from the byVm index
             */
            resourcesByHost.compute(host.getId(), (hostId, resources) -> {
                resources.stream()
                        .peek(pendingResources::remove)
                        .forEach(r -> removeFromSetMap(resourcesByVm, r.getVm(), r));

                resources.clear();
                return resources;
            });
        }

        notifyHostManagers(host.getId());
    }

    /**
     * Add a resource record to the pending lists.
     *
     * IMPORTANT: Call notifyHostManagers once all resources are
     *            registered.
     *
     * @param resource Pending resource instance with valid host and vm
     *                 fields.
     */
    public void addPending(PendingResource resource) {
        synchronized (this) {
            /* Clear VM and Host indexes when the resource is added again.
             *  This should not happen in theory, but lets anticipate future bugs :)
             */
            if (pendingResources.containsKey(resource)) {
                PendingResource old = pendingResources.get(resource);
                log.warn("Clearing stale pending resource {} (host: {}, vm: {})",
                        old, old.getHost(), old.getVm());

                removeFromSetMap(resourcesByVm, old.getVm(), old);
                removeFromSetMap(resourcesByHost, old.getHost(), old);
            }

            log.debug("Adding pending resource {} (host: {}, vm: {})",
                    resource, resource.getHost(), resource.getVm());

            /* Update indexes */
            addToSetMap(resourcesByVm, resource.getVm(), resource);
            addToSetMap(resourcesByHost, resource.getHost(), resource);
            pendingResources.put(resource, resource);
        }
    }

    /**
     * Return all currently pending resources of type "type" associated with host "vds".
     * @param host ID of a host
     * @param type Class object identifying the type of pending resources we are interested in
     * @return Iterable object with the requested resources
     */
    public <T extends PendingResource> List<T> pendingHostResources(Guid host, Class<T> type) {
        return collectResources(resourcesByHost, host, res -> res.getClass().equals(type)).stream()
                .map(r -> (T)r)
                .collect(Collectors.toList());
    }

    /**
     * Return all currently pending resources of type "type" associated with VM "vm".
     * @param vm ID of a VM
     * @param type Class object identifying the type of pending resources we are interested in
     * @return Iterable object with the requested resources
     */
    public <T extends PendingResource> List<T> pendingVmResources(Guid vm, Class<T> type) {
        return collectResources(resourcesByVm, vm, res -> res.getClass().equals(type)).stream()
                .map(r -> (T)r)
                .collect(Collectors.toList());
    }

    private static List<PendingResource> collectResources(Map<Guid, Set<PendingResource>> map,
            Guid id,
            Predicate<PendingResource> predicate) {

        List<PendingResource> list = new ArrayList<>();
        map.computeIfPresent(id, (k, resourceSet) -> {
            resourceSet.stream()
                    .filter(predicate)
                    .forEach(list::add);

            return resourceSet;
        });
        return list;
    }

    /**
     * Find pending resource that matches the provided template and return it.
     * @param template resource template filled with identification-specific fields
     * @return The actual pending resource
     */
    public <T extends PendingResource> T getExactPendingResource(T template) {
        return (T)pendingResources.get(template);
    }

    /**
     * Return all currently pending resources of type "type".
     * @param type Class object identifying the type of pending resources we are interested in
     * @return Iterable object with the requested resources
     */
    public <T extends PendingResource> Iterable<T> pendingResources(Class<T> type) {
        List<T> list = new ArrayList<>();
        for (PendingResource resource: pendingResources.values()) {
            if (resource.getClass().equals(type)) {
                list.add((T)resource);
            }
        }

        return list;
    }

    /**
     * Notify host manager that the pending memory and CPU data have changed.
     * This is automatically called when a VM or Host are cleared, however the user is responsible
     * for calling it after finished with adding all new pending resources for a VM.
     * @param hostId - it of the affected host
     */
    public void notifyHostManagers(Guid hostId) {
        if (resourceManager == null) {
            return;
        }

        VdsManager vdsManager = resourceManager.getVdsManager(hostId);

        int pendingCpus = PendingCpuCores.collectForHost(this, hostId);
        int pendingMemory = PendingOvercommitMemory.collectForHost(this, hostId);

        // Adding pending hugepages to pending memory for the host,
        // which will decrease the memory available for scheduling.
        int pendingHugePageMemMb = HugePageUtils.totalHugePageMemMb(PendingHugePages.collectForHost(this, hostId));

        vdsManager.updatePendingData(pendingMemory + pendingHugePageMemMb, pendingCpus);
    }

    /**
     * Atomically removes an element from a set that is a value in a map.
     *
     * @param map Map that contains sets of elements
     * @param id Key to the map
     * @param resource Element to be removed from the set
     *
     * @return True if an element was removed
     */
    private static boolean removeFromSetMap(Map<Guid, Set<PendingResource>> map, Guid id, PendingResource resource) {
        boolean[] res = {false};

        map.computeIfPresent(id, (k, resourceSet) -> {
            res[0] = resourceSet.remove(resource);
            return resourceSet;
        });

        return res[0];
    }

    /**
     * Atomically adds an element to a set that is a value in a map.
     *
     * If there is no value for the key, a new set is created.
     *
     * @param map Map that contains sets of elements
     * @param id Key to the map
     * @param resource Element to be added to the set
     */
    private static void addToSetMap(Map<Guid, Set<PendingResource>> map, Guid id, PendingResource resource) {
        map.compute(id, (k, resourceSet) -> {
            if (resourceSet == null) {
                resourceSet = new HashSet<>();
            }

            resourceSet.add(resource);
            return resourceSet;
        });
    }
}
