package org.ovirt.engine.ui.uicommonweb.models.hosts.network;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
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
        List<VdsNetworkInterface> allNics = command.getAllNics();
        executeNetworkCommand(op1, op2, allNics, params);

    }

    protected abstract void executeNetworkCommand(NetworkItemModel<?> op1,
            NetworkItemModel<?> op2,
            List<VdsNetworkInterface> allNics, Object... params);
}
