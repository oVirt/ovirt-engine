package org.ovirt.engine.core.bll;

import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.EventNotificationMethod;
import org.ovirt.engine.core.common.action.EventSubscriptionParametesBase;
import org.ovirt.engine.core.common.businessentities.EventSubscriber;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DbUserDao;

public class AddEventSubscriptionCommand<T extends EventSubscriptionParametesBase> extends
        EventSubscriptionCommandBase<T> {

    @Inject
    private DbUserDao dbUserDao;

    public AddEventSubscriptionCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected boolean validate() {
        boolean retValue;
        // check if user is not already subscribed to this event with same
        // method and address
        Guid subscriberId = getParameters().getEventSubscriber().getSubscriberId();
        String eventName = getParameters().getEventSubscriber().getEventUpName();
        EventNotificationMethod eventNotificationMethod =
                getParameters().getEventSubscriber().getEventNotificationMethod();
        List<EventSubscriber> subscriptions = eventDao.getAllForSubscriber(subscriberId);
        if (isAlreadySubscribed(subscriptions, subscriberId, eventName, eventNotificationMethod)) {
            addValidationMessage(EngineMessage.EN_ALREADY_SUBSCRIBED);
            retValue = false;
        } else if (!eventExists(eventName)) {
            addValidationMessage(EngineMessage.EN_UNSUPPORTED_NOTIFICATION_EVENT);
            retValue = false;
        } else {
            // get notification method
            if (eventNotificationMethod != null) {
                // Validate user
                DbUser user = dbUserDao.get(subscriberId);
                if (user == null) {
                    addValidationMessage(EngineMessage.USER_MUST_EXIST_IN_DB);
                    retValue = false;
                } else {
                    retValue = validateAdd(eventNotificationMethod, getParameters().getEventSubscriber(), user);
                }
            } else {
                addValidationMessage(EngineMessage.EN_UNKNOWN_NOTIFICATION_METHOD);
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
        } catch (Exception ignore) {
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
    private static boolean isAlreadySubscribed(List<EventSubscriber> subscriptions, Guid subscriberId,
            String eventName, EventNotificationMethod eventNotificationMethod) {
        return subscriptions.stream()
                .anyMatch(subscription -> subscriberId.equals(subscription.getSubscriberId())
                    && StringUtils.equals(subscription.getEventUpName(), eventName)
                    && subscription.getEventNotificationMethod() == eventNotificationMethod);
    }

    @Override
    protected void executeCommand() {
        if (getParameters().getEventSubscriber().getTagName() == null) {
            getParameters().getEventSubscriber().setTagName("");
        }
        eventDao.subscribe(getParameters().getEventSubscriber());
        setSucceeded(true);
    }
}
