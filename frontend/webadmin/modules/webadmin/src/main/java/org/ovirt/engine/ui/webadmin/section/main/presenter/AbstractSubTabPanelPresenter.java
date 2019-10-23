package org.ovirt.engine.ui.webadmin.section.main.presenter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.ui.common.presenter.AbstractMainSelectedItems;
import org.ovirt.engine.ui.common.presenter.ActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.presenter.DynamicTabContainerPresenter;
import org.ovirt.engine.ui.common.presenter.DynamicTabContainerPresenter.DynamicTabPanel;
import org.ovirt.engine.ui.common.presenter.PluginActionButtonHandler;
import org.ovirt.engine.ui.common.widget.action.ActionButtonDefinition;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.place.WebAdminPlaceManager;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.gwtplatform.mvp.client.ChangeTabHandler;
import com.gwtplatform.mvp.client.RequestTabsHandler;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.TabView;
import com.gwtplatform.mvp.client.proxy.Proxy;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;

/**
 * Base class for sub tab panel presenters.
 *
 * @param <V>
 *            View type.
 * @param <P>
 *            Proxy type.
 */
public abstract class AbstractSubTabPanelPresenter<V extends AbstractSubTabPanelPresenter.ViewDef &
    DynamicTabPanel, P extends Proxy<?>> extends DynamicTabContainerPresenter<V, P> {

    public interface ViewDef extends TabView {

        void setTabVisible(TabData tabData, boolean visible);

        ActionPanelPresenterWidget<?, ?, ?> getActionPanelPresenterWidget();

    }

    @Inject
    private PluginActionButtonHandler actionButtonPluginHandler;

    @Inject
    private WebAdminPlaceManager placeManager;

    private final Map<TabData, Model> detailTabToModelMapping = new HashMap<>();
    private boolean pluginActionButtonsInitialized = false;

    public AbstractSubTabPanelPresenter(EventBus eventBus, V view, P proxy,
            Object tabContentSlot,
            Type<RequestTabsHandler> requestTabsEventType,
            Type<ChangeTabHandler> changeTabEventType,
            AbstractMainSelectedItems<?> selectedItems) {
        this(eventBus, view, proxy, tabContentSlot, requestTabsEventType, changeTabEventType,
                selectedItems, MainContentPresenter.TYPE_SetContent);
    }

    public AbstractSubTabPanelPresenter(EventBus eventBus, V view, P proxy,
            Object tabContentSlot,
            Type<RequestTabsHandler> requestTabsEventType,
            Type<ChangeTabHandler> changeTabEventType,
            AbstractMainSelectedItems<?> selectedItems,
            Type<RevealContentHandler<?>> slot) {
        super(eventBus, view, proxy, tabContentSlot, requestTabsEventType, changeTabEventType,
                slot);
    }

    protected abstract void initDetailTabToModelMapping(Map<TabData, Model> mapping);

    @Override
    protected void onBind() {
        super.onBind();

        // initialize detail tab to model mappings
        initDetailTabToModelMapping(detailTabToModelMapping);

        // add IsAvailable property change listener for each detail model
        for (Map.Entry<TabData, Model> entry : detailTabToModelMapping.entrySet()) {
            TabData tabData = entry.getKey();
            Model detailModel = entry.getValue();
            detailModel.getPropertyChangedEvent().addListener((ev, sender, args) -> {
                if ("IsAvailable".equals(args.propertyName)) { //$NON-NLS-1$
                    updateTabVisibility(tabData, detailModel);
                }
            });
        }
    }

    @Override
    protected void onReveal() {
        super.onReveal();

        // make sure all detail tabs have their visibility updated
        for (Map.Entry<TabData, Model> entry : detailTabToModelMapping.entrySet()) {
            TabData tabData = entry.getKey();
            Model detailModel = entry.getValue();
            updateTabVisibility(tabData, detailModel);
        }

        if (!pluginActionButtonsInitialized) {
            String currentPlaceToken = placeManager.getCurrentPlaceRequest().getNameToken();
            String mainPlace = currentPlaceToken.substring(0,
                    currentPlaceToken.indexOf(WebAdminApplicationPlaces.SUB_TAB_PREFIX));

            addPluginActionButtons(actionButtonPluginHandler.getButtons(mainPlace), false);
            addPluginActionButtons(actionButtonPluginHandler.getMenuItems(mainPlace), true);
            pluginActionButtonsInitialized = true;
        }
    }

    private void updateTabVisibility(TabData tabData, Model model) {
        getView().setTabVisible(tabData, model.getIsAvailable());
    }

    private void addPluginActionButtons(List<ActionButtonDefinition<?, ?>> pluginActionButtonList, boolean isMenuItem) {
        ActionPanelPresenterWidget<?, ?, ?> actionPanel = getView().getActionPanelPresenterWidget();
        if (actionPanel != null) {
            for (ActionButtonDefinition<?, ?> buttonDef: pluginActionButtonList) {
                if (isMenuItem) {
                    actionPanel.addMenuListItem((ActionButtonDefinition) buttonDef);
                } else {
                    actionPanel.addActionButton((ActionButtonDefinition) buttonDef);
                }
            }
        }
    }

}
