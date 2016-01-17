package org.ovirt.engine.core.bll.network.cluster;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.action.AttachNetworkToClusterParameter;
import org.ovirt.engine.core.common.action.ManageNetworkClustersParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.compat.Version;

@NonTransactiveCommandAttribute
public class AttachNetworkToClusterCommand extends NetworkClusterCommandBase<AttachNetworkToClusterParameter> {

    @Inject
    private AttachNetworkClusterPermissionsChecker permissionsChecker;

    public AttachNetworkToClusterCommand(AttachNetworkToClusterParameter attachNetworkToClusterParameter,
            CommandContext cmdContext) {
        super(attachNetworkToClusterParameter, cmdContext);
    }

    protected Version getClusterVersion() {
        return getCluster().getCompatibilityVersion();
    }

    @Override
    protected void executeCommand() {

        final AttachNetworkToClusterParameter attachNetworkToClusterParameter = getParameters();

        final VdcReturnValueBase returnValue =
                runInternalAction(VdcActionType.AttachNetworkToClusterInternal, attachNetworkToClusterParameter);

        setSucceeded(returnValue.getSucceeded());

        if (returnValue.getSucceeded()) {
            attachLabeledNetwork();
        } else {
            propagateFailure(returnValue);
        }
    }

    private void attachLabeledNetwork() {
        final AttachNetworkToClusterParameter attachNetworkToClusterParameter = getParameters();

        runInternalAction(
                VdcActionType.PropagateLabeledNetworksToClusterHosts,
                new ManageNetworkClustersParameters(Collections.singleton(attachNetworkToClusterParameter.getNetworkCluster())));
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
