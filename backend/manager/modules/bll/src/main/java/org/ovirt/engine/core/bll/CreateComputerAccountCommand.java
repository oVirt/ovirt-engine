package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.adbroker.AdActionType;
import org.ovirt.engine.core.bll.adbroker.LdapCreateComputerAccountParameters;
import org.ovirt.engine.core.bll.adbroker.LdapFactory;
import org.ovirt.engine.core.bll.adbroker.LdapIsComputerWithSameNameExistsParameters;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.PermissionSubject;
import org.ovirt.engine.core.common.action.CreateComputerAccountParameters;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.RefObject;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

// Wrong domain handling in IsComputerWithTheSameNameExists ad command
public class CreateComputerAccountCommand<T extends CreateComputerAccountParameters> extends CommandBase<T> {
    public CreateComputerAccountCommand(T parameters) {
        super(parameters);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.AD_COMPUTER_ACCOUNT_SUCCEEDED : AuditLogType.AD_COMPUTER_ACCOUNT_FAILED;
    }

    private String privateComputerName;

    public String getComputerName() {
        return privateComputerName;
    }

    private void setComputerName(String value) {
        privateComputerName = value;
    }

    @Override
    protected boolean canDoAction() {
        boolean returnValue = true;
        String user = null;
        String password = null;
        String domain = null;
        Guid vmId = getParameters().getVmId();
        RefObject<String> tempRefObject = new RefObject<String>(user);
        RefObject<String> tempRefObject2 = new RefObject<String>(password);
        RefObject<String> tempRefObject3 = new RefObject<String>(domain);
        GetActualCredentials(tempRefObject, tempRefObject2, tempRefObject3);
        user = tempRefObject.argvalue;
        password = tempRefObject2.argvalue;
        domain = tempRefObject3.argvalue;
        VmStatic vmStatic = DbFacade.getInstance().getVmStaticDAO().get(vmId);
        if (vmStatic != null) {
            setComputerName(vmStatic.getvm_name());
            returnValue = ((Boolean) LdapFactory
                    .getInstance(getParameters().getDomain())
                    .RunAdAction(AdActionType.IsComputerWithTheSameNameExists,
                            new LdapIsComputerWithSameNameExistsParameters(domain, user, password, getComputerName()))
                    .getReturnValue()).booleanValue();
            if (returnValue) {
                addCanDoActionMessage(VdcBllMessages.DIRECTORY_COMPUTER_WITH_THE_SAME_NAME_ALREADY_EXITS);
            }
        } else {
            addCanDoActionMessage(VdcBllMessages.VAR__ACTION__CREATE);
            addCanDoActionMessage(VdcBllMessages.VAR__TYPE__COMPUTER_ACCOUNT);
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_NOT_EXIST);

        }
        return !returnValue;
    }

    @Override
    protected void executeCommand() {
        String user = null;
        String password = null;
        String domain = null;
        RefObject<String> tempRefObject = new RefObject<String>(user);
        RefObject<String> tempRefObject2 = new RefObject<String>(password);
        RefObject<String> tempRefObject3 = new RefObject<String>(domain);
        GetActualCredentials(tempRefObject, tempRefObject2, tempRefObject3);
        user = tempRefObject.argvalue;
        password = tempRefObject2.argvalue;
        domain = tempRefObject3.argvalue;
        setSucceeded(LdapFactory
                .getInstance(getParameters().getDomain())
                .RunAdAction(
                        AdActionType.CreateComputerAccount,
                        new LdapCreateComputerAccountParameters(domain, user, password, getParameters().getPath(),
                                getComputerName())).getSucceeded());
    }

    /**
     * Gets the actual credentials.
     *
     * @param user
     *            The user.
     * @param password
     *            The password.
     * @param domain
     *            The domain.
     */
    private void GetActualCredentials(RefObject<String> user, RefObject<String> password, RefObject<String> domain) {
        user.argvalue = StringHelper.isNullOrEmpty(getParameters().getUserName()) ? getCurrentUser().getUserName()
                : getParameters().getUserName();

        password.argvalue = StringHelper.isNullOrEmpty(getParameters().getUserPassword()) ? getCurrentUser()
                .getPassword() : getParameters().getUserPassword();

        domain.argvalue = StringHelper.isNullOrEmpty(getParameters().getDomain()) ? getCurrentUser()
                .getDomainControler() : getParameters().getDomain();

    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        // Not needed for admin operations.
        return Collections.emptyList();
    }
}
