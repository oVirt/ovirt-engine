package org.ovirt.engine.core.bll.network.host;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.network.cluster.NetworkClusterHelper;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.UpdateNetworkToVdsParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.network.NetworkBootProtocol;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcFault;
import org.ovirt.engine.core.common.vdscommands.NetworkVdsmVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsIdAndVdsVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.utils.IPAddress;
import org.ovirt.engine.core.utils.NetworkUtils;
import org.ovirt.engine.core.utils.dns.Dns;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;

@SuppressWarnings("serial")
public class UpdateNetworkToVdsInterfaceCommand<T extends UpdateNetworkToVdsParameters> extends VdsNetworkCommand<T> {

    private List<VdsNetworkInterface> interfaces;
    private VDSReturnValue retVal;
    private boolean editNetworkDone = false;
    private boolean editNetworkThreadFinish = false;

    public UpdateNetworkToVdsInterfaceCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeCommand() {
        String address = getParameters().getAddress();
        String subnet = StringUtils.isEmpty(getParameters().getSubnet()) ? getParameters().getNetwork()
                .getsubnet() : getParameters().getSubnet();
        String gateway = StringUtils.isEmpty(getParameters().getGateway()) ? "" : getParameters().getGateway();
        java.util.ArrayList<String> interfaceNames = new java.util.ArrayList<String>();
        for (VdsNetworkInterface i : getParameters().getInterfaces()) {
            if (i.getBonded() != null && i.getBonded() || NetworkUtils.IsBondVlan(interfaces, i)) {
                getParameters().setBondName(NetworkUtils.StripVlan(i.getName()));
                for (VdsNetworkInterface ix : interfaces) {
                    if (NetworkUtils.interfaceBasedOn(i.getName(), ix.getBondName())) {
                        interfaceNames.add(NetworkUtils.StripVlan(ix.getName()));
                    }
                }
            } else {
                interfaceNames.add(NetworkUtils.StripVlan(i.getName()));
            }
        }

        NetworkVdsmVDSCommandParameters parameters = new NetworkVdsmVDSCommandParameters(getParameters().getVdsId(),
                getParameters().getNetwork().getname(), getParameters().getNetwork().getvlan_id(), getParameters()
                        .getBondName(), interfaceNames.toArray(new String[] {}), address, subnet, gateway,
                getParameters().getNetwork().getstp(), getParameters().getBondingOptions(), getParameters()
                        .getBootProtocol());
        parameters.setVmNetwork(getParameters().getNetwork().isVmNetwork());
        parameters.setOldNetworkName(getParameters().getOldNetworkName());
        parameters.setConnectionTimeout(Config.<Integer> GetValue(ConfigValues.NetworkConnectivityCheckTimeoutInSeconds));
        parameters.setCheckConnectivity(getParameters().getCheckConnectivity());

        IPAddress[] adresses = Dns.GetHostAddresses(NetworkUtils.OS_REFERENCE_TO_MACHINE_NAME);
        if (adresses != null && adresses.length > 0) {
            parameters.setHostAddr(adresses[0].toString());
        }

        if (getParameters().getCheckConnectivity()) {
            ThreadPoolUtil.execute(new EditNetworkThread(parameters));
            pollVds(getParameters().getVdsId());
        } else {
            editNetworkThreadCompat(parameters);
        }

        if (retVal != null && editNetworkDone) {
            // update vds network data
            retVal = Backend
                    .getInstance()
                    .getResourceManager()
                    .RunVdsCommand(VDSCommandType.CollectVdsNetworkData,
                            new VdsIdAndVdsVDSCommandParametersBase(getParameters().getVdsId()));

            if (retVal.getSucceeded()) {
                Guid groupId = getVdsDAO().get(getParameters().getVdsId()).getvds_group_id();
                NetworkClusterHelper.setStatus(groupId, getParameters().getNetwork());
                setSucceeded(true);
            }
        }
    }

    private class EditNetworkThread implements Runnable {
        private final NetworkVdsmVDSCommandParameters parameters;

        public EditNetworkThread(NetworkVdsmVDSCommandParameters parameters) {
            this.parameters = parameters;
        }

        @Override
        public void run() {
            editNetworkThreadCompat(parameters);
        }
    }

    private void editNetworkThreadCompat(NetworkVdsmVDSCommandParameters parameters) {
        try {
            retVal = Backend.getInstance().getResourceManager().RunVdsCommand(VDSCommandType.EditNetwork, parameters);
            editNetworkDone = true;
        } catch (RuntimeException e) {
            if (e instanceof VdcBLLException) {
                getReturnValue().setFault(new VdcFault(e, ((VdcBLLException) e).getVdsError().getCode()));
            }
        } catch (java.lang.Exception e) {
        } finally {
            editNetworkThreadFinish = true;
        }
    }

