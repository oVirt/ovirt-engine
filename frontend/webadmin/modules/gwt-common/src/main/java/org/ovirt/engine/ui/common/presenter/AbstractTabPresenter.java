package org.ovirt.engine.ui.common.presenter;

import org.ovirt.engine.ui.common.widget.action.ActionButtonDefinition;
import org.ovirt.engine.ui.common.widget.table.ActionTable;

import com.google.gwt.event.shared.EventBus;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
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

    public AbstractTabPresenter(EventBus eventBus, V view, P proxy) {
        super(eventBus, view, proxy);
    }

    /**
     * Returns the table widget provided by view or {@code null} if this widget isn't available.
     */
    protected abstract ActionTable<?> getTable();

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @ProxyEvent
    public void onAddTabActionButton(AddTabActionButtonEvent event) {
        if (getProxy().getTargetHistoryToken().equals(event.getHistoryToken())) {
            if (getTable() != null) {
                getTable().addActionButton((ActionButtonDefinition) event.getButtonDefinition());
            }
        }
    }

}
