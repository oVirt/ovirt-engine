package org.ovirt.engine.core.vdsbroker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.vdscommands.UpdateVmPolicyVDSParams;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsBrokerCommand;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsProperties;

public class UpdateVmPolicyVDSCommand<P extends UpdateVmPolicyVDSParams> extends VdsBrokerCommand<P> {

    public UpdateVmPolicyVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        status = getBroker().updateVmPolicy(build());
        proceedProxyReturnValue();
    }

    protected Map<String, Object> build() {
        Map<String, Object> struct = new HashMap<>();
        struct.put(VdsProperties.vm_guid, getParameters().getVmId().toString());

        if (getParameters().getCpuLimit() != null) {
            struct.put(VdsProperties.vCpuLimit, String.valueOf(getParameters().getCpuLimit()));
        }

        List<Object> ioTunesList = new ArrayList<>();
        for (UpdateVmPolicyVDSParams.IoTuneParams param : getParameters().getIoTuneList()) {
            Map<String, Object> ioStruct = new HashMap<>();

            ioStruct.put(VdsProperties.DomainId, param.getDomainId());
            ioStruct.put(VdsProperties.PoolId, param.getPoolId());
            ioStruct.put(VdsProperties.ImageId, param.getImageId());
            ioStruct.put(VdsProperties.VolumeId, param.getVolumeId());

            // StorageQos has only one limit value,
            // so sending it to vdsm as maximum and guaranteed
            ioStruct.put(VdsProperties.IoPolicyMaximum, param.getIoTune());
            ioStruct.put(VdsProperties.IoPolicyGuarenteed, param.getIoTune());

            ioTunesList.add(ioStruct);
        }

        if (!ioTunesList.isEmpty()) {
            struct.put(VdsProperties.Iotune, ioTunesList);
        }

        return struct;
    }

}
