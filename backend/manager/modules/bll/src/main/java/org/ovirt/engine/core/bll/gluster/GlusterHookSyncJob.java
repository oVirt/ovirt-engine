package org.ovirt.engine.core.bll.gluster;

import static org.ovirt.engine.core.common.businessentities.gluster.GlusterHookConflictFlags.CONTENT_CONFLICT;
import static org.ovirt.engine.core.common.businessentities.gluster.GlusterHookConflictFlags.STATUS_CONFLICT;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.inject.Singleton;

import org.ovirt.engine.core.bll.scheduling.OnTimerMethodAnnotation;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServerHook;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterHookVDSParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class GlusterHookSyncJob extends GlusterJob {
    private static final Logger log = LoggerFactory.getLogger(GlusterHookSyncJob.class);

    @Override
    public Collection<GlusterJobSchedulingDetails> getSchedulingDetails() {
        return Collections.singleton(new GlusterJobSchedulingDetails(
                "refreshHooks", getRefreshRate(ConfigValues.GlusterRefreshRateHooks)));
    }

    @OnTimerMethodAnnotation("refreshHooks")
    public void refreshHooks() {
        log.debug("Refreshing hooks list");
        List<Cluster> clusters = clusterDao.getAll();

        for (Cluster cluster : clusters) {
            refreshHooksInCluster(cluster, false);
        }
    }

    /**
     *
     * @param cluster - the Cluster for which the gluster hook data is refreshed
     * @param throwError - set to true if this method should throw exception.
     */
    public void refreshHooksInCluster(Cluster cluster, boolean throwError) {
        if (!cluster.supportsGlusterService()) {
            return;
        }

        log.debug("Syncing hooks for cluster {}", cluster.getName());
        List<VDS> upServers = glusterUtil.getAllUpServers(cluster.getId());

        if (upServers == null || upServers.isEmpty()) {
            return;
        }

        List<Callable<Pair<VDS, VDSReturnValue>>> taskList = new ArrayList<>();
        for (final VDS upServer : upServers) {
            taskList.add(() -> {
                VDSReturnValue returnValue =runVdsCommand(VDSCommandType.GlusterHooksList,
                        new VdsIdVDSCommandParametersBase(upServer.getId()));
                return new Pair<>(upServer, returnValue);
            });
        }
        List<Pair<VDS, VDSReturnValue>> pairResults = ThreadPoolUtil.invokeAll(taskList);
        try {
            addOrUpdateHooks(cluster.getId(), pairResults);
        } catch (EngineException e) {
            if (throwError) {
                //propogate error to calling application.
                throw e;
            }
        }
    }

    private void addOrUpdateHooks(Guid clusterId,  List<Pair<VDS, VDSReturnValue>> pairResults ) {

        try {
            List<GlusterHookEntity> existingHooks = hooksDao.getByClusterId(clusterId);
            List<Callable<Pair<GlusterHookEntity, VDSReturnValue>>> contentTasksList = new ArrayList<>();

            Map<String, GlusterHookEntity> existingHookMap = new HashMap<>();
            Map<Guid, Set<VDS>> existingHookServersMap = new HashMap<>();
            Map<String, Integer> existingHookConflictMap = new HashMap<>();
            for (final GlusterHookEntity hook: existingHooks) {
                existingHookServersMap.put(hook.getId(), new HashSet<>());
                existingHookConflictMap.put(hook.getHookKey(), hook.getConflictStatus());
                //initialize hook conflict status as this is to be computed again
                hook.setConflictStatus(0);
                existingHookMap.put(hook.getHookKey(), hook);
            }

            Set<String> fetchedHookKeyList = new HashSet<>();
            Map<String, GlusterHookEntity> newHookMap = new HashMap<>();
            List<GlusterServerHook> newServerHooks = new ArrayList<>();
            List<GlusterServerHook> updatedServerHooks = new ArrayList<>();
            Set<VDS> upServers = new HashSet<>();


            for (Pair<VDS, VDSReturnValue> pairResult : pairResults) {
                final VDS server = pairResult.getFirst();
                upServers.add(server);

                if (!pairResult.getSecond().getSucceeded()) {
                    log.info("Failed to get list of hooks from server '{}' with error: {}", server,
                            pairResult.getSecond().getVdsError().getMessage());
                    logUtil.logServerMessage(server, AuditLogType.GLUSTER_HOOK_LIST_FAILED);
                    continue;
                }

                @SuppressWarnings("unchecked")
                List<GlusterHookEntity> fetchedHooks = (List<GlusterHookEntity>) pairResult.getSecond().getReturnValue();

                for (GlusterHookEntity fetchedHook : fetchedHooks) {
                    String key= fetchedHook.getHookKey();
                    fetchedHookKeyList.add(key);

                    GlusterHookEntity existingHook = existingHookMap.get(key);

                    if (existingHook != null) {
                        updateHookServerMap(existingHookServersMap, existingHook.getId(), server);

                        GlusterServerHook serverHook = hooksDao.getGlusterServerHook(existingHook.getId(), server.getId());

                        Integer conflictStatus = getConflictStatus(existingHook, fetchedHook);
                        //aggregate conflicts across hooks
                        existingHook.setConflictStatus(conflictStatus | existingHookMap.get(key).getConflictStatus());


                        if (conflictStatus!=0) {
                            //there is a conflict. we need to either add or update entry in server hook
                            if (serverHook == null) {
                                newServerHooks.add(buildServerHook(server.getId(), existingHook.getId(), fetchedHook));
                            } else {
                                if (!(serverHook.getChecksum().equals(fetchedHook.getChecksum()) && serverHook.getContentType().equals(fetchedHook.getContentType())
                                        && serverHook.getStatus().equals(fetchedHook.getStatus()))) {
                                    log.info("Updating existing server hook '{}' in server '{}' ", key, server);
                                    serverHook.setChecksum(fetchedHook.getChecksum());
                                    serverHook.setContentType(fetchedHook.getContentType());
                                    serverHook.setStatus(fetchedHook.getStatus());
                                    updatedServerHooks.add(serverHook);
                                }
                            }
                        }

                    } else {
                        GlusterHookEntity newHook = newHookMap.get(key);
                        if (newHook == null) {
                            newHook = fetchedHook;
                            newHook.setClusterId(clusterId);
                            newHook.setId(Guid.newGuid());
                            log.info("Detected new hook '{}' in server '{}', adding to engine hooks", key, server);
                            logMessage(clusterId, key, AuditLogType.GLUSTER_HOOK_DETECTED_NEW);

                            updateContentTasksList(contentTasksList, newHook, server);

                            existingHookServersMap.put(newHook.getId(), new HashSet<>());
                        }
                        Integer conflictStatus = getConflictStatus(newHook, fetchedHook);
                        if (conflictStatus > 0) {
                            newHook.getServerHooks().add(buildServerHook(server.getId(), newHook.getId(), fetchedHook));
                        }
                        newHook.setConflictStatus(newHook.getConflictStatus() | conflictStatus);
                        newHookMap.put(key, newHook);
                        updateHookServerMap(existingHookServersMap, newHook.getId(), server);
                    }
                }
            }

            //Save new hooks
            saveNewHooks(newHookMap, contentTasksList);

            //Add new server hooks
            for (GlusterServerHook serverHook: newServerHooks) {
                hooksDao.saveGlusterServerHook(serverHook);
            }

            //Update existing server hooks
            for (GlusterServerHook serverHook: updatedServerHooks) {
                hooksDao.updateGlusterServerHook(serverHook);
            }

            syncExistingHooks(existingHookMap, existingHookServersMap, existingHookConflictMap, upServers);

            //Update missing conflicts for hooks found only in db and not on any of the servers
            Set<String> hooksOnlyInDB = new HashSet<>(existingHookMap.keySet());
            hooksOnlyInDB.removeAll(fetchedHookKeyList);

            for (String key: hooksOnlyInDB) {
                GlusterHookEntity hook = existingHookMap.get(key);
                hook.addMissingConflict();
                logMessage(hook.getClusterId(), hook.getHookKey(), AuditLogType.GLUSTER_HOOK_CONFLICT_DETECTED);
                hooksDao.updateGlusterHookConflictStatus(hook.getId(), hook.getConflictStatus());
            }
        } catch (Exception e) {
            log.error("Exception in sync", e);
            throw new EngineException(EngineError.GlusterHookListException, e.getLocalizedMessage());
        }

    }

    private void saveNewHooks(Map<String, GlusterHookEntity> newHookMap,
            List<Callable<Pair<GlusterHookEntity, VDSReturnValue>>> contentTasksList) {
        for (GlusterHookEntity hook: newHookMap.values()) {
            hooksDao.save(hook);
        }
        //retrieve and update hook content
        saveHookContent(contentTasksList);
    }

    private void saveHookContent(List<Callable<Pair<GlusterHookEntity, VDSReturnValue>>> contentTasksList) {

        if (contentTasksList.isEmpty()) {
            return;
        }
        List<Pair<GlusterHookEntity, VDSReturnValue>> pairResults = ThreadPoolUtil.invokeAll(contentTasksList);

        for (Pair<GlusterHookEntity, VDSReturnValue> pairResult: pairResults) {
            final GlusterHookEntity hook = pairResult.getFirst();
            if (!pairResult.getSecond().getSucceeded()) {
                log.info("Failed to get content of hook '{}' with error: {}", hook.getHookKey(),
                        pairResult.getSecond().getVdsError().getMessage());
                logMessage(hook.getClusterId(), hook.getHookKey(), AuditLogType.GLUSTER_HOOK_GETCONTENT_FAILED);
                continue;
            }
            final String content = (String)pairResult.getSecond().getReturnValue();
            hooksDao.updateGlusterHookContent(hook.getId(), hook.getChecksum(), content);
        }

    }

    private void syncExistingHooks(Map<String, GlusterHookEntity> existingHookMap,
            Map<Guid, Set<VDS>> existingHookServersMap,
            Map<String, Integer> existingHookConflictMap,
            Set<VDS> upServers) {
        //Add missing conflicts for hooks that are missing on any one of the servers
        for (Map.Entry<Guid, Set<VDS>> entry : existingHookServersMap.entrySet()) {
            if (entry.getValue().size() == upServers.size()) {
                //hook is present in all of the servers. Nothing to do
                continue;
            }
            //Get servers on which the hooks are missing.
            Set<VDS> hookMissingServers = new HashSet<>(upServers);
            hookMissingServers.removeAll(entry.getValue());

            for (VDS missingServer : hookMissingServers) {
                GlusterServerHook missingServerHook = new GlusterServerHook();
                missingServerHook.setHookId(entry.getKey());
                missingServerHook.setServerId(missingServer.getId());
                missingServerHook.setStatus(GlusterHookStatus.MISSING);
                hooksDao.saveOrUpdateGlusterServerHook(missingServerHook);
            }
            //get the hook from database, as we don't have the hookkey for it
            GlusterHookEntity hookEntity = hooksDao.getById(entry.getKey());
            if (existingHookMap.get(hookEntity.getHookKey()) != null) {
                //if it was an already existing hook, get the hook with
                //updated conflict values from map
                hookEntity = existingHookMap.get(hookEntity.getHookKey());
            }
            hookEntity.addMissingConflict();
            existingHookMap.put(hookEntity.getHookKey(), hookEntity);
        }

        //Update conflict status for existing hooks
        for (GlusterHookEntity hook: existingHookMap.values()) {
            // Check if aggregated conflict status is different from existing hook
            Integer oldConflictStatus = existingHookConflictMap.get(hook.getHookKey());
            if (!hook.getConflictStatus().equals(oldConflictStatus)) {
                log.debug("Conflict change detected for hook '{}' in cluster '{}' ",
                        hook.getHookKey(), hook.getClusterId());
                logMessage(hook.getClusterId(), hook.getHookKey(), AuditLogType.GLUSTER_HOOK_CONFLICT_DETECTED);
                hooksDao.updateGlusterHookConflictStatus(hook.getId(), hook.getConflictStatus());
            }
        }
    }

    private void updateContentTasksList(List<Callable<Pair<GlusterHookEntity, VDSReturnValue>>> contentTasksList,
            final GlusterHookEntity hook,
            final VDS server) {
        contentTasksList.add(() -> {
            VDSReturnValue returnValue = runVdsCommand(VDSCommandType.GetGlusterHookContent,
                    new GlusterHookVDSParameters(server.getId(), hook.getGlusterCommand(), hook.getStage(), hook.getName()));
            return new Pair<>(hook, returnValue);
        });
    }

    private void updateHookServerMap(Map<Guid, Set<VDS>> existingHookServersMap,
            Guid hookId,
            VDS server) {
        Set<VDS> hookServers =  existingHookServersMap.get(hookId);
        hookServers.add(server);
        existingHookServersMap.put(hookId, hookServers);
    }

    @SuppressWarnings("serial")
    private void logMessage(Guid clusterId, final String hookName, AuditLogType logType) {
        logUtil.logAuditMessage(clusterId,
                clusterDao.get(clusterId).getName(),
                null,
                null,
                logType,
                Collections.singletonMap("hookName", hookName));
    }

    private int getConflictStatus(GlusterHookEntity hook, GlusterHookEntity fetchedHook) {
        //reinitialize conflict status as we are going to calculate conflicts again.
        Integer conflictStatus = 0;
        if (!hook.getChecksum().equals(fetchedHook.getChecksum())) {
            conflictStatus |= CONTENT_CONFLICT.getValue();
        }
        if (!hook.getContentType().equals(fetchedHook.getContentType())) {
            conflictStatus |= CONTENT_CONFLICT.getValue();
        }
        if (!hook.getStatus().equals(fetchedHook.getStatus())) {
            conflictStatus |= STATUS_CONFLICT.getValue();
        }
        return conflictStatus;
    }

    private GlusterServerHook buildServerHook(Guid serverId, Guid hookId, GlusterHookEntity returnedHook) {
        GlusterServerHook serverHook = new GlusterServerHook();
        serverHook.setHookId(hookId);
        serverHook.setServerId(serverId);
        serverHook.setStatus(returnedHook.getStatus());
        serverHook.setContentType(returnedHook.getContentType());
        serverHook.setChecksum(returnedHook.getChecksum());
        return serverHook;
    }
}
