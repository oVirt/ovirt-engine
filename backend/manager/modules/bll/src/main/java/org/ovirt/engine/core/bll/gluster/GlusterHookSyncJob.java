package org.ovirt.engine.core.bll.gluster;

import static org.ovirt.engine.core.common.businessentities.gluster.GlusterHookConflictFlags.CONTENT_CONFLICT;
import static org.ovirt.engine.core.common.businessentities.gluster.GlusterHookConflictFlags.STATUS_CONFLICT;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServerHook;
import org.ovirt.engine.core.common.gluster.GlusterFeatureSupported;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;
import org.ovirt.engine.core.utils.timer.OnTimerMethodAnnotation;

public class GlusterHookSyncJob extends GlusterJob {
    private static final Log log = LogFactory.getLog(GlusterHookSyncJob.class);

    private static final GlusterHookSyncJob instance = new GlusterHookSyncJob();

    public static GlusterHookSyncJob getInstance() {
        return instance;
    }

    @OnTimerMethodAnnotation("refreshHooks")
    public void refreshHooks() {
        log.debug("Refreshing hooks list");
        List<VDSGroup> clusters = getClusterDao().getAll();

        for (VDSGroup cluster : clusters) {
            if (!supportsGlusterHookFeature(cluster))
            {
                continue;
            }

            log.debugFormat("Syncing hooks for cluster {0}", cluster.getname());
            List<VDS> upServers = getClusterUtils().getAllUpServers(cluster.getId());

            if (upServers == null || upServers.isEmpty()) {
                continue;
            }

            List<Callable<Pair<VDS, VDSReturnValue>>> taskList = new ArrayList<Callable<Pair<VDS, VDSReturnValue>>>();
            for (final VDS upServer : upServers) {
                taskList.add(new Callable<Pair<VDS, VDSReturnValue>>() {
                    @Override
                    public Pair<VDS, VDSReturnValue> call() throws Exception {
                        VDSReturnValue returnValue =runVdsCommand(VDSCommandType.GlusterHooksList,
                                new VdsIdVDSCommandParametersBase(upServer.getId()));
                        return new Pair<VDS, VDSReturnValue>(upServer, returnValue);
                    }
                });
            }
            List<Pair<VDS, VDSReturnValue>> pairResults = ThreadPoolUtil.invokeAll(taskList);

            addOrUpdateHooks(cluster.getId(), pairResults);
        }
    }

