package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.utils.VmDeviceCommonUtils;
import org.ovirt.engine.core.common.vdscommands.CreateVmVDSCommandParameters;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public class CreateVDSCommand<P extends CreateVmVDSCommandParameters> extends VmReturnVdsBrokerCommand<P> {
    protected VM vm;
    protected Map<String, Object> createInfo;
    protected VmInfoBuilderBase builder;

    public CreateVDSCommand(P parameters) {
        super(parameters, parameters.getVm().getId());
        vm = parameters.getVm();
        createInfo = new HashMap<String, Object>();
        builder = createBuilder();
    }

    @Override
    protected void executeVdsBrokerCommand() {
        buildVmData();
        logCommandInfo();
        mVmReturn = getBroker().create(createInfo);
        proceedProxyReturnValue();
        VdsBrokerObjectsBuilder.updateVMDynamicData(vm.getDynamicData(),
                mVmReturn.mVm);
    }

    /**
     * Logs the command info.
     */
    private void logCommandInfo() {
        final char EQUAL = '=';
        final String SEP = ",";
        StringBuilder info = new StringBuilder();
        String sep = "";
        for (Map.Entry<String, Object> createInfoEntry : createInfo.entrySet()) {
            info.append(sep);
            info.append(createInfoEntry.getKey());
            info.append(EQUAL);
            info.append(createInfoEntry.getValue());
            sep = SEP;
        }
        log.infoFormat("{0} {1}", getClass().getName(), info.toString());
    }

    private VmInfoBuilderBase createBuilder() {
        if (VmDeviceCommonUtils.isOldClusterVersion(vm.getVdsGroupCompatibilityVersion())) {
            // backward compatibility for 3.0
            return new VmOldInfoBuilder(vm, createInfo);
        } else {
            return new VmInfoBuilder(vm, getParameters().getVdsId(), createInfo);
        }
    }

    private void buildVmData() {
        builder.buildVmProperties();
        builder.buildVmVideoCards();
        builder.buildVmCD();
        builder.buildVmFloppy();
        builder.buildVmDrives();
        builder.buildVmNetworkInterfaces();
        builder.buildVmNetworkCluster();
        builder.buildVmBootSequence();
        builder.buildVmBootOptions();
        builder.buildVmSoundDevices();
        builder.buildVmConsoleDevice();
        builder.buildVmTimeZone();
        builder.buildVmUsbDevices();
        builder.buildVmMemoryBalloon();
        builder.buildVmWatchdog();
        builder.buildVmVirtioScsi();
        builder.buildVmVirtioSerial();
        builder.buildVmRngDevice();
        builder.buildUnmanagedDevices();
        builder.buildVmSerialNumber();
        builder.buildVmNumaProperties();
    }

    private static final Log log = LogFactory.getLog(CreateVDSCommand.class);
}
