package org.ovirt.engine.core.bll.network.host;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.network.AddNetworksByLabelParametersBuilder;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.HostInterfaceValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.LabelNicParameters;
import org.ovirt.engine.core.common.action.PersistentSetupNetworksParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;

public class LabelNicCommand<T extends LabelNicParameters> extends CommandBase<T> {

    private VdsNetworkInterface nic;
    private List<Network> labeledNetworks;
    private List<VdsNetworkInterface> hostNics;

    public LabelNicCommand(T parameters) {
        super(parameters);
        setVdsId(getNic() == null ? null : getNic().getVdsId());
    }

    @Override
    protected void executeCommand() {
        addCustomValue("NicName", getNic().getName());

        VdcReturnValueBase result = runInternalAction(VdcActionType.PersistentSetupNetworks,
                createPersistentSetupNetworksParameters(),
                cloneContextAndDetachFromParent());

        if (result.getSucceeded()) {
            getReturnValue().setActionReturnValue(getLabel());
        } else {
            propagateFailure(result);
        }

        setSucceeded(result.getSucceeded());
    }

    private PersistentSetupNetworksParameters createPersistentSetupNetworksParameters() {
        return new AddNetworksByLabelParametersBuilder(getContext())
                .buildParameters(getNic(), getLabel(), getClusterNetworksWithLabel(getLabel()));
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(EngineMessage.VAR__ACTION__ADD);
        addCanDoActionMessage(EngineMessage.VAR__TYPE__LABEL);
    }

    @Override
    protected boolean canDoAction() {
        HostInterfaceValidator validator = new HostInterfaceValidator(getNic());

        return
            validate(validator.interfaceExists()) &&
            validate(validator.interfaceAlreadyLabeledWith(getLabel())) &&
            validate(validator.labeledValidBond(getHostInterfaces())) &&
            validate(validator.addLabelToNicAndValidate(getLabel(), getValidationGroups())) &&
            validate(validator.anotherInterfaceAlreadyLabeledWithThisLabel(getLabel(), getHostInterfaces())) &&
            validate(validator.networksAreAttachedToThisInterface(getHostInterfaces(),
                    getClusterNetworksWithLabel(getLabel())));
    }

    private List<VdsNetworkInterface> getHostInterfaces() {
        if (hostNics == null) {
            hostNics = getDbFacade().getInterfaceDao().getAllInterfacesForVds(getVdsId());
        }

        return hostNics;
    }

    private List<Network> getClusterNetworksWithLabel(String label) {
        if (labeledNetworks == null) {
            labeledNetworks = getNetworkDao().getAllByLabelForCluster(label, getVds().getVdsGroupId());
        }

        return labeledNetworks;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.LABEL_NIC : AuditLogType.LABEL_NIC_FAILED;
    }

    private VdsNetworkInterface getNic() {
        if (nic == null) {
            nic = getDbFacade().getInterfaceDao().get(getParameters().getNicId());
        }

        return nic;
    }

    public String getLabel() {
        return getParameters().getLabel();
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        Guid hostId = getNic() == null ? null : getNic().getVdsId();
        return Collections.singletonList(new PermissionSubject(hostId,
                VdcObjectType.VDS,
                getActionType().getActionGroup()));
    }
}
