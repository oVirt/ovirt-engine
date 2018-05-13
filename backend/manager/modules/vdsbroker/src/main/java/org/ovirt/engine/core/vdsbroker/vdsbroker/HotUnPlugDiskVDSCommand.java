package org.ovirt.engine.core.vdsbroker.vdsbroker;

import javax.inject.Inject;

import org.ovirt.engine.core.common.vdscommands.HotPlugDiskVDSParameters;
import org.ovirt.engine.core.utils.XmlUtils;
import org.ovirt.engine.core.vdsbroker.builder.vminfo.LibvirtVmXmlBuilder;
import org.ovirt.engine.core.vdsbroker.builder.vminfo.VmInfoBuildUtils;

public class HotUnPlugDiskVDSCommand<P extends HotPlugDiskVDSParameters> extends HotPlugDiskVDSCommand<P> {

    @Inject
    private VmInfoBuildUtils vmInfoBuildUtils;

    public HotUnPlugDiskVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        status = getBroker().hotunplugDisk(buildSendDataToVdsm());
        proceedProxyReturnValue();
    }

    @Override
    protected String generateDomainXml() {
        LibvirtVmXmlBuilder builder = new LibvirtVmXmlBuilder(
                getParameters().getVm(),
                getVds().getId(),
                getParameters().getDisk(),
                getParameters().getVmDevice(),
                vmInfoBuildUtils);
        String libvirtXml = builder.buildHotunplugDisk();
        String prettyLibvirtXml = XmlUtils.prettify(libvirtXml);
        if (prettyLibvirtXml != null) {
            log.info("Disk hot-unplug: {}", prettyLibvirtXml);
        }
        return libvirtXml;
    }
}
