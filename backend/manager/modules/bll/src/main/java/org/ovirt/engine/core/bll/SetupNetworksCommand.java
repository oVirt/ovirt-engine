package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.action.SetupNetworksParameters;
import org.ovirt.engine.core.common.businessentities.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.common.vdscommands.SetupNetworksVdsCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsIdAndVdsVDSCommandParametersBase;
import org.ovirt.engine.core.dal.VdcBllMessages;

public class SetupNetworksCommand<T extends SetupNetworksParameters> extends VdsCommand<T> {

    public SetupNetworksCommand(T parameters) {
        super(parameters);
        setVdsId(parameters.getVdsId());
    }

    @Override
    protected boolean canDoAction() {
        boolean retVal = true;

        if (getParameters().getConectivityTimeout() < 0) {
            addCanDoActionMessage(VdcBllMessages.NETWORK_CONNECTIVITY_TIMEOUT_NEGATIVE);
            retVal = false;
        }

        if (retVal && networksOrInterfacesEmpty()) {
            addCanDoActionMessage(VdcBllMessages.NETWORK_NO_NETWORKS_OR_INTERFACES);
            retVal = false;
        }

        if (retVal) {
            retVal = checkNetworksValidity();
        }

        if (retVal) {
            retVal = checkBondsValidity();
        }

        return retVal;
    }

    private boolean networksOrInterfacesEmpty() {
        T params = getParameters();
        return params.getNetworks().isEmpty() &&
                params.getRemovedNetworks().isEmpty() &&
                params.getBonds().isEmpty() &&
                params.getRemovedBonds().isEmpty();
    }

    private boolean checkBondsValidity() {
        boolean retVal = true;

        // Check that the same bond not exists in the removedBonds list
        T params = getParameters();
        if (params.getRemovedBonds().size() > 0) {
            for (VdsNetworkInterface bond : params.getBonds()) {
                for (VdsNetworkInterface removedBond : params.getRemovedBonds()) {
                    if (bond.getName().equals(removedBond.getName())) {
                        addCanDoActionMessage(VdcBllMessages.NETWORK_BOND_EXISTS_IN_ADD_AND_REMOVE);
                        retVal = false;
                        break;
                    }
                }
            }
        }

        if (retVal) {
            boolean ifaceExists = false;
            // Check that bond have at least one VdsNetworkInterface
            for (VdsNetworkInterface bond : params.getBonds()) {
                for (VdsNetworkInterface i : params.getInterfaces()) {
                    if (bond.getName().equals(i.getBondName())) {
                        ifaceExists = true;
                        break;
                    }
                }
                if (!ifaceExists) {
                    addCanDoActionMessage(VdcBllMessages.NETWORK_NO_INTERFACE_FOR_BOND);
                    addCanDoActionMessage(String.format("$BondName %1$s", bond.getName()));
                    retVal = false;
                    break;
                }
            }
        }

        return retVal;
    }

    private boolean checkNetworksValidity() {
        boolean retVal = true;

        // Check that the same network not exists in the removedNetworks list
        T params = getParameters();
        if (params.getRemovedNetworks().size() > 0) {
            for (network net : params.getNetworks()) {
                for (network removedNet : params.getRemovedNetworks()) {
                    if (net.getname().equals(removedNet.getname())) {
                        addCanDoActionMessage(VdcBllMessages.NETWORK_NETWORK_EXISTS_IN_ADD_AND_REMOVE);
                        retVal = false;
                        break;
                    }
                }
            }
        }

        if (retVal) {
            // Check that networks have at least one VdsNetworkInterface or bond
            for (network net : params.getNetworks()) {
                boolean ifaceExists = false;
                for (VdsNetworkInterface i : params.getInterfaces()) {
                    if (net.getname().equals(i.getNetworkName())) {
                        ifaceExists = true;
                        break;
                    }
                }
                if (ifaceExists) {
                    continue;
                }
                for (VdsNetworkInterface bond : params.getBonds()) {
                    if (net.getname().equals(bond.getNetworkName())) {
                        ifaceExists = true;
                        break;
                    }
                }
                if (!ifaceExists) {
                    addCanDoActionMessage(VdcBllMessages.NETWORK_NETWORK_HAVE_NO_INERFACES);
                    addCanDoActionMessage(String.format("$NetworkName %1$s", net.getname()));
                    retVal = false;
                    break;
                }
            }
        }

        return retVal;
    }

    @Override
    protected void executeCommand() {
        T bckndCmdParams = getParameters();
        SetupNetworksVdsCommandParameters vdsCmdParams = new SetupNetworksVdsCommandParameters(
                getVdsId(),
                bckndCmdParams.getNetworks(),
                bckndCmdParams.getRemovedNetworks(),
                bckndCmdParams.getBonds(),
                bckndCmdParams.getRemovedBonds(),
                bckndCmdParams.getInterfaces());
        vdsCmdParams.setForce(bckndCmdParams.isForce());
        vdsCmdParams.setCheckConnectivity(bckndCmdParams.isCheckConnectivity());
        vdsCmdParams.setConectivityTimeout(bckndCmdParams.getConectivityTimeout());

        VDSReturnValue retVal = Backend.getInstance().getResourceManager()
                .RunVdsCommand(VDSCommandType.SetupNetworks, vdsCmdParams);

        if (retVal != null && retVal.getSucceeded()) {
            // Refresh VDS networking to DB
            retVal = Backend
                    .getInstance()
                    .getResourceManager()
                    .RunVdsCommand(VDSCommandType.CollectVdsNetworkData,
                            new VdsIdAndVdsVDSCommandParametersBase(bckndCmdParams.getVdsId()));

            // Update cluster networks (i.e. check if need to activate each new network)
            for (network net : bckndCmdParams.getNetworks()) {
                AttachNetworkToVdsGroupCommand.SetNetworkStatus(getVdsGroupId(), net);
            }
            setSucceeded(true);
        }
    }
}
