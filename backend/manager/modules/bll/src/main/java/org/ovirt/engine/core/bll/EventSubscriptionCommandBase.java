package org.ovirt.engine.core.bll;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.EventNotificationMethods;
import org.ovirt.engine.core.common.action.EventSubscriptionParametesBase;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.businessentities.EventNotificationMethod;
import org.ovirt.engine.core.common.businessentities.event_subscriber;
import org.ovirt.engine.core.common.businessentities.tags;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Regex;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public abstract class EventSubscriptionCommandBase<T extends EventSubscriptionParametesBase> extends
        AdminOperationCommandBase<T> {
    protected EventSubscriptionCommandBase(T parameters) {
        super(parameters);
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = super.getJobMessageProperties();
        }
        jobProperties.put("address", getParameters().getEventSubscriber().getmethod_address());
        jobProperties.put("eventtype", getParameters().getEventSubscriber().getevent_up_name());
        return jobProperties;
    }

    /**
     * Validates the notification method.
     *
     * @param eventNotificationMethods
     *            The eventNotificationMethods.
     * @param event_subscriber
     *            The event_subscriber.
     * @param user
     *            The user.
     * @return
     */
    protected boolean ValidateNotificationMethod(java.util.List<EventNotificationMethod> eventNotificationMethods,
                                                 event_subscriber event_subscriber, DbUser user) {
        boolean retValue = true;
        EventNotificationMethods notificationMethod = eventNotificationMethods.get(0).getmethod_type();

        switch (notificationMethod) {
        case EMAIL:
            String mailAdress = (StringUtils.isEmpty(event_subscriber.getmethod_address())) ? user.getEmail()
                    : event_subscriber.getmethod_address();

            if (StringUtils.isEmpty(mailAdress) || !ValidatMailAddress(mailAdress)) {
                addCanDoActionMessage(VdcBllMessages.USER_DOES_NOT_HAVE_A_VALID_EMAIL);
                retValue = false;
            }
            break;
        default:
            addCanDoActionMessage(VdcBllMessages.EN_UNKNOWN_NOTIFICATION_METHOD);
            retValue = false;
            break;
        }
        return retValue;
    }

    /**
     * Validates the notification method and tag.
     *
     * @param eventNotificationMethods
     *            The eventNotificationMethods.
     * @param event_subscriber
     *            The event_subscriber.
     * @param user
     *            The user.
     * @return
     */
    protected boolean ValidateAdd(List<EventNotificationMethod> eventNotificationMethods,
                                  event_subscriber event_subscriber, DbUser user) {
        String tagName = event_subscriber.gettag_name();
        // validate notification method
        boolean retValue = ValidateNotificationMethod(eventNotificationMethods, event_subscriber, user);

        // validate tag name if exists
        if (retValue && StringUtils.isNotEmpty(tagName)) {
            retValue = ValidateTag(tagName);
        }
        return retValue;
    }

    protected boolean ValidateRemove(List<EventNotificationMethod> eventNotificationMethods,
                                     event_subscriber event_subscriber, DbUser user) {
        boolean retValue = false;
        // check if user is subscribed to the event
        List<event_subscriber> list = DbFacade.getInstance()
                .getEventDao()
                .getAllForSubscriber(event_subscriber.getsubscriber_id());
        if (list.isEmpty()) {
            addCanDoActionMessage(VdcBllMessages.EN_NOT_SUBSCRIBED);
        } else {
            if (!ValidateSubscription(list, event_subscriber)) {
                addCanDoActionMessage(VdcBllMessages.EN_NOT_SUBSCRIBED);
            } else {
                String tagName = event_subscriber.gettag_name();
                // validate notification method
                retValue = ValidateNotificationMethod(eventNotificationMethods, event_subscriber, user);

                // validate tag name if exists
                if (retValue && StringUtils.isNotEmpty(tagName)) {
                    retValue = ValidateTag(tagName);
                }
            }
        }
        return retValue;
    }

    /**
     * Validates the tag.
     *
     * @param tagName
     *            Name of the tag.
     * @return
     */
    protected boolean ValidateTag(String tagName) {
        boolean retValue = true;
        tags tag = DbFacade.getInstance().getTagDao().getByName(tagName);
        if (tag == null) {

            addCanDoActionMessage(VdcBllMessages.EN_UNKNOWN_TAG_NAME);
            retValue = false;
        }

        return retValue;
    }

    /**
     * Determines whether [is valid email] [the specified input email].
     *
     * @param inputEmail
     *            The input email.
     * @return <c>true</c> if [is valid email] [the specified input email];
     *         otherwise, <c>false</c>.
     */
    protected static boolean ValidatMailAddress(String inputEmail) {
        final String strRegex = "^[\\w-]+(?:\\.[\\w-]+)*@(?:[\\w-]+\\.)+[a-zA-Z]{2,7}$";
        Regex re = new Regex(strRegex);
        return re.IsMatch(inputEmail);
    }

    private static boolean ValidateSubscription(Iterable<event_subscriber> subscriptions, event_subscriber current) {
        boolean retValue = false;
        for (event_subscriber event_subscriber : subscriptions) {
            if (event_subscriber.getsubscriber_id().equals(current.getsubscriber_id())
                    && StringUtils.equals(event_subscriber.getevent_up_name(), current.getevent_up_name())
                    && event_subscriber.getmethod_id() == current.getmethod_id()) {
                retValue = true;
                break;
            }

        }
        return retValue;
    }
}
