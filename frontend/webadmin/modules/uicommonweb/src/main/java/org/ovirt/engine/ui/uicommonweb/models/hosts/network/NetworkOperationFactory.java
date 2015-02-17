package org.ovirt.engine.ui.uicommonweb.models.hosts.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;

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
     * If no Operation is valid returns null operation
     *
     * @param op1
     * @param op2
     * @return
     */
    public static NetworkOperation operationFor(NetworkItemModel<?> op1, NetworkItemModel<?> op2, boolean isDrag) {
        // no valid operation for external networks or networks attached via label
        if (op1 instanceof LogicalNetworkModel) {
            LogicalNetworkModel network = (LogicalNetworkModel) op1;
            if (network.getNetwork().isExternal() || network.isAttachedViaLabel()) {
                return NetworkOperation.NULL_OPERATION;
            }
        }

        // no valid operation for network labels
        if (op1 instanceof NetworkLabelModel) {
            return NetworkOperation.NULL_OPERATION;
        }

        // unary operation dragging op1 to nowhere
        if (op2 == null) {

            // op1 is a bond, break it
            if (op1 instanceof BondNetworkInterfaceModel) {
                return NetworkOperation.BREAK_BOND;
            }
            // op1 is an interface, if it's bonded remove from bond
            else if (op1 instanceof NetworkInterfaceModel) {
                NetworkInterfaceModel nic = (NetworkInterfaceModel) op1;
                if (nic.isBonded()) {
                    return NetworkOperation.REMOVE_FROM_BOND;
                }
            }
            // op1 is a network, detach it if already attached to a NIC
            else if (op1 instanceof LogicalNetworkModel) {
                LogicalNetworkModel network = (LogicalNetworkModel) op1;
                if (network.isAttached()) {
                    if (!network.isManaged()) {
                        if (isDrag) {
                            return NetworkOperation.NULL_OPERATION_UNMANAGED;
                        } else {
                            return NetworkOperation.REMOVE_UNMANAGED_NETWORK;
                        }
                    }
                    return NetworkOperation.DETACH_NETWORK;
                }
            }
        }
        // binary operation joining items together - in most cases valid iff their networks comply
        else if (op2 instanceof NetworkInterfaceModel) {
            NetworkInterfaceModel dst = (NetworkInterfaceModel) op2;

            // first collect the networks into one set
            Set<LogicalNetworkModel> networks = new HashSet<LogicalNetworkModel>();
            networks.addAll(dst.getItems());

            // op1 is a NIC, verify that it isn't already part of a bond or dragged unto itself
            if (op1 instanceof NetworkInterfaceModel) {
                NetworkInterfaceModel src = (NetworkInterfaceModel) op1;
                if (src.isBonded() || src.equals(dst)) {
                    return NetworkOperation.NULL_OPERATION;
                }
                networks.addAll(((NetworkInterfaceModel) op1).getItems());
            }
            // op1 is a network, verify that it isn't dragged unto the NIC already containing it
            else if (op1 instanceof LogicalNetworkModel) {
                if (!networks.add((LogicalNetworkModel) op1)) {
                    return NetworkOperation.NULL_OPERATION;
                }
            }

            // go over the networks and check whether they comply, if not - the reason is important
            boolean vlanFound = false;
            String nonVlanVmNetwork = null;
            int nonVlanCounter = 0;
            for (LogicalNetworkModel network : networks) {
                if (!network.isManaged()) {
                    if (op1 instanceof LogicalNetworkModel) {
                        return NetworkOperation.NULL_OPERATION_UNMANAGED;
                    } else if (op1 instanceof NetworkInterfaceModel) {
                        dst.setCulpritNetwork(network.getName());
                        return NetworkOperation.NULL_OPERATION_BOND_UNMANAGED;
                    }
                } else if (!network.isInSync()) {
                    if (op1 instanceof LogicalNetworkModel) {
                        return NetworkOperation.NULL_OPERATION_OUT_OF_SYNC;
                    } else if (op1 instanceof NetworkInterfaceModel) {
                        dst.setCulpritNetwork(network.getName());
                        return NetworkOperation.NULL_OPERATION_BOND_OUT_OF_SYNC;
                    }
                }
                if (network.hasVlan()) {
                    vlanFound = true;
                } else if (network.getNetwork().isVmNetwork()) {
                    nonVlanVmNetwork = network.getName();
                    ++nonVlanCounter;
                } else {
                    ++nonVlanCounter;
                }
                if (nonVlanCounter > 1) {
                    if (op1 instanceof LogicalNetworkModel) {
                        return NetworkOperation.NULL_OPERATION_TOO_MANY_NON_VLANS;
                    } else if (op1 instanceof NetworkInterfaceModel) {
                        dst.setCulpritNetwork(network.getName());
                        return NetworkOperation.NULL_OPERATION_BOND_TOO_MANY_NON_VLANS;
                    }
                } else if (nonVlanVmNetwork != null && vlanFound) {
                    if (op1 instanceof LogicalNetworkModel) {
                        return NetworkOperation.NULL_OPERATION_VM_WITH_VLANS;
                    } else if (op1 instanceof NetworkInterfaceModel) {
                        dst.setCulpritNetwork(nonVlanVmNetwork);
                        return NetworkOperation.NULL_OPERATION_BOND_VM_WITH_VLANS;
                    }
                }
            }

            // networks comply, all that's left is to return the correct operation
            if (op1 instanceof LogicalNetworkModel) {
                return NetworkOperation.ATTACH_NETWORK;
            } else if (op1 instanceof BondNetworkInterfaceModel) {
                if (op2 instanceof BondNetworkInterfaceModel) {
                    return NetworkOperation.JOIN_BONDS;
                } else {
                    return NetworkOperation.EXTEND_BOND_WITH;
                }
            } else if (op1 instanceof NetworkInterfaceModel) {
                if (op2 instanceof BondNetworkInterfaceModel) {
                    return NetworkOperation.ADD_TO_BOND;
                } else {
                    return NetworkOperation.BOND_WITH;
                }
            }
        }

        return NetworkOperation.NULL_OPERATION;
    }

    public static NetworkOperation operationFor(NetworkItemModel<?> op1, NetworkItemModel<?> op2) {
        return operationFor(op1, op2, false);
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
            if (!operation.isNullOperation()) {
                assertBinary(item, nic, operation);
                operations.addCommand(operation, operation.getCommand(item, nic, allNics));
            }
        }
        // with networks
        for (LogicalNetworkModel network : allNetworks) {
            NetworkOperation operation = operationFor(item, network);
            if (!operation.isNullOperation()) {
                assertBinary(item, network, operation);
                operations.addCommand(operation, operation.getCommand(item, network, allNics));
            }
        }

        // with self
        NetworkOperation operation = operationFor(item, null);
        if (!operation.isNullOperation()) {
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
