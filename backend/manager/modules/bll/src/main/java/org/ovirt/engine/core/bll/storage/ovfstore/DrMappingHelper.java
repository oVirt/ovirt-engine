package org.ovirt.engine.core.bll.storage.ovfstore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.ovirt.engine.core.bll.exportimport.vnics.MapVnicsContext;
import org.ovirt.engine.core.bll.exportimport.vnics.MapVnicsFlow;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.Label;
import org.ovirt.engine.core.common.businessentities.Nameable;
import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.Role;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.businessentities.network.ExternalVnicProfileMapping;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.storage.LunDisk;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.DbUserDao;
import org.ovirt.engine.core.dao.LabelDao;
import org.ovirt.engine.core.dao.PermissionDao;
import org.ovirt.engine.core.dao.RoleDao;
import org.ovirt.engine.core.dao.scheduling.AffinityGroupDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DrMappingHelper is a helper class that encapsulates the bll mapping logic that needs to be done related to Disaster
 * Recovery scenario.
 */
@Singleton
public class DrMappingHelper {
    @Inject
    private ClusterDao clusterDao;
    @Inject
    private LabelDao labelDao;
    @Inject
    private DbUserDao dbUserDao;
    @Inject
    private RoleDao roleDao;
    @Inject
    private AffinityGroupDao affinityGroupDao;
    @Inject
    private PermissionDao permissionDao;
    @Inject
    private MapVnicsFlow mapVnicsFlow;

    protected static final Logger log = LoggerFactory.getLogger(DrMappingHelper.class);

    private static <T, Q extends Nameable> Supplier<Q> getEntityByVal(Function<T, Q> fn, T val) {
        return () -> fn.apply(val);
    }

    /**
     * The function is mainly used for DR purposes, the functionality can be described by the following steps:
     * <ul>
     * <li>1. Check if a mapped value exists for the key <code>String</code> value</li>
     * <li>2. If it does, fetch an entity by the alternative name</li>
     * <li>3. If it doesn't, fetch an entity by the original name</li>
     * <li>4. If the alternative BE exists return it, if it doesn't and the original BE exists return the original, if
     * non exists return null</li>
     * </ul>
     *
     * @param entityMap
     *            - The mapping of the BE, for example Cluster to Cluster or AffinityGroups.
     * @param originalEntityName
     *            - The entity name which is about to be added to the return list map so the VM can be registered with
     *            it.
     * @param getterFunction
     *            - The getter function to be used to fetch the entity by its name.
     * @param <R>
     *            - This is the BE which is about to be added to the registered entity
     * @return - A list containing the entities to apply to the map. Null if none exists.
     */
    private <R extends String, S extends Nameable> S getRelatedEntity(Map<R, R> entityMap,
            R originalEntityName,
            Function<R, S> getterFunction) {
        // Try to fetch the entity from the DAO (usually by name).
        // The entity which is being used is usually indicated in the entity's OVF.
        Supplier<S> sup = getEntityByVal(getterFunction, originalEntityName);
        S original = sup.get();

        // Check if a map was sent by the user for DR purposes to cast the original BE with the alternative BE.
        if (entityMap != null) {
            R destName = entityMap.get(originalEntityName);
            // If an alternative entity appears in the DR mapping sent by the user, try to fetch the alternative entity
            // from the DAO to check if it exists.
            if (destName != null) {
                // Try to fetch the entity from the DAO (usually by name).
                // The entity which is being used is the mapped entity.
                Supplier<S> supplier = getEntityByVal(getterFunction, destName);
                S dest = supplier.get();

                // If the alternative entity exists add it, if not, try to add the original entity (if exists), if both
                // are null, do not add anything.
                return addBusinessEntityToList(dest, original);
            } else if (original != null) {
                // If the mapping destination was not found in the DB, try to add the original entity
                return addBusinessEntityToList(original, null);
            }
        } else if (original != null) {
            // If there is no mapping, only add the original entity
            return addBusinessEntityToList(original, null);
        }
        return null;
    }

