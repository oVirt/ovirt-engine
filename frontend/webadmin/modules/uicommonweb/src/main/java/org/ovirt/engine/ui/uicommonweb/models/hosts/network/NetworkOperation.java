package org.ovirt.engine.ui.uicommonweb.models.hosts.network;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.VdsNetworkInterface;

/**
 * An Enumeration of Setup Network Operations<BR>
 * An Operation can be Unary or Binary<BR>
 * An Operation has a Verb ("Attach to"), and a Noun ("eth0").<BR>
 * Unary Operations have no Nouns ("Remove from Bond").<BR>
 * Some Operations have required parameters
 *
 */
public enum NetworkOperation {
    BREAK_BOND {

        @Override
        public String getVerb(NetworkItemModel<?> op1) {
            return "Break Bond " + op1.getName();
        }

        @Override
        public boolean isUnary() {
            return true;
        }

        @Override
        protected NetworkOperationCommandTarget getTarget() {
            return new NetworkOperationCommandTarget() {
                @Override
                protected void ExecuteNetworkCommand(NetworkItemModel<?> op1,
                        NetworkItemModel<?> op2,
                        List<VdsNetworkInterface> allNics, Object... params) {
                    assert op1 instanceof BondNetworkInterfaceModel;
                    assert op2 == null;
                    List<VdsNetworkInterface> bridgesToRemove = new ArrayList<VdsNetworkInterface>();
                    BondNetworkInterfaceModel bond = (BondNetworkInterfaceModel) op1;
                    // break
                    bond.breakBond();
                    // detach networks
                    List<LogicalNetworkModel> networksToDetach = new ArrayList<LogicalNetworkModel>();
                    for (LogicalNetworkModel bondNetwork : bond.getItems()) {
                        networksToDetach.add(bondNetwork);
                    }
                    for (LogicalNetworkModel networkToDetach : networksToDetach) {
                        DETACH_NETWORK.getCommand(networkToDetach, null, allNics).Execute();
                    }

                    String bondName = bond.getName();
                    // delete bonds
                    for (VdsNetworkInterface iface : allNics) {
                        if (iface.getName().startsWith(bondName)) {
                            bridgesToRemove.add(iface);
                        }
                    }
                    allNics.removeAll(bridgesToRemove);
                }
            };
        }
    },
    DETACH_NETWORK {
        @Override
        public String getVerb(NetworkItemModel<?> op1) {
            return "Detach Network " + op1.getName();
        }

        @Override
        public boolean isUnary() {
            return true;
        }

        @Override
        protected NetworkOperationCommandTarget getTarget() {
            return new NetworkOperationCommandTarget() {
                @Override
                protected void ExecuteNetworkCommand(NetworkItemModel<?> op1,
                        NetworkItemModel<?> op2,
                        List<VdsNetworkInterface> allNics, Object... params) {
                    assert op1 instanceof LogicalNetworkModel;
                    LogicalNetworkModel networkToDetach = (LogicalNetworkModel) op1;
                    assert networkToDetach.isAttached();
                    // remove vlan bridges
                    detachNetwork(allNics, networkToDetach);
                }
            };
        }
    },
    ATTACH_NETWORK {
        @Override
        public String getVerb(NetworkItemModel<?> op1) {
            return "Attach " + op1.getName() + " to";
        }

        @Override
        protected NetworkOperationCommandTarget getTarget() {
            return new NetworkOperationCommandTarget() {
                @Override
                protected void ExecuteNetworkCommand(NetworkItemModel<?> op1,
                        NetworkItemModel<?> op2,
                        List<VdsNetworkInterface> allNics, Object... params) {
                    assert op1 instanceof LogicalNetworkModel;
                    assert op2 instanceof NetworkInterfaceModel;
                    LogicalNetworkModel networkToAttach = (LogicalNetworkModel) op1;
                    NetworkInterfaceModel targetNic = (NetworkInterfaceModel) op2;
                    // is network already attached?
                    if (networkToAttach.isAttached()) {
                        // detach first
                        DETACH_NETWORK.getCommand(op1, null, allNics).Execute();
                    }
                    VdsNetworkInterface vlanBridge = networkToAttach.attach(targetNic, true);
                    if (vlanBridge != null) {
                        allNics.add(vlanBridge);
                    }
                }
            };
        }
    },
    BOND_WITH {

        @Override
        public String getVerb(NetworkItemModel<?> op1) {
            return "Bond " + op1.getName() + " with";
        }

        @Override
        protected NetworkOperationCommandTarget getTarget() {
            return new NetworkOperationCommandTarget() {
                @Override
                protected void ExecuteNetworkCommand(NetworkItemModel<?> op1,
                        NetworkItemModel<?> op2,
                        List<VdsNetworkInterface> allNics, Object... params) {
                    assert op1 instanceof NetworkInterfaceModel && !(op1 instanceof BondNetworkInterfaceModel);
                    assert op2 instanceof NetworkInterfaceModel && !(op2 instanceof BondNetworkInterfaceModel);
                    assert params.length == 1 : "incorrect params length";
                    NetworkInterfaceModel nic1 = (NetworkInterfaceModel) op1;
                    NetworkInterfaceModel nic2 = (NetworkInterfaceModel) op2;

                    // param
                    VdsNetworkInterface bond = (VdsNetworkInterface) params[0];
                    String bondName = bond.getName();

                    // add to nic list
                    allNics.add(bond);

                    nic1.getEntity().setBondName(bondName);
                    nic2.getEntity().setBondName(bondName);
                }
            };
        }

    },
    ADD_TO_BOND {

        @Override
        public String getVerb(NetworkItemModel<?> op1) {
            return "Add " + op1.getName() + " to Bond";
        }

        @Override
        protected NetworkOperationCommandTarget getTarget() {
            return new NetworkOperationCommandTarget() {
                @Override
                protected void ExecuteNetworkCommand(NetworkItemModel<?> op1,
                        NetworkItemModel<?> op2,
                        List<VdsNetworkInterface> allNics, Object... params) {
                    assert op1 instanceof NetworkInterfaceModel;
                    assert op2 instanceof BondNetworkInterfaceModel;

                    NetworkInterfaceModel nic = (NetworkInterfaceModel) op1;
                    BondNetworkInterfaceModel bond = (BondNetworkInterfaceModel) op2;

                    // detach possible networks form the nic
                    clearNetworks(nic, allNics);

                    nic.getEntity().setBondName(bond.getEntity().getName());
                }
            };
        }

    },
    REMOVE_FROM_BOND {

        @Override
        public String getVerb(NetworkItemModel<?> op1) {
            return "Remove " + op1.getName() + " from Bond";
        }

        @Override
        public boolean isUnary() {
            return true;
        }

        @Override
        protected NetworkOperationCommandTarget getTarget() {
            return new NetworkOperationCommandTarget() {
                @Override
                protected void ExecuteNetworkCommand(NetworkItemModel<?> op1,
                        NetworkItemModel<?> op2,
                        List<VdsNetworkInterface> allNics, Object... params) {
                    assert op1 instanceof NetworkInterfaceModel;
                    NetworkInterfaceModel nic = (NetworkInterfaceModel) op1;
                    VdsNetworkInterface entity = nic.getEntity();
                    entity.setBondName(null);
                    // is there are only two nics, break the bond
                    BondNetworkInterfaceModel bond = nic.getBond();
                    if (bond.getBonded().size() == 2) {
                        BREAK_BOND.getCommand(bond, null, allNics).Execute();
                    }
                }
            };
        }

    },
    NULL_OPERATION {

        @Override
        public String getVerb(NetworkItemModel<?> op1) {
            return "No valid Operation for " + op1.getName() + " and ";
        }

        @Override
        protected NetworkOperationCommandTarget getTarget() {
            return new NetworkOperationCommandTarget() {

                @Override
                protected void ExecuteNetworkCommand(NetworkItemModel<?> op1,
                        NetworkItemModel<?> op2,
                        List<VdsNetworkInterface> allNics,
                        Object... params) {
                    // NOOP

                }
            };
        }

    };

