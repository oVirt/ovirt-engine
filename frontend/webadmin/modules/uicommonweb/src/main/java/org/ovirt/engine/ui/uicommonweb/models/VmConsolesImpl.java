package org.ovirt.engine.ui.uicommonweb.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.uicommonweb.ConsoleOptionsFrontendPersister;
import org.ovirt.engine.ui.uicommonweb.models.vms.ConsoleModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.ConsoleModelErrorEventListener;
import org.ovirt.engine.ui.uicommonweb.models.vms.RdpConsoleModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.SpiceConsoleModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VncConsoleModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.UIMessages;

public class VmConsolesImpl implements VmConsoles {

    private ConsoleProtocol selectedProtocol;
    private final Map<ConsoleProtocol, ConsoleModel> consoleModels;
    private static final Map<Class, ConsoleProtocol> modelTypeMapping;

    private final ConsoleOptionsFrontendPersister.ConsoleContext myContext;

    private final Model parentModel;
    private VM vm;

    private static final UIMessages messages = ConstantsManager.getInstance().getMessages();

    public VmConsolesImpl(VM vm, Model parentModel, ConsoleOptionsFrontendPersister.ConsoleContext consoleContext) {
        this.vm = vm;
        this.parentModel = parentModel;
        this.myContext = consoleContext;
        this.consoleModels = new HashMap<ConsoleProtocol, ConsoleModel>();

        fillModels();
        setDefaultSelectedProtocol();
    }

    static {
        modelTypeMapping = new HashMap<Class, ConsoleProtocol>();
        modelTypeMapping.put(SpiceConsoleModel.class, ConsoleProtocol.SPICE);
        modelTypeMapping.put(VncConsoleModel.class, ConsoleProtocol.VNC);
        modelTypeMapping.put(RdpConsoleModel.class, ConsoleProtocol.RDP);
    }

    private void fillModels() {
        SpiceConsoleModel spiceConsoleModel = new SpiceConsoleModel(vm, parentModel);
        spiceConsoleModel.getErrorEvent().addListener(new ConsoleModelErrorEventListener(parentModel));
        spiceConsoleModel.setForceVmStatusUp(getConsoleContext() == ConsoleOptionsFrontendPersister.ConsoleContext.UP_BASIC);
        consoleModels.put(ConsoleProtocol.SPICE, spiceConsoleModel);

        VncConsoleModel vncConsoleModel = new VncConsoleModel(vm, parentModel);
        vncConsoleModel.setForceVmStatusUp(getConsoleContext() == ConsoleOptionsFrontendPersister.ConsoleContext.UP_BASIC);
        consoleModels.put(ConsoleProtocol.VNC, vncConsoleModel);

        RdpConsoleModel rdpConsoleModel = new RdpConsoleModel(vm, parentModel);
        rdpConsoleModel.getErrorEvent().addListener(new ConsoleModelErrorEventListener(parentModel));
        rdpConsoleModel.setForceVmStatusUp(getConsoleContext() == ConsoleOptionsFrontendPersister.ConsoleContext.UP_BASIC);
        consoleModels.put(ConsoleProtocol.RDP, rdpConsoleModel);
    }

    @Override
    public boolean canSelectProtocol(ConsoleProtocol protocol) {
        return consoleModels.get(protocol).canBeSelected();
    }

    @Override
    public void selectProtocol(ConsoleProtocol protocol) throws IllegalArgumentException {
        if (!canSelectProtocol(protocol)) {
            throw new IllegalArgumentException("Cannot select " +protocol.toString() + " protocol for vm " + getVm().getName()); // $NON-NLS-1$ $NON-NLS-2$
        }
        this.selectedProtocol = protocol;
    }

    @Override
    public ConsoleProtocol getSelectedProcotol() {
        return selectedProtocol;
    }

    public <T extends ConsoleModel> T getConsoleModel(Class <T> type) {
        return (T) consoleModels.get(modelTypeMapping.get(type));
    }

    public boolean canConnectToConsole() {
        return consoleModels.get(selectedProtocol).canConnect();
    }

    @Override
    public void connect() throws ConsoleConnectException {
        if (!canConnectToConsole()) {
            throw new ConsoleConnectException(connectErrorMessage());
        }
        consoleModels.get(selectedProtocol).getConnectCommand().execute();
    }

    private void setDefaultSelectedProtocol() {
        List<ConsoleProtocol> allProtocols = new ArrayList<ConsoleProtocol>(Arrays.asList(ConsoleProtocol.values()));

        if (selectedProtocol != null) { // if it's selected, it's prefered -> set it to the 1st position
            allProtocols.remove(selectedProtocol);
            allProtocols.add(0, selectedProtocol);
        }

        for (ConsoleProtocol protocol : allProtocols) {
            if (canSelectProtocol(protocol)) {
                selectProtocol(protocol);
                break;
            }
        }
    }

    @Override
    public String cannotConnectReason() {
        // so far this is too general - more cases can be added based on state of underlying console models
        return canConnectToConsole()
                ? ""
                : messages.cannotConnectToTheConsole(getVm().getName());
    }

    private String connectErrorMessage() {
        return canConnectToConsole()
                ? ""
                : messages.errorConnectingToConsole(getVm().getName(), getSelectedProcotol().toString());
    }

    @Override
    public ConsoleOptionsFrontendPersister.ConsoleContext getConsoleContext() {
        return myContext;
    }

    @Override
    public VM getVm() {
        return vm;
    }

    public void setVm(VM newVm) {
        DisplayType oldDisplayType = getVm().getDisplayType();
        DisplayType oldDefaultDisplayType = getVm().getDefaultDisplayType();
        int oldOs = getVm().getOs();

        this.vm = newVm;
        for (ConsoleModel cModel : consoleModels.values()) { // update my console models too
            cModel.setEntity(newVm);
        }

        // if display types changed, we'd like to update the default selected protocol as the old one may be invalid
        if (newVm.getDisplayType() != oldDisplayType
                || newVm.getDefaultDisplayType() != oldDefaultDisplayType
                || newVm.getOs() != oldOs) {
            setDefaultSelectedProtocol();
        }
    }
}
