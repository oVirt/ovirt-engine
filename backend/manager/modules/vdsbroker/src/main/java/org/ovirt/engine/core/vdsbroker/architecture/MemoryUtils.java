package org.ovirt.engine.core.vdsbroker.architecture;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.utils.VmDeviceCommonUtils;
import org.ovirt.engine.core.utils.StringMapUtils;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsProperties;

public class MemoryUtils {

    /**
     * Creates "HotplugMemoryParams" as defined
     * in <a href="https://github.com/oVirt/vdsm/blob/master/lib/vdsm/api/vdsm-api.yml">vdsm-api.yml</a>
     *
     * @param vmDevice memory device
     */
    public static Map<String, Object> createHotplugMemoryParamsMap(VmDevice vmDevice, boolean copyAlias, int minAllocatedMem) {
        Map<String, Object> data = new HashMap<>();
        data.put("vmId", vmDevice.getVmId().toString());
        data.put("memory", createVmMemoryDeviceMap(vmDevice, copyAlias));
        data.put("memGuaranteedSize", minAllocatedMem);
        return data;
    }

    /**
     * Creates "VmMemoryDevice" as defined
     * in <a href="https://github.com/oVirt/vdsm/blob/master/lib/vdsm/api/vdsm-api.yml">vdsm-api.yml</a>
     */
    public static Map<String, Object> createVmMemoryDeviceMap(VmDevice vmDevice, boolean copyAlias) {
        Map<String, Object> memDeviceData = new HashMap<>();

        memDeviceData.put(VdsProperties.Type, vmDevice.getType().getValue());
        memDeviceData.put(VdsProperties.Device, vmDevice.getDevice());
        if (StringUtils.isNotBlank(vmDevice.getAddress())) {
            memDeviceData.put(VdsProperties.Address, StringMapUtils.string2Map(vmDevice.getAddress()));
        }
        memDeviceData.put(VdsProperties.SpecParams, vmDevice.getSpecParams());
        memDeviceData.put(VdsProperties.DeviceId, vmDevice.getId().getDeviceId().toString());
        if (copyAlias) {
            memDeviceData.put(VdsProperties.Alias, vmDevice.getAlias());
        }

        memDeviceData.put("size", VmDeviceCommonUtils.getSizeOfMemoryDeviceMb(vmDevice).orElse(0));
        memDeviceData.put("node",
                VmDeviceCommonUtils.getSpecParamsIntValue(vmDevice, VmDeviceCommonUtils.SPEC_PARAM_NODE)
                        .orElse(0));
        return memDeviceData;
    }
}
