package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.vdscommands.CreateVDSCommandParameters;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.vdsbroker.builder.vminfo.VmInfoBuilder;
import org.ovirt.engine.core.vdsbroker.builder.vminfo.VmInfoBuilderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateBrokerVDSCommand<P extends CreateVDSCommandParameters> extends VmReturnVdsBrokerCommand<P> {

    private static final Logger log = LoggerFactory.getLogger(CreateBrokerVDSCommand.class);

    protected VM vm;
    protected Map<String, Object> createInfo;
    protected VmInfoBuilder builder;

    public CreateBrokerVDSCommand(P parameters) {
        super(parameters, parameters.getVm().getId());
        vm = parameters.getVm();
        createInfo = new HashMap<>();
        builder = createBuilder();
    }

    @Override
    protected void executeVdsBrokerCommand() {
        buildVmData();
        log.info("VM {}", createInfo);
        vmReturn = getBroker().create(createInfo);
        proceedProxyReturnValue();
        VdsBrokerObjectsBuilder.updateVMDynamicData(vm.getDynamicData(),
                vmReturn.vm, getVds());
    }

    private VmInfoBuilder createBuilder() {
        final VmInfoBuilderFactory vmInfoBuilderFactory = Injector.get(VmInfoBuilderFactory.class);
        return vmInfoBuilderFactory.createVmInfoBuilder(vm, getParameters().getVdsId(), createInfo);
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
