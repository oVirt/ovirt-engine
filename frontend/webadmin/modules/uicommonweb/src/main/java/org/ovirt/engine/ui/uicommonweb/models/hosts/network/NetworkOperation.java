package org.ovirt.engine.ui.uicommonweb.models.hosts.network;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ovirt.engine.core.common.action.CreateOrUpdateBond;
import org.ovirt.engine.core.common.businessentities.network.NicLabel;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.NetworkLabelModel.NewNetworkLabelModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

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
            return ConstantsManager.getInstance().getMessages().breakBond(op1.getName());
        }

        @Override
        public boolean isUnary() {
            return true;
        }

        @Override
        protected NetworkOperationCommandTarget getTarget() {
            return new NetworkOperationCommandTarget() {
                @Override
                protected void executeNetworkCommand(NetworkItemModel<?> op1,
                        NetworkItemModel<?> op2,
                        DataFromHostSetupNetworksModel dataFromHostSetupNetworksModel,
                        Object... params) {
                    assert op1 instanceof BondNetworkInterfaceModel;
                    assert op2 == null;
                    BondNetworkInterfaceModel bondModel = (BondNetworkInterfaceModel) op1;

                    // detach labels
                    detachAllLabels(bondModel, dataFromHostSetupNetworksModel);

                    // detach networks
                    detachAllNetworks(bondModel, dataFromHostSetupNetworksModel);

                    // break bond
                    dataFromHostSetupNetworksModel
                            .removeBondFromParameters(bondModel.getCreateOrUpdateBond());
                }
            };
        }

        @Override
        public boolean isDisplayNetworkAffected(NetworkItemModel<?> op1, NetworkItemModel<?> op2) {

            return isDisplayNetworkAttached((NetworkInterfaceModel) op1);
        }

    },
    DETACH_NETWORK {

        @Override
        public String getVerb(NetworkItemModel<?> op1) {
            return ConstantsManager.getInstance().getMessages().detachNetwork(op1.getName());
        }

        @Override
        public boolean isUnary() {
            return true;
        }

        @Override
        protected NetworkOperationCommandTarget getTarget() {
            return new NetworkOperationCommandTarget() {
                @Override
                protected void executeNetworkCommand(NetworkItemModel<?> op1,
                        NetworkItemModel<?> op2,
                        DataFromHostSetupNetworksModel dataFromHostSetupNetworksModel,
                        Object... params) {
                    assert op1 instanceof LogicalNetworkModel;
                    LogicalNetworkModel networkToDetach = (LogicalNetworkModel) op1;
                    assert networkToDetach.isAttached();

                    new LogicalNetworkModelParametersHelper(networkToDetach).updateParametersToDetach();
                }
            };
        }

        @Override
        public boolean isDisplayNetworkAffected(NetworkItemModel<?> op1, NetworkItemModel<?> op2) {

            final LogicalNetworkModel logicalNetworkModel = (LogicalNetworkModel) op1;
            return isDisplayNetwork(logicalNetworkModel);
        }

        @Override
        public boolean isRequiredNetworkAffected(NetworkItemModel<?> op1, NetworkItemModel<?> op2) {
            final LogicalNetworkModel logicalNetworkModel = (LogicalNetworkModel) op1;
            return logicalNetworkModel.getNetwork().getCluster().isRequired();
        }
    },
    ATTACH_NETWORK {

        @Override
        public String getVerb(NetworkItemModel<?> op1) {
            return ConstantsManager.getInstance().getMessages().attachTo(op1.getName());
        }

        @Override
        protected NetworkOperationCommandTarget getTarget() {
            return new NetworkOperationCommandTarget() {
                @Override
                protected void executeNetworkCommand(NetworkItemModel<?> op1,
                        NetworkItemModel<?> op2,
                        DataFromHostSetupNetworksModel dataFromHostSetupNetworksModel,
                        Object... params) {
                    assert op1 instanceof LogicalNetworkModel;
                    assert op2 instanceof NetworkInterfaceModel;
                    LogicalNetworkModel networkModelToAttach = (LogicalNetworkModel) op1;
                    NetworkInterfaceModel targetNicModel = (NetworkInterfaceModel) op2;
                    // is network already attached?
                    if (networkModelToAttach.isAttached()) {
                        // detach first
                        NetworkCommand command = DETACH_NETWORK.getCommand(op1,
                                null,
                                dataFromHostSetupNetworksModel);
                        command.execute();
                    }

                    new LogicalNetworkModelParametersHelper(networkModelToAttach)
                            .prepareSetupNetworksParamsToAttachTo(targetNicModel);
                }
            };
        }

        @Override
        public boolean isDisplayNetworkAffected(NetworkItemModel<?> op1, NetworkItemModel<?> op2) {
            LogicalNetworkModel networkToBeAttached = (LogicalNetworkModel) op1;
            return isDisplayNetwork(networkToBeAttached) && networkToBeAttached.isAttached();
        }

    },
    BOND_WITH {

        @Override
        public String getVerb(NetworkItemModel<?> op1) {
            return ConstantsManager.getInstance().getMessages().bondWith(op1.getName());

        }

        @Override
        protected NetworkOperationCommandTarget getTarget() {
            return new NetworkOperationCommandTarget() {
                @Override
                protected void executeNetworkCommand(NetworkItemModel<?> op1,
                        NetworkItemModel<?> op2,
                        DataFromHostSetupNetworksModel dataFromHostSetupNetworksModel,
                        Object... params) {
                    assert op1 instanceof NetworkInterfaceModel && !(op1 instanceof BondNetworkInterfaceModel);
                    assert op2 instanceof NetworkInterfaceModel && !(op2 instanceof BondNetworkInterfaceModel);
                    assert params.length == 1 : "incorrect params length"; //$NON-NLS-1$
                    NetworkInterfaceModel nic1Model = (NetworkInterfaceModel) op1;
                    NetworkInterfaceModel nic2Model = (NetworkInterfaceModel) op2;

                    // detach possible networks from both nics
                    detachAllNetworks(nic1Model, dataFromHostSetupNetworksModel);
                    detachAllNetworks(nic2Model, dataFromHostSetupNetworksModel);

                    // detach labels from both nics
                    detachAllLabels(nic1Model, dataFromHostSetupNetworksModel);
                    detachAllLabels(nic1Model, dataFromHostSetupNetworksModel);

                    // param
                    CreateOrUpdateBond bond = (CreateOrUpdateBond) params[0];
                    bond.getSlaves().add(nic1Model.getName());
                    bond.getSlaves().add(nic2Model.getName());
                    dataFromHostSetupNetworksModel.addBondToParameters(bond);
                }
            };
        }

        @Override
        public boolean isDisplayNetworkAffected(NetworkItemModel<?> op1, NetworkItemModel<?> op2) {
            return isDisplayNetworkAttached((NetworkInterfaceModel) op1, (NetworkInterfaceModel) op2);
        }
    },
    JOIN_BONDS {

        @Override
        public String getVerb(NetworkItemModel<?> op1) {
            return BOND_WITH.getVerb(op1);
        }

        @Override
        protected NetworkOperationCommandTarget getTarget() {
            return new NetworkOperationCommandTarget() {
                @Override
                protected void executeNetworkCommand(NetworkItemModel<?> op1,
                        NetworkItemModel<?> op2,
                        DataFromHostSetupNetworksModel dataFromHostSetupNetworksModel,
                        Object... params) {
                    assert op1 instanceof BondNetworkInterfaceModel;
                    assert op2 instanceof BondNetworkInterfaceModel;
                    assert params.length == 1 : "incorrect params length"; //$NON-NLS-1$
                    Set<NetworkInterfaceModel> slaveModels = new HashSet<>();
                    slaveModels.addAll(((BondNetworkInterfaceModel) op1).getSlaves());
                    slaveModels.addAll(((BondNetworkInterfaceModel) op2).getSlaves());

                    // break both bonds
                    BREAK_BOND.getCommand(op1, null, dataFromHostSetupNetworksModel).execute();
                    BREAK_BOND.getCommand(op2, null, dataFromHostSetupNetworksModel).execute();

                    // param
                    CreateOrUpdateBond bond = (CreateOrUpdateBond) params[0];
                    Set<String> slaves = new HashSet<>();

                    for (NetworkInterfaceModel slaveModel : slaveModels) {
                        slaves.add(slaveModel.getName());
                    }
                    bond.getSlaves().addAll(slaves);
                    dataFromHostSetupNetworksModel.addBondToParameters(bond);
                }
            };
        }

        @Override
        public boolean isDisplayNetworkAffected(NetworkItemModel<?> op1, NetworkItemModel<?> op2) {
            return isDisplayNetworkAttached((NetworkInterfaceModel) op1, (NetworkInterfaceModel) op2);
        }

    },
    ADD_TO_BOND {

        @Override
        public String getVerb(NetworkItemModel<?> op1) {
            return ConstantsManager.getInstance().getMessages().addToBond(op1.getName());
        }

        @Override
        protected NetworkOperationCommandTarget getTarget() {
            return new NetworkOperationCommandTarget() {
                @Override
                protected void executeNetworkCommand(NetworkItemModel<?> op1,
                        NetworkItemModel<?> op2,
                        DataFromHostSetupNetworksModel dataFromHostSetupNetworksModel,
                        Object... params) {
                    assert op1 instanceof NetworkInterfaceModel;
                    assert op2 instanceof BondNetworkInterfaceModel;

                    NetworkInterfaceModel nicModel = (NetworkInterfaceModel) op1;
                    BondNetworkInterfaceModel bondModel = (BondNetworkInterfaceModel) op2;

                    // Save the networks and labels of the nic before they are detached
                    List<LogicalNetworkModel> networksToReatach =
                            nicModel.getItems() != null ? new ArrayList<>(nicModel.getItems())
                                    : new ArrayList<LogicalNetworkModel>();
                    List<NetworkLabelModel> labelsToReatach =
                            nicModel.getLabels() != null ? new ArrayList<>(nicModel.getLabels())
                                    : new ArrayList<NetworkLabelModel>();

                    // Detach possible networks and labels from the nic
                    detachAllNetworks(nicModel, dataFromHostSetupNetworksModel);
                    detachAllLabels(nicModel, dataFromHostSetupNetworksModel);

                    // Attach previous nic networks and labels to bond
                    attachNetworks(bondModel, networksToReatach, dataFromHostSetupNetworksModel);
                    attachLabels(bondModel, labelsToReatach, dataFromHostSetupNetworksModel);

                    CreateOrUpdateBond bondParam = bondModel.getCreateOrUpdateBond();
                    bondParam.getSlaves().add(nicModel.getName());
                }
            };
        }

        @Override
        public boolean isDisplayNetworkAffected(NetworkItemModel<?> op1, NetworkItemModel<?> op2) {
            return isDisplayNetworkAttached((NetworkInterfaceModel) op1);
        }

    },
    EXTEND_BOND_WITH {

        @Override
        public String getVerb(NetworkItemModel<?> op1) {
            return ConstantsManager.getInstance().getMessages().extendBond(op1.getName());
        }

        @Override
        protected NetworkOperationCommandTarget getTarget() {
            return new NetworkOperationCommandTarget() {
                @Override
                protected void executeNetworkCommand(NetworkItemModel<?> op1,
                        NetworkItemModel<?> op2,
                        DataFromHostSetupNetworksModel dataFromHostSetupNetworksModel,
                        Object... params) {
                    ADD_TO_BOND.getCommand(op2, op1, dataFromHostSetupNetworksModel).execute();
                }
            };
        }

        @Override
        public boolean isDisplayNetworkAffected(NetworkItemModel<?> op1, NetworkItemModel<?> op2) {
            return isDisplayNetworkAttached((NetworkInterfaceModel) op2);
        }

    },
    REMOVE_FROM_BOND {

        @Override
        public String getVerb(NetworkItemModel<?> op1) {
            return ConstantsManager.getInstance().getMessages().removeFromBond(op1.getName());
        }

        @Override
        public boolean isUnary() {
            return true;
        }

        @Override
        protected NetworkOperationCommandTarget getTarget() {
            return new NetworkOperationCommandTarget() {
                @Override
                protected void executeNetworkCommand(NetworkItemModel<?> op1,
                        NetworkItemModel<?> op2,
                        DataFromHostSetupNetworksModel dataFromHostSetupNetworksModel,
                        Object... params) {
                    assert op1 instanceof NetworkInterfaceModel;
                    assert op2 == null;
                    NetworkInterfaceModel nicModel = (NetworkInterfaceModel) op1;
                    String slaveName = nicModel.getName();

                    // if there are only two nics, break the bond
                    BondNetworkInterfaceModel bondModel = nicModel.getBond();

                    if (bondModel.getSlaves().size() == 2) {
                        BREAK_BOND.getCommand(bondModel, null, dataFromHostSetupNetworksModel).execute();
                    } else {
                        CreateOrUpdateBond bondParam = bondModel.getCreateOrUpdateBond();
                        bondParam.getSlaves().remove(slaveName);
                    }
                }
            };
        }

        @Override
        public boolean isDisplayNetworkAffected(NetworkItemModel<?> op1, NetworkItemModel<?> op2) {
            final BondNetworkInterfaceModel bond = ((NetworkInterfaceModel) op1).getBond();
            if (bond.getSlaves().size() == 2) {
                return isDisplayNetworkAttached(bond);
            }
            return false;
        }

    },
    REMOVE_UNMANAGED_NETWORK {

        @Override
        public String getVerb(NetworkItemModel<?> op1) {
            return ConstantsManager.getInstance().getMessages().removeNetwork(op1.getName());
        }

        @Override
        public boolean isUnary() {
            return true;
        }

        @Override
        protected NetworkOperationCommandTarget getTarget() {
            return new NetworkOperationCommandTarget() {
                @Override
                protected void executeNetworkCommand(NetworkItemModel<?> op1,
                        NetworkItemModel<?> op2,
                        DataFromHostSetupNetworksModel dataFromHostSetupNetworksModel,
                        Object... params) {

                    assert op1 instanceof LogicalNetworkModel;
                    LogicalNetworkModel networkToDetach = (LogicalNetworkModel) op1;
                    assert networkToDetach.isAttached();
                    Guid networkId = networkToDetach.getNetwork().getId();
                    assert networkId == null;

                    dataFromHostSetupNetworksModel.getRemovedUnmanagedNetworks()
                            .add(networkToDetach.getNetwork().getName());
                }
            };
        }

    },
    LABEL {

        @Override
        public String getVerb(NetworkItemModel<?> op1) {
            return (op1 instanceof NewNetworkLabelModel) ? ConstantsManager.getInstance().getConstants().newLabel()
                    : ConstantsManager.getInstance().getMessages().label(op1.getName());
        }

        @Override
        protected NetworkOperationCommandTarget getTarget() {
            return new NetworkOperationCommandTarget() {
                @Override
                protected void executeNetworkCommand(NetworkItemModel<?> op1,
                        NetworkItemModel<?> op2,
                        DataFromHostSetupNetworksModel dataFromHostSetupNetworksModel,
                        Object... params) {
                    NetworkLabelModel labelModel = (NetworkLabelModel) op1;
                    NetworkInterfaceModel ifaceModel = (NetworkInterfaceModel) op2;

                    if (labelModel.isAttached()) {
                        UNLABEL.getCommand(labelModel, null, dataFromHostSetupNetworksModel).execute();
                    }

                    addLabel(ifaceModel.getName(),
                            ifaceModel.getOriginalIface().getId(),
                            labelModel.getName(),
                            dataFromHostSetupNetworksModel);

                    for (LogicalNetworkModel network : labelModel.getNetworks()) {
                        ATTACH_NETWORK.getCommand(network, ifaceModel, dataFromHostSetupNetworksModel).execute();
                    }
                }
            };
        }

        @Override
        public boolean isDisplayNetworkAffected(NetworkItemModel<?> op1, NetworkItemModel<?> op2) {
            if (op1 != null && op1 instanceof NetworkLabelModel) {
                for (LogicalNetworkModel network : ((NetworkLabelModel) op1).getNetworks()) {
                    if (ATTACH_NETWORK.isDisplayNetworkAffected(network, null)) {
                        return true;
                    }
                }
            }
            return false;
        }

    },
    UNLABEL {

        @Override
        public String getVerb(NetworkItemModel<?> op1) {
            return ConstantsManager.getInstance().getMessages().unlabel(op1.getName());
        }

        @Override
        public boolean isUnary() {
            return true;
        }

        @Override
        protected NetworkOperationCommandTarget getTarget() {
            return new NetworkOperationCommandTarget() {

                @Override
                protected void executeNetworkCommand(NetworkItemModel<?> op1,
                        NetworkItemModel<?> op2,
                        DataFromHostSetupNetworksModel dataFromHostSetupNetworksModel,
                        Object... params) {
                    NetworkLabelModel labelModel = (NetworkLabelModel) op1;
                    NetworkInterfaceModel interfaceModel = labelModel.getInterface();

                    removeLabel(interfaceModel.getName(),
                            interfaceModel.getOriginalIface().getId(),
                            labelModel.getName(),
                            dataFromHostSetupNetworksModel);

                    for (LogicalNetworkModel network : labelModel.getNetworks()) {
                        DETACH_NETWORK.getCommand(network, null, dataFromHostSetupNetworksModel).execute();
                    }
                }
            };
        }

        @Override
        public boolean isDisplayNetworkAffected(NetworkItemModel<?> op1, NetworkItemModel<?> op2) {
            if (op1 != null && op1 instanceof NetworkLabelModel) {
                for (LogicalNetworkModel network : ((NetworkLabelModel) op1).getNetworks()) {
                    if (DETACH_NETWORK.isDisplayNetworkAffected(network, null)) {
                        return true;
                    }
                }
            }
            return false;
        }

    },
    NULL_OPERATION {

        @Override
        public String getVerb(NetworkItemModel<?> op1) {
            return ""; //$NON-NLS-1$
        }

        @Override
        public boolean isUnary() {
            return true;
        }

        @Override
        public boolean isNullOperation() {
            return true;
        }

    },
    NULL_OPERATION_UNMANAGED {

        @Override
        public String getVerb(NetworkItemModel<?> op1) {
            return ConstantsManager.getInstance().getMessages().nullOperationUnmanagedNetwork(op1.getName());
        }

        @Override
        public String getMessage(NetworkItemModel<?> op1, NetworkItemModel<?> op2) {
            return appendDetachNetworkSuggestion(getVerb(op1), op2);
        }

        @Override
        public boolean isUnary() {
            return true;
        }

        @Override
        public boolean isNullOperation() {
            return true;
        }

    },
    NULL_OPERATION_BATCH_UNMANAGED {

        @Override
        public String getVerb(NetworkItemModel<?> op1) {
            return ConstantsManager.getInstance().getMessages().nullOperationUnmanagedNetwork(op1.getName());
        }

        @Override
        public String getMessage(NetworkItemModel<?> op1, NetworkItemModel<?> op2) {
            return appendDetachNetworkSuggestion(getVerb(op1), op2);
        }

        @Override
        public boolean isNullOperation() {
            return true;
        }

    },
    NULL_OPERATION_OUT_OF_SYNC {

        @Override
        public String getVerb(NetworkItemModel<?> op1) {
            return ConstantsManager.getInstance().getMessages().nullOperationOutOfSyncNetwork(op1.getName());
        }

        @Override
        public String getMessage(NetworkItemModel<?> op1, NetworkItemModel<?> op2) {
            return appendDetachNetworkSuggestion(getVerb(op1), op2);
        }

        @Override
        public boolean isNullOperation() {
            return true;
        }

    },
    NULL_OPERATION_BATCH_OUT_OF_SYNC {

        @Override
        public String getVerb(NetworkItemModel<?> op1) {
            return ConstantsManager.getInstance().getMessages().nullOperationOutOfSyncNetwork(op1.getName());
        }

        @Override
        public String getMessage(NetworkItemModel<?> op1, NetworkItemModel<?> op2) {
            return appendDetachNetworkSuggestion(getVerb(op1), op2);
        }

        @Override
        public boolean isNullOperation() {
            return true;
        }

    },
    NULL_OPERATION_TOO_MANY_NON_VLANS {

        @Override
        public String getVerb(NetworkItemModel<?> op1) {
            return ConstantsManager.getInstance().getConstants().nullOperationTooManyNonVlans();
        }

        @Override
        public String getMessage(NetworkItemModel<?> op1, NetworkItemModel<?> op2) {
            return getVerb(op1);
        }

        @Override
        public boolean isNullOperation() {
            return true;
        }

    },
    NULL_OPERATION_DUPLICATE_VLAN_IDS {

        @Override
        public String getVerb(NetworkItemModel<?> op1) {
            return ConstantsManager.getInstance().getConstants().nullOperationDuplicateVlanIds();
        }

        @Override
        public String getMessage(NetworkItemModel<?> op1, NetworkItemModel<?> op2) {
            return getVerb(op1);
        }

        @Override
        public boolean isNullOperation() {
            return true;
        }

    },
    NULL_OPERATION_BATCH_TOO_MANY_NON_VLANS {

        @Override
        public String getVerb(NetworkItemModel<?> op1) {
            return ConstantsManager.getInstance().getConstants().nullOperationTooManyNonVlans();
        }

        @Override
        public String getMessage(NetworkItemModel<?> op1, NetworkItemModel<?> op2) {
            return getVerb(op1);
        }

        @Override
        public boolean isNullOperation() {
            return true;
        }

    },
    NULL_OPERATION_INVALID_BOND_MODE {

        @Override
        public String getVerb(NetworkItemModel<?> op1) {
            return ConstantsManager.getInstance().getConstants().nullOperationInvalidBondMode();
        }

        @Override
        public String getMessage(NetworkItemModel<?> op1, NetworkItemModel<?> op2) {
            return getVerb(op1);
        }

        @Override
        public boolean isNullOperation() {
            return true;
        }
    };

    public static void detachAllNetworks(NetworkInterfaceModel nic,
            DataFromHostSetupNetworksModel dataFromHostSetupNetworksModel) {
        List<LogicalNetworkModel> attachedNetworks = nic.getItems();
        if (attachedNetworks.size() > 0) {
            for (LogicalNetworkModel networkModel : attachedNetworks) {
                boolean managedNetwork = networkModel.getNetworkAttachment() != null;
                if (managedNetwork) {
                    DETACH_NETWORK.getCommand(networkModel, null, dataFromHostSetupNetworksModel).execute();
                } else {
                    REMOVE_UNMANAGED_NETWORK.getCommand(networkModel, null, dataFromHostSetupNetworksModel).execute();
                }
            }
        }
    }

    public static void detachAllLabels(NetworkInterfaceModel nic,
            DataFromHostSetupNetworksModel dataFromHostSetupNetworksModel) {
        List<NetworkLabelModel> attachedLabels = nic.getLabels();
        if (attachedLabels.size() > 0) {
            for (NetworkLabelModel labelModel : attachedLabels) {
                UNLABEL.getCommand(labelModel, null, dataFromHostSetupNetworksModel).execute();
            }
        }
    }

    public static void attachNetworks(NetworkInterfaceModel nic,
            List<LogicalNetworkModel> networks,
            DataFromHostSetupNetworksModel dataFromHostSetupNetworksModel) {
        for (LogicalNetworkModel networkModel : networks) {
            ATTACH_NETWORK.getCommand(networkModel, nic, dataFromHostSetupNetworksModel).execute();
        }
    }

    public static void attachLabels(NetworkInterfaceModel nic,
            List<NetworkLabelModel> labels,
            DataFromHostSetupNetworksModel dataFromHostSetupNetworksModel) {
        for (NetworkLabelModel labelModel : labels) {
            LABEL.getCommand(labelModel, nic, dataFromHostSetupNetworksModel).execute();
        }
    }

    private static void addLabel(String dstIfaceName,
            Guid dstIfaceId,
            String label,
            DataFromHostSetupNetworksModel dataFromHostSetupNetworksModel) {
        dataFromHostSetupNetworksModel.addLabelToParameters(new NicLabel(dstIfaceId, dstIfaceName, label));
    }

    private static void removeLabel(String srcIfaceName,
            Guid srcIfaceId,
            String label,
            DataFromHostSetupNetworksModel dataFromHostSetupNetworksModel) {
        dataFromHostSetupNetworksModel.removeLabelFromParameters(new NicLabel(srcIfaceId, srcIfaceName, label));
    }

    /**
     * Creates the Command for this Operation<BR>
     * The Command acts and on the specified Operands, and manipulates the provided nic list
     *
     * @param op1
     *            first operand
     * @param op2
     *            second operand
     * @return NetworkCommand
     */
    public NetworkCommand getCommand(final NetworkItemModel<?> op1,
            final NetworkItemModel<?> op2,
            DataFromHostSetupNetworksModel dataFromHostSetupNetworksModel) {

        return new NetworkCommand(getMenuTitle(op1, op2),
                getTarget(),
                op1,
                op2,
                dataFromHostSetupNetworksModel);
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
        if (!isUnary()) {
            message += ' ' + getNoun(op2);
        }
        return message;
    }

    protected String appendDetachNetworkSuggestion(String originalMessage, NetworkItemModel<?> item) {
        String res = originalMessage;
        String culpritNetwork = item.getCulpritNetwork();
        if (culpritNetwork != null) {
            res += ' ' + ConstantsManager.getInstance().getMessages().suggestDetachNetwork(item.getCulpritNetwork());
        }
        return res;
    }

    /**
     * Gets the Noun for this Operation
     */
    public String getNoun(NetworkItemModel<?> op2) {
        assert !isUnary() : "The Unary Operation " + name() + " has no Noun"; //$NON-NLS-1$ //$NON-NLS-2$
        assert op2 != null : "Can't perform binary operation " + name() + "without a second operand"; //$NON-NLS-1$ $NON-NLS-2$
        return op2.getName();
    }

    /**
     * Gets the Verb for this Operation
     */
    public abstract String getVerb(NetworkItemModel<?> op1);

    /**
     * @return true if operation is unary.
     */
    public boolean isUnary() {
        return false;
    }

    /**
     * Implement to provide a Command Target for this Operation
     */
    protected NetworkOperationCommandTarget getTarget(){
        return new NetworkOperationCommandTarget() {

            @Override
            protected void executeNetworkCommand(NetworkItemModel<?> op1,
                    NetworkItemModel<?> op2,
                    DataFromHostSetupNetworksModel dataFromHostSetupNetworksModel,
                    Object... params) {
                // NOOP

            }
        };
    }

    public boolean isNullOperation(){
        return false;
    }

    public boolean isDisplayNetworkAffected(NetworkItemModel<?> op1, NetworkItemModel<?> op2) {
        return false;
    }

    public boolean isRequiredNetworkAffected(NetworkItemModel<?> op1, NetworkItemModel<?> op2) {
        return false;
    }

    public boolean isErroneousOperation() {
        return isNullOperation() && this != NULL_OPERATION;
    }

    private static boolean isDisplayNetworkAttached(Iterable<LogicalNetworkModel> logicalNetworkInterfaces) {
        if (logicalNetworkInterfaces == null) {
            return false;
        }
        for (LogicalNetworkModel logicalNetworkModel : logicalNetworkInterfaces) {
            if (isDisplayNetwork(logicalNetworkModel)) {
                return true;
            }
        }

        return false;
    }

    private static boolean isDisplayNetworkAttached(NetworkInterfaceModel networkItemModel) {
        return isDisplayNetworkAttached(networkItemModel.getItems());
    }

    private static boolean isDisplayNetworkAttached(NetworkInterfaceModel op1, NetworkInterfaceModel op2) {
        return isDisplayNetworkAttached(op1) ||
                isDisplayNetworkAttached(op2);
    }

    private static boolean isDisplayNetwork(LogicalNetworkModel logicalNetworkModel) {
        return logicalNetworkModel.getNetwork().getCluster().isDisplay();
    }
}
