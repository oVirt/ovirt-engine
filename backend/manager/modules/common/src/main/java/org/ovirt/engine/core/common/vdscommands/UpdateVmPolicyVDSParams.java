package org.ovirt.engine.core.common.vdscommands;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.compat.Guid;

public class UpdateVmPolicyVDSParams extends VdsAndVmIDVDSParametersBase {

    private Integer cpuLimit;
    private List<IoTuneParams> ioTuneList = new ArrayList<>();

    public UpdateVmPolicyVDSParams() {
    }

    public UpdateVmPolicyVDSParams(Guid vdsId, Guid vmId, Integer cpuLimit) {
        super(vdsId, vmId);
        this.cpuLimit = cpuLimit;
    }

    public Integer getCpuLimit() {
        return cpuLimit;
    }

    public void setCpuLimit(Integer cpuLimit) {
        this.cpuLimit = cpuLimit;
    }

    public List<IoTuneParams> getIoTuneList() {
        return ioTuneList;
    }

    public void setIoTuneList(List<IoTuneParams> ioTuneList) {
        this.ioTuneList = ioTuneList;
    }

    public void addIoTuneParams(IoTuneParams params) {
        ioTuneList.add(params);
    }

    public void addIoTuneParams(String domainId, String poolId,
            String imageId, String volumeId,
            Map<String, Long> ioTune) {
        addIoTuneParams(new IoTuneParams(domainId, poolId, imageId, volumeId, ioTune));
    }

    public void addIoTuneParams(DiskImage diskImage, Map<String, Long> ioTune) {
        String domainId = diskImage.getStorageIds().get(0).toString();
        String poolId = diskImage.getStoragePoolId().toString();
        String imageId = diskImage.getId().toString();
        String volumeId = diskImage.getImageId().toString();

        addIoTuneParams(domainId, poolId, imageId, volumeId, ioTune);
    }

    public static class IoTuneParams {
        private String domainId;
        private String poolId;
        private String imageId;
        private String volumeId;
        private Map<String, Long> ioTune;

        public IoTuneParams() {
        }

        public IoTuneParams(String domainId, String poolId,
                String imageId, String volumeId,
                Map<String, Long> ioTune) {
            this.domainId = domainId;
            this.poolId = poolId;
            this.imageId = imageId;
            this.volumeId = volumeId;
            this.ioTune = ioTune;
        }

        public String getDomainId() {
            return domainId;
        }

        public String getImageId() {
            return imageId;
        }

        public String getPoolId() {
            return poolId;
        }

        public String getVolumeId() {
            return volumeId;
        }

        public Map<String, Long> getIoTune() {
            return ioTune;
        }
    }
}