    protected void pollVds(Guid vdsId) {
        int retries = 10;
        while (retries > 0 && retVal == null && !editNetworkThreadFinish) {
            retries--;
            try {
                Backend.getInstance()
                        .getResourceManager()
                        .RunVdsCommand(VDSCommandType.CollectVdsNetworkData,
                                new VdsIdAndVdsVDSCommandParametersBase(vdsId));
            } catch (java.lang.Exception e) {
            }
        }
    }

    @Override
    protected boolean canDoAction() {
        String ifaceGateway = null;
        interfaces = getDbFacade().getInterfaceDao().getAllInterfacesForVds(getParameters().getVdsId());

        // check that interface exists
        for (final VdsNetworkInterface i : getParameters().getInterfaces()) {
            VdsNetworkInterface iface = LinqUtils.firstOrNull(interfaces, new Predicate<VdsNetworkInterface>() {
                @Override
                public boolean eval(VdsNetworkInterface x) {
                    return x.getName().equals(i.getName());
                }
            });
            if (iface == null) {
                addCanDoActionMessage(VdcBllMessages.NETWORK_INTERFACE_NOT_EXISTS);
                return false;
            }
            ifaceGateway = iface.getGateway();
        }

        // check that the old network name is not null
        if (StringUtils.isEmpty(getParameters().getOldNetworkName())) {
            addCanDoActionMessage(VdcBllMessages.NETWORK_OLD_NETWORK_NOT_SPECIFIED);
            return false;
        }

        VDS vds = getVdsDAO().get(getParameters().getVdsId());
        if (vds.getstatus() != VDSStatus.Maintenance) {
            // check that the old network exists in host
            VdsNetworkInterface iface = LinqUtils.firstOrNull(interfaces, new Predicate<VdsNetworkInterface>() {
                @Override
                public boolean eval(VdsNetworkInterface i) {
                    if (i.getNetworkName() != null) {
                        return i.getNetworkName().equals(getParameters().getNetwork().getname());
                    }
                    return false;
                }
            });
            if (iface != null) {
                addCanDoActionMessage(VdcBllMessages.NETWORK_HOST_IS_BUSY);
                return false;
            }
        }

        // check that the old network exists in host
        VdsNetworkInterface ifacenet = LinqUtils.firstOrNull(interfaces, new Predicate<VdsNetworkInterface>() {
            @Override
            public boolean eval(VdsNetworkInterface i) {
                if (i.getNetworkName() != null) {
                    return i.getNetworkName().equals(getParameters().getOldNetworkName());
                }
                return false;
            }
        });
        if (ifacenet == null) {
            addCanDoActionMessage(VdcBllMessages.NETWORK_NOT_EXISTS);
            return false;
        }

        if (StringUtils.equals(getParameters().getOldNetworkName(), NetworkUtils.getEngineNetwork())
                && !StringUtils.equals(getParameters().getNetwork().getname(), NetworkUtils.getEngineNetwork())) {
            getReturnValue().getCanDoActionMessages()
                    .add(VdcBllMessages.NETWORK_DEFAULT_UPDATE_NAME_INVALID.toString());
            getReturnValue().getCanDoActionMessages().add(String.format("$NetworkName %1$s",
                    Config.<String> GetValue(ConfigValues.ManagementNetwork)));
            return false;
        }

        if (!NetworkUtils.getEngineNetwork().equals(getParameters().getNetwork().getname())
                && StringUtils.isNotEmpty(getParameters().getGateway())) {
            if (!getParameters().getGateway().equals(ifaceGateway)) {
                addCanDoActionMessage(VdcBllMessages.NETWORK_ATTACH_ILLEGAL_GATEWAY);
                return false;
            }
            // if the gateway didn't change we don't want the vdsm to set it.
            else {
                getParameters().setGateway(null);
            }
        }

        // check conectivity
        getParameters().setCheckConnectivity(getParameters().getCheckConnectivity());
        if (getParameters().getCheckConnectivity()) {
            if (!StringUtils.equals(getParameters().getNetwork().getname(), NetworkUtils.getEngineNetwork())) {
                addCanDoActionMessage(VdcBllMessages.NETWORK_CHECK_CONNECTIVITY);
                return false;
            }
        }

        // check address exists in static ip
        if (getParameters().getBootProtocol() == NetworkBootProtocol.StaticIp) {
            if (StringUtils.isEmpty(getParameters().getAddress())) {
                addCanDoActionMessage(VdcBllMessages.NETWORK_ADDR_MANDATORY_IN_STATIC_IP);
                return false;
            }
        }

        return true;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.NETWORK_ATTACH_NETWORK_TO_VDS
                : AuditLogType.NETWORK_ATTACH_NETWORK_TO_VDS_FAILED;
    }
}
