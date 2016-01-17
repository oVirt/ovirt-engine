package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.EventNotificationMethod;
import org.ovirt.engine.core.common.action.EventSubscriptionParametesBase;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class RemoveEventSubscriptionCommand<T extends EventSubscriptionParametesBase> extends
        EventSubscriptionCommandBase<T> {

    public RemoveEventSubscriptionCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected boolean validate() {
        boolean retValue;
        // get notification method
        EventNotificationMethod event_notification_method = getParameters().getEventSubscriber().getEventNotificationMethod();
        if (event_notification_method != null) {
            // Validate user
            DbUser user =
                    DbFacade.getInstance()
                            .getDbUserDao()
                            .get(getParameters().getEventSubscriber().getSubscriberId());
            if (user == null) {
                addValidationMessage(EngineMessage.USER_MUST_EXIST_IN_DB);
                retValue = false;
            } else {
                retValue = validateRemove(event_notification_method, getParameters().getEventSubscriber(), user);
            }
        } else {
            addValidationMessage(EngineMessage.EN_UNKNOWN_NOTIFICATION_METHOD);
            retValue = false;
        }
        return retValue;
    }

    @Override
    protected void executeCommand() {
        DbFacade.getInstance().getEventDao().unsubscribe(getParameters().getEventSubscriber());
        setSucceeded(true);
    }
}
