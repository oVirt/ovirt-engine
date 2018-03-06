package org.ovirt.engine.ui.webadmin.section.main.presenter;

import java.util.ArrayList;
import java.util.List;

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
        int addMenuItemPlace(int priority, String label, String href, Integer primaryMenuIndex, String iconCssName);
        int addPrimaryMenuItemContainer(int index, String label, String iconCssName);
        String getLabelFromHref(String href);
        void setMenuActive(String href);
    }

    private final Provider<ConfigurePopupPresenterWidget> configurePopupProvider;

    private final List<String> menuContainers = new ArrayList<>();

    @Inject
    public MenuPresenterWidget(EventBus eventBus, MenuPresenterWidget.ViewDef view,
            Provider<ConfigurePopupPresenterWidget> configurePopupProvider) {
        super(eventBus, view);
        this.configurePopupProvider = configurePopupProvider;
        for (PrimaryMenuContainerType type: PrimaryMenuContainerType.values()) {
            menuContainers.add(type.getId());
        }
        // Add null for events which is not a container, but we need to keep the indexes in sync.
        menuContainers.add(null);
    }

    @Override
    public void onBind() {
        super.onBind();
        registerHandler(getView().getConfigureItem().addClickHandler(event ->
            RevealRootPopupContentEvent.fire(MenuPresenterWidget.this, configurePopupProvider.get())));
    }

    public void addMenuItemPlace(int priority, String label, String historyToken, String primaryMenuId,
            String iconCssName) {
        if (primaryMenuId != null) {
            int containerIndex = menuContainers.indexOf(primaryMenuId);
            if (containerIndex > -1) {
                getView().addMenuItemPlace(priority, label, historyToken, containerIndex, iconCssName);
            }
        } else {
            int newMainPlaceIndex = getView().addMenuItemPlace(priority, label, historyToken, null, iconCssName);
            // Add null for non container, we need to keep the indexes in sync.
            menuContainers.add(newMainPlaceIndex, null);
        }
    }

    public void addPrimaryMenuItemContainer(String label, String primaryMenuId, int index, String iconCssName) {
        if (!menuContainers.contains(primaryMenuId)) {
            int containerIndex = getView().addPrimaryMenuItemContainer(index, label, iconCssName);
            if (containerIndex > -1 && containerIndex < menuContainers.size()) {
                menuContainers.add(containerIndex, primaryMenuId);
            } else {
                menuContainers.add(primaryMenuId);
            }
        }
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
