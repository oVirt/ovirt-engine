package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.EventNotificationMethod;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.EventSubscriptionParametesBase;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.businessentities.event_subscriber;
import org.ovirt.engine.core.common.businessentities.Tags;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

public abstract class EventSubscriptionCommandBase<T extends EventSubscriptionParametesBase> extends
        CommandBase<T> {
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
     *
     * @param eventNotificationMethod
     *            The eventNotificationMethods.
     * @param event_subscriber
     *            The event_subscriber.
     * @param user
     *            The user.
     * @return
     */
    protected boolean ValidateNotificationMethod(EventNotificationMethod eventNotificationMethod,
                                                 event_subscriber event_subscriber, DbUser user) {
        boolean retValue = true;
        EventNotificationMethod notificationMethod = eventNotificationMethod;

        switch (notificationMethod) {
        case SMTP:
            String mailAddress = (StringUtils.isEmpty(event_subscriber.getmethod_address())) ? user.getEmail()
                    : event_subscriber.getmethod_address();

            if (!isEmailValid(mailAddress)) {
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
     * @param eventNotificationMethod
     *            The eventNotificationMethod.
     * @param event_subscriber
     *            The event_subscriber.
     * @param user
     *            The user.
     * @return
     */
    protected boolean ValidateAdd(EventNotificationMethod eventNotificationMethod,
                                  event_subscriber event_subscriber, DbUser user) {
        String tagName = event_subscriber.gettag_name();
        // validate notification method
        boolean retValue = ValidateNotificationMethod(eventNotificationMethod, event_subscriber, user);

        // validate tag name if exists
        if (retValue && StringUtils.isNotEmpty(tagName)) {
            retValue = ValidateTag(tagName);
        }
        return retValue;
    }

    protected boolean ValidateRemove(EventNotificationMethod eventNotificationMethod,
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
                retValue = ValidateNotificationMethod(eventNotificationMethod, event_subscriber, user);

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
        Tags tag = DbFacade.getInstance().getTagDao().getByName(tagName);
        if (tag == null) {

            addCanDoActionMessage(VdcBllMessages.EN_UNKNOWN_TAG_NAME);
            retValue = false;
        }

        return retValue;
    }

    /**
     * Determines whether [is valid email] [the specified input email].
     *
     * @param email
     *            The input email.
     * @return <c>true</c> if [is valid email] [the specified input email];
     *         otherwise, <c>false</c>.
     */
    protected static boolean isEmailValid(String email) {
        boolean valid = false;
        try {
            if (email == null){
                throw new AddressException();
            }
            new InternetAddress(email, true);
            valid = true;
        } catch (AddressException ignored) {

        }
        return valid;
    }

    private static boolean ValidateSubscription(Iterable<event_subscriber> subscriptions, event_subscriber current) {
        boolean retValue = false;
        for (event_subscriber event_subscriber : subscriptions) {
            if (event_subscriber.getsubscriber_id().equals(current.getsubscriber_id())
                    && StringUtils.equals(event_subscriber.getevent_up_name(), current.getevent_up_name())
                    && event_subscriber.getevent_notification_method() == current.getevent_notification_method()) {
                retValue = true;
                break;
            }

        }
        return retValue;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(Guid.SYSTEM,
                VdcObjectType.System,
                ActionGroup.EVENT_NOTIFICATION_MANAGEMENT));
    }

}
