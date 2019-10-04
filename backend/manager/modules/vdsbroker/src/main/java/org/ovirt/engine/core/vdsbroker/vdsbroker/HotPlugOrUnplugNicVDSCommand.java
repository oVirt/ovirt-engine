package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.ovirt.engine.core.common.vdscommands.VmNicDeviceVDSParameters;

public abstract class HotPlugOrUnplugNicVDSCommand<P  extends VmNicDeviceVDSParameters> extends VdsBrokerCommand<P> {

    public HotPlugOrUnplugNicVDSCommand(P parameters) {
        super(parameters);
    }

    protected Map<String, Object> createParametersStruct() {
        Map<String, Object> struct = new HashMap<>();
        struct.put(VdsProperties.vm_guid, getParameters().getVm().getId().toString());
        try {
            struct.put(VdsProperties.engineXml, generateDomainXml());
        } catch (JAXBException e) {
            log.error("failed to create xml for hot-(un)plug", e);
            throw new RuntimeException(e);
        }
        return struct;
    }

    protected abstract String generateDomainXml() throws JAXBException;
}
