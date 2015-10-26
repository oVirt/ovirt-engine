package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.provider;

import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.ui.common.presenter.AbstractMainTabSelectedItems;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.ProviderSelectionChangeEvent;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class ProviderMainTabSelectedItems extends AbstractMainTabSelectedItems<Provider>
    implements ProviderSelectionChangeEvent.ProviderSelectionChangeHandler {

    @Inject
    ProviderMainTabSelectedItems(EventBus eventBus) {
        //This is singleton, so won't leak handlers.
        eventBus.addHandler(ProviderSelectionChangeEvent.getType(), this);
    }

    @Override
    public void onProviderSelectionChange(ProviderSelectionChangeEvent event) {
        selectedItemsChanged(event.getSelectedItems());
    }
}
