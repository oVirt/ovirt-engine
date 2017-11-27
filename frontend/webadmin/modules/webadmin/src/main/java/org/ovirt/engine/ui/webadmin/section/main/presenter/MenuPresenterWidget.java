package org.ovirt.engine.ui.webadmin.section.main.presenter;

import javax.inject.Inject;

import org.ovirt.engine.ui.common.widget.MenuDetailsProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.configure.ConfigurePopupPresenterWidget;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.inject.Provider;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.proxy.RevealRootPopupContentEvent;

public class MenuPresenterWidget extends PresenterWidget<MenuPresenterWidget.ViewDef> implements MenuDetailsProvider {

    public interface ViewDef extends View {
        HasClickHandlers getConfigureItem();
        void addMenuItem(int index, String label, String href);
        String getLabelFromHref(String href);
        void setMenuActive(String href);
    }

    private final Provider<ConfigurePopupPresenterWidget> configurePopupProvider;

    @Inject
    public MenuPresenterWidget(EventBus eventBus, MenuPresenterWidget.ViewDef view,
            Provider<ConfigurePopupPresenterWidget> configurePopupProvider) {
        super(eventBus, view);
        this.configurePopupProvider = configurePopupProvider;
    }

    @Override
    public void onBind() {
        super.onBind();
        registerHandler(getView().getConfigureItem().addClickHandler(event ->
            RevealRootPopupContentEvent.fire(MenuPresenterWidget.this, configurePopupProvider.get())));
    }

    public void addMenuItem(int index, String label, String historyToken) {
        getView().addMenuItem(index, label, historyToken);
    }

    @Override
    public String getLabelFromHref(String href) {
        return getView().getLabelFromHref(href);
    }

    @Override
    public void setMenuActive(String href) {
        getView().setMenuActive(href);
    }
}
