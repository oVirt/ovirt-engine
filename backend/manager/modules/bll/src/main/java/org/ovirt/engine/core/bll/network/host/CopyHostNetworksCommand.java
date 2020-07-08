package org.ovirt.engine.core.bll.network.host;

import static org.ovirt.engine.core.common.AuditLogType.NETWORK_COPY_HOST_NETWORKS_INVALID_IFACE_COUNT;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.VdsCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.CopyHostNetworksParameters;
import org.ovirt.engine.core.common.action.HostSetupNetworksParameters;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkAttachmentDao;

@NonTransactiveCommandAttribute
public class CopyHostNetworksCommand<T extends CopyHostNetworksParameters> extends VdsCommand<T> {

    private static final String SOURCE_HOST = "sourcehost";
    private static final String DESTINATION_HOST = "destinationhost";

    @Inject
    VdsStaticDao vdsStaticDao;

    @Inject
    InterfaceDao interfaceDao;

    @Inject
    NetworkAttachmentDao networkAttachmentDao;

    private VdsStatic sourceHost;
    private VdsStatic destinationHost;
    private CopyHostNetworksHelper copyHostNetworksHelper;

    public CopyHostNetworksCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        CopyHostNetworksHelper helper = getCopyHostNetworksHelper();
        helper.buildDestinationConfig();

        var setupNetworksParameters = new HostSetupNetworksParameters(getParameters().getVdsId());
        setupNetworksParameters.setRemovedBonds(helper.getBondsToRemove());
        setupNetworksParameters.setRemovedNetworkAttachments(helper.getAttachmentsToRemove());
        setupNetworksParameters.setNetworkAttachments(helper.getAttachmentsToApply());
        setupNetworksParameters.setCreateOrUpdateBonds(helper.getBondsToApply());
        setupNetworksParameters.setCommitOnSuccess(true);

        ActionReturnValue returnValue = runInternalAction(ActionType.HostSetupNetworks,
                setupNetworksParameters,
                cloneContextAndDetachFromParent());

        if (!returnValue.getSucceeded()) {
            propagateFailure(returnValue);
        }
        setSucceeded(returnValue.getSucceeded());
    }

    @Override
    protected boolean validate() {
        return validateHostId(getParameters().getSourceHostId()) &&
                validateHostId(getParameters().getVdsId()) &&
                validateHost(getSourceHost()) &&
                validateHost(getDestinationHost()) &&
                validateClusterId() &&
                validateInterfaceCount();
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__COPY_NETWORKS);
        addValidationMessage(EngineMessage.VAR__TYPE__HOST);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionList = new ArrayList<>();
        permissionList.add(new PermissionSubject(getParameters().getVdsId(),
                VdcObjectType.VDS,
                getActionType().getActionGroup()));
        permissionList.add(new PermissionSubject(getParameters().getSourceHostId(),
                VdcObjectType.VDS,
                getActionType().getActionGroup()));

        return permissionList;
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = super.getJobMessageProperties();
            VdsStatic sourceHost = getSourceHost();
            if (sourceHost != null) {
                jobProperties.put(SOURCE_HOST, sourceHost.getName());
            }
            VdsStatic destinationHost = getDestinationHost();
            if (destinationHost != null) {
                jobProperties.put(DESTINATION_HOST, destinationHost.getName());
            }
        }
        return jobProperties;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        addCustomValue(SOURCE_HOST, getSourceHost().getName());
        addCustomValue(DESTINATION_HOST, getDestinationHost().getName());
        return getSucceeded() ?
                AuditLogType.NETWORK_COPY_HOST_NETWORKS :
                AuditLogType.NETWORK_COMMINT_NETWORK_CHANGES_FAILED;
    }

    private boolean validateHostId(Guid id) {
        return validate(ValidationResult.failWith(EngineMessage.HOST_ID_IS_NULL).when(id == null));
    }

    private boolean validateHost(VdsStatic vds) {
        return validate(ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_HOST_NOT_EXIST).when(vds == null));
    }

    private boolean validateClusterId() {
        VdsStatic sourceHost = getSourceHost();
        VdsStatic destinationHost = getDestinationHost();
        return validate(ValidationResult.failWith(EngineMessage.HOSTS_NOT_IN_SAME_CLUSTER)
                .when(!sourceHost.getClusterId().equals(destinationHost.getClusterId())));
    }

    private boolean validateInterfaceCount() {
        boolean valid = validate(ValidationResult.failWith(EngineMessage.INTERFACE_COUNT_DOES_NOT_MATCH)
                .when(getCopyHostNetworksHelper().getSourceNicsCount()
                        > getCopyHostNetworksHelper().getDestinationNicsCount()));
        if (!valid) {
            auditLog(auditEventMismatchingInterfaceCount(), NETWORK_COPY_HOST_NETWORKS_INVALID_IFACE_COUNT);
        }
        return valid;
    }

    private VdsStatic getSourceHost() {
        if (sourceHost == null) {
            sourceHost = vdsStaticDao.get(getParameters().getSourceHostId());
        }
        return sourceHost;
    }

    private VdsStatic getDestinationHost() {
        if (destinationHost == null) {
            destinationHost = vdsStaticDao.get(getParameters().getVdsId());
        }
        return destinationHost;
    }

    private CopyHostNetworksHelper getCopyHostNetworksHelper() {
        if (copyHostNetworksHelper == null) {
            Guid sourceId = getParameters().getSourceHostId();
            Guid destinationId = getParameters().getVdsId();
            copyHostNetworksHelper = new CopyHostNetworksHelper(
                    interfaceDao.getAllInterfacesForVds(sourceId),
                    networkAttachmentDao.getAllForHost(sourceId),
                    interfaceDao.getAllInterfacesForVds(destinationId),
                    networkAttachmentDao.getAllForHost(destinationId)
            );
        }
        return copyHostNetworksHelper;
    }

    private AuditLogable auditEventMismatchingInterfaceCount() {
        AuditLogable event = new AuditLogableImpl();
        event.setVdsId(getVdsId());
        event.addCustomValue(SOURCE_HOST, getSourceHost().getName());
        event.addCustomValue(DESTINATION_HOST, getDestinationHost().getName());
        return event;

    }

}