    private static void clearNetworks(NetworkInterfaceModel nic, List<VdsNetworkInterface> allNics) {
        List<LogicalNetworkModel> attachedNetworks = nic.getItems();
        if (attachedNetworks.size() > 0) {
            for (LogicalNetworkModel networkModel : new ArrayList<LogicalNetworkModel>(attachedNetworks)) {
                DETACH_NETWORK.getCommand(networkModel, null, allNics).Execute();
            }
        }
    }

    private static void detachNetwork(List<VdsNetworkInterface> allNics,
            LogicalNetworkModel networkToDetach) {
        // get the bridge and remove it
        NetworkInterfaceModel bridge = networkToDetach.getBridge();
        if (bridge.getName().indexOf('.') > 0) {
            allNics.remove(bridge.getEntity());
        }
        networkToDetach.detach();
    }

    /**
     * Creates the Command for this Operation<BR>
     * The Command acts and on the specified Operands, and manipulates the provided nic list
     *
     * @param op1
     *            first operand
     * @param op2
     *            second operand
     * @param allNics
     *            the complete nic list
     * @return
     */
    public NetworkCommand getCommand(final NetworkItemModel<?> op1,
            final NetworkItemModel<?> op2,
            List<VdsNetworkInterface> allNics) {
        return new NetworkCommand(getMenuTitle(op1, op2), getTarget(), op1, op2, allNics);
    }

    /**
     * Gets the String representing this Operation in a Menu Item
     */
    public String getMenuTitle(NetworkItemModel<?> op1, NetworkItemModel<?> op2) {
        return isUnary() ? getVerb(op1) : getNoun(op2);
    }

    /**
     * Gets the String representing this Operation in regular UI
     */
    public String getMessage(NetworkItemModel<?> op1, NetworkItemModel<?> op2) {
        String message = getVerb(op1);
        return isUnary() ? message : message + " " + getNoun(op2);
    }

    /**
     * Gets the Noun for this Operation
     */
    public String getNoun(NetworkItemModel<?> op2) {
        assert !isUnary() : "The Unary Operation " + name() + " has no Noun";
        return op2.getName();
    }

    /**
     * Gets the Verb for this Operation
     */
    public abstract String getVerb(NetworkItemModel<?> op1);

    /**
     * Is this Operation Unary?
     *
     * @return
     */
    public boolean isUnary() {
        return false;
    }

    /**
     * Implement to provide a Command Target for this Operation
     */
    protected abstract NetworkOperationCommandTarget getTarget();
}
