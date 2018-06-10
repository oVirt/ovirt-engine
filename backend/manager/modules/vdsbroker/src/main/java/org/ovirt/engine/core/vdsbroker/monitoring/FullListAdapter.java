package org.ovirt.engine.core.vdsbroker.monitoring;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.vdscommands.FullListVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.VdsManager;
import org.ovirt.engine.core.vdsbroker.libvirt.VmDevicesConverter;
import org.ovirt.engine.core.vdsbroker.vdsbroker.DumpXmlsVDSCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class FullListAdapter {

    private static final Logger log = LoggerFactory.getLogger(FullListAdapter.class);

    @Inject
    private ResourceManager resourceManager;

    @Inject
    private VmDevicesConverter converter;

    public VDSReturnValue getVmFullList(Guid vdsId, List<Guid> vmIds) {
        return isDomainXmlEnabledForVds(vdsId) ? dumpXmls(vdsId, vmIds) : fullList(vdsId, vmIds);
    }

    private VDSReturnValue fullList(Guid vdsId, List<Guid> vmIds) {
        return runVdsCommand(VDSCommandType.FullList, new FullListVDSCommandParameters(vdsId, vmIds));
    }

    @SuppressWarnings("unchecked")
    private VDSReturnValue dumpXmls(Guid vdsId, List<Guid> vmIds) {
        VDSReturnValue retVal = runVdsCommand(VDSCommandType.DumpXmls, new DumpXmlsVDSCommand.Params(vdsId, vmIds));
        if (retVal.getSucceeded()) {
            Map<Guid, String> vmIdToXml = (Map<Guid, String>) retVal.getReturnValue();
            Map<String, Object>[] devices = vmIds.stream()
                    .map(vmId -> extractDevices(vmId, vdsId, vmIdToXml.get(vmId)))
                    .filter(Objects::nonNull)
                    .toArray(Map[]::new);
            retVal.setReturnValue(devices);
        }
        return retVal;
    }

    private boolean isDomainXmlEnabledForVds(Guid vdsId) {
        return FeatureSupported.isDomainXMLSupported(getVdsManager(vdsId).getCompatibilityVersion());
    }

    private <P extends VDSParametersBase> VDSReturnValue runVdsCommand(VDSCommandType commandType, P parameters) {
        return resourceManager.runVdsCommand(commandType, parameters);
    }

    private Map<String, Object> extractDevices(Guid vmId, Guid vdsId, String domxml) {
        try {
            return converter.convert(vmId, vdsId, domxml);
        } catch (Exception ex) {
            log.error("Failed during parsing devices of VM {} ({}) error is: {}", resourceManager.getVmManager(vmId).getName(), vmId, ex);
            log.error("Exception:", ex);
            return null;
        }
    }

    VdsManager getVdsManager(Guid vdsId) {
        return resourceManager.getVdsManager(vdsId);
    }
}
