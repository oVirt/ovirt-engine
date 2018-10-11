package org.ovirt.engine.core.common.businessentities.gluster;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class GlusterLocalVolumeInfo {
    private static class ThinSize {
        Long free;
        Long size;
        Integer savings;

        private ThinSize() { }

        ThinSize(Long free, Long size, Integer savings) {
            this.free = free;
            this.size = size;
            this.savings = savings;
        }
    }

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
        return getThinSizeForDevice(device).map(s -> s.free);
    }

    public Optional<Long> getTotalThinSizeForDevice(String device) {
        return getThinSizeForDevice(device).map(s -> s.size);
    }

    public Optional<Integer> getSavingsForDevice(String device) {
        return getThinSizeForDevice(device).map(s -> s.savings);
    }

    private Optional<ThinSize> getThinSizeForDevice(String device) {
        Optional<ThinSize> result = getLvmSizeForDevice(device);
        if (!result.isPresent()) {
            result = getVdoSizeForDevice(device);
        }
        return result;
    }

    private Optional<ThinSize> getLvmSizeForDevice(String device) {
        Optional<GlusterLocalLogicalVolume> thinVolume = logicalVolumes.stream()
                .filter(lvmMatchMapperName(device))
                .filter(v -> !v.getPoolName().trim().isEmpty())
                .findAny();

        thinVolume = thinVolume.flatMap(v -> logicalVolumes.stream()
                .filter(lv -> v.getPoolName().equals(lv.getLogicalVolumeName()))
                .findAny());

        Optional<ThinSize> result = thinVolume.map(v -> new ThinSize(v.getFree(), v.getSize(), 0));

        Optional<GlusterLocalLogicalVolume> thisVolume = logicalVolumes.stream()
                .filter(lvmMatchMapperName(device))
                .findAny();

        List<String> physicalDevices = thinVolume.map(Optional::of).orElse(thisVolume)
                .map(GlusterLocalLogicalVolume::getVolumeGroupName)
                .map(v -> physicalVolumes.stream()
                        .filter(g -> g.getVolumeGroupName().equals(v))
                        .map(GlusterLocalPhysicalVolume::getPhysicalVolumeName)
                        .collect(Collectors.toList())
                ).orElseGet(Collections::emptyList);
        if (physicalDevices.size() != 1) {
            //We can not handle other number of physical devices
            return result;
        }
        Optional<ThinSize> innerResult = getThinSizeForDevice(physicalDevices.get(0));

        if (innerResult.isPresent()) {
            if (result.isPresent()) {
                result = result.map(chooseSmallest(innerResult));
            } else {
                result = innerResult;
            }
        }

        return result;
    }

    private Optional<ThinSize> getVdoSizeForDevice(String device) {
        Optional<GlusterVDOVolume> vdoVolume = vdoVolumes.stream()
                .filter(v -> device.equals(v.getName()))
                .findAny();

        Optional<ThinSize> result = vdoVolume.map(v -> new ThinSize(v.getFree(), v.getSize(), getVdoSavings(v)));
        Optional<ThinSize> innerResult = vdoVolume.flatMap(v -> getThinSizeForDevice(v.getDevice()));

        if (innerResult.isPresent()) {
            result = result.map(chooseSmallest(innerResult));
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

    private Integer getVdoSavings(GlusterVDOVolume v) {
        if ( v.getLogicalBlocks() == 0 || v.getPhysicalBlocks() == 0) {
            return 0;
        }
        return (int)(((v.getLogicalBlocks() - v.getPhysicalBlocks())/v.getLogicalBlocks().doubleValue())*100);
    }

    private Function<ThinSize, ThinSize> chooseSmallest(Optional<ThinSize> innerResult) {
        return s -> {
            if (s.free > innerResult.get().free) {
                return new ThinSize(innerResult.get().free, innerResult.get().size, innerResult.get().savings != 0 ? innerResult.get().savings : s.savings);
            } else {
                return s;
            }
        };
    }

}