    /**
     * If the original BE exists, add it to the list. If the original BE is null and the alternative BE exists add it to
     * the list. If both are null, don't add anything.
     *
     * @param primaryEntity
     *            - The BE which should be added to the list
     * @param alternativeEntity
     *            - The BE which should be added to the list if originalVal is null
     * @param <S>
     *            - The BE to be added
     */
    private static <S extends Nameable> S addBusinessEntityToList(S primaryEntity,
            S alternativeEntity) {
        if (primaryEntity != null) {
            return primaryEntity;
        } else if (alternativeEntity != null) {
            return alternativeEntity;
        } else {
            log.warn("Nor primary entity of alternative entity were found. Not adding anything to the return list");
            return null;
        }
    }

    public Cluster getMappedCluster(String clusterName, Guid vmId, Map<String, String> clusterMap) {
        Cluster mappedCluster = getRelatedEntity(clusterMap,
                clusterName,
                val -> clusterDao.getByName(val));
        log.info("Mapping cluster '{}' to '{}' for vm '{}'.", clusterName,
                mappedCluster,
                vmId);
        return mappedCluster;
    }

    public void mapExternalLunDisks(List<LunDisk> luns, Map<String, Object> externalLunMap) {
        luns.forEach(lunDisk -> {
            if (externalLunMap != null) {
                LunDisk targetLunDisk = (LunDisk) externalLunMap.get(lunDisk.getLun().getId());
                if (targetLunDisk != null) {
                    log.info("Mapping LUN disk '{}' to '{}'", lunDisk.getLun().getLUNId(),
                            targetLunDisk.getLun().getLUNId());
                    lunDisk.setLun(targetLunDisk.getLun());
                    lunDisk.getLun()
                            .getLunConnections()
                            .forEach(conn -> conn.setStorageType(lunDisk.getLun().getLunType()));
                } else {
                    log.warn("No LUN disk will be mapped, LUN id '{}' has no mapping LUN disk", lunDisk.getLun().getId());
                }
            }
        });
    }

    public List<AffinityGroup> mapAffinityGroups(Map<String, String> affinityGroupMap,
            List<AffinityGroup> affinityGroupsFromParam,
            String vmName) {
        if (affinityGroupsFromParam == null) {
            return Collections.emptyList();
        }

        List<AffinityGroup> affinityGroupsToAdd = new ArrayList<>();
        affinityGroupsFromParam.forEach(affinityGroup -> {
            log.info("Mapping affinity group '{}' for vm '{}'.",
                    affinityGroup.getName(),
                    vmName);
            String mappedAffinityGroupName = affinityGroupMap.get(affinityGroup.getName());
            if (mappedAffinityGroupName == null) {
                log.warn("Mapping for affinity group '{}' not found, will use the affinity group name from OVF",
                        affinityGroup.getName());
                affinityGroupsToAdd.add(affinityGroup);
            } else {
                log.info("Mapping for affinity group '{}' found, attempting to map to '{}'",
                        affinityGroup.getName(),
                        mappedAffinityGroupName);
                AffinityGroup mappedAffinityGroup = affinityGroupDao.getByName(mappedAffinityGroupName);
                AffinityGroup affinityGroupToAdd = Optional.ofNullable(mappedAffinityGroup).orElse(affinityGroup);
                log.info("Will try to add affinity group: {}", affinityGroupToAdd.getName());
                affinityGroupsToAdd.add(affinityGroupToAdd);
            }
        });

        return affinityGroupsToAdd;
    }

    public List<Label> mapAffinityLabels(Map<String, String> affinityLabelMap,
                                         String vmName,
                                         List<Label> affinityLabelsFromParam) {
        if (affinityLabelsFromParam == null) {
            return Collections.emptyList();
        }
        List<Label> affinityLabelsToAdd = new ArrayList<>();
        affinityLabelsFromParam.forEach(affinityLabel -> {
            log.info("Mapping affinity label '{}' for vm '{}'.",
                    affinityLabel,
                    vmName);
            String mappedAffinityLabelName = affinityLabelMap.get(affinityLabel.getName());
            if (mappedAffinityLabelName == null) {
                affinityLabelsToAdd.add(affinityLabel);
                log.warn("Mapping for affinity label '{}' not found, will use the affinity label name from OVF",
                        affinityLabel.getName());
            } else {
                if (labelDao.getByName(mappedAffinityLabelName) == null) {
                    log.warn("Mapping for affinity label '{}' not found, will use the affinity label name from OVF",
                            affinityLabel.getName());
                    affinityLabelsToAdd.add(affinityLabel);
                } else {
                    log.info("Mapping for affinity label '{}' found, attempting to map to '{}'",
                            affinityLabel.getName(),
                            mappedAffinityLabelName);
                    Label mappedAffinityLabel = labelDao.getByName(mappedAffinityLabelName);
                    Label affinityLabelToAdd = Optional.ofNullable(mappedAffinityLabel).orElse(affinityLabel);
                    log.info("Will try to add affinity label '{}'", affinityLabelToAdd.getName());
                    affinityLabelsToAdd.add(affinityLabelToAdd);
                }
            }
        });

        return affinityLabelsToAdd;
    }

