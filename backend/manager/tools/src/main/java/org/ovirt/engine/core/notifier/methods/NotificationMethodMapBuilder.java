package org.ovirt.engine.core.notifier.methods;

import java.util.HashMap;
import java.util.List;

import org.ovirt.engine.core.common.EventNotificationMethods;
import org.ovirt.engine.core.common.businessentities.EventNotificationMethod;
import org.ovirt.engine.core.notifier.utils.NotificationProperties;
import org.ovirt.engine.core.notifier.utils.sender.EventSender;

/**
 * The group of classes used to map a factory for each notification method.<br>
 * Different notification methods might require different instantiation methods of the notification<br>
 * method implementation class.</p>
 * <b>Components description:</b><br>
 * {@link EventSender} - interface of any notification method implementation<br>
 * {@code EventSenderMailImpl} - implements email notification method<br>
 * {@link NotificationMethodFactory} - interface of factories of notification method classes. Each factory will define the<br>
 * the policy of creation notification classes (some factories might produce a single class for all notifications, others<br>
 * might produce a single instance per notification)</br>
 * {@link NotificationMethodFactoryEmailImpl} - factory for producing a single email notification method class<br>
 * {@link NotificationMethodFactoryMapper} - stores the association between the notification method type to its factory<br>
 * {@link NotificationMethodMapBuilder} - creates the map of notification method type to the factory. Could be recreated
 * upon <br>modification of the notification method types.</p> <b>Adding new notification method</b><br>
 * The following steps will describe how to add and register new notification method. Let's add support for SMS
 * notification method:<br>
 * <li>Add new notification method to {@link EventNotificationMethods}, e.g. {@code SMS(1)}
 * <li>Add an entry representing the SMS notification method to database table <i>event_notification_methods<i>
 * <li>Create {@code EventSenderSMSImpel} which implements the {@link EventSender}. The class will be responsible for
 * dispatching a notification via SMS<br> <li>Create {@code NotificationMethodFactorySMS} which implements the
 * {@link NotificationMethodFactory}. The class will be responsible for instantiating the SMS sender class.
 * <li>Register the notification method and the factory in {@link NotificationMethodMapBuilder#createMethodsMapper(List)}
 */
public class NotificationMethodMapBuilder {

    private static NotificationMethodMapBuilder instance = null;

    static {
        instance = new NotificationMethodMapBuilder();
    }

    private NotificationMethodMapBuilder() {
    }

    /**
     * a getter of single instance of the method builder
     * @return a reference to the event notification map builder
     */
    public static NotificationMethodMapBuilder instance() {
        return instance;
    }

    /**
     * Maps pairs of notification method type to the factory which produces the class that handles the notification
     * action.<br>
     * Design meant to provide a varied instantiation of the method type implementation class.<br>
     * @param notificationMethods
     *            supported list of notification methods
     * @param properties
     *            configuration properties for the factories
     * @return a map of notification map and its factory
     */
    public NotificationMethodFactoryMapper createMethodsMapper(List<EventNotificationMethod> notificationMethods,
            NotificationProperties properties) {
        NotificationMethodFactoryMapper methodMapper = new NotificationMethodFactoryMapper();

        for (EventNotificationMethod method : notificationMethods) {
            if (EventNotificationMethods.EMAIL.equals(method.getmethod_type())) {
                methodMapper.addMethodFactory(EventNotificationMethods.EMAIL,
                        new NotificationMethodFactoryEmailImpl(properties));
            }
        }
        return methodMapper;
    }

    /**
     * A map of the notification method type to its factory. Once a map was created it is non-modifiable.<br>
     * The class designed to provide an implementation class instance by a given notification method type, <br>
     * encapsulating the policy of the notification method implementation class instantiation.<br>
     * Adding notification method types and factories could be done only using:
     * {@link NotificationMethodMapBuilder.createMethodsMapper(List)}
     */
    public class NotificationMethodFactoryMapper {

        private HashMap<EventNotificationMethods, NotificationMethodFactory<? extends EventSender>> methods =
                new HashMap<EventNotificationMethods, NotificationMethodFactory<? extends EventSender>>();

        private void addMethodFactory(EventNotificationMethods methodType,
                NotificationMethodFactory<? extends EventSender> factory) {
            methods.put(methodType, factory);
        }

        /**
         * Returns an instance of the notification method implementation class
         * @param methodType
         *            a notification method type
         * @return an instance of the notification method implementation class or null of no match found
         */
        public EventSender getMethod(EventNotificationMethods methodType) {
            return methods.get(methodType).createMethodClass();
        }
    }
}
