package org.ovirt.engine.ui.uicommonweb.models.hosts.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.network.BondMode;

/**
 * A Factory responsible for providing Setup Network Operations for Network Items.<BR>
 * The Factory also generates Menu Items for these Operations.
 *
 */
@SuppressWarnings("ChainOfInstanceofChecks")
public class NetworkOperationFactory {

    public static NetworkOperation operationFor(NetworkItemModel<?> op1, NetworkItemModel<?> op2) {
        return operationFor(op1, op2, false);
    }

    /**
     * Gets the valid Operation involving the two operands.<BR>
     * If no Operation is valid returns null operation
     */
    public static NetworkOperation operationFor(NetworkItemModel<?> op1, NetworkItemModel<?> op2, boolean isDrag) {
        if (noValidOperationForFirstOperand(op1)) {
            return NetworkOperation.NULL_OPERATION;
        }

        boolean unaryOperation = op2 == null;
        if (unaryOperation) {
            return handleUnaryOperation(op1, isDrag);
        } else {
            return handleBinaryOperation(op1, op2);
        }
    }

    private static NetworkOperation handleUnaryOperation(NetworkItemModel<?> op1, boolean isDrag) {
        // unary operation dragging op1 to nowhere

        // op1 is a bond, break it
        if (op1 instanceof BondNetworkInterfaceModel) {
            return NetworkOperation.BREAK_BOND;
        }

        // op1 is an interface, if it's bonded remove from bond
        if (op1 instanceof NetworkInterfaceModel) {
            NetworkInterfaceModel nic = (NetworkInterfaceModel) op1;
            if (nic.isBonded()) {
                return NetworkOperation.REMOVE_FROM_BOND;
            } else {
                return NetworkOperation.NULL_OPERATION;
            }
        }

        // op1 is a network, detach it if already attached to a NIC
        if (op1 instanceof LogicalNetworkModel) {
            LogicalNetworkModel network = (LogicalNetworkModel) op1;
            if (network.isAttached()) {
                if (!network.isManaged()) {
                    if (isDrag) {
                        return NetworkOperation.NULL_OPERATION_UNMANAGED;
                    } else {
                        return NetworkOperation.REMOVE_UNMANAGED_NETWORK;
                    }
                } else {
                    return NetworkOperation.DETACH_NETWORK;
                }
            } else {
                return NetworkOperation.NULL_OPERATION;
            }
        }

        // op1 is a label, if an interface is labelled with it - unlabel
        if (op1 instanceof NetworkLabelModel) {
            NetworkLabelModel label = (NetworkLabelModel) op1;
            if (label.isAttached()) {
                return NetworkOperation.UNLABEL;
            } else {
                return NetworkOperation.NULL_OPERATION;
            }
        }

        return NetworkOperation.NULL_OPERATION;
    }

    private static NetworkOperation handleBinaryOperation(NetworkItemModel<?> op1, NetworkItemModel<?> op2) {
        // binary operation joining items together - in most cases valid iff their networks comply
        if (op2 instanceof NetworkInterfaceModel) {
            return binaryOperationWithNetworkInterfaceModelAsSecondOperand(op1, (NetworkInterfaceModel) op2);
        }

        return NetworkOperation.NULL_OPERATION;
    }

    private static NetworkOperation binaryOperationWithNetworkInterfaceModelAsSecondOperand(NetworkItemModel<?> op1,
            NetworkInterfaceModel dst) {

        // first collect the networks into one set
        Set<LogicalNetworkModel> networks = new HashSet<>();
        networks.addAll(dst.getItems());

        // op1 is a NIC, verify that it isn't already part of a bond or dragged unto itself
        if (op1 instanceof NetworkInterfaceModel) {
            NetworkInterfaceModel src = (NetworkInterfaceModel) op1;
            if (src.isBonded() || src.equals(dst)) {
                return NetworkOperation.NULL_OPERATION;
            }

            networks.addAll(src.getItems());
        } else if (op1 instanceof LogicalNetworkModel) {
            // op1 is a network, verify that it isn't dragged unto the NIC already containing it
            if (!networks.add((LogicalNetworkModel) op1)) {
                return NetworkOperation.NULL_OPERATION;
            }
        } else if(op1 instanceof NetworkLabelModel) {
            // op1 is a label, verify that it's not applied to the interface already labelled by it
            NetworkLabelModel src = (NetworkLabelModel) op1;
            if (dst.equals(src.getInterface())) {
                return NetworkOperation.NULL_OPERATION;
            }
            networks.addAll(src.getNetworks());
        }

        // go over the networks and check whether they comply, if not - the reason is important
        int nonVlanCounter = 0;
        Set<Integer> vLanIds = new HashSet<>();
        for (LogicalNetworkModel network : networks) {
            if (!network.isManaged()) {
                if (op1 instanceof LogicalNetworkModel) {
                    dst.setCulpritNetwork(network.getName());
                    return NetworkOperation.NULL_OPERATION_UNMANAGED;
                }

                if (op1.aggregatesNetworks()) {
                    dst.setCulpritNetwork(network.getName());
                    return NetworkOperation.NULL_OPERATION_BATCH_UNMANAGED;
                }
            } else {
                if (!network.isInSync()) {
                    if (op1 instanceof LogicalNetworkModel) {
                        dst.setCulpritNetwork(network.getName());
                        return NetworkOperation.NULL_OPERATION_OUT_OF_SYNC;
                    }

                    if (op1.aggregatesNetworks()) {
                        dst.setCulpritNetwork(network.getName());
                        return NetworkOperation.NULL_OPERATION_BATCH_OUT_OF_SYNC;
                    }
                }
            }

            if (network.hasVlan()) {
                vLanIds.add(network.getVlanId());
            } else {
                ++nonVlanCounter;
            }

            if (nonVlanCounter > 1) {
                if (op1 instanceof LogicalNetworkModel) {
                    return NetworkOperation.NULL_OPERATION_TOO_MANY_NON_VLANS;
                }

                if (op1.aggregatesNetworks()) {
                    dst.setCulpritNetwork(network.getName());
                    return NetworkOperation.NULL_OPERATION_BATCH_TOO_MANY_NON_VLANS;
                }
            }

            if (network.getNetwork().isVmNetwork()) {
                if (dst instanceof BondNetworkInterfaceModel) {
                    BondNetworkInterfaceModel bondModel = (BondNetworkInterfaceModel) dst;
                    if (!BondMode.isBondModeValidForVmNetwork(bondModel.getCreateOrUpdateBond().getBondOptions())) {
                        return NetworkOperation.NULL_OPERATION_INVALID_BOND_MODE;
                    }
                }
            }
        }

        Integer vLanCounter = networks.size() - nonVlanCounter;
        if (vLanCounter > vLanIds.size()) {
            return NetworkOperation.NULL_OPERATION_DUPLICATE_VLAN_IDS;
        }

        // networks comply, all that's left is to return the correct operation
        if (op1 instanceof LogicalNetworkModel) {
            return NetworkOperation.ATTACH_NETWORK;
        }

        if (op1 instanceof BondNetworkInterfaceModel) {
            if (dst instanceof BondNetworkInterfaceModel) {
                return NetworkOperation.JOIN_BONDS;
            } else {
                return NetworkOperation.EXTEND_BOND_WITH;
            }
        }

        if (op1 instanceof NetworkInterfaceModel) {
            if (dst instanceof BondNetworkInterfaceModel) {
                return NetworkOperation.ADD_TO_BOND;
            } else {
                return NetworkOperation.BOND_WITH;
            }
        }

        if (op1 instanceof NetworkLabelModel) {
            return NetworkOperation.LABEL;
        }

        return NetworkOperation.NULL_OPERATION;
    }

