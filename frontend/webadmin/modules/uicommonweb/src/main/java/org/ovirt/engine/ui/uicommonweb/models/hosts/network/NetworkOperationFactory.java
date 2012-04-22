package org.ovirt.engine.ui.uicommonweb.models.hosts.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.VdsNetworkInterface;

/**
 * A Factory responsible for providing Setup Network Operations for Network Items.<BR>
 * The Factory also generates Menu Items for these Operations.
 *
 */
public class NetworkOperationFactory {

    @SuppressWarnings("serial")
    public static class OperationMap extends HashMap<NetworkOperation, List<NetworkCommand>> {
        protected void addCommand(NetworkOperation operation, NetworkCommand command) {
            List<NetworkCommand> menuItems = getItems(operation);
            menuItems.add(command);
        }

        private List<NetworkCommand> getItems(NetworkOperation operation) {
            if (containsKey(operation)) {
                return get(operation);
            }
            List<NetworkCommand> menuItems = new ArrayList<NetworkCommand>();
            put(operation, menuItems);
            return menuItems;
        }
    }

    /**
     * Gets the valid Operation involving the two operands.<BR>
     * If no Operation is valid returns {@link NetworkOperation#NULL_OPERATION}
     *
     * @param op1
     * @param op2
     * @return
     */
    public static NetworkOperation operationFor(NetworkItemModel<?> op1, NetworkItemModel<?> op2) {
        // !! always check bond before nic because of inheritance !!

        // op1: bond
        if (op1 instanceof BondNetworkInterfaceModel) {
            BondNetworkInterfaceModel bond = (BondNetworkInterfaceModel) op1;
            // op2: null
            if (op2 == null) {
                return NetworkOperation.BREAK_BOND;
            }
            return NetworkOperation.NULL_OPERATION;
        }
        // op1: nic
        else if (op1 instanceof NetworkInterfaceModel) {
            NetworkInterfaceModel nic1 = (NetworkInterfaceModel) op1;
            // op2: null
            if (nic1.isBonded() && op2 == null) {
                return NetworkOperation.REMOVE_FROM_BOND;
            }
            // op2: bond
            else if (op2 instanceof BondNetworkInterfaceModel) {
                BondNetworkInterfaceModel bond = (BondNetworkInterfaceModel) op2;
                if (!nic1.isBonded()) {
                    return NetworkOperation.ADD_TO_BOND;
                }
            }
            // op2: nic
            else if (op2 instanceof NetworkInterfaceModel) {
                NetworkInterfaceModel nic2 = (NetworkInterfaceModel) op2;
                if (nic1 != nic2 && !nic1.isBonded() && !nic2.isBonded()) {
                    return NetworkOperation.BOND_WITH;
                }
            }
            return NetworkOperation.NULL_OPERATION;
        }
        // op1: network
        else if (op1 instanceof LogicalNetworkModel) {
            LogicalNetworkModel network = (LogicalNetworkModel) op1;
            // op2: null
            if (network.isAttached() && op2 == null) {
                return NetworkOperation.DETACH_NETWORK;
            }
            // op2: nic
            else if (op2 instanceof NetworkInterfaceModel) {
                NetworkInterfaceModel nic = (NetworkInterfaceModel) op2;
                List<LogicalNetworkModel> nicNetworks = nic.getItems();
                if (!nicNetworks.contains(network) && !nic.isBonded()) {
                    if (!network.hasVlan()) {
                        // cannot connect a non-vlan network if there is already a non-vlan network
                        for (LogicalNetworkModel nicNetwork : nicNetworks) {
                            if (!nicNetwork.hasVlan()) {
                                return NetworkOperation.NULL_OPERATION;
                            }
                        }
                    }
                    return NetworkOperation.ATTACH_NETWORK;
                }
            }
            return NetworkOperation.NULL_OPERATION;
        }
        return NetworkOperation.NULL_OPERATION;
    }

    private final List<LogicalNetworkModel> allNetworks;

    private final List<NetworkInterfaceModel> nics;

    /**
     * Create an Operation Factory with the provided list of Networks and Nics
     *
     * @param allNetworks
     * @param nics
     */
    public NetworkOperationFactory(List<LogicalNetworkModel> allNetworks, List<NetworkInterfaceModel> nics) {
        this.allNetworks = allNetworks;
        this.nics = nics;
    }

    /**
     * Calculate all possible Commands for this Item, taking into account all Network Items (Nics and Networks) this
     * Factory is aware of.
     *
     * @param item
     * @param allNics
     * @return
     */
    public OperationMap commandsFor(NetworkItemModel<?> item, List<VdsNetworkInterface> allNics) {
        OperationMap operations = new OperationMap();
        // with nics
        for (NetworkInterfaceModel nic : nics) {
            NetworkOperation operation = operationFor(item, nic);
            if (operation != NetworkOperation.NULL_OPERATION) {
                assertBinary(item, nic, operation);
                operations.addCommand(operation, operation.getCommand(item, nic, allNics));
            }
        }
        // with networks
        for (LogicalNetworkModel network : allNetworks) {
            NetworkOperation operation = operationFor(item, network);
            if (operation != NetworkOperation.NULL_OPERATION) {
                assertBinary(item, network, operation);
                operations.addCommand(operation, operation.getCommand(item, network, allNics));
            }
        }
        // with self
        NetworkOperation operation = operationFor(item, null);
        if (operation != NetworkOperation.NULL_OPERATION) {
            assert operation.isUnary() : "Operation " + operation.name() //$NON-NLS-1$
                    + " is Binary, while a Uniary Operation is expected for " + item.getName(); //$NON-NLS-1$
            operations.addCommand(operation, operation.getCommand(item, null, allNics));
        }
        return operations;
    }

    private void assertBinary(NetworkItemModel<?> op1, NetworkItemModel<?> op2, NetworkOperation operation) {
        assert !operation.isUnary() : "Operation " + operation.name() //$NON-NLS-1$
                + " is Unary, while a Binary Operation is expected for: " + op1.getName() + " and " + op2.getName(); //$NON-NLS-1$ //$NON-NLS-2$
    }

}
