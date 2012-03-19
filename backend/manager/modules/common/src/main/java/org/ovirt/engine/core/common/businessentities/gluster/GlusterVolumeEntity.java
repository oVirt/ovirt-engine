package org.ovirt.engine.core.common.businessentities.gluster;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.constants.gluster.GlusterConstants;
import org.ovirt.engine.core.common.utils.gluster.GlusterCoreUtil;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;

/**
 * The gluster volume entity. This is a logical partition within the virtual storage space provided by a Gluster
 * Cluster. It is made up of multiple bricks (server:brickDirectory) across multiple servers of the cluster.
 *
 * @see GlusterVolumeType
 * @see TransportType
 * @see GlusterVolumeStatus
 * @see GlusterBrickEntity
 * @see GlusterBrickStatus
 * @see GlusterVolumeOption
 * @see AccessProtocol
 */
public class GlusterVolumeEntity extends IVdcQueryable implements BusinessEntity<Guid> {
    private static final long serialVersionUID = 2355384696827317277L;

    private Guid id;
    private Guid clusterId;
    private String name;

    private GlusterVolumeType volumeType = GlusterVolumeType.DISTRIBUTE;
    private TransportType transportType = TransportType.ETHERNET;
    private GlusterVolumeStatus status = GlusterVolumeStatus.DOWN;

    private int replicaCount;
    private int stripeCount;

    private final Map<String, GlusterVolumeOption> options = new LinkedHashMap<String, GlusterVolumeOption>();
    private List<GlusterBrickEntity> bricks = new ArrayList<GlusterBrickEntity>();

    public GlusterVolumeEntity() {
    }

    // Gluster and NFS are enabled by default
    private Set<AccessProtocol> accessProtocols = new LinkedHashSet<AccessProtocol>(Arrays.asList(new AccessProtocol[] {
            AccessProtocol.GLUSTER, AccessProtocol.NFS }));

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

    public GlusterVolumeType getVolumeType() {
        return volumeType;
    }

    public void setVolumeType(GlusterVolumeType volumeType) {
        this.volumeType = volumeType;

        switch (volumeType) {
        case DISTRIBUTE:
            setReplicaCount(0);
            setStripeCount(0);
            break;
        case REPLICATE:
        case DISTRIBUTED_REPLICATE:
            setStripeCount(0);
            break;
        case STRIPE:
        case DISTRIBUTED_STRIPE:
            setReplicaCount(0);
            break;
        }
    }

    public void setVolumeType(String volumeType) {
        setVolumeType(GlusterVolumeType.valueOf(volumeType));
    }

    public TransportType getTransportType() {
        return transportType;
    }

    public void setTransportType(TransportType transportType) {
        this.transportType = transportType;
    }

    public void setTransportType(String transportType) {
        setTransportType(TransportType.valueOf(transportType));
    }

    public GlusterVolumeStatus getStatus() {
        return status;
    }

    public void setStatus(GlusterVolumeStatus status) {
        this.status = status;
    }

    public boolean isOnline() {
        return this.status == GlusterVolumeStatus.UP;
    }

    public int getReplicaCount() {
        return replicaCount;
    }

    public void setReplicaCount(int replicaCount) {
        this.replicaCount = replicaCount;
    }

    public int getStripeCount() {
        return stripeCount;
    }

    public void setStripeCount(int stripeCount) {
        this.stripeCount = stripeCount;
    }

    public Set<AccessProtocol> getAccessProtocols() {
        return accessProtocols;
    }

    public void setAccessProtocols(Set<AccessProtocol> accessProtocols) {
        this.accessProtocols = accessProtocols;
    }

    /**
     * Sets a single access protocol, removing other if set already
     *
     * @param protocol
     */
    public void setAccessProtocol(AccessProtocol protocol) {
        accessProtocols = new HashSet<AccessProtocol>();
        addAccessProtocol(protocol);
    }

    public void addAccessProtocol(AccessProtocol protocol) {
        accessProtocols.add(protocol);
    }

    public void removeAccessProtocol(AccessProtocol protocol) {
        accessProtocols.remove(protocol);
    }

