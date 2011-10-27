package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.action.EventSubscriptionParametesBase;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.businessentities.event_map;
import org.ovirt.engine.core.common.businessentities.event_notification_methods;
import org.ovirt.engine.core.dal.VdcBllMessages;
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
        List<event_notification_methods> event_notification_methods = (DbFacade.getInstance()
                .getEventDAO().getEventNotificationMethodsById(getParameters().getEventSubscriber().getmethod_id()));
        if (event_notification_methods.size() > 0) {
            // validate event
            List<event_map> event_map = DbFacade.getInstance().getEventDAO().getEventMapByName(
                    getParameters().getEventSubscriber().getevent_up_name());
            if (event_map.size() > 0) {
                // Validate user
                DbUser user =
                        DbFacade.getInstance()
                                .getDbUserDAO()
                                .get(getParameters().getEventSubscriber().getsubscriber_id());
                if (user == null) {
                    addCanDoActionMessage(VdcBllMessages.USER_MUST_EXIST_IN_DB);
                    retValue = false;
                } else {
                    retValue = ValidateRemove(event_notification_methods, getParameters().getEventSubscriber(), user);
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
        DbFacade.getInstance().getEventDAO().unsubscribe(getParameters().getEventSubscriber());
        setSucceeded(true);
    }
}
