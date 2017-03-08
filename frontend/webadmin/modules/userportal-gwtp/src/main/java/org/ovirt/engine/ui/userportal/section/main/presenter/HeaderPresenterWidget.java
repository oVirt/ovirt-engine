package org.ovirt.engine.ui.userportal.section.main.presenter;

import org.ovirt.engine.ui.common.auth.CurrentUser;
import org.ovirt.engine.ui.common.presenter.AbstractHeaderPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.OptionsProvider;
import org.ovirt.engine.ui.common.widget.tab.AbstractTab;
import org.ovirt.engine.ui.common.widget.tab.TabDefinition;
import org.ovirt.engine.ui.common.widget.tab.TabWidgetHandler;
import org.ovirt.engine.ui.uicommonweb.auth.CurrentUserRole;
import org.ovirt.engine.ui.userportal.ApplicationDynamicMessages;
import org.ovirt.engine.ui.userportal.auth.UserPortalCurrentUserRole;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.gwtplatform.mvp.client.proxy.RevealRootPopupContentEvent;

public class HeaderPresenterWidget extends AbstractHeaderPresenterWidget<HeaderPresenterWidget.ViewDef> implements TabWidgetHandler {

    public interface ViewDef extends AbstractHeaderPresenterWidget.ViewDef, TabWidgetHandler {

        HasClickHandlers getAboutLink();

        void setMainTabPanelVisible(boolean visible);

        void addTab(String title, String href, int index);

        void removeTab(String title, String href);

        void updateTab(String title, String href, boolean accessible);

        void markActiveTab(String text, String href);
    }

    private final UserPortalCurrentUserRole userRole;
    private final Provider<AboutPopupPresenterWidget> aboutPopupProvider;

    @Inject
    public HeaderPresenterWidget(EventBus eventBus, ViewDef view, CurrentUser user,
            CurrentUserRole userRole, Provider<AboutPopupPresenterWidget> aboutPopupProvider,
            OptionsProvider optionsProvider, ApplicationDynamicMessages dynamicMessages) {
        super(eventBus, view, user, optionsProvider, dynamicMessages.applicationDocTitle(), dynamicMessages.guideUrl());
        this.userRole = (UserPortalCurrentUserRole) userRole;
        this.aboutPopupProvider = aboutPopupProvider;
    }

    @Override
    public void addTabWidget(TabDefinition tab, int index) {
        String href = "#"; //$NON-NLS-1$
        if (tab instanceof AbstractTab) {
            href = ((AbstractTab)tab).getTargetHistoryToken();
        }
        getView().addTab(tab.getText(), href, index);
    }

    @Override
    public void removeTabWidget(TabDefinition tab) {
        String href = "#"; //$NON-NLS-1$
        if (tab instanceof AbstractTab) {
            href = ((AbstractTab)tab).getTargetHistoryToken();
        }
        getView().removeTab(tab.getText(), href);
    }

    @Override
    public void updateTab(TabDefinition tab) {
        String href = "#"; //$NON-NLS-1$
        if (tab instanceof AbstractTab) {
            href = ((AbstractTab)tab).getTargetHistoryToken();
        }
        getView().updateTab(tab.getText(), href, tab.isAccessible());
    }

    @Override
    public void setActiveTab(TabDefinition tab) {
        String href = "#"; //$NON-NLS-1$
        if (tab instanceof AbstractTab) {
            href = ((AbstractTab)tab).getTargetHistoryToken();
        }
        getView().markActiveTab(tab.getText(), href);
    }

    @Override
    protected void onBind() {
        super.onBind();

        registerHandler(getView().getAboutLink().addClickHandler(event ->
                RevealRootPopupContentEvent.fire(HeaderPresenterWidget.this, aboutPopupProvider.get())));
    }

    @Override
    protected void onReset() {
        super.onReset();

        getView().setMainTabPanelVisible(userRole.isExtendedUser());
    }

}
