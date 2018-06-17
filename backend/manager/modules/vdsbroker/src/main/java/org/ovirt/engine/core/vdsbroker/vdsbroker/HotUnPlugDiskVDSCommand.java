package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.vdscommands.HotPlugDiskVDSParameters;
import org.ovirt.engine.core.utils.XmlUtils;
import org.ovirt.engine.core.vdsbroker.libvirt.Hotunplug;
import org.ovirt.engine.core.vdsbroker.libvirt.Hotunplug.Device;
import org.ovirt.engine.core.vdsbroker.libvirt.Hotunplug.Devices;

public class HotUnPlugDiskVDSCommand<P extends HotPlugDiskVDSParameters> extends HotPlugDiskVDSCommand<P> {

    public HotUnPlugDiskVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        status = getBroker().hotunplugDisk(buildSendDataToVdsm());
        proceedProxyReturnValue();
    }

    @Override
    protected String generateDomainXml() throws JAXBException {
        Marshaller jaxbMarshaller = JAXBContext.newInstance(Hotunplug.class).createMarshaller();
        VmDevice device = getParameters().getVmDevice();
        Hotunplug hotunplug = new Hotunplug().setDevices(new Devices().setDisk(new Device(device)));
        StringWriter sw = new StringWriter();
        jaxbMarshaller.marshal(hotunplug, sw);
        String libvirtXml = sw.toString();
        String prettyLibvirtXml = XmlUtils.prettify(libvirtXml);
        if (prettyLibvirtXml != null) {
            log.info("Disk hot-unplug: {}", prettyLibvirtXml);
        }
        return libvirtXml;
    }
}
