package org.ovirt.engine.ui.common.presenter;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.ui.common.widget.action.ActionButtonDefinition;
import org.ovirt.engine.ui.common.widget.table.ActionTable;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;
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

    public AbstractTabPresenter(EventBus eventBus, V view, P proxy,
            Type<RevealContentHandler<?>> slot) {
        super(eventBus, view, proxy, slot);
    }

    @Override
    protected void onBind() {
        super.onBind();
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            @Override
            public void execute() {
                addPluginActionButtons(actionButtonPluginHandler.getButtons(getProxy().getTargetHistoryToken()));
            }
        });
        //Register this handler for whichever tab opens first, it is bound before the plugin fires its events and
        //before the actionButtonPluginHandler is instantiated and listening to events.
        registerHandler(getEventBus().addHandler(AddTabActionButtonEvent.getType(),
                new AddTabActionButtonEvent.AddTabActionButtonHandler() {

            @Override
            public void onAddTabActionButton(AddTabActionButtonEvent event) {
                if (getProxy().getTargetHistoryToken().equals(event.getHistoryToken())) {
                    List<ActionButtonDefinition<?>> pluginActionButtonList = new ArrayList<>();
                    pluginActionButtonList.add(event.getButtonDefinition());
                    addPluginActionButtons(pluginActionButtonList);
                }
            }
        }));

    }

    private void addPluginActionButtons(List<ActionButtonDefinition<?>> pluginActionButtonList) {
        if (getTable() != null) {
            for(ActionButtonDefinition<?> buttonDef: pluginActionButtonList) {
                getTable().addActionButton((ActionButtonDefinition) buttonDef);
            }
        }
    }
    /**
     * Returns the table widget provided by view or {@code null} if this widget isn't available.
     */
    protected abstract ActionTable<?> getTable();

    @Inject
    public void setActionButtonPluginHandler(PluginActionButtonHandler actionButtonPluginHandler) {
        this.actionButtonPluginHandler = actionButtonPluginHandler;
    }
}
