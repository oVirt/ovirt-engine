package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.utils.VmDeviceCommonUtils;
import org.ovirt.engine.core.common.vdscommands.CreateVmVDSCommandParameters;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcStruct;

public class CreateVDSCommand<P extends CreateVmVDSCommandParameters> extends VmReturnVdsBrokerCommand<P> {
    protected VM vm;
    protected XmlRpcStruct createInfo = new XmlRpcStruct();

    public CreateVDSCommand(P parameters) {
        super(parameters, parameters.getVm().getId());
        vm = parameters.getVm();
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        buildVmData();
        mVmReturn = getBroker().create(createInfo);
        logCommandInfo();
        ProceedProxyReturnValue();
        VdsBrokerObjectsBuilder.updateVMDynamicData(vm.getDynamicData(),
                mVmReturn.mVm);
    }

    /**
     * Logs the command info.
     */
    private void logCommandInfo() {
        final char EQUAL = '=';
        final char SEP = ',';
        StringBuilder info = new StringBuilder();
        String sep = "";
        for (String o : createInfo.getKeys()) {
            info.append(sep);
            info.append(o);
            info.append(EQUAL);
            info.append(createInfo.getItem(o));
            sep = (new Character(SEP)).toString();
        }
        log.infoFormat("{0} {1}", getClass().getName(), info.toString());
    }

    private VmInfoBuilderBase createBuilder() {
        if (VmDeviceCommonUtils.isOldClusterVersion(vm.getvds_group_compatibility_version())) {
            // backward compatibility for 3.0
            return new VmOldInfoBuilder(vm, createInfo);
        } else {
            return new VmInfoBuilder(vm, createInfo);
        }
    }

    private void buildVmData() {
        VmInfoBuilderBase builder = createBuilder();
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
        builder.buildVmTimeZone();
        builder.buildUnmanagedDevices();
    }

    private static Log log = LogFactory.getLog(CreateVDSCommand.class);
}
