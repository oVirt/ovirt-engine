package org.ovirt.engine.core.bll;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.EventNotificationMethod;
import org.ovirt.engine.core.common.action.EventSubscriptionParametesBase;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.businessentities.event_subscriber;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

import java.util.List;

public class AddEventSubscriptionCommand<T extends EventSubscriptionParametesBase> extends
        EventSubscriptionCommandBase<T> {
    public AddEventSubscriptionCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected boolean canDoAction() {
        boolean retValue;
        // check if user is not already subscribed to this event with same
        // method and address
        Guid subscriberId = getParameters().getEventSubscriber().getsubscriber_id();
        String eventName = getParameters().getEventSubscriber().getevent_up_name();
        EventNotificationMethod eventNotificationMethod =
                getParameters().getEventSubscriber().getevent_notification_method();
        List<event_subscriber> subscriptions = DbFacade.getInstance()
                .getEventDao().getAllForSubscriber(subscriberId);
        if (IsAlreadySubscribed(subscriptions, subscriberId, eventName, eventNotificationMethod)) {
            addCanDoActionMessage(VdcBllMessages.EN_ALREADY_SUBSCRIBED);
            retValue = false;
        } else if (!eventExists(eventName)) {
            addCanDoActionMessage(VdcBllMessages.EN_UNSUPPORTED_NOTIFICATION_EVENT);
            retValue = false;
        } else {
            // get notification method
            if (eventNotificationMethod != null) {
                // Validate user
                DbUser user = DbFacade.getInstance().getDbUserDao().get(subscriberId);
                if (user == null) {
                    addCanDoActionMessage(VdcBllMessages.USER_MUST_EXIST_IN_DB);
                    retValue = false;
                } else {
                    retValue = ValidateAdd(eventNotificationMethod, getParameters().getEventSubscriber(), user);
                }
            } else {
                addCanDoActionMessage(VdcBllMessages.EN_UNKNOWN_NOTIFICATION_METHOD);
                retValue = false;
            }
        }
        return retValue;
    }

    private boolean eventExists(String eventName) {
        boolean exists = false;
        try {
            AuditLogType.valueOf(eventName);
            exists = true;
        } catch (Exception ex) {
        }
        return exists;
    }

    /**
     * Determines whether [is already subscribed] [the specified subscriptions].
     *
     * @param subscriptions           The subscriptions.
     * @param subscriberId            The subscriber id.
     * @param eventName               Name of the event.
     * @param eventNotificationMethod The notification method.
     * @return <c>true</c> if [is already subscribed] [the specified
     * subscriptions]; otherwise, <c>false</c>.
     */
    private static boolean IsAlreadySubscribed(Iterable<event_subscriber> subscriptions, Guid subscriberId,
            String eventName, EventNotificationMethod eventNotificationMethod) {
        boolean retval = false;
        for (event_subscriber eventSubscriber : subscriptions) {
            if (subscriberId.equals(eventSubscriber.getsubscriber_id())
                    && StringUtils.equals(eventSubscriber.getevent_up_name(), eventName)
                    && eventSubscriber.getevent_notification_method() == eventNotificationMethod) {
                retval = true;
                break;
            }
        }
        return retval;
    }

    @Override
    protected void executeCommand() {
        if (getParameters().getEventSubscriber().gettag_name() == null) {
            getParameters().getEventSubscriber().settag_name("");
        }
        DbFacade.getInstance().getEventDao().subscribe(getParameters().getEventSubscriber());
        setSucceeded(true);
    }
}
