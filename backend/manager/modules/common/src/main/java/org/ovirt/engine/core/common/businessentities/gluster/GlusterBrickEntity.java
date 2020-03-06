package org.ovirt.engine.core.common.businessentities.gluster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.common.asynctasks.gluster.GlusterAsyncTask;
import org.ovirt.engine.core.common.businessentities.BusinessEntityWithStatus;
import org.ovirt.engine.core.common.businessentities.Nameable;
import org.ovirt.engine.core.common.businessentities.Queryable;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.gluster.AddBrick;
import org.ovirt.engine.core.common.validation.group.gluster.RemoveBrick;
import org.ovirt.engine.core.compat.Guid;

/**
 * Brick is the building block of a Gluster Volume. It represents a directory on one of the servers of the cluster, and
 * is typically represented in the form <b>serverName:brickDirectory</b><br>
 * It also has a status (ONLINE / OFFLINE) which represents the status of the brick process that runs on the server to
 * which the brick belongs.
 *
 * @see GlusterVolumeEntity
 * @see GlusterBrickStatus
 */
public class GlusterBrickEntity implements Queryable, BusinessEntityWithStatus<Guid, GlusterStatus>, GlusterTaskSupport, Nameable {
    private static final long serialVersionUID = 7119439284741452278L;

    @NotNull(message = "VALIDATION_GLUSTER_BRICK_ID_NOT_NULL", groups = RemoveBrick.class)
    private Guid id;

    @NotNull(message = "VALIDATION_GLUSTER_VOLUME_ID_NOT_NULL", groups = AddBrick.class)
    private Guid volumeId;

    private String volumeName;

    @NotNull(message = "VALIDATION_GLUSTER_VOLUME_BRICK_SERVER_ID_NOT_NULL", groups = CreateEntity.class)
    private Guid serverId;

    private Guid networkId;

    private String networkAddress;

    private String serverName;

    @NotNull(message = "VALIDATION_GLUSTER_VOLUME_BRICK_BRICK_DIR_NOT_NULL", groups = CreateEntity.class)
    private String brickDirectory;

    private GlusterStatus status;

    private Integer brickOrder;

    private BrickDetails brickDetails;

    private GlusterAsyncTask asyncTask;

    private Integer unSyncedEntries;

    private List<Integer> unSyncedEntriesTrend;

    private Double selfHealEta;

    private Boolean isArbiter;

    public GlusterBrickEntity() {
        status = GlusterStatus.DOWN;
        asyncTask = new GlusterAsyncTask();
        unSyncedEntriesTrend = Collections.emptyList();
        selfHealEta = 0D;
        isArbiter = Boolean.FALSE;
    }

    public Guid getVolumeId() {
        return volumeId;
    }

    public void setVolumeId(Guid volumeId) {
        this.volumeId = volumeId;
    }

    public Guid getServerId() {
        return serverId;
    }

    public void setServerId(Guid serverId) {
        this.serverId = serverId;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public void setBrickDirectory(String brickDirectory) {
        this.brickDirectory = brickDirectory;
    }

    public String getBrickDirectory() {
        return brickDirectory;
    }

    public Guid getNetworkId() {
        return networkId;
    }

    public void setNetworkId(Guid networkId) {
        this.networkId = networkId;
    }

    public String getNetworkAddress() {
        return networkAddress;
    }

    public void setNetworkAddress(String networkAddress) {
        this.networkAddress = networkAddress;
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
        return status == GlusterStatus.UP;
    }

    public String getQualifiedName() {
        if (networkId != null && networkAddress != null && !networkAddress.isEmpty()) {
            return networkAddress + ":" + brickDirectory;
        }
        return serverName + ":" + brickDirectory;
    }

    @Override
    public String toString() {
        return getQualifiedName() + "(" + serverName + ")";
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                volumeId,
                serverId,
                brickDirectory,
                brickOrder,
                status,
                asyncTask,
                unSyncedEntries,
                unSyncedEntriesTrend,
                selfHealEta,
                isArbiter
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof GlusterBrickEntity)) {
            return false;
        }

