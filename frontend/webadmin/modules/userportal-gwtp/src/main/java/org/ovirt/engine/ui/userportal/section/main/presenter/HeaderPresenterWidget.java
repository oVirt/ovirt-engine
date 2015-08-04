package org.ovirt.engine.ui.userportal.section.main.presenter;

import org.ovirt.engine.ui.common.auth.CurrentUser;
import org.ovirt.engine.ui.common.presenter.AbstractHeaderPresenterWidget;
import org.ovirt.engine.ui.common.widget.tab.TabWidgetHandler;
import org.ovirt.engine.ui.uicommonweb.auth.CurrentUserRole;
import org.ovirt.engine.ui.uicommonweb.models.OptionsModel;
import org.ovirt.engine.ui.userportal.ApplicationDynamicMessages;
import org.ovirt.engine.ui.userportal.auth.UserPortalCurrentUserRole;
import org.ovirt.engine.ui.userportal.uicommon.model.basic.OptionsProvider;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.proxy.RevealRootPopupContentEvent;

public class HeaderPresenterWidget extends AbstractHeaderPresenterWidget<HeaderPresenterWidget.ViewDef> implements TabWidgetHandler {

    public interface ViewDef extends AbstractHeaderPresenterWidget.ViewDef, TabWidgetHandler {

        HasClickHandlers getAboutLink();

        HasClickHandlers getOptionsLink();

        void setMainTabPanelVisible(boolean visible);

    }

    private final UserPortalCurrentUserRole userRole;
    private final AboutPopupPresenterWidget aboutPopup;
    private final OptionsProvider optionsProvider;

    @Inject
    public HeaderPresenterWidget(EventBus eventBus, ViewDef view, CurrentUser user,
            CurrentUserRole userRole, AboutPopupPresenterWidget aboutPopup,
            OptionsProvider optionsProvider, ApplicationDynamicMessages dynamicMessages) {
        super(eventBus, view, user, dynamicMessages.applicationDocTitle(), dynamicMessages.guideUrl());
        this.userRole = (UserPortalCurrentUserRole) userRole;
        this.aboutPopup = aboutPopup;
        this.optionsProvider = optionsProvider;
    }

    @Override
    public void addTabWidget(IsWidget tabWidget, int index) {
        getView().addTabWidget(tabWidget, index);
    }

    @Override
    public void removeTabWidget(IsWidget tabWidget) {
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

        registerHandler(getView().getOptionsLink().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                OptionsModel model = optionsProvider.getModel();
                model.executeCommand(model.getEditCommand());
            }
        }));
    }

    @Override
    protected void onReset() {
        super.onReset();

        getView().setMainTabPanelVisible(userRole.isExtendedUser());
    }

}
