package org.ovirt.engine.ui.webadmin.section.main.presenter;

import org.ovirt.engine.ui.common.auth.CurrentUser;
import org.ovirt.engine.ui.common.presenter.AbstractHeaderPresenterWidget;
import org.ovirt.engine.ui.common.utils.WebUtils;
import org.ovirt.engine.ui.common.widget.tab.AbstractHeadlessTabPanel.TabWidgetHandler;
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
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.annotations.ContentSlot;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;
import com.gwtplatform.mvp.client.proxy.RevealRootPopupContentEvent;

public class HeaderPresenterWidget extends AbstractHeaderPresenterWidget<HeaderPresenterWidget.ViewDef> implements TabWidgetHandler, MainTabBarOffsetUiHandlers {

    public interface ViewDef extends AbstractHeaderPresenterWidget.ViewDef, TabWidgetHandler, MainTabBarOffsetUiHandlers {

        HasClickHandlers getConfigureLink();

        HasClickHandlers getAboutLink();

        HasClickHandlers getFeedbackLink();

        void setFeedbackText(String feedbackText);
    }

    @ContentSlot
    public static final Type<RevealContentHandler<?>> TYPE_SetSearchPanel = new Type<RevealContentHandler<?>>();

    private final SearchPanelPresenterWidget searchPanel;
    private final AboutPopupPresenterWidget aboutPopup;
    private final ConfigurePopupPresenterWidget configurePopup;
    private String feedbackUrl;
    private final String feedbackLinkLabel;
    private final ApplicationDynamicMessages dynamicMessages;

    @Inject
    public HeaderPresenterWidget(EventBus eventBus, ViewDef view, CurrentUser user,
            SearchPanelPresenterWidget searchPanel,
            AboutPopupPresenterWidget aboutPopup,
            ConfigurePopupPresenterWidget configurePopup,
            ApplicationDynamicMessages dynamicMessages) {
        super(eventBus, view, user, dynamicMessages.applicationDocTitle(), dynamicMessages.guideUrl());
        this.searchPanel = searchPanel;
        this.aboutPopup = aboutPopup;
        this.configurePopup = configurePopup;
        this.feedbackLinkLabel = dynamicMessages.feedbackLinkLabel();
        this.dynamicMessages = dynamicMessages;
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
    public void setMainTabBarOffset(int left) {
        getView().setMainTabBarOffset(left);
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
                    getView().setFeedbackText(feedbackLinkLabel);
                    registerHandler(getView().getFeedbackLink().addClickHandler(new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent event) {
                            WebUtils.openUrlInNewWindow(feedbackLinkLabel, feedbackUrl);
                        }
                    }));
                }
            }
        };
        AsyncDataProvider.getRpmVersion(_asyncQuery);
    }
}
