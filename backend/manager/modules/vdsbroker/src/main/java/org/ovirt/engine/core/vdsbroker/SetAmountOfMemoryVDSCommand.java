package org.ovirt.engine.core.vdsbroker;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.common.vdscommands.VdsAndVmIDVDSParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsBrokerCommand;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsProperties;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcStringUtils;

public class SetAmountOfMemoryVDSCommand <P extends SetAmountOfMemoryVDSCommand.Params> extends VdsBrokerCommand<P> {
    public SetAmountOfMemoryVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        try {
            status = getBroker().hotplugMemory(buildData());
            proceedProxyReturnValue();
        } catch (RuntimeException e) {
            setVdsRuntimeError(e);
            // prevent exception handler from rethrowing an exception
            getVDSReturnValue().setExceptionString(null);
        }
    }

    private Map<String, Object> buildData() {
        Map<String, Object> data = new HashMap<>();
        Map<String, Object> memDeviceData = new HashMap<>();
        VmDevice vmDevice = getParameters().getMemoryDevice();
        data.put("vmId", getParameters().getVmId().toString());

        memDeviceData.put(VdsProperties.Type, vmDevice.getType().getValue());
        memDeviceData.put(VdsProperties.Device, vmDevice.getDevice());
        if (StringUtils.isNotBlank(vmDevice.getAddress())) {
            memDeviceData.put(VdsProperties.Address, XmlRpcStringUtils.string2Map(vmDevice.getAddress()));
        }
        memDeviceData.put(VdsProperties.SpecParams, vmDevice.getSpecParams());
        memDeviceData.put(VdsProperties.DeviceId, vmDevice.getId().getDeviceId().toString());

        memDeviceData.put("size", vmDevice.getSpecParams().get("size"));
        memDeviceData.put("node", vmDevice.getSpecParams().get("node"));

        data.put("memory", memDeviceData);
        return data;
    }

    public static class Params extends VdsAndVmIDVDSParametersBase {

        private VmDevice memoryDevice;

        public Params(Guid vdsId, Guid vmId, VmDevice memoryDevice) {
            super(vdsId, vmId);
            this.memoryDevice = memoryDevice;
        }

        public VmDevice getMemoryDevice() {
            return memoryDevice;
        }

        @Override
        protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
            return super.appendAttributes(tsb)
                    .append("memoryDevice", getMemoryDevice());
        }
    }
}
