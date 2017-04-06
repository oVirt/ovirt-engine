package org.ovirt.engine.core.bll.network.vm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.VmCommand;
import org.ovirt.engine.core.bll.common.comparator.NumericSuffixNameableComparator;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.VmOperationParameterBase;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.dao.network.VmNicDao;

public class ReorderVmNicsCommand<T extends VmOperationParameterBase> extends VmCommand<T> {

    @Inject
    private NumericSuffixNameableComparator numericSuffixNameableComparator;
    @Inject
    private VmNicDao vmNicDao;
    @Inject
    private VmDeviceDao vmDeviceDao;

    public ReorderVmNicsCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected boolean validate() {
        VM vm = getVm();
        if (vm == null || vm.getStaticData() == null) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_VM_NOT_FOUND);
            return false;
        }

        if (!canRunActionOnNonManagedVm()) {
            return false;
        }

        return true;
    }

    @Override
    protected void executeVmCommand() {
        reorderNics();
        setSucceeded(true);
    }

    private Map<Guid, VmDevice> getVmInterfaceDevices() {
        List<VmDevice> vmInterfaceDevicesList = vmDeviceDao.getVmDeviceByVmIdAndType(getParameters().getVmId(), VmDeviceGeneralType.INTERFACE);
        Map<Guid, VmDevice> vmInterfaceDevices = new HashMap<>();
        for (VmDevice device : vmInterfaceDevicesList) {
            vmInterfaceDevices.put(device.getDeviceId(), device);
        }
        return vmInterfaceDevices;
    }

    private void reorderNics() {
        Map<Guid, VmDevice> vmInterfaceDevices = getVmInterfaceDevices();
        List<VmNic> nics = vmNicDao.getAllForVm(getParameters().getVmId());
        List<VmNic> nicsToReorder = new ArrayList<>();
        List<String> macsToReorder = new ArrayList<>();

        for (VmNic nic : nics) {
            VmDevice nicDevice = vmInterfaceDevices.get(nic.getId());
            // If there is not device, or the PCI address is empty
            if (nicDevice == null || StringUtils.isEmpty(nicDevice.getAddress())) {
                nicsToReorder.add(nic);
                // We know that all the NICs have a MAC address
                macsToReorder.add(nic.getMacAddress());
            }
        }

        // Sorting the NICs to reorder by name
        Collections.sort(nicsToReorder, numericSuffixNameableComparator);

        // Sorting the MAC addresses to reorder
        Collections.sort(macsToReorder);
        for (int i = 0; i < nicsToReorder.size(); ++i) {
            VmNic nic = nicsToReorder.get(i);
            nic.setMacAddress(macsToReorder.get(i));
            vmNicDao.update(nic);
        }
    }
}

