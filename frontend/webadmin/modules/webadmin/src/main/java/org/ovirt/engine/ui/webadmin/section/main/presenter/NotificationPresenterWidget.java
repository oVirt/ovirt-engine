package org.ovirt.engine.ui.webadmin.section.main.presenter;

import java.util.Date;
import java.util.LinkedList;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.UserProfileProperty;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.widget.uicommon.tasks.ToastNotification;
import org.ovirt.engine.ui.common.widget.uicommon.tasks.ToastNotification.NotificationStatus;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.UserProfileManager;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.Timer;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class NotificationPresenterWidget extends PresenterWidget<NotificationPresenterWidget.ViewDef> {

    public interface ViewDef extends View {

        void showNotification(ToastNotification notification);

        void removeNotification(ToastNotification notification);

        void clear();

        void show();

        void hide();

        HasClickHandlers getDismissAllButton();

        HasClickHandlers getDoNotDisturb10Minutes();

        HasClickHandlers getDoNotDisturb1Hour();

        HasClickHandlers getDoNotDisturb1Day();

        HasClickHandlers getDoNotDisturbNextLogin();
    }

    private static final String SHOW_NOTIFICATIONS = "webAdmin.showNotifications"; //$NON-NLS-1$
    private static final int AUTO_CLOSE_DELAY_SECONDS = 8;

    private static final int MILLIS_10_MINUTES = 1000 * 60 * 10;
    private static final int MILLIS_1_HOUR = 1000 * 60 * 60;
    private static final int MILLIS_1_DAY = 1000 * 60 * 60 * 24;

    private static final String DO_NOT_DISTURB = "_doNotDisturb"; //$NON-NLS-1$

    private final UserProfileManager userProfileManger;
    private final ClientStorage clientStorage;
    private boolean sessionDoNotDisturb = false;

    private final LinkedList<ToastNotification> notifications = new LinkedList<>();

    @Inject
    public NotificationPresenterWidget(ClientStorage clientStorage, EventBus eventBus, ViewDef view) {
        super(eventBus, view);
        this.clientStorage = clientStorage;
        this.userProfileManger = Frontend.getInstance().getUserProfileManager();
        getView().hide();
        if (notifications.size() > 0) {
            getView().show();
        }
    }

    @Override
    public void onBind() {
        super.onBind();
        registerHandler(getView().getDismissAllButton().addClickHandler(event -> dismissAll()));
        registerHandler(getView().getDoNotDisturb10Minutes().addClickHandler(event -> doNotDisturb10Minutes()));
        registerHandler(getView().getDoNotDisturb1Hour().addClickHandler(event -> doNotDisturb1Hour()));
        registerHandler(getView().getDoNotDisturb1Day().addClickHandler(event -> doNotDisturb1Day()));
        registerHandler(getView().getDoNotDisturbNextLogin().addClickHandler(event -> doNotDisturbNextLogin()));
    }

    private boolean isDoNotDisturb() {
        if (sessionDoNotDisturb || isPermanentDoNotDisturb()) {
            return true;
        }

        try {
            String clientDoNotDisturb = clientStorage.getLocalItem(DO_NOT_DISTURB);
            if (clientDoNotDisturb != null && !clientDoNotDisturb.isEmpty()) {
                double clientDoNotDisturbExpires = Double.parseDouble(clientDoNotDisturb);
                // is the expiration later than now?
                Date now = new Date();
                if (clientDoNotDisturbExpires > now.getTime()) {
                    return true;
                }
            }
        } catch (NumberFormatException e) {
            // no-op
        }

        return false;
    }

    private boolean isPermanentDoNotDisturb() {
        return userProfileManger
                .getUserProfile()
                .getUserProfileProperty(SHOW_NOTIFICATIONS, UserProfileProperty.PropertyType.JSON)
                .map(UserProfileProperty::getContent)
                .map(Boolean::parseBoolean)
                // showNotifications == !doNotDisturb
                .map(showNotifications -> !showNotifications)
                .orElse(false);
    }

    public ToastNotification createNotification(String text, NotificationStatus status) {
        ToastNotification notification = new ToastNotification(text, status);

        if (isDoNotDisturb()) {
            // don't show anything
            // some callers style the notification, so return a stub that won't be shown
            return notification;
        }

        // manual close
        HandlerRegistration handlerRegistration =
                notification.addCloseClickHandler(event -> hideNotification(notification));
        registerHandler(handlerRegistration);

        // auto close
        Timer closeTimer = new Timer() {
            @Override
            public void run() {
                hideNotification(notification);
                handlerRegistration.removeHandler();
            }
        };
        closeTimer.schedule(1000 * AUTO_CLOSE_DELAY_SECONDS);

        notifications.add(notification);
        getView().showNotification(notification);
        getView().show();

        return notification;
    }

    private void dismissAll() {
        notifications.clear();
        getView().clear();
        getView().hide();
    }

    private void doNotDisturb(double time) {
        dismissAll();
        clientStorage.setLocalItem(DO_NOT_DISTURB, "" + time);
    }

    private void doNotDisturb10Minutes() {
        double time = new Date().getTime() + MILLIS_10_MINUTES;
        doNotDisturb(time);
    }

    private void doNotDisturb1Hour() {
        double time = new Date().getTime() + MILLIS_1_HOUR;
        doNotDisturb(time);
    }

    private void doNotDisturb1Day() {
        double time = new Date().getTime() + MILLIS_1_DAY;
        doNotDisturb(time);
    }

    private void doNotDisturbNextLogin() {
        dismissAll();
        // use a simple variable so a page refresh or logout/login re-enables notifications
        sessionDoNotDisturb = true;
    }

    public void hideNotification(ToastNotification notification) {
        notifications.remove(notification);
        getView().removeNotification(notification);
        if (notifications.size() == 0) {
            getView().hide();
        }
    }

}
