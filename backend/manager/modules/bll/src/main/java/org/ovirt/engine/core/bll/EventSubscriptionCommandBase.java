package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.EventNotificationMethod;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.EventSubscriptionParametesBase;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.EventSubscriber;
import org.ovirt.engine.core.common.businessentities.Tags;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.EventDao;
import org.ovirt.engine.core.dao.TagDao;

public abstract class EventSubscriptionCommandBase<T extends EventSubscriptionParametesBase> extends
        CommandBase<T> {

    @Inject
    protected EventDao eventDao;

    @Inject
    private TagDao tagDao;

    public EventSubscriptionCommandBase(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = super.getJobMessageProperties();
        }
        jobProperties.put("address", getParameters().getEventSubscriber().getMethodAddress());
        jobProperties.put("eventtype", getParameters().getEventSubscriber().getEventUpName());
        return jobProperties;
    }

    /**
     * Validates the notification method.
     *
     *
     * @param eventNotificationMethod
     *            The eventNotificationMethods.
     * @param eventSubscriber
     *            The eventSubscriber.
     * @param user
     *            The user.
     */
    protected boolean validateNotificationMethod(EventNotificationMethod eventNotificationMethod,
                                                 EventSubscriber eventSubscriber, DbUser user) {
        boolean retValue = true;

        switch (eventNotificationMethod) {
        case SMTP:
            String mailAddress = StringUtils.isEmpty(eventSubscriber.getMethodAddress()) ? user.getEmail()
                    : eventSubscriber.getMethodAddress();

            if (!isEmailValid(mailAddress)) {
                addValidationMessage(EngineMessage.USER_DOES_NOT_HAVE_A_VALID_EMAIL);
                retValue = false;
            }
            break;
        default:
            addValidationMessage(EngineMessage.EN_UNKNOWN_NOTIFICATION_METHOD);
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
     * @param eventSubscriber
     *            The eventSubscriber.
     * @param user
     *            The user.
     */
    protected boolean validateAdd(EventNotificationMethod eventNotificationMethod,
                                  EventSubscriber eventSubscriber, DbUser user) {
        String tagName = eventSubscriber.getTagName();
        // validate notification method
        boolean retValue = validateNotificationMethod(eventNotificationMethod, eventSubscriber, user);

        // validate tag name if exists
        if (retValue && StringUtils.isNotEmpty(tagName)) {
            retValue = validateTag(tagName);
        }
        return retValue;
    }

    protected boolean validateRemove(EventNotificationMethod eventNotificationMethod,
                                     EventSubscriber eventSubscriber, DbUser user) {
        boolean retValue = false;
        // check if user is subscribed to the event
        List<EventSubscriber> list = eventDao.getAllForSubscriber(eventSubscriber.getSubscriberId());
        if (list.isEmpty()) {
            addValidationMessage(EngineMessage.EN_NOT_SUBSCRIBED);
        } else {
            if (!validateSubscription(list, eventSubscriber)) {
                addValidationMessage(EngineMessage.EN_NOT_SUBSCRIBED);
            } else {
                String tagName = eventSubscriber.getTagName();
                // validate notification method
                retValue = validateNotificationMethod(eventNotificationMethod, eventSubscriber, user);

                // validate tag name if exists
                if (retValue && StringUtils.isNotEmpty(tagName)) {
                    retValue = validateTag(tagName);
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
     */
    protected boolean validateTag(String tagName) {
        boolean retValue = true;
        Tags tag = tagDao.getByName(tagName);
        if (tag == null) {

            addValidationMessage(EngineMessage.EN_UNKNOWN_TAG_NAME);
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

    private static boolean validateSubscription(Iterable<EventSubscriber> subscriptions, EventSubscriber current) {
        boolean retValue = false;
        for (EventSubscriber eventSubscriber : subscriptions) {
            if (eventSubscriber.getSubscriberId().equals(current.getSubscriberId())
                    && StringUtils.equals(eventSubscriber.getEventUpName(), current.getEventUpName())
                    && eventSubscriber.getEventNotificationMethod() == current.getEventNotificationMethod()) {
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