    /**
     * Sets access protocols from comma separated list. Each element must be a string representation of valid values of
     * the enum {@code AccessProtocol}
     *
     * @param accessProtocols
     * @see AccessProtocol
     */
    public void setAccessProtocols(String accessProtocols) {
        if (accessProtocols == null || accessProtocols.trim().isEmpty()) {
            this.accessProtocols = null;
            return;
        }

        this.accessProtocols = new HashSet<AccessProtocol>();
        String[] accessProtocolList = accessProtocols.split(",", -1);
        for (String accessProtocol : accessProtocolList) {
            addAccessProtocol(AccessProtocol.valueOf(accessProtocol.trim()));
        }
    }

    public String getAccessControlList() {
        return getOptionValue(GlusterConstants.OPTION_AUTH_ALLOW);
    }

    public void setAccessControlList(String accessControlList) {
        if (!StringHelper.isNullOrEmpty(accessControlList)) {
            setOption(GlusterConstants.OPTION_AUTH_ALLOW, accessControlList);
        }
    }

    public Collection<GlusterVolumeOption> getOptions() {
        return options.values();
    }

    /**
     * Returns value of given option key as set on the volume. <br>
     * In case the option is not set, <code>null</code> will be returned.
     */
    public String getOptionValue(String optionKey) {
        GlusterVolumeOption option = options.get(optionKey);
        if (option == null) {
            return null;
        }
        return option.getValue();
    }

    private void setOption(GlusterVolumeOption option) {
        options.put(option.getKey(), option);
    }

    public void setOption(String key, String value) {
        if (options.containsKey(key)) {
            options.get(key).setValue(value);
        } else {
            options.put(key, new GlusterVolumeOption(id, key, value));
        }
    }

    /**
     * Sets options from a comma separated list of key value pairs separated by = <br>
     * e.g. key=val1,key2=val2,...,keyn=valn
     *
     * @param options
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

    public void setOptions(Collection<GlusterVolumeOption> options) {
        this.options.clear();
        for (GlusterVolumeOption option : options) {
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

    public void removeBrick(GlusterBrickEntity GlusterBrick) {
        bricks.remove(GlusterBrick);
    }

    /**
     * Replaces an existing brick in the volume with the given new brick. The new brick will have same index as the
     * existing one.
     *
     * @param existingBrick
     * @param newBrick
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
        return (nfsDisabled == null || nfsDisabled.equalsIgnoreCase(GlusterConstants.OFF));
    }

    public void enableCifs() {
        accessProtocols.add(AccessProtocol.CIFS);
    }

    public void disableCifs() {
        accessProtocols.remove(AccessProtocol.CIFS);
    }

    public boolean isCifsEnabled() {
        return accessProtocols.contains(AccessProtocol.CIFS);
    }

    public List<String> getBrickDirectories() {
        List<String> brickDirectories = new ArrayList<String>();
        for (GlusterBrickEntity brick : getBricks()) {
            brickDirectories.add(brick.getQualifiedName());
        }
        return brickDirectories;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((clusterId == null) ? 0 : clusterId.hashCode());
        result = prime * result + ((volumeType == null) ? 0 : volumeType.hashCode());
        result = prime * result + ((transportType == null) ? 0 : transportType.hashCode());
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        result = prime * result + replicaCount;
        result = prime * result + stripeCount;
        result = prime * result + ((options == null) ? 0 : options.hashCode());
        result = prime * result + ((accessProtocols == null) ? 0 : accessProtocols.hashCode());
        result = prime * result + ((bricks == null) ? 0 : bricks.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GlusterVolumeEntity)) {
            return false;
        }

        GlusterVolumeEntity volume = (GlusterVolumeEntity) obj;

        if (!(clusterId.equals(volume.getClusterId()))) {
            return false;
        }

        if (!(name.equals(volume.getName()) && volumeType == volume.getVolumeType()
                && transportType == volume.getTransportType() && status == volume.getStatus()
                && replicaCount == volume.getReplicaCount()
                && stripeCount == volume.getStripeCount())) {
            return false;
        }

        if (!GlusterCoreUtil.listsEqual(getOptions(), volume.getOptions())) {
            return false;
        }

        if (!GlusterCoreUtil.listsEqual(accessProtocols, volume.getAccessProtocols())) {
            return false;
        }

        if (!GlusterCoreUtil.listsEqual(bricks, volume.getBricks())) {
            return false;
        }

        return true;
    }

    @Override
    public Object getQueryableId() {
        return getId();
    }
}
