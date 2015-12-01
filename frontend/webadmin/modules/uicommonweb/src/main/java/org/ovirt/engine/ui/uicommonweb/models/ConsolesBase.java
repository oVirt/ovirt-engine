package org.ovirt.engine.ui.uicommonweb.models;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.uicommonweb.ConsoleOptionsFrontendPersister;
import org.ovirt.engine.ui.uicommonweb.models.vms.ConsoleModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.ConsoleModelErrorEventListener;
import org.ovirt.engine.ui.uicommonweb.models.vms.RdpConsoleModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.SpiceConsoleModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VncConsoleModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.UIConstants;
import org.ovirt.engine.ui.uicompat.UIMessages;

public abstract class ConsolesBase implements VmConsoles {

    private ConsoleProtocol selectedProtocol;
    protected final Map<ConsoleProtocol, ConsoleModel> consoleModels;
    private static final Map<Class, ConsoleProtocol> modelTypeMapping;

    private final ConsoleOptionsFrontendPersister.ConsoleContext myContext;

    private final Model parentModel;
    private VM vm;

    protected static final UIMessages messages = ConstantsManager.getInstance().getMessages();
    protected static final UIConstants constants = ConstantsManager.getInstance().getConstants();

    static {
        modelTypeMapping = new HashMap<>();
        modelTypeMapping.put(SpiceConsoleModel.class, ConsoleProtocol.SPICE);
        modelTypeMapping.put(VncConsoleModel.class, ConsoleProtocol.VNC);
        modelTypeMapping.put(RdpConsoleModel.class, ConsoleProtocol.RDP);
    }

    public ConsolesBase(VM vm, Model parentModel, ConsoleOptionsFrontendPersister.ConsoleContext consoleContext) {
        this.vm = vm;
        this.parentModel = parentModel;
        this.myContext = consoleContext;
        this.consoleModels = new HashMap<>();

        fillModels();
        setDefaultSelectedProtocol();
    }

    private void fillModels() {
        SpiceConsoleModel spiceConsoleModel = new SpiceConsoleModel(vm, parentModel);
        spiceConsoleModel.getErrorEvent().addListener(new ConsoleModelErrorEventListener(parentModel));
        spiceConsoleModel.setForceVmStatusUp(myContext == ConsoleOptionsFrontendPersister.ConsoleContext.UP_BASIC);
        consoleModels.put(ConsoleProtocol.SPICE, spiceConsoleModel);

        VncConsoleModel vncConsoleModel = new VncConsoleModel(vm, parentModel);
        vncConsoleModel.setForceVmStatusUp(myContext == ConsoleOptionsFrontendPersister.ConsoleContext.UP_BASIC);
        consoleModels.put(ConsoleProtocol.VNC, vncConsoleModel);

        RdpConsoleModel rdpConsoleModel = new RdpConsoleModel(vm, parentModel);
        rdpConsoleModel.getErrorEvent().addListener(new ConsoleModelErrorEventListener(parentModel));
        rdpConsoleModel.setForceVmStatusUp(myContext == ConsoleOptionsFrontendPersister.ConsoleContext.UP_BASIC);
        consoleModels.put(ConsoleProtocol.RDP, rdpConsoleModel);
    }

    protected void setDefaultSelectedProtocol() {
        List<ConsoleProtocol> allProtocols = ConsoleProtocol.getProtocolsByPriority();
        Collections.reverse(allProtocols);

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

    public boolean canSelectProtocol(ConsoleProtocol protocol) {
        return (protocol == null)
            ? false
            : consoleModels.get(protocol).canBeSelected();
    }

    public void selectProtocol(ConsoleProtocol protocol) throws IllegalArgumentException {
        if (!canSelectProtocol(protocol)) {
            throw new IllegalArgumentException("Cannot select " +protocol.toString() + " protocol for vm " + getVm().getName()); // $NON-NLS-1$ $NON-NLS-2$
        }
        this.selectedProtocol = protocol;
    }

    public ConsoleProtocol getSelectedProcotol() {
        return selectedProtocol;
    }

    public <T extends ConsoleModel> T getConsoleModel(Class <T> type) {
        return (T) consoleModels.get(modelTypeMapping.get(type));
    }

    public boolean canConnectToConsole() {
        return (selectedProtocol == null)
                ? false
                : consoleModels.get(selectedProtocol).canConnect();
    }

    public ConsoleOptionsFrontendPersister.ConsoleContext getConsoleContext() {
        return myContext;
    }

    public VM getVm() {
        return vm;
    }

    public void setVm(VM newVm) {
        boolean graphicsTypeChanged = false;
        if (!getVm().getGraphicsInfos().equals(newVm.getGraphicsInfos())) {
            graphicsTypeChanged = true;
        }

        int oldOs = getVm().getOs();

        this.vm = newVm;

        for (ConsoleModel cModel : consoleModels.values()) { // update my console models too
            cModel.setEntity(newVm);
        }

        // if display types changed, we'd like to update the default selected protocol as the old one may be invalid
        if (graphicsTypeChanged || newVm.getOs() != oldOs) {
            setDefaultSelectedProtocol();
        }
    }
}
