package org.ovirt.engine.ui.webadmin.section.main.presenter;

import org.ovirt.engine.ui.common.auth.CurrentUser;
import org.ovirt.engine.ui.common.presenter.main.AbstractHeaderPresenterWidget;
import org.ovirt.engine.ui.common.widget.tab.AbstractHeadlessTabPanel.TabWidgetHandler;
import org.ovirt.engine.ui.webadmin.ApplicationDynamicConstants;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.configure.ConfigurePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.uicommon.WebAdminConfigurator;

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
    }

    @ContentSlot
    public static final Type<RevealContentHandler<?>> TYPE_SetSearchPanel = new Type<RevealContentHandler<?>>();

    private final SearchPanelPresenterWidget searchPanel;
    private final AboutPopupPresenterWidget aboutPopup;
    private final ConfigurePopupPresenterWidget configurePopup;

    @Inject
    public HeaderPresenterWidget(EventBus eventBus, ViewDef view, CurrentUser user,
            SearchPanelPresenterWidget searchPanel,
            AboutPopupPresenterWidget aboutPopup,
            ConfigurePopupPresenterWidget configurePopup,
            ApplicationDynamicConstants constants) {
        super(eventBus, view, user,
                WebAdminConfigurator.DOCUMENTATION_GUIDE_PATH, constants.engineWebAdminDoc());
        this.searchPanel = searchPanel;
        this.aboutPopup = aboutPopup;
        this.configurePopup = configurePopup;
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
    }

}
