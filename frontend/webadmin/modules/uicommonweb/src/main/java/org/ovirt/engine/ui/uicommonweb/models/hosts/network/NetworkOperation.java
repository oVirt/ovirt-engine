package org.ovirt.engine.ui.uicommonweb.models.hosts.network;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.network.Bond;
import org.ovirt.engine.core.common.businessentities.network.IPv4Address;
import org.ovirt.engine.core.common.businessentities.network.IpConfiguration;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.NicLabel;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.utils.MapNetworkAttachments;
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
//TODO MM: NetworkOperation contains duplicates; getVerb, isNullOperation to constructor
//TODO MM: fix naming: model/entity etc. (.*?)Model should be named as \1Model and \1Model.getEntity should be called \1 and not entity. Etc.
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
                    List<VdsNetworkInterface> bridgesToRemove = new ArrayList<>();
                    BondNetworkInterfaceModel bondModel = (BondNetworkInterfaceModel) op1;
                    // break
                    bondModel.breakBond();
                    // detach networks
                    List<LogicalNetworkModel> networksToDetach = new ArrayList<>();
                    for (LogicalNetworkModel bondNetwork : bondModel.getItems()) {
                        networksToDetach.add(bondNetwork);
                    }
                    for (LogicalNetworkModel networkToDetach : networksToDetach) {
                        NetworkCommand command = DETACH_NETWORK.getCommand(networkToDetach,
                                null,
                                dataFromHostSetupNetworksModel);
                        command.execute();
                    }

                    Map<String, Bond> bondsMap = byName(dataFromHostSetupNetworksModel.newOrModifiedBonds);
                    Bond bond = bondModel.getIface();
                    boolean bondActuallyExisted = bond.getId() != null;
                    String bondName = bondModel.getName();

                    dataFromHostSetupNetworksModel.newOrModifiedBonds.remove(bondsMap.get(bondName));
                    if (bondActuallyExisted) {
                        dataFromHostSetupNetworksModel.removedBonds.add(bond);
                    }

                    // delete bonds
                    for (VdsNetworkInterface iface : dataFromHostSetupNetworksModel.allNics) {
                        if (iface.getName().startsWith(bondName)) {
                            bridgesToRemove.add(iface);
                        }
                    }
                    dataFromHostSetupNetworksModel.allNics.removeAll(bridgesToRemove);
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

                    detachNetworkAndUpdateHostSetupNetworksParameters(dataFromHostSetupNetworksModel, networkToDetach);
                }
            };
        }

        @Override
        public boolean isDisplayNetworkAffected(NetworkItemModel<?> op1, NetworkItemModel<?> op2) {

            final LogicalNetworkModel logicalNetworkModel = (LogicalNetworkModel) op1;
            return isDisplayNetwork(logicalNetworkModel);
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

                    Network networkToAttach = networkModelToAttach.getNetwork();
                    Map<Guid, NetworkAttachment> allNetworkAttachmentMap =
                            new MapNetworkAttachments(dataFromHostSetupNetworksModel.existingNetworkAttachments).byNetworkId();
                    boolean networkUsedInPreexistingAttachment = allNetworkAttachmentMap.containsKey(networkToAttach.getId());
                    Map<Guid, NetworkAttachment> removedNetworkAttachmentByIdMap =
                            new MapNetworkAttachments(dataFromHostSetupNetworksModel.removedNetworkAttachments).byNetworkId();

                    VdsNetworkInterface targetNic = targetNicModel.getIface();

                    boolean previouslyDetachedNetwork = removedNetworkAttachmentByIdMap.containsKey(networkToAttach.getId());
                    if (previouslyDetachedNetwork) {
                        dataFromHostSetupNetworksModel.removedNetworkAttachments.remove(removedNetworkAttachmentByIdMap.get(
                                networkToAttach.getId()));
                    }

                    if (networkUsedInPreexistingAttachment) {
                        Guid oldNetworkAttachmentId = allNetworkAttachmentMap.get(networkToAttach.getId()).getId();
                        dataFromHostSetupNetworksModel.newOrModifiedNetworkAttachments.add(
                                newNetworkAttachment(networkToAttach,
                                        targetNic,
                                        oldNetworkAttachmentId));
                    } else {
                        dataFromHostSetupNetworksModel.newOrModifiedNetworkAttachments.add(
                                newNetworkAttachment(networkToAttach,
                                        targetNic));
                    }

                    VdsNetworkInterface vlanBridge = networkModelToAttach.attach(targetNicModel, true);
                    if (vlanBridge != null) {
                        Iterator<VdsNetworkInterface> i = dataFromHostSetupNetworksModel.allNics.iterator();
                        // If a vlan device with the same vlan id as the new one already exists- remove it
                        while (i.hasNext()) {
                            VdsNetworkInterface nic = i.next();
                            if (vlanBridge.getVlanId().equals(nic.getVlanId())) {
                                if (vlanBridge.getBaseInterface().equals(nic.getBaseInterface())) {
                                    vlanBridge.setName(nic.getName());
                                }
                                i.remove();
                                break;
                            }
                        }
                        dataFromHostSetupNetworksModel.allNics.add(vlanBridge);
                    }
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
                    clearNetworks(nic1Model, dataFromHostSetupNetworksModel);
                    clearNetworks(nic2Model, dataFromHostSetupNetworksModel);

                    // param
                    Bond bond = (Bond) params[0];
                    String bondName = bond.getName();

                    // add to nic list
                    dataFromHostSetupNetworksModel.allNics.add(bond);

                    VdsNetworkInterface nic1 = nic1Model.getIface();
                    VdsNetworkInterface nic2 = nic2Model.getIface();
                    nic1.setBondName(bondName);
                    nic2.setBondName(bondName);
                    bond.getSlaves().add(nic1.getName());
                    bond.getSlaves().add(nic2.getName());

                    Map<String, Bond> removedBondsMap = byName(dataFromHostSetupNetworksModel.removedBonds);
                    boolean previouslyRemovedBond = removedBondsMap.containsKey(bond.getName());
                    if (previouslyRemovedBond) {
                        dataFromHostSetupNetworksModel.removedBonds.remove(removedBondsMap.get(bond.getName()));
                    }
                    dataFromHostSetupNetworksModel.newOrModifiedBonds.add(bond);

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
                    Set<NetworkInterfaceModel> slaveModels = new HashSet<>();      ////TODO MM: why set? There shouldn't be overlap in slaves and if there is, this cover-ups problem.
                    slaveModels.addAll(((BondNetworkInterfaceModel) op1).getBonded());
                    slaveModels.addAll(((BondNetworkInterfaceModel) op2).getBonded());

                    // break both bonds
                    BREAK_BOND.getCommand(op1, null, dataFromHostSetupNetworksModel).execute();
                    BREAK_BOND.getCommand(op2, null, dataFromHostSetupNetworksModel).execute();

                    // param
                    Bond bond = (Bond) params[0];
                    String bondName = bond.getName();

                    // add to nic list
                    dataFromHostSetupNetworksModel.allNics.add(bond);

                    for (NetworkInterfaceModel slaveModel : slaveModels) {
                        VdsNetworkInterface slave = slaveModel.getIface();
                        slave.setBondName(bondName);
                        bond.getSlaves().add(slave.getName());
                    }

                    Map<String, Bond> removedBondsMap = byName(dataFromHostSetupNetworksModel.removedBonds);
                    boolean previouslyRemovedBond = removedBondsMap.containsKey(bond.getName());
                    if (previouslyRemovedBond) {
                        dataFromHostSetupNetworksModel.removedBonds.remove(removedBondsMap.get(bond.getName()));
                        dataFromHostSetupNetworksModel.newOrModifiedBonds.add(bond);
                    } else {
                        dataFromHostSetupNetworksModel.newOrModifiedBonds.add(bond);
                    }
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

                    // Save the networks on the nic before they are detached
                    List<LogicalNetworkModel> networksToReatach =
                            nicModel.getItems() != null ? new ArrayList<>(nicModel.getItems())
                                    : new ArrayList<LogicalNetworkModel>();

                    // Detach possible networks from the nic
                    clearNetworks(nicModel, dataFromHostSetupNetworksModel);

                    // Attach previous nic networks to bond
                    attachNetworks(bondModel, networksToReatach, dataFromHostSetupNetworksModel);
                    moveLabels(Collections.singletonList(nicModel.getIface()), bondModel.getIface(), dataFromHostSetupNetworksModel);

                    Bond bond = bondModel.getIface();
                    String bondName = bond.getName();

                    VdsNetworkInterface nic = nicModel.getIface();
                    nic.setBondName(bondName);
                    String slaveName = nic.getName();

                    Map<String, Bond> bondsMap = byName(dataFromHostSetupNetworksModel.newOrModifiedBonds);

                    //TODO MM: removing and adding back a slave will end up in bond update even if there's no need for that.
                    boolean bondIsAlreadyBeingUpdated = bondsMap.containsKey(bondName);
                    if (bondIsAlreadyBeingUpdated) {
                        bondsMap.get(bondModel.getName()).getSlaves().add(slaveName);
                    } else {
                        bond.getSlaves().add(slaveName);
                        dataFromHostSetupNetworksModel.newOrModifiedBonds.add(bond);
                    }
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
                    VdsNetworkInterface slaveNic = nicModel.getIface();
                    slaveNic.setBondName(null);
                    // is there are only two nics, break the bond
                    BondNetworkInterfaceModel bondModel = nicModel.getBond();
                    Bond bond = bondModel.getIface();
                    String bondName = bond.getName();

                    if (bondModel.getBonded().size() == 2) {
                        BREAK_BOND.getCommand(bondModel, null, dataFromHostSetupNetworksModel).execute();
                    } else {
                        nicModel.setBonded(false);
                        slaveNic.setBonded(false);
                        slaveNic.setBondName(null);
                        Map<String, Bond> bondsMap = byName(dataFromHostSetupNetworksModel.newOrModifiedBonds);
                        boolean bondWasAlreadyUpdated = bondsMap.containsKey(bondName);
                        if (bondWasAlreadyUpdated) {
                            Bond formerlyUpdatedBond = bondsMap.get(bondName);
                            formerlyUpdatedBond.getSlaves().remove(slaveNic.getName());
                        } else {
                            bond.getSlaves().remove(slaveNic.getName());
                            dataFromHostSetupNetworksModel.newOrModifiedBonds.add(bond);
                        }

                    }
                }
            };
        }

        @Override
        public boolean isDisplayNetworkAffected(NetworkItemModel<?> op1, NetworkItemModel<?> op2) {
            final BondNetworkInterfaceModel bond = ((NetworkInterfaceModel) op1).getBond();
            if (bond.getBonded().size() == 2) {
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

                    detachNetworkWithoutUpdatingHostSetupNetworksParameters(dataFromHostSetupNetworksModel.allNics,
                            networkToDetach);
                    dataFromHostSetupNetworksModel.removedUnmanagedNetworks.add(networkToDetach.getNetwork().getName());
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

                    ifaceModel.label(labelModel);

                    addLabelToAddedRemovedSets(dataFromHostSetupNetworksModel,
                            labelModel.getName(),
                            ifaceModel.getIface());
                    for (LogicalNetworkModel network : labelModel.getNetworks()) {
                        ATTACH_NETWORK.getCommand(network, ifaceModel, dataFromHostSetupNetworksModel).execute();
                        network.attachViaLabel();
                    }
                }

                private void addLabelToAddedRemovedSets(DataFromHostSetupNetworksModel dataFromHostSetupNetworksModel,
                        String label,
                        VdsNetworkInterface iface) {

                    Map<String, NicLabel> removedLabelToNicLabel =
                            Entities.entitiesByName(dataFromHostSetupNetworksModel.removedLabels);
                    if (removedLabelToNicLabel.containsKey(label)) {
                        dataFromHostSetupNetworksModel.removedLabels.remove(removedLabelToNicLabel.get(label));
                    }

                    Map<String, NicLabel> labelToNicLabel =
                            Entities.entitiesByName(dataFromHostSetupNetworksModel.addedLabels);
                    NicLabel nicLabel;
                    if (labelToNicLabel.containsKey(label)) {
                        nicLabel = labelToNicLabel.get(label);
                        nicLabel.setNicId(iface.getId());
                        nicLabel.setNicName(iface.getName());
                    } else {
                        nicLabel = new NicLabel(iface.getId(), iface.getName(), label);
                    }
                    dataFromHostSetupNetworksModel.addedLabels.add(nicLabel);
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
                    interfaceModel.unlabel(labelModel);

                    removeLabelFromAddedRemovedSets(dataFromHostSetupNetworksModel,
                            labelModel.getName(),
                            interfaceModel.getIface());
                    for (LogicalNetworkModel network : labelModel.getNetworks()) {
                        DETACH_NETWORK.getCommand(network, null, dataFromHostSetupNetworksModel).execute();
                    }
                }

                private void removeLabelFromAddedRemovedSets(DataFromHostSetupNetworksModel dataFromHostSetupNetworksModel,
                        String label,
                        VdsNetworkInterface iface) {

                    NicLabel labelOnNic = new NicLabel(iface.getId(), iface.getName(), label);
                    if (dataFromHostSetupNetworksModel.addedLabels.contains(labelOnNic)) {
                        dataFromHostSetupNetworksModel.addedLabels.remove(labelOnNic);
                    } else {
                        dataFromHostSetupNetworksModel.removedLabels.add(labelOnNic);
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
            return ConstantsManager.getInstance().getConstants().nullOperationUnmanagedNetwork();
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
    NULL_OPERATION_BATCH_UNMANAGED {

        @Override
        public String getVerb(NetworkItemModel<?> op1) {
            return ConstantsManager.getInstance().getConstants().nullOperationUnmanagedNetwork();
        }

        @Override
        public String getMessage(NetworkItemModel<?> op1, NetworkItemModel<?> op2) {
            return appendDetachNetworkSuggestion(getVerb(op1), (NetworkInterfaceModel) op2);
        }

        @Override
        public boolean isNullOperation() {
            return true;
        }

    },
    NULL_OPERATION_OUT_OF_SYNC {

        @Override
        public String getVerb(NetworkItemModel<?> op1) {
            return ConstantsManager.getInstance().getConstants().nullOperationOutOfSyncNetwork();
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
    NULL_OPERATION_BATCH_OUT_OF_SYNC {

        @Override
        public String getVerb(NetworkItemModel<?> op1) {
            return ConstantsManager.getInstance().getConstants().nullOperationOutOfSyncNetwork();
        }

        @Override
        public String getMessage(NetworkItemModel<?> op1, NetworkItemModel<?> op2) {
            return appendDetachNetworkSuggestion(getVerb(op1), (NetworkInterfaceModel) op2);
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
    NULL_OPERATION_BATCH_TOO_MANY_NON_VLANS {

        @Override
        public String getVerb(NetworkItemModel<?> op1) {
            return ConstantsManager.getInstance().getConstants().nullOperationTooManyNonVlans();
        }

        @Override
        public String getMessage(NetworkItemModel<?> op1, NetworkItemModel<?> op2) {
            return appendDetachNetworkSuggestion(getVerb(op1), (NetworkInterfaceModel) op2);
        }

        @Override
        public boolean isNullOperation() {
            return true;
        }

    },
    NULL_OPERATION_VM_WITH_VLANS {

        @Override
        public String getVerb(NetworkItemModel<?> op1) {
            return ConstantsManager.getInstance().getConstants().nullOperationVmWithVlans();
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
    NULL_OPERATION_BATCH_VM_WITH_VLANS {

        @Override
        public String getVerb(NetworkItemModel<?> op1) {
            return ConstantsManager.getInstance().getConstants().nullOperationVmWithVlans();
        }

        @Override
        public String getMessage(NetworkItemModel<?> op1, NetworkItemModel<?> op2) {
            return appendDetachNetworkSuggestion(getVerb(op1), (NetworkInterfaceModel) op2);
        }

        @Override
        public boolean isNullOperation() {
            return true;
        }

    };

    public static void clearNetworks(NetworkInterfaceModel nic,
            DataFromHostSetupNetworksModel dataFromHostSetupNetworksModel) {
        List<LogicalNetworkModel> attachedNetworks = nic.getItems();
        if (attachedNetworks.size() > 0) {
            for (LogicalNetworkModel networkModel : new ArrayList<>(attachedNetworks)) {
                DETACH_NETWORK.getCommand(networkModel, null, dataFromHostSetupNetworksModel).execute();
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

    private static void detachNetworkAndUpdateHostSetupNetworksParameters(
            DataFromHostSetupNetworksModel dataFromHostSetupNetworksModel,
            LogicalNetworkModel modelOfNetworkToDetach) {

        Network networkToDetach = modelOfNetworkToDetach.getNetwork();
        Guid networkId = networkToDetach.getId();

        detachNetworkWithoutUpdatingHostSetupNetworksParameters(dataFromHostSetupNetworksModel.allNics,
                modelOfNetworkToDetach);

        Map<Guid, NetworkAttachment> allNetworkAttachmentMap = new MapNetworkAttachments(
                dataFromHostSetupNetworksModel.existingNetworkAttachments).byNetworkId();
        boolean detachingPreexistingNetworkAttachment = allNetworkAttachmentMap.containsKey(networkId);

        if (detachingPreexistingNetworkAttachment) {
            dataFromHostSetupNetworksModel.removedNetworkAttachments.add(allNetworkAttachmentMap.get(networkId));
        }

        // if network attachment was issued to be updated, remove it from such request
        for (Iterator<NetworkAttachment> iterator =
                dataFromHostSetupNetworksModel.newOrModifiedNetworkAttachments.iterator(); iterator.hasNext();) {

            NetworkAttachment networkAttachment = iterator.next();
            if (networkAttachment.getNetworkId().equals(networkId)) {
                iterator.remove();
            }
        }
    }

    private static void detachNetworkWithoutUpdatingHostSetupNetworksParameters(List<VdsNetworkInterface> allNics,
            LogicalNetworkModel networkToDetach) {
        // remove the vlan device
        if (networkToDetach.hasVlan()) {
            allNics.remove(networkToDetach.getVlanNicModel().getIface());
        }
        networkToDetach.detach();
    }

    public static void moveLabels(List<VdsNetworkInterface> srcIfaces,
            VdsNetworkInterface dstIface,
            DataFromHostSetupNetworksModel dataFromHostSetupNetworksModel) {
        Set<String> labels = new HashSet<>();
        for (VdsNetworkInterface srcIface : srcIfaces) {
            if (srcIface.getLabels() != null) {
                labels.addAll(srcIface.getLabels());
                srcIface.setLabels(null);
            }
        }

        if (labels.isEmpty()) {
            return;
        }

        if (dstIface.getLabels() == null) {
            dstIface.setLabels(labels);
        } else {
            dstIface.getLabels().addAll(labels);
        }

        Map<String, NicLabel> labelToNicLabel = Entities.entitiesByName(dataFromHostSetupNetworksModel.addedLabels);
        for (String label : labels) {
            NicLabel nicLabel = labelToNicLabel.get(label);

            if (nicLabel != null) {
                nicLabel.setNicId(dstIface.getId());
                nicLabel.setNicName(dstIface.getName());
            } else {
                nicLabel = new NicLabel(dstIface.getId(), dstIface.getName(), label);
                dataFromHostSetupNetworksModel.addedLabels.add(nicLabel);
            }
        }
    }

    /**
     * Creates the Command for this Operation<BR>
     * The Command acts and on the specified Operands, and manipulates the provided nic list
     *
     * @param op1
     *            first operand
     * @param op2
     *            second operand
     * @return
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

    public static NetworkAttachment newNetworkAttachment(Network network, VdsNetworkInterface targetNic) {
        return newNetworkAttachment(network, targetNic, null);
    }

    public static NetworkAttachment newNetworkAttachment(Network network,
            VdsNetworkInterface targetNic,
            Guid networkAttachmentId) {
        NetworkAttachment networkAttachment = new NetworkAttachment();
        networkAttachment.setId(networkAttachmentId);
        networkAttachment.setNetworkId(network.getId());
        networkAttachment.setNicId(targetNic.getId());
        networkAttachment.setNicName(targetNic.getName());
        networkAttachment.setOverrideConfiguration(true);
        IpConfiguration ipConfiguration = new IpConfiguration();
        networkAttachment.setIpConfiguration(ipConfiguration);
        ipConfiguration.getIPv4Addresses().add(newPrimaryAddress(targetNic));

        return networkAttachment;
    }

    public static IPv4Address newPrimaryAddress(VdsNetworkInterface targetNic) {
        IPv4Address primaryAddress = new IPv4Address();
        primaryAddress.setGateway(targetNic.getGateway());
        primaryAddress.setNetmask(targetNic.getSubnet());
        primaryAddress.setAddress(targetNic.getAddress());
        primaryAddress.setBootProtocol(targetNic.getBootProtocol());
        return primaryAddress;
    }

    //TODO MM: rename & move to better place.
    public static <E extends VdsNetworkInterface> Map<String, E> byName(Collection<E> collection) {
        Map<String, E> map = new HashMap<>();
        for (E e : collection) {
            map.put(e.getName(), e);
        }
        return map;
    }
}
