package org.ovirt.engine.core.bll;

import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.SetVmTicketParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.vdscommands.SetVmTicketVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDynamicDao;
import org.ovirt.engine.core.utils.Ticketing;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.VmManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SetVmTicketCommand<T extends SetVmTicketParameters> extends VmOperationCommandBase<T> {

    // The log:
    private static final Logger log = LoggerFactory.getLogger(SetVmTicketCommand.class);

    @Inject
    private VmDynamicDao vmDynamicDao;

    @Inject
    private ResourceManager resourceManager;

    private String ticket;
    // This flag is calculated during the authorization phase and indicates if
    // the user needed additional permission in order to connect to the console
    // of the virtual machine:
    private boolean neededPermissions = false;

    public SetVmTicketCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        ticket = parameters.getTicket();
    }

    @Override
    protected void setActionMessageParameters () {
        addValidationMessage(EngineMessage.VAR__ACTION__SET);
        addValidationMessage(EngineMessage.VAR__TYPE__VM_TICKET);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects () {
        final List<PermissionSubject> permissions = super.getPermissionCheckSubjects();
        if (needPermissionForConnectingToConsole()) {
            permissions.add(new PermissionSubject(getVmId(), VdcObjectType.VM, ActionGroup.RECONNECT_TO_VM, EngineMessage.USER_CANNOT_FORCE_RECONNECT_TO_VM));
            neededPermissions = true;
        }
        return permissions;
    }

    String getConsoleUserName() {
        DbUser user = getCurrentUser();
        String domain = user.getDomain();
        String name = user.getLoginName();
        if (StringUtils.isEmpty(name)) {
            return null;
        }
        if (name.contains("@") || StringUtils.isEmpty(domain)) {
            return name;
        }
        return name + "@" + domain;
    }

    /**
     * Checks if the user needs additional permissions in order to connect
     * to the console.
     *
     * @return <code>true</code> if additional permissions are needed,
     *   <code>false</code> otherwise
     */
    private boolean needPermissionForConnectingToConsole() {
        // Check if the virtual machine has the flag that allows forced connection to
        // any user, in that case no additional permission is needed:
        final VM vm = getVm();
        if (vm == null || vm.getAllowConsoleReconnect()) {
            return false;
        }

        // If this is not the first user to connect to the console then it does need
        // additional permissions:
        final Guid currentId = getCurrentUser().getId();
        final Guid previousId = vm.getConsoleUserId();
        if (previousId != null && !previousId.equals(currentId)) {
            log.warn("User '{}' is trying to take the console of virtual machine '{}', but the console is already"
                            + " taken by user '{}'.",
                    currentId, vm.getId(), previousId);
            return true;
        }

        // If we are here then the user is the first to connect to the console, so no
        // additional permissions are needed:
        return false;
    }

    @Override
    protected boolean validate() {
        // Check that the virtual machine exists:
        final VM vm = getVm();
        if (vm == null) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_VM_NOT_FOUND);

            return false;
        }

        if (!canRunActionOnNonManagedVm()) {
            return false;
        }

        // Check that the virtual machine is in state that allows connections
        // to the console:
        final VMStatus status = vm.getStatus();
        if (status != VMStatus.Up && status != VMStatus.Paused && status != VMStatus.PoweringUp && status != VMStatus.PoweringDown && status != VMStatus.RebootInProgress) {
            return failVmStatusIllegal();
        }

        // Nothing else, all checks have been performed using permission
        // subjects:
        return true;
    }

    @Override
    protected void perform() {
        // Generate the ticket if needed (in some situations the client will not send
        // a ticket):
        if (StringUtils.isEmpty(ticket)) {
            ticket = Ticketing.generateOTP();
        }

        VmManager vmManager = resourceManager.getVmManager(getParameters().getVmId());
        vmManager.lockVm();

        try {
            // Update the dynamic information of the virtual machine in memory (we need it
            // to update the database later):
            final VM vm = getVm();
            final DbUser user = getCurrentUser();
            vm.setConsoleUserId(user.getId());
            vm.setConsoleCurrentUserName(getConsoleUserName());

            // If the virtual machine has the allow reconnect flag or the user
            // needed additional permissions to connect to the console then we just
            // have to save the user id to the database, regardless of what was
            // there before and without locking.
            //
            // Note that the fact that the user needed permissions actually means
            // that it has them, otherwise we will not be here, performing the
            // operation.
            //
            // In any other situation we try to save the new user to the database
            // and proceed only if the previous user in the database is null. This
            // is needed to prevent races between different users trying to access
            // the console of the same virtual machine simultaneously.
            if (vm.getAllowConsoleReconnect() || neededPermissions) {
                vmManager.update(vm.getDynamicData());
                sendTicket();
            } else {
                final boolean saved = vmDynamicDao.updateConsoleUserWithOptimisticLocking(vm.getDynamicData());
                if (saved) {
                    sendTicket();
                } else {
                    dontSendTicket();
                }
            }
        } finally {
            vmManager.unlockVm();
        }
    }

    /**
     * Don't send the ticket to the virtual machine and send a warning indicating
     * that two users are trying to connect to the console of the virtual machine
     * simultaneously.
     */
    private void dontSendTicket() {
        // Send messages to the log explaining the situation:
        final VM vm = getVm();
        final DbUser user = getCurrentUser();
        log.warn("Can't give console of virtual machine '{}' to user '{}', it has probably been taken by another user.",
                vm.getId(), user.getId());

        // Set the result messages indicating that the operation failed:
        addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_VM_IN_USE_BY_OTHER_USER);

        // The command failed:
        setSucceeded(false);
    }

    /**
     * Send a previously generated ticket to the virtual machine. If sending
     * the ticket fails the command will be marked as failed.
     */
    private void sendTicket() {
        // Send the ticket to the virtual machine:
        if (getVm().getOrigin() == OriginType.KUBEVIRT) {
            setActionReturnValue(ticket);
            setSucceeded(true);
            return;
        }
        final DbUser user = getCurrentUser();
        final boolean sent =
                runVdsCommand(VDSCommandType.SetVmTicket,
                        new SetVmTicketVDSCommandParameters(getVdsId(), getVmId(),
                                ticket, getParameters().getValidTime(),
                                user.getLoginName(), user.getId(),
                                getParameters().getGraphicsType(), getVm().getConsoleDisconnectAction(),
                                getVm().getCompatibilityVersion(), getVm().getConsoleDisconnectActionDelay())).getSucceeded();

        // Return the ticket only if sending it to the virtual machine succeeded:
        if (sent) {
            setActionReturnValue(ticket);
        }
        setSucceeded(sent);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.VM_SET_TICKET : AuditLogType.VM_SET_TICKET_FAILED;
    }
}
