package org.ovirt.engine.ui.uicommonweb.models.hosts.network;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.ui.uicommonweb.ICommandTarget;
import org.ovirt.engine.ui.uicommonweb.UICommand;

/**
 * A {@link UICommand} for Network Operations
 */
public class NetworkCommand extends UICommand {

    private final NetworkItemModel<?> op1;
    private final NetworkItemModel<?> op2;
    private final List<VdsNetworkInterface> allNics;

    public NetworkCommand(String name,
            ICommandTarget target,
            NetworkItemModel<?> op1,
            NetworkItemModel<?> op2,
            List<VdsNetworkInterface> allNics) {
        super(name, target);
        this.op1 = op1;
        this.op2 = op2;
        this.allNics = allNics;
    }

    public List<VdsNetworkInterface> getAllNics() {
        return allNics;
    }

    public NetworkItemModel<?> getOp1() {
        return op1;
    }

    public NetworkItemModel<?> getOp2() {
        return op2;
    }

}
