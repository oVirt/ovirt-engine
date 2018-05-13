package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Collections;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.vdscommands.VmNicDeviceVDSParameters;
import org.ovirt.engine.core.utils.XmlUtils;
import org.ovirt.engine.core.vdsbroker.builder.vminfo.LibvirtVmXmlBuilder;
import org.ovirt.engine.core.vdsbroker.builder.vminfo.VmInfoBuildUtils;

public class HotPlugNicVDSCommand<P extends VmNicDeviceVDSParameters> extends HotPlugOrUnplugNicVDSCommand<P> {

    @Inject
    private VmInfoBuildUtils vmInfoBuildUtils;

    private VmInfoReturn result;

    public HotPlugNicVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        result = getBroker().hotPlugNic(createParametersStruct());
        proceedProxyReturnValue();
        setReturnValue(result);
     }

     @Override
     protected Status getReturnStatus() {
         return result.getStatus();
     }

     @Override
     protected Object getReturnValueFromBroker() {
         return result;
     }

     @Override
     protected String generateDomainXml() {
         VmNic nic = getParameters().getNic();
         VmDevice vmDevice = getParameters().getVmDevice();
         LibvirtVmXmlBuilder builder = new LibvirtVmXmlBuilder(
                 getParameters().getVm(),
                 getVds().getId(),
                 nic,
                 vmDevice,
                 vmInfoBuildUtils,
                 nic.isPassthrough() ?
                         Collections.singletonMap(nic.getId(), vmDevice.getHostDevice())
                         : Collections.emptyMap());
         String libvirtXml = builder.buildHotplugNic();
         String prettyLibvirtXml = XmlUtils.prettify(libvirtXml);
         if (prettyLibvirtXml != null) {
             log.info("NIC hot-plug: {}", prettyLibvirtXml);
         }
         return libvirtXml;
     }
}
