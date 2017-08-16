package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.quota;

import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.ui.common.presenter.AbstractMainSelectedItems;
import org.ovirt.engine.ui.webadmin.section.main.presenter.QuotaSelectionChangeEvent;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class QuotaMainSelectedItems extends AbstractMainSelectedItems<Quota>
    implements QuotaSelectionChangeEvent.QuotaSelectionChangeHandler {

    @Inject
    QuotaMainSelectedItems(EventBus eventBus) {
        //This is singleton, so won't leak handlers.
        eventBus.addHandler(QuotaSelectionChangeEvent.getType(), this);
    }

    @Override
    public void onQuotaSelectionChange(QuotaSelectionChangeEvent event) {
        selectedItemsChanged(event.getSelectedItems());
    }
}