    public Set<DbUser> mapDbUsers(Set<DbUser> dbUsers,
                                  Map<String, String> userDomainsMap) {
        if (dbUsers == null) {
            return Collections.emptySet();
        }

        Set<DbUser> dbUsersToAdd = new HashSet<>();

        dbUsers.forEach(dbUser -> {
            String destDomain = userDomainsMap.get(dbUser.getLoginName());
            log.info("Attempting to map user '{}@{}' to '{}@{}'",
                    dbUser.getLoginName(),
                    dbUser.getDomain(),
                    dbUser.getLoginName(),
                    destDomain);
            if (destDomain == null) {
                log.warn("Mapping for domain not found, falling back to OVF user '{}@{}'",
                        dbUser.getLoginName(),
                        dbUser.getDomain());
                dbUsersToAdd.add(dbUser);
            } else {
                DbUser destUser = dbUserDao.getByUsernameAndDomain(dbUser.getLoginName(), destDomain);
                dbUsersToAdd.add(Optional.ofNullable(destUser).orElse(dbUser));
            }
        });

        return dbUsersToAdd;
    }

    public Map<String, Set<String>> mapRoles(Map<String, String> roleMap, Map<String, Set<String>> userToRoles) {
        if (MapUtils.isEmpty(userToRoles)) {
            return Collections.emptyMap();
        }

        Map<String, Set<String>> candidateUserToRoles = new HashMap<>();
        userToRoles.forEach((user, roles) -> {
            Set<String> rolesToAdd = new HashSet<>();
            roles.forEach(roleName -> {
                String destRoleName = roleMap.get(roleName);
                log.info("Attempting to map role '{}' to '{}'", roleName, destRoleName);
                if (destRoleName == null) {
                    log.info("Mapping for role '{}' was not found, will try to use OVF role", roleName);
                    rolesToAdd.add(roleName);
                } else {
                    Role destRole = roleDao.getByName(destRoleName);
                    String roleToAdd = Optional.ofNullable(destRole).map(Role::getName).orElse(roleName);
                    log.info("Will try to add role '{}' for user '{}'", roleToAdd, user);
                    rolesToAdd.add(roleToAdd);
                }
            });
            candidateUserToRoles.put(user, rolesToAdd);
        });
        return candidateUserToRoles;
    }

    public void addPermissions(Set<DbUser> dbUsers,
                               Map<String, Set<String>> userToRoles,
                               Guid objectId,
                               VdcObjectType objectType,
                               Map<String, String> roleMap) {
        dbUsers.forEach(dbUser -> userToRoles.getOrDefault(dbUser.getLoginName(), Collections.emptySet()).forEach(roleName -> {
            Role role = getRelatedEntity(roleMap, roleName, val -> roleDao.getByName(val));
            if (role != null) {
                DbUser dbUserFromDB =
                        dbUserDao.getByUsernameAndDomain(dbUser.getLoginName(), dbUser.getDomain());
                Permission p = new Permission(dbUserFromDB.getId(), role.getId(), objectId, objectType);
                permissionDao.save(p);
            } else {
                log.warn("Role {} was not found", roleName);
            }
        }));
    }

    public List<String> updateVnicsFromMappings(Guid clusterId, String vmName, List<VmNetworkInterface> vnics, Collection<ExternalVnicProfileMapping> mappings) {
        MapVnicsContext ctx = new MapVnicsContext("updateVnicsFromMappings")
            .setClusterId(clusterId)
            .setVmName(vmName)
            .setOvfVnics(vnics)
            .setUserMappings(mappings);
        mapVnicsFlow.getHead().process(ctx);
        return !CollectionUtils.isEmpty(ctx.getNonAssociableVnics()) ? ctx.getNonAssociableVnics() : Collections.emptyList();
    }
}
