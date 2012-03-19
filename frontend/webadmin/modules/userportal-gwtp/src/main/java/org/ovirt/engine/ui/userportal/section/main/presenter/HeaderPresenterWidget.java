package org.ovirt.engine.ui.userportal.section.main.presenter;

import org.ovirt.engine.ui.common.auth.CurrentUser;
import org.ovirt.engine.ui.common.widget.tab.AbstractHeadlessTabPanel.TabWidgetHandler;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.proxy.RevealRootPopupContentEvent;

public class HeaderPresenterWidget extends PresenterWidget<HeaderPresenterWidget.ViewDef> implements TabWidgetHandler {

    public interface ViewDef extends View, TabWidgetHandler {

        void setUserName(String userName);

        HasClickHandlers getLogoutLink();

        HasClickHandlers getAboutLink();

    }

    private final CurrentUser user;
    private final AboutPopupPresenterWidget aboutPopup;

    @Inject
    public HeaderPresenterWidget(
            EventBus eventBus,
            ViewDef view,
            CurrentUser user,
            AboutPopupPresenterWidget aboutPopup) {
        super(eventBus, view);
        this.user = user;
        this.aboutPopup = aboutPopup;
    }

    @Override
    public void addTabWidget(Widget tabWidget, int index) {
        getView().addTabWidget(tabWidget, index);
    }

    @Override
    public void removeTabWidget(Widget tabWidget) {
        getView().removeTabWidget(tabWidget);
    }

    @Override
    protected void onBind() {
        super.onBind();

        registerHandler(getView().getLogoutLink().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                user.logout();
            }
        }));

        registerHandler(getView().getAboutLink().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                RevealRootPopupContentEvent.fire(HeaderPresenterWidget.this, aboutPopup);
            }
        }));
    }

    @Override
    protected void onReset() {
        super.onReset();

        getView().setUserName(user.getUserName());
    }

}
