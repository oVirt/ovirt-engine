package org.ovirt.engine.core.common.businessentities.gluster;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.common.asynctasks.gluster.GlusterAsyncTask;
import org.ovirt.engine.core.common.businessentities.BusinessEntityWithStatus;
import org.ovirt.engine.core.common.businessentities.Nameable;
import org.ovirt.engine.core.common.businessentities.Queryable;
import org.ovirt.engine.core.common.constants.gluster.GlusterConstants;
import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.RemoveEntity;
import org.ovirt.engine.core.common.validation.group.gluster.CreateReplicatedVolume;
import org.ovirt.engine.core.common.validation.group.gluster.CreateStripedVolume;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;

/**
 * The gluster volume entity. This is a logical partition within the virtual storage space provided by a Gluster
 * Cluster. It is made up of multiple bricks (server:brickDirectory) across multiple servers of the cluster.
 *
 * @see GlusterVolumeType
 * @see TransportType
 * @see GlusterStatus
 * @see GlusterBrickEntity
 * @see GlusterVolumeOptionEntity
 * @see AccessProtocol
 */
public class GlusterVolumeEntity implements Queryable, BusinessEntityWithStatus<Guid, GlusterStatus>, GlusterTaskSupport, Nameable {
    private static final long serialVersionUID = 2355384696827317277L;

    @NotNull(message = "VALIDATION_GLUSTER_VOLUME_ID_NOT_NULL", groups = RemoveEntity.class)
    private Guid id;

    @NotNull(message = "VALIDATION_GLUSTER_VOLUME_CLUSTER_ID_NOT_NULL", groups = {CreateEntity.class, CreateReplicatedVolume.class, CreateStripedVolume.class})
    private Guid clusterId;

    private String clusterName;

    @NotNull(message = "VALIDATION_GLUSTER_VOLUME_NAME_NOT_NULL", groups = {CreateEntity.class, CreateReplicatedVolume.class, CreateStripedVolume.class})
    private String name;

    @NotNull(message = "VALIDATION_GLUSTER_VOLUME_TYPE_NOT_NULL", groups = {CreateEntity.class, CreateReplicatedVolume.class, CreateStripedVolume.class})
    private GlusterVolumeType volumeType;

    @NotNull(message = "VALIDATION_GLUSTER_VOLUME_REPLICA_COUNT_NOT_NULL", groups = CreateReplicatedVolume.class)
    private Integer replicaCount;

    @NotNull(message = "VALIDATION_GLUSTER_VOLUME_STRIPE_COUNT_NOT_NULL", groups = CreateStripedVolume.class)
    private Integer stripeCount;

    private Integer disperseCount;

    private Integer redundancyCount;

    private Boolean isArbiter;

    @Valid
    private Map<String, GlusterVolumeOptionEntity> options;

    @NotNull(message = "VALIDATION_GLUSTER_VOLUME_BRICKS_NOT_NULL", groups = {CreateEntity.class, CreateReplicatedVolume.class, CreateStripedVolume.class})
    @Valid
    private List<GlusterBrickEntity> bricks;

    private Integer snapshotsCount;

    private Integer snapMaxLimit;

    private Boolean snapshotScheduled;

    private GlusterStatus status;

    // Gluster and NFS are enabled by default
    private Set<AccessProtocol> accessProtocols;

    private Set<TransportType> transportTypes;

    private GlusterAsyncTask asyncTask;

    private GlusterVolumeAdvancedDetails advancedDetails;

    private Boolean isGeoRepMaster;

    private String geoRepMasterVolAndClusterName;

    public GlusterVolumeEntity() {
        options = new LinkedHashMap<>();
        bricks = new ArrayList<>();
        status = GlusterStatus.DOWN;
        accessProtocols = new LinkedHashSet<>(Arrays.asList(AccessProtocol.GLUSTER, AccessProtocol.NFS));
        transportTypes = new LinkedHashSet<>();
        volumeType = GlusterVolumeType.DISTRIBUTE;
        asyncTask = new GlusterAsyncTask();
        advancedDetails = new GlusterVolumeAdvancedDetails();
        snapshotScheduled = Boolean.valueOf(false);
        isArbiter = Boolean.FALSE;
    }

