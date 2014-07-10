package org.ovirt.engine.ui.webadmin.section.main.presenter;

import org.ovirt.engine.ui.common.auth.CurrentUser;
import org.ovirt.engine.ui.common.presenter.AbstractHeaderPresenterWidget;
import org.ovirt.engine.ui.common.presenter.ScrollableTabBarPresenterWidget;
import org.ovirt.engine.ui.common.system.HeaderOffsetChangeEvent;
import org.ovirt.engine.ui.common.utils.WebUtils;
import org.ovirt.engine.ui.common.widget.tab.TabWidgetHandler;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.webadmin.ApplicationDynamicMessages;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.configure.ConfigurePopupPresenterWidget;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;
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
    public static final Type<RevealContentHandler<?>> TYPE_SetSearchPanel = new Type<RevealContentHandler<?>>();

    @ContentSlot
    public static final Type<RevealContentHandler<?>> TYPE_SetTabBar = new Type<RevealContentHandler<?>>();

    private static final int MAIN_TABBAR_INITIAL_OFFSET = 240; //pixels.

    private final SearchPanelPresenterWidget searchPanel;
    private final ScrollableTabBarPresenterWidget tabBar;
    private final AboutPopupPresenterWidget aboutPopup;
    private final ConfigurePopupPresenterWidget configurePopup;
    private String feedbackUrl;
    private final String feedbackLinkLabel;
    private final ApplicationDynamicMessages dynamicMessages;

    @Inject
    public HeaderPresenterWidget(EventBus eventBus, ViewDef view, CurrentUser user,
            SearchPanelPresenterWidget searchPanel,
            ScrollableTabBarPresenterWidget tabBar,
            AboutPopupPresenterWidget aboutPopup,
            ConfigurePopupPresenterWidget configurePopup,
            ApplicationDynamicMessages dynamicMessages) {
        super(eventBus, view, user, dynamicMessages.applicationDocTitle(), dynamicMessages.guideUrl());
        this.searchPanel = searchPanel;
        this.tabBar = tabBar;
        this.aboutPopup = aboutPopup;
        this.configurePopup = configurePopup;
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

    private void setScrollDistance(int scrollDistance) {
        tabBar.setScrollDistance(scrollDistance);
    }

    @Override
    protected void onBind() {
        super.onBind();

        registerHandler(getView().getConfigureLink().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                RevealRootPopupContentEvent.fire(HeaderPresenterWidget.this, configurePopup);
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
    protected void onReveal() {
        super.onReveal();

        setInSlot(TYPE_SetSearchPanel, searchPanel);
        setInSlot(TYPE_SetTabBar, tabBar);
        configureFeedbackUrl();
    }

    private void configureFeedbackUrl() {
        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object result) {
                String version = (String) result;
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
        };
        AsyncDataProvider.getInstance().getRpmVersion(_asyncQuery);
    }
}
