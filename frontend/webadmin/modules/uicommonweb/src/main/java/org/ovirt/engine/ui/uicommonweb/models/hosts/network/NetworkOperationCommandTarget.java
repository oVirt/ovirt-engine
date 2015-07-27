package org.ovirt.engine.ui.uicommonweb.models.hosts.network;

import org.ovirt.engine.ui.uicommonweb.ICommandTarget;
import org.ovirt.engine.ui.uicommonweb.UICommand;

/**
 * An {@link ICommandTarget} for Network Commands
 */
public abstract class NetworkOperationCommandTarget implements ICommandTarget {

    @Override
    public void executeCommand(UICommand uiCommand) {
        executeCommand(uiCommand, new Object[0]);
    }

    @Override
    public void executeCommand(UICommand uiCommand, Object... params) {
        NetworkCommand command = (NetworkCommand) uiCommand;
        NetworkItemModel<?> op1 = command.getOp1();
        NetworkItemModel<?> op2 = command.getOp2();
        executeNetworkCommand(op1, op2, command.getDataFromHostSetupNetworksModel(), params);
    }

    protected abstract void executeNetworkCommand(NetworkItemModel<?> op1,
            NetworkItemModel<?> op2,
            DataFromHostSetupNetworksModel dataFromHostSetupNetworksModel,
            Object... params);
}
