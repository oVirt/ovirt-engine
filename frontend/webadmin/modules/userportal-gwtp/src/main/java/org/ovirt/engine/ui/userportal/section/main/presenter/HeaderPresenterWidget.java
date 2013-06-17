package org.ovirt.engine.ui.userportal.section.main.presenter;

import org.ovirt.engine.ui.common.auth.CurrentUser;
import org.ovirt.engine.ui.common.presenter.main.AbstractHeaderPresenterWidget;
import org.ovirt.engine.ui.common.widget.tab.AbstractHeadlessTabPanel.TabWidgetHandler;
import org.ovirt.engine.ui.userportal.ApplicationDynamicMessages;
import org.ovirt.engine.ui.userportal.auth.CurrentUserRole;
import org.ovirt.engine.ui.userportal.uicommon.UserPortalConfigurator;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.proxy.RevealRootPopupContentEvent;

public class HeaderPresenterWidget extends AbstractHeaderPresenterWidget<HeaderPresenterWidget.ViewDef> implements TabWidgetHandler {

    public interface ViewDef extends AbstractHeaderPresenterWidget.ViewDef, TabWidgetHandler {

        HasClickHandlers getAboutLink();

        void setMainTabPanelVisible(boolean visible);

    }

    private final CurrentUserRole userRole;
    private final AboutPopupPresenterWidget aboutPopup;

    @Inject
    public HeaderPresenterWidget(EventBus eventBus, ViewDef view, CurrentUser user,
            CurrentUserRole userRole, AboutPopupPresenterWidget aboutPopup,
            ApplicationDynamicMessages dynamicMessages) {
        super(eventBus, view, user,
                UserPortalConfigurator.DOCUMENTATION_GUIDE_PATH, dynamicMessages.applicationDocTitle());
        this.userRole = userRole;
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

        getView().setMainTabPanelVisible(userRole.isExtendedUser());
    }

}
