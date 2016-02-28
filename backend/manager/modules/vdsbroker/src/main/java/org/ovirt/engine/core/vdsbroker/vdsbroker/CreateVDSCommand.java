package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.vdscommands.CreateVmVDSCommandParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateVDSCommand<P extends CreateVmVDSCommandParameters> extends VmReturnVdsBrokerCommand<P> {

    private static final Logger log = LoggerFactory.getLogger(CreateVDSCommand.class);

    protected VM vm;
    protected Map<String, Object> createInfo;
    protected VmInfoBuilderBase builder;

    public CreateVDSCommand(P parameters) {
        super(parameters, parameters.getVm().getId());
        vm = parameters.getVm();
        createInfo = new HashMap<>();
        builder = createBuilder();
    }

    @Override
    protected void executeVdsBrokerCommand() {
        buildVmData();
        logCommandInfo();
        vmReturn = getBroker().create(createInfo);
        proceedProxyReturnValue();
        VdsBrokerObjectsBuilder.updateVMDynamicData(vm.getDynamicData(),
                vmReturn.vm, getVds());
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
        log.info("{} {}", getClass().getName(), info);
    }

    private VmInfoBuilderBase createBuilder() {
        return new VmInfoBuilder(vm, getParameters().getVdsId(), createInfo);
    }

    private void buildVmData() {
        builder.buildVmProperties();
        builder.buildVmVideoCards();
        builder.buildVmGraphicsDevices();
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
        builder.buildVmHostDevices();
    }
}
