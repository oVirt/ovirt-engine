package org.ovirt.engine.ui.common.presenter;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.ui.common.widget.action.ActionButtonDefinition;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.presenter.slots.NestedSlot;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

/**
 * Base class for presenters representing tabs within the user interface.
 *
 * @param <V>
 *            View type.
 * @param <P>
 *            Proxy type.
 */
public abstract class AbstractTabPresenter<V extends View, P extends TabContentProxyPlace<?>> extends Presenter<V, P> {

    private PluginActionButtonHandler actionButtonPluginHandler;
    private final ActionPanelPresenterWidget<?, ?, ?> actionPanel;

    public AbstractTabPresenter(EventBus eventBus, V view, P proxy, ActionPanelPresenterWidget<?, ?, ?> actionPanel,
            NestedSlot slot) {
        super(eventBus, view, proxy, slot);
        this.actionPanel = actionPanel;
    }

    public ActionPanelPresenterWidget<?, ?, ?> getActionPanelPresenterWidget() {
        return actionPanel;
    }

    public boolean hasActionPanelPresenterWidget() {
        return getActionPanelPresenterWidget() != null;
    }

    @Override
    protected void onBind() {
        super.onBind();

        Scheduler.get().scheduleDeferred(() -> {
            addPluginActionButtons(actionButtonPluginHandler.getButtons(getProxy().getTargetHistoryToken()), false);
            addPluginActionButtons(actionButtonPluginHandler.getMenuItems(getProxy().getTargetHistoryToken()), true);
        });
        registerHandler(getEventBus().addHandler(AddActionButtonEvent.getType(),
            event -> {
                if (getProxy().getTargetHistoryToken().equals(event.getHistoryToken())) {
                    List<ActionButtonDefinition<?, ?>> pluginActionButtonList = new ArrayList<>();
                    pluginActionButtonList.add(event.getButtonDefinition());
                    addPluginActionButtons(pluginActionButtonList, event.isAddToKebabMenu());
                }
            }));
    }

    private void addPluginActionButtons(List<ActionButtonDefinition<?, ?>> pluginActionButtonList, boolean isMenuItem) {
        if (hasActionPanelPresenterWidget()) {
            for (ActionButtonDefinition<?, ?> buttonDef : pluginActionButtonList) {
                if (isMenuItem) {
                    getActionPanelPresenterWidget().addMenuListItem((ActionButtonDefinition) buttonDef);
                } else {
                    getActionPanelPresenterWidget().addActionButton((ActionButtonDefinition) buttonDef);
                }
            }
        }
    }

    @Inject
    public void setActionButtonPluginHandler(PluginActionButtonHandler actionButtonPluginHandler) {
        this.actionButtonPluginHandler = actionButtonPluginHandler;
    }

}
