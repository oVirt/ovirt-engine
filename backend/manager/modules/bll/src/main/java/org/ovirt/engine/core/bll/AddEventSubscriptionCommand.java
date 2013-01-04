package org.ovirt.engine.core.bll;

import java.util.List;


import org.ovirt.engine.core.common.action.EventSubscriptionParametesBase;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.businessentities.EventMap;
import org.ovirt.engine.core.common.businessentities.EventNotificationMethod;
import org.ovirt.engine.core.common.businessentities.event_subscriber;
import org.ovirt.engine.core.common.users.VdcUser;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

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
        int methodId = getParameters().getEventSubscriber().getmethod_id();
        List<event_subscriber> subscriptions = DbFacade.getInstance()
                .getEventDao().getAllForSubscriber(subscriberId);
        if (IsAlreadySubscribed(subscriptions, subscriberId, eventName, methodId)) {
            addCanDoActionMessage(VdcBllMessages.EN_ALREADY_SUBSCRIBED);
            retValue = false;
        } else {
            // get notification method
            List<EventNotificationMethod> eventNotificationMethods = (DbFacade.getInstance()
                    .getEventDao().getEventNotificationMethodsById(methodId));
            if (eventNotificationMethods.size() > 0) {
                // validate event
                List<EventMap> eventMap = DbFacade.getInstance().getEventDao().getEventMapByName(eventName);
                if (eventMap.size() > 0) {
                    String domain = getParameters().getDomain();
                    // Validate user
                    DbUser user = DbFacade.getInstance().getDbUserDao().get(subscriberId);
                    if (user == null) {
                        // If user exists in AD and does not exist in DB - try to add it to DB
                        // If an exception is thrown while trying, handle it and and fail with the relevant message
                        try {
                            user = UserCommandBase.initUser(new VdcUser(subscriberId, "", domain), getParameters()
                                    .getSessionId());
                            if (user == null) {
                                addCanDoActionMessage(VdcBllMessages.USER_MUST_EXIST_IN_DIRECTORY);
                                retValue = false;
                            } else {
                                retValue =
                                        ValidateAdd(eventNotificationMethods, getParameters().getEventSubscriber(),
                                                user);
                            }
                        } catch (VdcBLLException vdcBllException) {
                            addCanDoActionMessage(VdcBllMessages.USER_MUST_EXIST_IN_DIRECTORY);
                            retValue = false;
                        }
                    } else {
                        retValue = ValidateAdd(eventNotificationMethods, getParameters().getEventSubscriber(), user);
                    }
                } else {
                    addCanDoActionMessage(VdcBllMessages.EN_UNSUPPORTED_NOTIFICATION_EVENT);
                    retValue = false;
                }
            } else {
                addCanDoActionMessage(VdcBllMessages.EN_UNKNOWN_NOTIFICATION_METHOD);
                retValue = false;
            }
        }
        return retValue;
    }

    /**
     * Determines whether [is already subscribed] [the specified subscriptions].
     *
     * @param subscriptions
     *            The subscriptions.
     * @param subscriberId
     *            The subscriber id.
     * @param eventName
     *            Name of the event.
     * @param methodId
     *            The method id.
     * @return <c>true</c> if [is already subscribed] [the specified
     *         subscriptions]; otherwise, <c>false</c>.
     */
    private static boolean IsAlreadySubscribed(Iterable<event_subscriber> subscriptions, Guid subscriberId,
                                               String eventName, int methodId) {
        boolean retval = false;
        for (event_subscriber eventSubscriber : subscriptions) {
            if (subscriberId.equals(eventSubscriber.getsubscriber_id())
                    && StringHelper.EqOp(eventSubscriber.getevent_up_name(), eventName)
                    && eventSubscriber.getmethod_id() == methodId) {
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
