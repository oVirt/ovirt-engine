package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.adbroker.AdActionType;
import org.ovirt.engine.core.bll.adbroker.LdapCreateComputerAccountParameters;
import org.ovirt.engine.core.bll.adbroker.LdapFactory;
import org.ovirt.engine.core.bll.adbroker.LdapIsComputerWithSameNameExistsParameters;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.action.CreateComputerAccountParameters;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

// Wrong domain handling in IsComputerWithTheSameNameExists ad command
@SuppressWarnings("serial")
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
        final VmStatic vmStatic = DbFacade.getInstance().getVmStaticDao().get(getParameters().getVmId());
        if (vmStatic != null) {
            setComputerName(vmStatic.getName());
            returnValue =
                    ((Boolean) LdapFactory
                            .getInstance(getParameters().getDomain())
                            .RunAdAction(AdActionType.IsComputerWithTheSameNameExists,
                                    new LdapIsComputerWithSameNameExistsParameters(getActualDomain(),
                                            getActualUser(),
                                            getActualPassword(),
                                            getComputerName()))
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
        setSucceeded(LdapFactory
                .getInstance(getParameters().getDomain())
                .RunAdAction(
                        AdActionType.CreateComputerAccount,
                        new LdapCreateComputerAccountParameters(getActualDomain(),
                                getActualUser(),
                                getActualPassword(),
                                getParameters().getPath(),
                                getComputerName()))
                .getSucceeded());
    }

    String getActualUser() {
        return StringUtils.isEmpty(getParameters().getUserName()) ? getCurrentUser().getUserName()
                : getParameters().getUserName();
    }

    String getActualPassword() {
        return StringUtils.isEmpty(getParameters().getUserPassword()) ? getCurrentUser()
                .getPassword() : getParameters().getUserPassword();
    }

    String getActualDomain() {
        return StringUtils.isEmpty(getParameters().getDomain()) ? getCurrentUser()
                .getDomainControler() : getParameters().getDomain();
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        // Not needed for admin operations.
        return Collections.emptyList();
    }
}