    private void addOrUpdateHooks(Guid clusterId,  List<Pair<VDS, VDSReturnValue>> pairResults ) {

        try {
            List<GlusterHookEntity> existingHooks = getHooksDao().getByClusterId(clusterId);

            Map<String, GlusterHookEntity> existingHookMap = new HashMap<String,GlusterHookEntity>();
            Map<Guid, Set<VDS>> existingHookServersMap = new HashMap<Guid,Set<VDS>>();
            Map<String, Integer> existingHookConflictMap = new HashMap<String, Integer>();
            for (final GlusterHookEntity hook: existingHooks) {
                existingHookServersMap.put(hook.getId(),new HashSet<VDS>());
                existingHookConflictMap.put(hook.getHookKey(), hook.getConflictStatus());
                //initialize hook conflict status as this is to be computed again
                hook.setConflictStatus(0);
                existingHookMap.put(hook.getHookKey(), hook);
            }

            Set<String> fetchedHookKeyList = new HashSet<String>();
            Map<String, GlusterHookEntity> newHookMap = new HashMap<String,GlusterHookEntity>();
            List<GlusterServerHook> newServerHooks = new ArrayList<GlusterServerHook>();
            List<GlusterServerHook> updatedServerHooks = new ArrayList<GlusterServerHook>();
            List<GlusterServerHook> deletedServerHooks = new ArrayList<GlusterServerHook>();
            Set<VDS> upServers = new HashSet<VDS>();


            for (Pair<VDS, VDSReturnValue> pairResult : pairResults) {
                VDS server = pairResult.getFirst();
                upServers.add(server);

                if (!pairResult.getSecond().getSucceeded()) {
                    log.infoFormat("Failed to get list of hooks from server {0} with error {1} ", server,
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

                        GlusterServerHook serverHook = getHooksDao().getGlusterServerHook(existingHook.getId(), server.getId());

                        Integer conflictStatus = getConflictStatus(existingHook, fetchedHook);
                        //aggregate conflicts across hooks
                        existingHook.setConflictStatus(conflictStatus | existingHookMap.get(key).getConflictStatus());


                        if (conflictStatus==0) {
                            //there is no conflict in server hook and engine's copy of hook
                            //so remove from server hooks table if exists
                            if (serverHook != null)
                            {
                                deletedServerHooks.add(serverHook);
                            }
                        } else {
                            //there is a conflict. we need to either add or update entry in server hook
                            if (serverHook == null)
                            {
                                newServerHooks.add(buildServerHook(server.getId(), existingHook.getId(), fetchedHook));
                            } else {
                                if (!(serverHook.getChecksum().equals(fetchedHook.getChecksum()) && serverHook.getContentType().equals(fetchedHook.getContentType())
                                        && serverHook.getStatus().equals(fetchedHook.getStatus()))) {
                                    log.infoFormat("Updating existing server hook {0} in server {1} ", key, server);
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
                            newHook.setId(Guid.NewGuid());
                            log.infoFormat("Detected new hook {0} in server {1}, adding to engine hooks", key,server);
                            logMessage(clusterId, key, AuditLogType.GLUSTER_HOOK_ADDED);
                            //for new hook we need to fetch content as well -TBD: in another patch
                            existingHookServersMap.put(newHook.getId(),new HashSet<VDS>());
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
            for (GlusterHookEntity hook: newHookMap.values()) {
                getHooksDao().save(hook);
            }

            //Add new server hooks
            for (GlusterServerHook serverHook: newServerHooks) {
                getHooksDao().saveGlusterServerHook(serverHook);
            }

            //Update existing server hooks
            for (GlusterServerHook serverHook: updatedServerHooks) {
                getHooksDao().updateGlusterServerHook(serverHook);
            }

            //Add missing conflicts for hooks that are missing on any one of the servers
            for (Guid hookId: existingHookServersMap.keySet()) {
                if (existingHookServersMap.get(hookId).size() == upServers.size()) {
                    //hook is present in all of the servers. Nothing to do
                    continue;
                }
                //Get servers on which the hooks are missing.
                Set<VDS> hookMissingServers = new HashSet<VDS>(upServers);
                hookMissingServers.removeAll(existingHookServersMap.get(hookId));

                for (VDS missingServer : hookMissingServers) {
                    GlusterServerHook missingServerHook = new GlusterServerHook();
                    missingServerHook.setHookId(hookId);
                    missingServerHook.setServerId(missingServer.getId());
                    missingServerHook.setStatus(GlusterHookStatus.MISSING);
                    getHooksDao().saveOrUpdateGlusterServerHook(missingServerHook);
                }
                //get the hook from database, as we don't have the hookkey for it
                GlusterHookEntity hookEntity = getHooksDao().getById(hookId);
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
                if (!(hook.getConflictStatus().equals(oldConflictStatus))) {
                    log.debugFormat("Conflict change detected for hook {0} in cluster {1} ", hook.getHookKey(),clusterId);
                    logMessage(clusterId,hook.getHookKey(), AuditLogType.GLUSTER_HOOK_CONFLICT_DETECTED);
                    getHooksDao().updateGlusterHookConflictStatus(hook.getId(), hook.getConflictStatus());
                }
            }

            //Update missing conflicts for hooks found only in db and not on any of the servers
            Set<String> hooksOnlyInDB = new HashSet<String>(existingHookMap.keySet());
            hooksOnlyInDB.removeAll(fetchedHookKeyList);

            for (String key: hooksOnlyInDB) {
                GlusterHookEntity hook = existingHookMap.get(key);
                hook.addMissingConflict();
                logMessage(hook.getClusterId(),hook.getHookKey(),AuditLogType.GLUSTER_HOOK_CONFLICT_DETECTED);
                getHooksDao().updateGlusterHookConflictStatus(hook.getId(), hook.getConflictStatus());
            }
        } catch (Exception e) {
            log.error("Exception in sync", e);
        }

    }

    private void updateHookServerMap(Map<Guid, Set<VDS>> existingHookServersMap,
            Guid hookId,
            VDS server) {
        Set<VDS> hookServers =  existingHookServersMap.get(hookId);
        hookServers.add(server);
        existingHookServersMap.put(hookId, hookServers);
    }

    @SuppressWarnings("serial")
    private void logMessage(Guid clusterId, final String hookName,AuditLogType logType) {
        logUtil.logAuditMessage(clusterId,null,null,logType,new HashMap<String,String>(){
            {put("hookName",hookName);}});
    }

    private int getConflictStatus(GlusterHookEntity hook, GlusterHookEntity fetchedHook) {
        //reinitialize conflict status as we are going to calculate conflicts again.
        Integer conflictStatus = 0;
        if (!hook.getChecksum().equals(fetchedHook.getChecksum())) {
            conflictStatus = conflictStatus | CONTENT_CONFLICT.getValue();
        }
        if (!hook.getContentType().equals(fetchedHook.getContentType())) {
            conflictStatus = conflictStatus | CONTENT_CONFLICT.getValue();
        }
        if (!hook.getStatus().equals(fetchedHook.getStatus())) {
            conflictStatus = conflictStatus | STATUS_CONFLICT.getValue();
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

    private boolean supportsGlusterHookFeature(VDSGroup cluster) {
        return cluster.supportsGlusterService() && GlusterFeatureSupported.glusterHooks(cluster.getcompatibility_version());
    }

}
