package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.vdscommands.VmNicDeviceVDSParameters;
import org.ovirt.engine.core.utils.XmlUtils;
import org.ovirt.engine.core.vdsbroker.libvirt.Hotunplug;
import org.ovirt.engine.core.vdsbroker.libvirt.Hotunplug.Device;
import org.ovirt.engine.core.vdsbroker.libvirt.Hotunplug.Devices;

public class HotUnplugNicVDSCommand<P extends VmNicDeviceVDSParameters> extends HotPlugOrUnplugNicVDSCommand<P> {

    public HotUnplugNicVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        status = getBroker().hotUnplugNic(createParametersStruct());
        proceedProxyReturnValue();
    }

    @Override
    protected String generateDomainXml() throws JAXBException {
        Marshaller jaxbMarshaller = JAXBContext.newInstance(Hotunplug.class).createMarshaller();
        VmDevice device = getParameters().getVmDevice();
        Hotunplug hotunplug = new Hotunplug().setDevices(new Devices().setInterface(new Device(device)));
        StringWriter sw = new StringWriter();
        jaxbMarshaller.marshal(hotunplug, sw);
        String libvirtXml = sw.toString();
        String prettyLibvirtXml = XmlUtils.prettify(libvirtXml);
        if (prettyLibvirtXml != null) {
            log.info("NIC hot-unplug: {}", prettyLibvirtXml);
        }
        return libvirtXml;
    }
}
