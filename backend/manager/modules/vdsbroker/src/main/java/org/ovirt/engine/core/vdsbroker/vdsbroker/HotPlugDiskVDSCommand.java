package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;

import org.ovirt.engine.core.common.vdscommands.HotPlugDiskVDSParameters;
import org.ovirt.engine.core.utils.XmlUtils;
import org.ovirt.engine.core.vdsbroker.builder.vminfo.LibvirtVmXmlBuilder;
import org.ovirt.engine.core.vdsbroker.builder.vminfo.VmInfoBuildUtils;

public class HotPlugDiskVDSCommand<P extends HotPlugDiskVDSParameters> extends VdsBrokerCommand<P> {
    @Inject
    private VmInfoBuildUtils vmInfoBuildUtils;

    public HotPlugDiskVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        status = getBroker().hotplugDisk(buildSendDataToVdsm());
        proceedProxyReturnValue();
    }

    protected Map<String, Object> buildSendDataToVdsm() {
        Map<String, Object> sendInfo = new HashMap<>();
        sendInfo.put("vmId", getParameters().getVmId().toString());
        try {
            sendInfo.put(VdsProperties.engineXml, generateDomainXml());
        } catch (JAXBException e) {
            log.error("failed to create xml for hot-(un)plug", e);
            throw new RuntimeException(e);
        }
        return sendInfo;
    }

    protected String generateDomainXml() throws JAXBException {
        LibvirtVmXmlBuilder builder = new LibvirtVmXmlBuilder(
                getParameters().getVm(),
                getVds().getId(),
                getParameters().getDisk(),
                getParameters().getVmDevice(),
                vmInfoBuildUtils);
        String libvirtXml = builder.buildHotplugDisk();
        String prettyLibvirtXml = XmlUtils.prettify(libvirtXml);
        if (prettyLibvirtXml != null) {
            log.info("Disk hot-plug: {}", prettyLibvirtXml);
        }
        return libvirtXml;
    }
}
