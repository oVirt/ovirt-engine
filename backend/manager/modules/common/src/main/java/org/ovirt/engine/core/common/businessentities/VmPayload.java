package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;


public class VmPayload extends VmDevice implements Serializable {
    private static final long serialVersionUID = -3665087594884425768L;
    private static final String SpecParamsPayload = "vmPayload";
    private static final String SpecParamsVolumeIdType = "volId";
    private static final String SpecParamsFileType = "file";

    private String volumeId;
    private Map<String, String> files; // file data is base64-encoded

    // Use the constructor with the vmid if you have it!
    public VmPayload() {
        this.setId(new VmDeviceId(Guid.newGuid(), Guid.newGuid()));
        setDeviceType(VmDeviceType.CDROM);
        this.volumeId = null;
        this.files = new HashMap<>();
    }

    public VmPayload(Guid vmid) {
        this();
        this.setId(new VmDeviceId(Guid.newGuid(), vmid));
    }

    @SuppressWarnings("unchecked")
    public VmPayload(VmDevice dev) {
        super(dev.getId(), dev.getType(), dev.getDevice(),
                dev.getAddress(), dev.getSpecParams(),
                dev.isManaged(), dev.isPlugged(), dev.getReadOnly(),
                dev.getAlias(), dev.getCustomProperties(), dev.getSnapshotId(),
                dev.getLogicalName());

        if (dev.getSpecParams() != null) {
            Map<String, Object> payload = (Map<String, Object>)dev.getSpecParams().get(SpecParamsPayload);
            this.volumeId = (String)payload.get(SpecParamsVolumeIdType);
            this.files = (HashMap<String, String>)payload.get(SpecParamsFileType);
        }
    }

    public static boolean isPayload(Map<String, Object> specParams) {
        return specParams == null ? false : specParams.containsKey(SpecParamsPayload);
    }

    public static boolean isPayloadSizeLegal(String payload) {
        return payload.length() <= Config.<Integer> getValue(ConfigValues.PayloadSize);
    }

    public VmDeviceType getDeviceType() {
        return VmDeviceType.getByName(super.getDevice());
    }

    public void setDeviceType(VmDeviceType type) {
        super.setDevice(type.getName());
    }

    public String getVolumeId() {
        return this.volumeId;
    }

    public void setVolumeId(String volumeId) {
        this.volumeId = volumeId;
    }

    /**
     * Retrieve a map of files in this payload.  The map is always initialized,
     * and can be updated to add/remove files to/from the payload.
     * The key is the file path, and the value is base64-encoded file content.
     *
     * @return Map of files in this payload
     */
    public Map<String, String> getFiles() {
        return files;
    }

    public Map<String, Object> getSpecParams() {
        // function produce something like that:
        // vmPayload={volumeId:volume-id,file:{filename:content,filename2:content2,...}}
        Map<String, Object> specParams = new HashMap<>();
        Map<String, Object> payload = new HashMap<>();

        specParams.put(SpecParamsPayload, payload);
        if (volumeId != null) {
            payload.put(SpecParamsVolumeIdType, volumeId);
        }
        payload.put(SpecParamsFileType, files);

        return specParams;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                super.hashCode(),
                volumeId,
                files
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof VmDevice)) {
            return false;
        }
        VmPayload other = (VmPayload) obj;
        return super.equals(obj)
                && Objects.equals(volumeId, other.volumeId)
                && Objects.equals(files, other.files);
    }
}