    private static boolean noValidOperationForFirstOperand(NetworkItemModel<?> op1) {
        // no valid operation for external networks or networks attached via label
        if (op1 instanceof LogicalNetworkModel) {
            LogicalNetworkModel network = (LogicalNetworkModel) op1;
            if (network.getNetwork().isExternal() || network.isAttachedViaLabel()) {
                return true;
            }
        }

        return false;
    }

    private final List<LogicalNetworkModel> allNetworks;

    private final List<NetworkInterfaceModel> nics;

    /**
     * Create an Operation Factory with the provided list of Networks and Nics
     */
    public NetworkOperationFactory(List<LogicalNetworkModel> allNetworks, List<NetworkInterfaceModel> nics) {
        this.allNetworks = allNetworks;
        this.nics = nics;
    }

    /**
     * Calculate all possible Commands for this Item, taking into account all Network Items (Nics and Networks) this
     * Factory is aware of.
     */
    public Map<NetworkOperation, List<NetworkCommand>> commandsFor(NetworkItemModel<?> item,
            DataFromHostSetupNetworksModel dataFromHostSetupNetworksModel) {
        Map<NetworkOperation, List<NetworkCommand>> operations = new HashMap<>();
        // with nics
        for (NetworkInterfaceModel nic : nics) {
            NetworkOperation operation = operationFor(item, nic);
            if (!operation.isNullOperation()) {
                assertBinary(item, nic, operation);
                NetworkCommand command = operation.getCommand(item,
                        nic,
                        dataFromHostSetupNetworksModel);
                addToOperationMultiMap(operations, operation, command);
            }
        }
        // with networks
        for (LogicalNetworkModel network : allNetworks) {
            NetworkOperation operation = operationFor(item, network);
            if (!operation.isNullOperation()) {
                assertBinary(item, network, operation);
                NetworkCommand command = operation.getCommand(item,
                        network,
                        dataFromHostSetupNetworksModel);
                addToOperationMultiMap(operations, operation, command);
            }
        }

        // with self
        NetworkOperation operation = operationFor(item, null);
        if (!operation.isNullOperation()) {
            assert operation.isUnary() : "Operation " + operation.name() //$NON-NLS-1$
                    + " is Binary, while a Uniary Operation is expected for " + item.getName(); //$NON-NLS-1$
            NetworkCommand command = operation.getCommand(item,
                    null,
                    dataFromHostSetupNetworksModel);
            addToOperationMultiMap(operations, operation, command);
        }

        return operations;

    }

    private void addToOperationMultiMap(Map<NetworkOperation, List<NetworkCommand>> operationsMap,
            NetworkOperation operation,
            NetworkCommand command) {

        List<NetworkCommand> menuItems = operationsMap.get(operation);
        if (menuItems == null) {
            menuItems = new ArrayList<>();
            operationsMap.put(operation, menuItems);
        }
        menuItems.add(command);
    }

    private void assertBinary(NetworkItemModel<?> op1, NetworkItemModel<?> op2, NetworkOperation operation) {
        assert !operation.isUnary() : "Operation " + operation.name() //$NON-NLS-1$
                + " is Unary, while a Binary Operation is expected for: " + op1.getName() + " and " + op2.getName(); //$NON-NLS-1$ //$NON-NLS-2$
    }

}
