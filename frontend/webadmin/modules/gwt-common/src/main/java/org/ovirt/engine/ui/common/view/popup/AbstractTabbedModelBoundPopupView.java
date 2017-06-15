package org.ovirt.engine.ui.common.view.popup;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.ui.common.presenter.AbstractTabbedModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.widget.dialog.tab.OvirtTabListItem;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.TabName;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.shared.EventBus;

public abstract class AbstractTabbedModelBoundPopupView<T extends Model> extends AbstractModelBoundPopupView<T>
    implements AbstractTabbedModelBoundPopupPresenterWidget.ViewDef<T> {

    /**
     * The map containing the mapping between the {@code TabName} and {@code TabDialog}s.
     */
    private final Map<TabName, OvirtTabListItem> tabMap = new HashMap<>();

    /**
     * Constructor that calls populateTabMap.
     * @param eventBus The GWT event bus.
     */
    public AbstractTabbedModelBoundPopupView(EventBus eventBus) {
        super(eventBus);
        //Have to populate deferred, so we can be sure that the DialogTabs have been initialized and are not null.
        Scheduler.get().scheduleDeferred(() -> populateTabMap());
    }

    /**
     * Populate the map containing the mapping between the tab name and tab widgets.
     */
    protected abstract void populateTabMap();

    @Override
    public final Map<TabName, OvirtTabListItem> getTabNameMapping() {
        return tabMap;
    }
}
