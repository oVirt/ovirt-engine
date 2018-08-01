package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;

import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.vdscommands.VmNicDeviceVDSParameters;
import org.ovirt.engine.core.utils.XmlUtils;
import org.ovirt.engine.core.vdsbroker.builder.vminfo.LibvirtVmXmlBuilder;
import org.ovirt.engine.core.vdsbroker.builder.vminfo.VmInfoBuildUtils;

public class UpdateVmInterfaceVDSCommand extends VdsBrokerCommand<VmNicDeviceVDSParameters> {

    @Inject
    private VmInfoBuildUtils vmInfoBuildUtils;

    public UpdateVmInterfaceVDSCommand(VmNicDeviceVDSParameters parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        status = getBroker().vmUpdateDevice(getParameters().getVm().getId().toString(), createParametersStruct());
        proceedProxyReturnValue();
    }

    protected Map<String, Object> createParametersStruct() {
        if (FeatureSupported.isDomainXMLSupported(getParameters().getVm().getClusterCompatibilityVersion())) {
            try {
                Map<String, Object> updateInfo = new HashMap<>();
                updateInfo.put(VdsProperties.DeviceType, getParameters().getVmDevice().getType().getValue());
                updateInfo.put(VdsProperties.engineXml, generateDomainXml());
                return updateInfo;
            } catch (JAXBException e) {
                log.error("failed to create xml for update vm interface", e);
                throw new RuntimeException(e);
            }
        } else {
            return initDeviceStructure();
        }
    }

    protected Map<String, Object> initDeviceStructure() {
        Map<String, Object> deviceStruct = new HashMap<>();
        deviceStruct.put(VdsProperties.DeviceType, getParameters().getVmDevice().getType().getValue());
        deviceStruct.put(VdsProperties.Alias, getParameters().getVmDevice().getAlias());

        VmNic nic = getParameters().getNic();
        deviceStruct.put(VdsProperties.LINK_ACTIVE, String.valueOf(nic.isLinked()));
        VmDevice vmDevice = getParameters().getVmDevice();

        vmInfoBuildUtils.addProfileDataToNic(deviceStruct, getParameters().getVm(), vmDevice, nic);

        return deviceStruct;
    }

    private String generateDomainXml() throws JAXBException {
        VmNic nic = getParameters().getNic();
        VmDevice vmDevice = getParameters().getVmDevice();
        LibvirtVmXmlBuilder builder = new LibvirtVmXmlBuilder(
                getParameters().getVm(),
                getVds().getId(),
                nic,
                vmDevice,
                vmInfoBuildUtils,
                nic.isPassthrough() ? Collections.singletonMap(nic.getId(), vmDevice.getHostDevice())
                        : Collections.emptyMap());
        String libvirtXml = builder.buildHotplugNic();
        String prettyLibvirtXml = XmlUtils.prettify(libvirtXml);
        if (prettyLibvirtXml != null) {
            log.info("NIC update: {}", prettyLibvirtXml);
        }
        return libvirtXml;
    }
}
