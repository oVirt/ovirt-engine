package org.ovirt.engine.ui.uicommonweb.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmOsType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.vms.ConsoleModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.ConsoleModelErrorEventListener;
import org.ovirt.engine.ui.uicommonweb.models.vms.ConsoleSelectionContext;
import org.ovirt.engine.ui.uicommonweb.models.vms.RdpConsoleModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.SpiceConsoleModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VncConsoleModel;

public class ConsoleModelsCache {

    private static final int SPICE_INDEX = 0;
    private static final int VNC_INDEX = 1;
    private static final int RDP_INDEX = 2;

    private static final List<VmOsType> vmOsTypeWithoutSpiceDriverSupport = Arrays.asList(VmOsType.Windows8,
            VmOsType.Windows8x64,
            VmOsType.Windows2012x64);

    private final HashMap<Guid, ArrayList<ConsoleModel>> cachedConsoleModels;
    private final Model parentModel;

    public ConsoleModelsCache(Model parentModel) {
        this.parentModel = parentModel;
        this.cachedConsoleModels = new HashMap<Guid, ArrayList<ConsoleModel>>();
    }

    public void updateConsoleModelsForVm(VM vm) {
        if (!cachedConsoleModels.containsKey(vm.getId())) {
            SpiceConsoleModel spiceConsoleModel = new SpiceConsoleModel();
            spiceConsoleModel.setParentModel(parentModel);
            spiceConsoleModel.getErrorEvent().addListener(new ConsoleModelErrorEventListener(parentModel));

            VncConsoleModel vncConsoleModel = new VncConsoleModel();
            vncConsoleModel.setParentModel(parentModel);

            RdpConsoleModel rdpConsoleModel = new RdpConsoleModel();
            rdpConsoleModel.getErrorEvent().addListener(new ConsoleModelErrorEventListener(parentModel));

            cachedConsoleModels.put(vm.getId(),
                    new ArrayList<ConsoleModel>(Arrays.asList(new ConsoleModel[] {
                            spiceConsoleModel, vncConsoleModel, rdpConsoleModel })));

            updateDefaultSelectedConsoleProtocol(vm);
        } else if (selectionContextChanged(vm)) {
            // if new data comes which has changed the selection context, (e.g. the OS type changed)
            // recalculate the default selected protocol
            updateDefaultSelectedConsoleProtocol(vm);
        }

        ArrayList<ConsoleModel> cachedModels = cachedConsoleModels.get(vm.getId());
        for (ConsoleModel a : cachedModels) {
            a.setEntity(null);
            a.setEntity(vm);
        }
    }

    private void updateDefaultSelectedConsoleProtocol(VM vm) {
        // for wind8+ guests the RDP is selected, for all other OS the spice
        if (vm.getId() == null) {
            return;
        }

        ArrayList<ConsoleModel> cachedModels = cachedConsoleModels.get(vm.getId());
        if (cachedModels == null) {
            return;
        }

        deselectUserSelectedProtocol(vm.getId());

        boolean isWindowsExplorer = parentModel.getConfigurator().isClientWindowsExplorer();

        if (vmOsTypeWithoutSpiceDriverSupport.contains(vm.getOs()) && isWindowsExplorer) {
            cachedModels.get(RDP_INDEX).setUserSelected(true);
        } else {
            determineConsoleModelFromVm(vm, cachedModels).setUserSelected(true);
        }

        setupSelectionContext(vm);
    }

    private void deselectUserSelectedProtocol(Guid vmId) {
        for (ConsoleModel model : cachedConsoleModels.get(vmId)) {
            model.setUserSelected(false);
        }
    }

    public void setSelectedProtocol(ConsoleProtocol protocol, HasConsoleModel item) {
        Guid vmId = item.getVM() != null ? item.getVM().getId() : null;
        if (vmId == null) {
            return;
        }

        deselectUserSelectedProtocol(vmId);

        for (ConsoleModel model : cachedConsoleModels.get(vmId)) {
            if (protocol.isBackedBy(model.getClass())) {
                model.setUserSelected(true);
                break;
            }
        }
    }

    private void setupSelectionContext(VM vm) {
        for (ConsoleModel model : cachedConsoleModels.get(vm.getId())) {

            DisplayType vmDisplay = vm.isRunningOrPaused() ? vm.getDisplayType() : vm.getDefaultDisplayType();
            model.setSelectionContext(new ConsoleSelectionContext(vm.getVmOs(), vmDisplay));
        }
    }

    private boolean selectionContextChanged(VM vm) {
        DisplayType vmDisplay = vm.isRunningOrPaused() ? vm.getDisplayType() : vm.getDefaultDisplayType();
        ConsoleSelectionContext newContext = new ConsoleSelectionContext(vm.getVmOs(), vmDisplay);
        ConsoleModel selectedConsole = resolveUserSelectedConsoleModel(vm.getId());

        if (selectedConsole == null) {
            return true;
        }

        return !newContext.equals(selectedConsole.getSelectionContext());
    }

    public ConsoleProtocol resolveUserSelectedProtocol(HasConsoleModel item) {
        if (item == null || item.getVM() == null || item.getVM().getId() == null) {
            return null;
        }
        Guid vmId = item.getVM().getId();

        ConsoleModel selectedConsoleModel = resolveUserSelectedConsoleModel(vmId);
        return selectedConsoleModel == null ? null
                : ConsoleProtocol.getProtocolByModel(selectedConsoleModel.getClass());
    }

    public ArrayList<ConsoleModel> getConsoleModelsByVmGuid(Guid vmGuid) {
        if (cachedConsoleModels != null) {
            return cachedConsoleModels.get(vmGuid);
        }

        return null;
    }

    private ConsoleModel resolveUserSelectedConsoleModel(Guid vmId) {
        if (!cachedConsoleModels.containsKey(vmId)) {
            return null;
        }

        for (ConsoleModel model : cachedConsoleModels.get(vmId)) {
            if (model.isUserSelected()) {
                return model;
            }
        }

        return null;
    }

    private ConsoleModel determineConsoleModelFromVm(VM vm, ArrayList<ConsoleModel> cachedModels) {
        DisplayType vmDisplayType = vm.isRunningOrPaused() ? vm.getDisplayType() : vm.getDefaultDisplayType();
        return cachedModels.get(vmDisplayType == DisplayType.vnc ? VNC_INDEX : SPICE_INDEX);
    }

    public ConsoleModel determineConsoleModelForVm(VM vm) {
        return determineConsoleModelFromVm(vm, cachedConsoleModels.get(vm.getId()));
    }

    public ConsoleModel determineAdditionalConsoleModelForVm(VM vm) {
        if (AsyncDataProvider.IsWindowsOsType(vm.getVmOs())) {
            return cachedConsoleModels.get(vm.getId()).get(RDP_INDEX);
        }

        return null;
    }

}
