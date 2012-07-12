package org.ovirt.engine.core.bll;

import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.UpdateNetworkToVdsParameters;
import org.ovirt.engine.core.common.businessentities.NetworkBootProtocol;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsNetworkInterface;
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
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.InterfaceDAO;
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
    private String oldNetworkName;

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
                    if (StringUtils.equals(ix.getBondName(), NetworkUtils.StripVlan(i.getName()))) {
                        interfaceNames.add(NetworkUtils.StripVlan(ix.getName()));
                    }
                }
            } else {
                interfaceNames.add(NetworkUtils.StripVlan(i.getName()));
            }
        }

        NetworkVdsmVDSCommandParameters parameters =
                new NetworkVdsmVDSCommandParameters(getParameters().getVdsId(),
                        getParameters().getNetwork().getname(),
                        getParameters().getNetwork().getvlan_id(),
                        getParameters().getBondName(),
                        interfaceNames.toArray(new String[] {}),
                        address,
                        subnet,
                        gateway,
                        getParameters().getNetwork().getstp(),
                        getParameters().getBondingOptions(),
                        getParameters().getBootProtocol());

        parameters.setVmNetwork(getParameters().getNetwork().isVmNetwork());
        parameters.setOldNetworkName(oldNetworkName);
        parameters.setConnectionTimeout(Config.<Integer> GetValue(ConfigValues.NetworkConnectivityCheckTimeoutInSeconds));
        parameters.setCheckConnectivity(getParameters().getCheckConnectivity());

        IPAddress[] adresses = Dns.GetHostAddresses(NetworkUtils.OS_REFERENCE_TO_MACHINE_NAME);
        if (adresses != null && adresses.length > 0) {
            parameters.setHostAddr(adresses[0].toString());
        }

        if (getParameters().getCheckConnectivity()) {
            ThreadPoolUtil.execute(new EditNetworkThread(parameters));
            PollVds(getParameters().getVdsId());
        } else {
            EditNetworkThreadCompat(parameters);
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
                AttachNetworkToVdsGroupCommand.SetNetworkStatus(groupId, getParameters().getNetwork());
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
            EditNetworkThreadCompat(parameters);
        }
    }

    private void EditNetworkThreadCompat(NetworkVdsmVDSCommandParameters parameters) {
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

    protected void PollVds(Guid vdsId) {
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
        interfaces = getInterfaceDAO().getAllInterfacesForVds(getParameters().getVdsId());
        VdsNetworkInterface updatedIface = null;

        // check that interface exists
        for (final VdsNetworkInterface i : getParameters().getInterfaces()) {
            updatedIface = LinqUtils.firstOrNull(interfaces, new Predicate<VdsNetworkInterface>() {
                @Override
                public boolean eval(VdsNetworkInterface x) {
                    return x.getName().equals(i.getName());
                }
            });
            if (updatedIface == null) {
                addCanDoActionMessage(VdcBllMessages.NETWORK_INTERFACE_NOT_EXISTS);
                return false;
            }
            ifaceGateway = updatedIface.getGateway();
        }

        // obtain the network name from the host interface and check that the old network name is not null
        oldNetworkName = StringUtils.isEmpty(updatedIface.getNetworkName()) ? null : updatedIface.getNetworkName();
        if (oldNetworkName == null) {
            addCanDoActionMessage(VdcBllMessages.NETWROK_OLD_NETWORK_NOT_SPECIFIED);
            return false;
        }

        VDS vds = getVdsDAO().get(getParameters().getVdsId());
        if (vds.getstatus() != VDSStatus.Maintenance) {
            // check that the new network doesn't exist in host
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
                addCanDoActionMessage(VdcBllMessages.NETWROK_HOST_IS_BUSY);
                return false;
            }
        }

        if (StringUtils.equals(oldNetworkName, NetworkUtils.getEngineNetwork())
                && !StringUtils.equals(getParameters().getNetwork().getname(), NetworkUtils.getEngineNetwork())) {
            getReturnValue().getCanDoActionMessages()
                    .add(VdcBllMessages.NETWORK_DEFAULT_UPDATE_NAME_INVALID.toString());
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
            Pattern IP_PATTERN = Pattern
                    .compile("^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}$");
            if (StringUtils.isEmpty(getParameters().getAddress())) {
                addCanDoActionMessage(VdcBllMessages.NETWROK_ADDR_MANDATORY_IN_STATIC_IP);
                return false;
            }
            if (!IP_PATTERN.matcher(getParameters().getAddress()).matches()) {
                addCanDoActionMessage(VdcBllMessages.NETWROK_ADDR_IN_STATIC_IP_BAD_FORMAT);
                return false;
            }
            if (StringUtils.isNotEmpty(getParameters().getGateway())
                    && !IP_PATTERN.matcher(getParameters().getGateway()).matches()) {
                addCanDoActionMessage(VdcBllMessages.NETWROK_ADDR_IN_GATEWAY_BAD_FORMAT);
                return false;
            }
            if (StringUtils.isNotEmpty(getParameters().getSubnet())
                    && !IP_PATTERN.matcher(getParameters().getSubnet()).matches()) {
                addCanDoActionMessage(VdcBllMessages.NETWROK_ADDR_IN_SUBNET_BAD_FORMAT);
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

    protected InterfaceDAO getInterfaceDAO() {
        return DbFacade.getInstance().getInterfaceDAO();
    }
}