        GlusterBrickEntity other = (GlusterBrickEntity) obj;
        return Objects.equals(id, other.id)
                && Objects.equals(volumeId, other.volumeId)
                && Objects.equals(serverId, other.serverId)
                && Objects.equals(brickDirectory, other.brickDirectory)
                && Objects.equals(brickOrder, other.brickOrder)
                && Objects.equals(asyncTask, other.asyncTask)
                && Objects.equals(unSyncedEntries, other.unSyncedEntries)
                && Objects.equals(unSyncedEntriesTrend, other.unSyncedEntriesTrend)
                && Objects.equals(selfHealEta, other.selfHealEta)
                && status == other.status
                && Objects.equals(isArbiter, other.isArbiter);
    }

    public void copyFrom(GlusterBrickEntity brick) {
        setId(brick.getId());
        setVolumeId(brick.getVolumeId());
        setServerId(brick.getServerId());
        setServerName(brick.getServerName());
        setBrickDirectory(brick.getBrickDirectory());
        setBrickOrder(brick.getBrickOrder());
        setUnSyncedEntries(brick.unSyncedEntries);
        setUnSyncedEntriesTrend(brick.getUnSyncedEntriesTrend());
        setStatus(brick.getStatus());
        setIsArbiter(brick.getIsArbiter());
    }

    /**
     * Generates the id if not present. Volume brick doesn't have an id in
     * GlusterFS, and hence is generated on the backend side.
     * @return id of the brick
     */
    @Override
    public Guid getId() {
        return getId(true);
    }

    public Guid getId(boolean generateIfNull) {
        if(id == null && generateIfNull) {
            id = Guid.newGuid();
        }
        return id;
    }

    @Override
    public void setId(Guid id) {
        this.id = id;
    }

    @Override
    public Object getQueryableId() {
        return getId();
    }

    public Integer getBrickOrder() {
        return brickOrder;
    }

    public void setBrickOrder(Integer brickOrder) {
        this.brickOrder = brickOrder;
    }

    public BrickDetails getBrickDetails() {
        return brickDetails;
    }

    public void setBrickDetails(BrickDetails brickDetails) {
        this.brickDetails = brickDetails;
    }

    public BrickProperties getBrickProperties() {
        if (brickDetails != null) {
            return brickDetails.getBrickProperties();
        }
        return null;
    }

    @Override
    public GlusterAsyncTask getAsyncTask() {
       return asyncTask;
    }

    @Override
    public void setAsyncTask(GlusterAsyncTask asyncTask) {
        this.asyncTask = asyncTask;
    }

    public String getVolumeName() {
        return volumeName;
    }

    public void setVolumeName(String volumeName) {
        this.volumeName = volumeName;
    }

    @Override
    public String getName() {
        return getQualifiedName();
    }

    public List<Integer> getUnSyncedEntriesTrend() {
        return unSyncedEntriesTrend;
    }

    public void setUnSyncedEntriesTrend(List<Integer> unSyncedEntriesTrend) {
        this.unSyncedEntriesTrend = unSyncedEntriesTrend;
        setSelfHealEta(calculateSelfHealEta());
    }

    public Integer getUnSyncedEntries() {
        return unSyncedEntries;
    }

    public void setUnSyncedEntries(Integer unSyncedEntries) {
        this.unSyncedEntries = unSyncedEntries;
    }

    public Double getSelfHealEta() {
        return selfHealEta;
    }

    private void setSelfHealEta(Double value) {
        this.selfHealEta = value;
    }

    private Double calculateSelfHealEta() {
        if (this.getUnSyncedEntries() == null || this.getUnSyncedEntriesTrend() == null
                || this.getUnSyncedEntriesTrend().size() < 2) {
            return 0D;
        }

        // Calculate Heal rate between each entries in the unsynced entries list and calculate the average heal rate.
        List<Double> healRates = new ArrayList<>();
        for (int index = 0; index < this.getUnSyncedEntriesTrend().size() - 1; index++) {
            Integer entries = this.unSyncedEntriesTrend.get(index);
            Integer entriesRemaining = this.unSyncedEntriesTrend.get(index + 1);
            // -1 is added when fetching heal info fails. We can ignore them for heal rate calculation.
            if (entries >= 0 && entriesRemaining >= 0) {
                double healRate = ((double) entries - entriesRemaining)
                        / (Config.<Integer> getValue(ConfigValues.GlusterRefreshRateHealInfo));
                if (healRate > 0) {
                healRates.add(healRate);
                }
            }
        }

        if (healRates.isEmpty()) {
            return 0D;
        }

        Double healRateSum = 0D;
        for (Double healRate : healRates) {
            healRateSum += healRate;
        }
        Double healRateAvg = healRateSum / healRates.size();
        return this.getUnSyncedEntries() / healRateAvg;
    }

    public Boolean getIsArbiter() {
        return isArbiter;
    }

    public void setIsArbiter(Boolean isArbiter) {
        this.isArbiter = isArbiter;
    }
}
