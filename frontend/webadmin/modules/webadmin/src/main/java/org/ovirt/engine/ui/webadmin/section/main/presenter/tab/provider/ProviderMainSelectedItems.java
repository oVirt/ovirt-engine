package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.provider;

import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.ui.common.presenter.AbstractMainSelectedItems;
import org.ovirt.engine.ui.webadmin.section.main.presenter.ProviderSelectionChangeEvent;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class ProviderMainSelectedItems extends AbstractMainSelectedItems<Provider>
    implements ProviderSelectionChangeEvent.ProviderSelectionChangeHandler {

    @Inject
    ProviderMainSelectedItems(EventBus eventBus) {
        //This is singleton, so won't leak handlers.
        eventBus.addHandler(ProviderSelectionChangeEvent.getType(), this);
    }

    @Override
    public void onProviderSelectionChange(ProviderSelectionChangeEvent event) {
        selectedItemsChanged(event.getSelectedItems());
    }
}
