package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.EventNotificationMethod;
import org.ovirt.engine.core.common.action.EventSubscriptionParametesBase;
import org.ovirt.engine.core.common.businessentities.EventMap;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class RemoveEventSubscriptionCommand<T extends EventSubscriptionParametesBase> extends
        EventSubscriptionCommandBase<T> {
    public RemoveEventSubscriptionCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected boolean canDoAction() {
        boolean retValue;
        // get notification method
        EventNotificationMethod event_notification_method = getParameters().getEventSubscriber().getevent_notification_method();
        if (event_notification_method != null) {
            // validate event
            List<EventMap> event_map = DbFacade.getInstance().getEventDao().getEventMapByName(
                    getParameters().getEventSubscriber().getevent_up_name());
            if (event_map.size() > 0) {
                // Validate user
                DbUser user =
                        DbFacade.getInstance()
                                .getDbUserDao()
                                .get(getParameters().getEventSubscriber().getsubscriber_id());
                if (user == null) {
                    addCanDoActionMessage(VdcBllMessages.USER_MUST_EXIST_IN_DB);
                    retValue = false;
                } else {
                    retValue = ValidateRemove(event_notification_method, getParameters().getEventSubscriber(), user);
                }
            } else {
                addCanDoActionMessage(VdcBllMessages.EN_UNSUPPORTED_NOTIFICATION_EVENT);
                retValue = false;
            }
        } else {
            addCanDoActionMessage(VdcBllMessages.EN_UNKNOWN_NOTIFICATION_METHOD);
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
