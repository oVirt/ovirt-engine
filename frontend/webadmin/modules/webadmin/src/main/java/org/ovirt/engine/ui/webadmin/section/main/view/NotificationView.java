package org.ovirt.engine.ui.webadmin.section.main.view;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.view.AbstractView;
import org.ovirt.engine.ui.common.widget.uicommon.tasks.ToastNotification;
import org.ovirt.engine.ui.webadmin.section.main.presenter.NotificationPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

public class NotificationView extends AbstractView implements NotificationPresenterWidget.ViewDef {

    interface ViewUiBinder extends UiBinder<Widget, NotificationView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<NotificationView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField
    FlowPanel notificationsPanel;

    @UiField
    FlowPanel toastsContainer;

    @UiField
    FlowPanel buttonContainer;

    @UiField
    AnchorListItem doNotDisturb10Minutes;

    @UiField
    AnchorListItem doNotDisturb1Hour;

    @UiField
    AnchorListItem doNotDisturb1Day;

    @UiField
    AnchorListItem doNotDisturbNextLogin;

    @UiField
    Anchor dismissAllButton;

    public NotificationView() {
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    @Override
    public HasClickHandlers getDismissAllButton() {
        return dismissAllButton;
    }

    @Override
    public HasClickHandlers getDoNotDisturb10Minutes() {
        return doNotDisturb10Minutes;
    }

    @Override
    public HasClickHandlers getDoNotDisturb1Hour() {
        return doNotDisturb1Hour;
    }

    @Override
    public HasClickHandlers getDoNotDisturb1Day() {
        return doNotDisturb1Day;
    }

    @Override
    public HasClickHandlers getDoNotDisturbNextLogin() {
        return doNotDisturbNextLogin;
    }

    @Override
    public void showNotification(ToastNotification notification) {
        toastsContainer.insert(notification, 0);
    }

    @Override
    public void removeNotification(ToastNotification notification) {
        toastsContainer.remove(notification);
    }

    @Override
    public void clear() {
        toastsContainer.clear();
        buttonContainer.setVisible(false);
    }

    @Override
    public void show() {
        notificationsPanel.setVisible(true);
        buttonContainer.setVisible(true);
    }

    @Override
    public void hide() {
        notificationsPanel.setVisible(false);
        buttonContainer.setVisible(false);
    }

}