    @Override
    public Guid getId() {
        return id;
    }

    @Override
    public void setId(Guid id) {
        this.id = id;
    }

    public Guid getClusterId() {
        return clusterId;
    }

    public void setClusterId(Guid clusterId) {
        this.clusterId = clusterId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public GlusterVolumeType getVolumeType() {
        return volumeType;
    }

    public void setVolumeType(GlusterVolumeType volumeType) {
        this.volumeType = volumeType;

        if (!volumeType.isReplicatedType()) {
            setReplicaCount(0);
        }
        if (!volumeType.isStripedType()) {
            setStripeCount(0);
        }
        if (!volumeType.isDispersedType()) {
            setDisperseCount(0);
            setRedundancyCount(0);
        }
    }

    public void setVolumeType(String volumeType) {
        setVolumeType(GlusterVolumeType.fromValue(volumeType));
    }

    @Override
    public GlusterStatus getStatus() {
        return status;
    }

    @Override
    public void setStatus(GlusterStatus status) {
        this.status = status;
    }

    public boolean isOnline() {
        return this.status == GlusterStatus.UP;
    }

    public Integer getReplicaCount() {
        return replicaCount;
    }

    public void setReplicaCount(Integer replicaCount) {
        this.replicaCount = replicaCount;
    }

    public Integer getStripeCount() {
        return stripeCount;
    }

    public void setStripeCount(Integer stripeCount) {
        this.stripeCount = stripeCount;
    }

    public Integer getDisperseCount() {
        return this.disperseCount;
    }

    public void setDisperseCount(Integer disperseCount) {
        this.disperseCount = disperseCount;
    }

    public Integer getRedundancyCount() {
        return this.redundancyCount;
    }

    public void setRedundancyCount(Integer redundancyCount) {
        this.redundancyCount = redundancyCount;
    }

    public Set<AccessProtocol> getAccessProtocols() {
        return accessProtocols;
    }

    public void setAccessProtocols(Set<AccessProtocol> accessProtocols) {
        this.accessProtocols = accessProtocols;
    }

    public void addAccessProtocol(AccessProtocol protocol) {
        accessProtocols.add(protocol);
    }

    public void removeAccessProtocol(AccessProtocol protocol) {
        accessProtocols.remove(protocol);
    }

    public Set<TransportType> getTransportTypes() {
        return transportTypes;
    }

    public void setTransportTypes(Set<TransportType> transportTypes) {
        this.transportTypes = transportTypes;
    }

    public void addTransportType(TransportType transportType) {
        transportTypes.add(transportType);
    }

    public void removeTransportType(TransportType transportType) {
        transportTypes.remove(transportType);
    }

    public String getAccessControlList() {
        return getOptionValue(GlusterConstants.OPTION_AUTH_ALLOW);
    }

    public void setAccessControlList(String accessControlList) {
        if (!StringHelper.isNullOrEmpty(accessControlList)) {
            setOption(GlusterConstants.OPTION_AUTH_ALLOW, accessControlList);
        }
    }

    public Collection<GlusterVolumeOptionEntity> getOptions() {
        return options.values();
    }

    public GlusterVolumeOptionEntity getOption(String optionKey) {
        return options.get(optionKey);
    }

    /**
     * Returns value of given option key as set on the volume. <br>
     * In case the option is not set, <code>null</code> will be returned.
     */
    public String getOptionValue(String optionKey) {
        GlusterVolumeOptionEntity option = options.get(optionKey);
        if (option == null) {
            return null;
        }
        return option.getValue();
    }

    public void setOption(GlusterVolumeOptionEntity option) {
        options.put(option.getKey(), option);
    }

    public void setOption(String key, String value) {
        if (options.containsKey(key)) {
            options.get(key).setValue(value);
        } else {
            options.put(key, new GlusterVolumeOptionEntity(id, key, value));
        }
    }

    /**
     * Sets options from a comma separated list of key value pairs separated by = <br>
     * e.g. key=val1,key2=val2,...,keyn=valn
     */
    public void setOptions(String options) {
        this.options.clear();
        if (options == null || options.trim().isEmpty()) {
            return;
        }

        String[] optionArr = options.split(",", -1);
        for (String option : optionArr) {
            String[] optionInfo = option.split("=", -1);
            if (optionInfo.length == 2) {
                setOption(optionInfo[0], optionInfo[1]);
            }
        }
    }

    public void setOptions(Collection<GlusterVolumeOptionEntity> options) {
        this.options.clear();
        for (GlusterVolumeOptionEntity option : options) {
            setOption(option);
        }
    }

    public void setOptions(Map<String, String> options) {
        this.options.clear();
        for (Entry<String, String> entry : options.entrySet()) {
            setOption(entry.getKey(), entry.getValue());
        }
    }

    public void removeOption(String optionKey) {
        options.remove(optionKey);
    }

    public void addBrick(GlusterBrickEntity GlusterBrick) {
        bricks.add(GlusterBrick);
    }

    public void addBricks(Collection<GlusterBrickEntity> bricks) {
        this.bricks.addAll(bricks);
    }

    public void setBricks(List<GlusterBrickEntity> bricks) {
        this.bricks = bricks;
    }

    public Integer getSnapshotsCount() {
        return this.snapshotsCount;
    }

    public void setSnapshotsCount(Integer value) {
        this.snapshotsCount = value;
    }

    public Integer getSnapMaxLimit() {
        return this.snapMaxLimit;
    }

    public void setSnapMaxLimit(Integer limit) {
        this.snapMaxLimit = limit;
    }

    public Boolean getSnapshotScheduled() {
        return this.snapshotScheduled;
    }

    public void setSnapshotScheduled(Boolean snapshotScheduled) {
        this.snapshotScheduled = snapshotScheduled;
    }

    public Boolean getIsGeoRepMaster() {
        return isGeoRepMaster;
    }

    public void setIsGeoRepMaster(Boolean isGeoRepMaster) {
        this.isGeoRepMaster = isGeoRepMaster;
    }

    public Boolean getIsGeoRepSlave() {
        return getGeoRepMasterVolAndClusterName() != null;
    }

    public String getGeoRepMasterVolAndClusterName() {
        return geoRepMasterVolAndClusterName;
    }

    public void setGeoRepMasterVolAndClusterName(String masterVolAndClusterName) {
        this.geoRepMasterVolAndClusterName = masterVolAndClusterName;
    }

    public void removeBrick(GlusterBrickEntity GlusterBrick) {
        bricks.remove(GlusterBrick);
    }

    /**
     * Replaces an existing brick in the volume with the given new brick. The new brick will have same index as the
     * existing one.
     *
     * @return Index of the brick that was replaced. Returns -1 if the {@code existingBrick} is not found in the volume, leaving the volume unchanged.
     */
    public int replaceBrick(GlusterBrickEntity existingBrick, GlusterBrickEntity newBrick) {
        int index = bricks.indexOf(existingBrick);
        if (index != -1) {
            GlusterBrickEntity brick = bricks.get(index);
            brick.copyFrom(newBrick);
        }
        return index;
    }

    public List<GlusterBrickEntity> getBricks() {
        return bricks;
    }

    public void enableNFS() {
        accessProtocols.add(AccessProtocol.NFS);
        setOption(GlusterConstants.OPTION_NFS_DISABLE, GlusterConstants.OFF);
    }

    public void disableNFS() {
        accessProtocols.remove(AccessProtocol.NFS);
        setOption(GlusterConstants.OPTION_NFS_DISABLE, GlusterConstants.ON);
    }

    public boolean isNfsEnabled() {
        String nfsDisabled = getOptionValue(GlusterConstants.OPTION_NFS_DISABLE);
        return nfsDisabled == null || nfsDisabled.equalsIgnoreCase(GlusterConstants.OFF);
    }

    public void enableCifs() {
        accessProtocols.add(AccessProtocol.CIFS);
        setOption(GlusterConstants.OPTION_USER_CIFS, GlusterConstants.ENABLE);
    }

    public void disableCifs() {
        accessProtocols.remove(AccessProtocol.CIFS);
        setOption(GlusterConstants.OPTION_USER_CIFS, GlusterConstants.DISABLE);
    }

    public boolean isCifsEnabled() {
        String cifsEnabled = getOptionValue(GlusterConstants.OPTION_USER_CIFS);
        return cifsEnabled == null || cifsEnabled.equalsIgnoreCase(GlusterConstants.ENABLE);
    }

    public List<String> getBrickDirectories() {
        List<String> brickDirectories = new ArrayList<>();
        for (GlusterBrickEntity brick : getBricks()) {
            brickDirectories.add(brick.getQualifiedName());
        }
        return brickDirectories;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                name,
                clusterId,
                volumeType,
                status,
                replicaCount,
                stripeCount,
                disperseCount,
                redundancyCount,
                isArbiter,
                options,
                accessProtocols,
                transportTypes,
                bricks,
                asyncTask,
                advancedDetails,
                snapshotsCount,
                snapMaxLimit,
                snapshotScheduled);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof GlusterVolumeEntity)) {
            return false;
        }

        GlusterVolumeEntity other = (GlusterVolumeEntity) obj;
        return Objects.equals(name, other.getName())
                && Objects.equals(clusterId, other.clusterId)
                && volumeType == other.volumeType
                && status == other.status
                && Objects.equals(replicaCount, other.replicaCount)
                && Objects.equals(stripeCount, other.stripeCount)
                && Objects.equals(disperseCount, other.disperseCount)
                && Objects.equals(redundancyCount, other.redundancyCount)
                && Objects.equals(isArbiter, other.isArbiter)
                && ObjectUtils.haveSameElements(getOptions(), other.getOptions())
                && ObjectUtils.haveSameElements(accessProtocols, other.accessProtocols)
                && ObjectUtils.haveSameElements(transportTypes, other.transportTypes)
                && ObjectUtils.haveSameElements(bricks, other.bricks)
                && Objects.equals(asyncTask, other.asyncTask)
                && Objects.equals(advancedDetails, other.advancedDetails)
                && Objects.equals(snapshotsCount, other.snapshotsCount)
                && Objects.equals(snapMaxLimit, other.snapMaxLimit)
                && Objects.equals(snapshotScheduled, other.snapshotScheduled);
    }

    public GlusterBrickEntity getBrickWithId(Guid brickId) {
        for(GlusterBrickEntity brick : getBricks()) {
            if (brick.getId().equals(brickId)) {
                return brick;
            }
        }
        return null;
    }

    public GlusterBrickEntity getBrickWithQualifiedName(String qualifiedName) {
        for (GlusterBrickEntity brick : getBricks()) {
            if (brick.getQualifiedName().equals(qualifiedName)) {
                return brick;
            }
        }

        return null;
    }

    @Override
    public Object getQueryableId() {
        return getId();
    }

    @Override
    public GlusterAsyncTask getAsyncTask() {
       return asyncTask;
    }

    @Override
    public void setAsyncTask(GlusterAsyncTask asyncTask) {
        this.asyncTask = asyncTask;
    }

    public GlusterVolumeAdvancedDetails getAdvancedDetails() {
        return advancedDetails;
    }

    public void setAdvancedDetails(GlusterVolumeAdvancedDetails advancedDetails) {
        this.advancedDetails = advancedDetails;
    }

    public Boolean getIsArbiter() {
        return isArbiter;
    }

    public void setIsArbiter(Boolean isArbiter) {
        this.isArbiter = isArbiter;
    }
}
