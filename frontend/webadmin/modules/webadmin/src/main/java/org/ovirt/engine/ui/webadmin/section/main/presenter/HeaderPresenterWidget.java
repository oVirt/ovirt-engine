package org.ovirt.engine.ui.webadmin.section.main.presenter;

import org.ovirt.engine.ui.common.auth.CurrentUser;
import org.ovirt.engine.ui.common.presenter.AbstractHeaderPresenterWidget;
import org.ovirt.engine.ui.common.presenter.ScrollableTabBarPresenterWidget;
import org.ovirt.engine.ui.common.system.EngineRpmVersionData;
import org.ovirt.engine.ui.common.system.HeaderOffsetChangeEvent;
import org.ovirt.engine.ui.common.uicommon.model.OptionsProvider;
import org.ovirt.engine.ui.common.utils.WebUtils;
import org.ovirt.engine.ui.common.widget.tab.TabWidgetHandler;
import org.ovirt.engine.ui.webadmin.ApplicationDynamicMessages;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.configure.ConfigurePopupPresenterWidget;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.gwtplatform.mvp.client.annotations.ContentSlot;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;
import com.gwtplatform.mvp.client.proxy.RevealRootPopupContentEvent;

public class HeaderPresenterWidget extends AbstractHeaderPresenterWidget<HeaderPresenterWidget.ViewDef> implements
    TabWidgetHandler, MainTabBarOffsetUiHandlers {

    public interface ViewDef extends AbstractHeaderPresenterWidget.ViewDef {

        HasClickHandlers getConfigureLink();

        HasClickHandlers getAboutLink();

        HasClickHandlers getFeedbackLink();

        void setFeedbackText(String feedbackText, String feedbackTitle);
    }

    @ContentSlot
    public static final Type<RevealContentHandler<?>> TYPE_SetSearchPanel = new Type<>();

    @ContentSlot
    public static final Type<RevealContentHandler<?>> TYPE_SetTabBar = new Type<>();

    private final SearchPanelPresenterWidget searchPanel;
    private final ScrollableTabBarPresenterWidget tabBar;
    private final Provider<AboutPopupPresenterWidget> aboutPopupProvider;
    private final Provider<ConfigurePopupPresenterWidget> configurePopupProvider;
    private String feedbackUrl;
    private final String feedbackLinkLabel;
    private final ApplicationDynamicMessages dynamicMessages;

    @Inject
    public HeaderPresenterWidget(EventBus eventBus, ViewDef view, CurrentUser user,
            SearchPanelPresenterWidget searchPanel,
            ScrollableTabBarPresenterWidget tabBar,
            OptionsProvider optionsProvider,
            Provider<AboutPopupPresenterWidget> aboutPopupProvider,
            Provider<ConfigurePopupPresenterWidget> configurePopupProvider,
            ApplicationDynamicMessages dynamicMessages) {
        super(eventBus, view, user, optionsProvider, dynamicMessages.applicationDocTitle(), dynamicMessages.guideUrl());
        this.searchPanel = searchPanel;
        this.tabBar = tabBar;
        this.aboutPopupProvider = aboutPopupProvider;
        this.configurePopupProvider = configurePopupProvider;
        this.feedbackLinkLabel = dynamicMessages.feedbackLinkLabel();
        this.dynamicMessages = dynamicMessages;
    }

    @Override
    public void addTabWidget(IsWidget tabWidget, int index) {
        tabBar.addTabWidget(tabWidget, index);
    }

    @Override
    public void removeTabWidget(IsWidget tabWidget) {
        tabBar.removeTabWidget(tabWidget);
    }

    @Override
    public void setMainTabBarOffset(int left) {
        HeaderOffsetChangeEvent.fire(this, left);
    }

    @Override
    protected void onBind() {
        super.onBind();

        registerHandler(getView().getConfigureLink().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                RevealRootPopupContentEvent.fire(HeaderPresenterWidget.this, configurePopupProvider.get());
            }
        }));

        registerHandler(getView().getAboutLink().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                RevealRootPopupContentEvent.fire(HeaderPresenterWidget.this, aboutPopupProvider.get());
            }
        }));
    }

    @Override
    protected void onReveal() {
        super.onReveal();

        setInSlot(TYPE_SetSearchPanel, searchPanel);
        setInSlot(TYPE_SetTabBar, tabBar);
        configureFeedbackUrl();
    }

    private void configureFeedbackUrl() {
        String version = EngineRpmVersionData.getVersion();
        feedbackUrl = dynamicMessages.feedbackUrl(version);

        if (feedbackUrl != null && feedbackUrl.length() > 0) {
            getView().setFeedbackText(feedbackLinkLabel, dynamicMessages.feedbackLinkTooltip());
            registerHandler(getView().getFeedbackLink().addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    WebUtils.openUrlInNewWindow(feedbackLinkLabel, feedbackUrl);
                }
            }));
        }
    }

}
