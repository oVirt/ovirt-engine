package org.ovirt.engine.ui.uicommonweb.models;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.uicommonweb.ConsoleOptionsFrontendPersister;

public class VmConsolesImpl extends ConsolesBase {

    public VmConsolesImpl(VM vm, Model parentModel, ConsoleOptionsFrontendPersister.ConsoleContext consoleContext) {
        super(vm, parentModel, consoleContext);
    }

    public void connect() throws ConsoleConnectException {
        if (!canConnectToConsole()) {
            throw new ConsoleConnectException(connectErrorMessage());
        }

        getConsoleModel(getSelectedProcotol().getBackingClass()).getConnectCommand().execute();
    }

    /**
     * @return id of underlying VM.
     */
    @Override
    public Guid getEntityId() {
        return getVm().getId();
    }

    /**
     * @return name of underlying VM.
     */
    @Override
    public String getEntityName() {
        return getVm().getName();
    }

    @Override
    public String cannotConnectReason() {
        // so far this is too general - more cases can be added based on state of underlying console models
        return canConnectToConsole()
                ? ""
                : messages.cannotConnectToTheConsole(getVm().getName());
    }

    private String connectErrorMessage() {
        return getSelectedProcotol() == null ? messages.errorConnectingToConsoleNoProtocol(getVm().getName())
                : messages.errorConnectingToConsole(getVm().getName(), getSelectedProcotol().toString());
    }

}
