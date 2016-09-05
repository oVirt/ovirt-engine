package org.ovirt.engine.core.bll.network.cluster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.action.AttachNetworkToVdsGroupParameter;
import org.ovirt.engine.core.common.action.ManageNetworkClustersParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.compat.Version;

@NonTransactiveCommandAttribute
public class AttachNetworkToVdsGroupCommand extends NetworkClusterCommandBase<AttachNetworkToVdsGroupParameter> {

    @Inject
    private AttachNetworkClusterPermissionsChecker permissionsChecker;

    public AttachNetworkToVdsGroupCommand(AttachNetworkToVdsGroupParameter attachNetworkToVdsGroupParameter,
            CommandContext cmdContext) {
        super(attachNetworkToVdsGroupParameter, cmdContext);
    }

    public AttachNetworkToVdsGroupCommand(AttachNetworkToVdsGroupParameter attachNetworkToVdsGroupParameter) {
        super(attachNetworkToVdsGroupParameter);
    }

    protected Version getClusterVersion() {
        return getVdsGroup().getCompatibilityVersion();
    }

    @Override
    protected void executeCommand() {

        final AttachNetworkToVdsGroupParameter attachNetworkToVdsGroupParameter = getParameters();

        final VdcReturnValueBase returnValue =
                runInternalAction(VdcActionType.AttachNetworkToClusterInternal, attachNetworkToVdsGroupParameter);

        setSucceeded(returnValue.getSucceeded());

        if (returnValue.getSucceeded()) {
            attachLabeledNetwork();
        } else {
            propagateFailure(returnValue);
        }
    }

    private void attachLabeledNetwork() {
        final AttachNetworkToVdsGroupParameter attachNetworkToVdsGroupParameter = getParameters();

        runInternalAction(
                VdcActionType.PropagateLabeledNetworksToClusterHosts,
                new ManageNetworkClustersParameters(new ArrayList<>(Collections.singleton(attachNetworkToVdsGroupParameter.getNetworkCluster()))));
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return permissionsChecker.findPermissionCheckSubjects(
                getNetworkCluster(),
                getActionType());
    }

    @Override
    protected boolean checkPermissions(final List<PermissionSubject> permSubjects) {
        return permissionsChecker.checkPermissions(this, permSubjects);
    }

}
