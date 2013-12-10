package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.adbroker.AdActionType;
import org.ovirt.engine.core.bll.adbroker.LdapFactory;
import org.ovirt.engine.core.bll.adbroker.LdapSearchByIdParameters;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddGroupParameters;
import org.ovirt.engine.core.common.businessentities.LdapGroup;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;

public class AddGroupCommand<T extends AddGroupParameters>
        extends CommandBase<T> {

    public AddGroupCommand(T params) {
        super(params);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_ADD : AuditLogType.USER_FAILED_ADD_ADUSER;
    }

    @Override
    protected boolean canDoAction() {
        Guid id = null;
        String domain = null;
        if (getParameters().getGroup() != null) {
            addCustomValue("NewUserName", getParameters().getGroup().getname());
            id = getParameters().getGroup().getid();
            domain = getParameters().getGroup().getdomain();
            LdapGroup adGroup =
                    (LdapGroup) LdapFactory.getInstance(domain).runAdAction(AdActionType.GetAdGroupByGroupId,
                    new LdapSearchByIdParameters(domain, id)).getReturnValue();
            if (adGroup == null) {
                addCanDoActionMessage(VdcBllMessages.USER_MUST_EXIST_IN_DIRECTORY);
                return false;
            }
        }

        if (id == null) {
            addCanDoActionMessage(VdcBllMessages.MISSING_DIRECTORY_ELEMENT_ID);
            return false;
        }

        return true;
    }

    @Override
    protected void executeCommand() {
        if (getParameters().getGroup() != null) {
            AdGroupsHandlingCommandBase.initAdGroup(getParameters().getGroup());
        }
        setSucceeded(true);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(
            new PermissionSubject(MultiLevelAdministrationHandler.SYSTEM_OBJECT_ID,
            VdcObjectType.System,
            getActionType().getActionGroup())
        );
    }
}
