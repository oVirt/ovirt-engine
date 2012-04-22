package org.ovirt.engine.ui.webadmin.section.main.presenter;

import org.ovirt.engine.ui.common.auth.CurrentUser;
import org.ovirt.engine.ui.common.utils.WebUtils;
import org.ovirt.engine.ui.common.widget.tab.AbstractHeadlessTabPanel.TabWidgetHandler;
import org.ovirt.engine.ui.uicommonweb.Configurator;
import org.ovirt.engine.ui.uicommonweb.TypeResolver;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.configure.ConfigurePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.uicommon.WebAdminConfigurator;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.ContentSlot;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;
import com.gwtplatform.mvp.client.proxy.RevealRootPopupContentEvent;

public class HeaderPresenterWidget extends PresenterWidget<HeaderPresenterWidget.ViewDef> implements TabWidgetHandler, MainTabBarOffsetUiHandlers {

    public interface ViewDef extends View, TabWidgetHandler, MainTabBarOffsetUiHandlers {

        void setUserName(String userName);

        HasClickHandlers getConfigureLink();

        HasClickHandlers getLogoutLink();

        HasClickHandlers getAboutLink();

        HasClickHandlers getGuideLink();

    }

    @ContentSlot
    public static final Type<RevealContentHandler<?>> TYPE_SetSearchPanel = new Type<RevealContentHandler<?>>();

    private final CurrentUser user;
    private final SearchPanelPresenterWidget searchPanel;
    private final AboutPopupPresenterWidget aboutPopup;
    private final ConfigurePopupPresenterWidget configurePopup;
    private final ApplicationConstants constants;

    @Inject
    public HeaderPresenterWidget(EventBus eventBus,
            ViewDef view,
            CurrentUser user,
            SearchPanelPresenterWidget searchPanel,
            AboutPopupPresenterWidget aboutPopup,
            ConfigurePopupPresenterWidget configurePopup, ApplicationConstants constants) {
        super(eventBus, view);
        this.user = user;
        this.searchPanel = searchPanel;
        this.aboutPopup = aboutPopup;
        this.configurePopup = configurePopup;
        this.constants = constants;
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

        registerHandler(getView().getGuideLink().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Configurator configurator = (Configurator) TypeResolver.getInstance().Resolve(Configurator.class);
                if (configurator.isDocumentationAvailable()) {
                    String url = configurator.getDocumentationLibURL() + WebAdminConfigurator.DOCUMENTATION_GUIDE_PATH;
                    WebUtils.openUrlInNewWindow(constants.engineWebAdminDoc(), url);
                }
            }
        }));
    }

    @Override
    protected void onReveal() {
        super.onReveal();

        setInSlot(TYPE_SetSearchPanel, searchPanel);
    }

    @Override
    protected void onReset() {
        super.onReset();

        getView().setUserName(user.getUserName());
    }

}
