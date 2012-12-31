package org.ovirt.engine.core.bll.network.cluster;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.VdsGroupCommandBase;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AttachNetworkToVdsGroupParameter;
import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.SearchReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogField;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogFields;
import org.ovirt.engine.core.utils.NetworkUtils;

@SuppressWarnings("serial")
@CustomLogFields({ @CustomLogField("NetworkName") })
public class DetachNetworkToVdsGroupCommand<T extends AttachNetworkToVdsGroupParameter> extends
        VdsGroupCommandBase<T> {
    public DetachNetworkToVdsGroupCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeCommand() {
        getNetworkClusterDAO().remove(getParameters().getVdsGroupId(), getParameters().getNetwork().getId());
        setSucceeded(true);
    }

    @Override
    protected boolean canDoAction() {
        // check that we are not removing the management network
        if (StringUtils.equals(getParameters().getNetwork().getname(),
                Config.<String> GetValue(ConfigValues.ManagementNetwork))) {
            addCanDoActionMessage(VdcBllMessages.NETWORK_CANNOT_REMOVE_MANAGEMENT_NETWORK);
            getReturnValue().getCanDoActionMessages().add(String.format("$NetworkName %1$s",
                Config.<String> GetValue(ConfigValues.ManagementNetwork)));
            return false;
        }

        // check that there is no vm running with this network
        List<VmStatic> vms = getVmStaticDAO().getAllByGroupAndNetworkName(
                getParameters().getVdsGroupId(), getParameters().getNetwork().getname());
        if (vms.size() > 0) {
            addCanDoActionMessage(VdcBllMessages.NETWORK_CANNOT_REMOVE_NETWORK_IN_USE_BY_VM);
            getReturnValue().getCanDoActionMessages().add(String.format("$NetworkName %1$s",
                    getParameters().getNetwork().getname()));
            return false;
        }

        // check that no template is using this network
        List<VmTemplate> templates = getVmTemplateDAO().getAllForVdsGroup(getParameters().getVdsGroupId());
        for (VmTemplate tmpl : templates) {
            List<VmNetworkInterface> interfaces = getVmNetworkInterfaceDao().getAllForTemplate(tmpl.getId());
            if (networkUsedByAnInterface(interfaces)) {
                addCanDoActionMessage(VdcBllMessages.NETWORK_CANNOT_REMOVE_NETWORK_IN_USE_BY_TEMPLATE);
                return false;
            }
        }

        // check if network in use by vm
        String query = "Vms: cluster = " + getVdsGroup().getname();
        SearchParameters searchParams = new SearchParameters(query, SearchType.VM);
        searchParams.setMaxCount(Integer.MAX_VALUE);
        VdcQueryReturnValue tempVar = Backend.getInstance().runInternalQuery(VdcQueryType.Search,
                searchParams);
        SearchReturnValue ret = (SearchReturnValue) ((tempVar instanceof SearchReturnValue) ? tempVar
                : null);
        if (ret != null && ret.getSucceeded()) {
            @SuppressWarnings("unchecked")
            List<IVdcQueryable> vmList = (List<IVdcQueryable>) ret.getReturnValue();
            for (IVdcQueryable vm_helper : vmList) {
                VM vm = (VM) vm_helper;
                List<VmNetworkInterface> interfaces = getVmNetworkInterfaceDao().getAllForVm(vm.getId());
                if (networkUsedByAnInterface(interfaces)) {
                    addCanDoActionMessage(VdcBllMessages.NETWORK_INTERFACE_IN_USE_BY_VM);
                    return false;
                }
            }
        }

        if (getParameters().getNetwork().getname().equals(NetworkUtils.getEngineNetwork())) {
            getReturnValue().getCanDoActionMessages()
                    .add(VdcBllMessages.NETWORK_DEFAULT_UPDATE_NAME_INVALID.toString());
            getReturnValue().getCanDoActionMessages().add(String.format("$NetworkName %1$s",
                    Config.<String> GetValue(ConfigValues.ManagementNetwork)));
            return false;
        }

        return true;
    }

    /**
     * Check if the given list has a {@link VmNetworkInterface} with the Network name that is being detached.
     */
    private boolean networkUsedByAnInterface(List<VmNetworkInterface> interfaces) {
        for (VmNetworkInterface vmNetworkInterface : interfaces) {
            if (getNetworkName().equals(vmNetworkInterface.getNetworkName())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.NETWORK_DETACH_NETWORK_TO_VDS_GROUP
                : AuditLogType.NETWORK_DETACH_NETWORK_TO_VDS_GROUP_FAILED;
    }

    public String getNetworkName() {
        return getParameters().getNetwork().getname();
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__DETACH);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__NETWORK);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        Guid networkId = getParameters().getNetwork() == null ? null : getParameters().getNetwork().getId();
        return Collections.singletonList(new PermissionSubject(networkId,
                VdcObjectType.Network,
                getActionType().getActionGroup()));
    }
}
