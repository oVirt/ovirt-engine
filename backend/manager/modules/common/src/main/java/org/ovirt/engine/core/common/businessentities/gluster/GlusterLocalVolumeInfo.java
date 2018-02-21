package org.ovirt.engine.core.common.businessentities.gluster;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class GlusterLocalVolumeInfo {
    private List<GlusterLocalLogicalVolume> logicalVolumes = Collections.emptyList();
    private List<GlusterLocalPhysicalVolume> physicalVolumes = Collections.emptyList();
    private List<GlusterVDOVolume> vdoVolumes = Collections.emptyList();

    public void setLogicalVolumes(List<GlusterLocalLogicalVolume> logicalVolumes) {
        this.logicalVolumes = logicalVolumes;
    }

    public void setPhysicalVolumes(List<GlusterLocalPhysicalVolume> physicalVolumes) {
        this.physicalVolumes = physicalVolumes;
    }

    public void setVdoVolumes(List<GlusterVDOVolume> vdoVolumes) {
        this.vdoVolumes = vdoVolumes;
    }

    public Optional<Long> getAvailableThinSizeForDevice(String device) {
        Optional<Long> result = getLvmSizeForDevice(device);
        if (!result.isPresent()) {
            result = getVdoSizeForDevice(device);
        }
        return result;
    }

    private Optional<Long> getLvmSizeForDevice(String device) {
        Optional<GlusterLocalLogicalVolume> thinVolume = logicalVolumes.stream()
                .filter(lvmMatchMapperName(device))
                .filter(v -> !v.getPoolName().trim().isEmpty())
                .findAny();

        thinVolume = thinVolume.flatMap(v -> logicalVolumes.stream()
                .filter(lv -> v.getPoolName().equals(lv.getLogicalVolumeName()))
                .findAny());

        Optional<Long> result = thinVolume.map(GlusterLocalLogicalVolume::getFree);
        List<String> physicalDevices = thinVolume.map(GlusterLocalLogicalVolume::getVolumeGroupName)
                .map(v -> physicalVolumes.stream()
                        .filter(g -> g.getVolumeGroupName().equals(v))
                        .map(GlusterLocalPhysicalVolume::getPhysicalVolumeName)
                        .collect(Collectors.toList())
                ).orElseGet(Collections::emptyList);
        if (physicalDevices.size() != 1) {
            //We can not handle other number of physical devices
            return result;
        }
        Optional<Long> innerResult = getAvailableThinSizeForDevice(physicalDevices.get(0));

        if (innerResult.isPresent()) {
            result = result.map(s -> s > innerResult.get() ? innerResult.get() : s);
        }

        return result;
    }

    private Optional<Long> getVdoSizeForDevice(String device) {
        Optional<GlusterVDOVolume> vdoVolume = vdoVolumes.stream()
                .filter(v -> device.equals(v.getName()))
                .findAny();

        Optional<Long> result = vdoVolume.map(GlusterVDOVolume::getFree);
        Optional<Long> innerResult = vdoVolume.flatMap(v -> getAvailableThinSizeForDevice(v.getDevice()));

        if (innerResult.isPresent()) {
            result = result.map(s -> s > innerResult.get() ? innerResult.get() : s);
        }

        return result;
    }

    private Predicate<GlusterLocalLogicalVolume> lvmMatchMapperName(String device) {
        return volume -> {
            String mapperDevice = String.format("/dev/mapper/%s-%s", volume.getVolumeGroupName(), volume.getLogicalVolumeName());
            String lvmDevice = String.format("/dev/%s/%s", volume.getVolumeGroupName(), volume.getLogicalVolumeName());
            return device.equals(mapperDevice) || device.equals(lvmDevice);
        };
    }
}
