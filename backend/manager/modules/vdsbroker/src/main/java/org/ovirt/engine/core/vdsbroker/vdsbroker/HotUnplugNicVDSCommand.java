package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Collections;

import javax.inject.Inject;

import org.ovirt.engine.core.common.vdscommands.VmNicDeviceVDSParameters;
import org.ovirt.engine.core.utils.XmlUtils;
import org.ovirt.engine.core.vdsbroker.builder.vminfo.LibvirtVmXmlBuilder;
import org.ovirt.engine.core.vdsbroker.builder.vminfo.VmInfoBuildUtils;

public class HotUnplugNicVDSCommand<P extends VmNicDeviceVDSParameters> extends HotPlugOrUnplugNicVDSCommand<P> {

    @Inject
    private VmInfoBuildUtils vmInfoBuildUtils;

    public HotUnplugNicVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        status = getBroker().hotUnplugNic(createParametersStruct());
        proceedProxyReturnValue();
    }

    @Override
    protected String generateDomainXml() {
        LibvirtVmXmlBuilder builder = new LibvirtVmXmlBuilder(
                getParameters().getVm(),
                getVds().getId(),
                getParameters().getNic(),
                getParameters().getVmDevice(),
                vmInfoBuildUtils,
                Collections.emptyMap());
        String libvirtXml = builder.buildHotunplugNic();
        String prettyLibvirtXml = XmlUtils.prettify(libvirtXml);
        if (prettyLibvirtXml != null) {
            log.info("NIC hot-unplug: {}", prettyLibvirtXml);
        }
        return libvirtXml;
    }
}
