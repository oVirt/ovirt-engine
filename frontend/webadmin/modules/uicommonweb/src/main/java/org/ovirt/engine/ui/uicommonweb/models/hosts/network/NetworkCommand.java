package org.ovirt.engine.ui.uicommonweb.models.hosts.network;

import org.ovirt.engine.ui.uicommonweb.ICommandTarget;
import org.ovirt.engine.ui.uicommonweb.UICommand;

/**
 * A {@link UICommand} for Network Operations
 */
public class NetworkCommand extends UICommand {

    private final NetworkItemModel<?> op1;
    private final NetworkItemModel<?> op2;
    private final DataFromHostSetupNetworksModel dataFromHostSetupNetworksModel;

    public NetworkCommand(String name,
            ICommandTarget target,
            NetworkItemModel<?> op1,
            NetworkItemModel<?> op2,
            DataFromHostSetupNetworksModel dataFromHostSetupNetworksModel) {
        super(name, target);
        this.op1 = op1;
        this.op2 = op2;
        this.dataFromHostSetupNetworksModel = dataFromHostSetupNetworksModel;
    }

    public NetworkItemModel<?> getOp1() {
        return op1;
    }

    public NetworkItemModel<?> getOp2() {
        return op2;
    }

    public DataFromHostSetupNetworksModel getDataFromHostSetupNetworksModel() {
        return dataFromHostSetupNetworksModel;
    }
}
