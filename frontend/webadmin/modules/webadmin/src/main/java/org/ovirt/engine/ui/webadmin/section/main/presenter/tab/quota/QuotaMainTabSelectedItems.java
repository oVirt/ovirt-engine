package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.quota;

import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.ui.common.presenter.AbstractMainTabSelectedItems;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.QuotaSelectionChangeEvent;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class QuotaMainTabSelectedItems extends AbstractMainTabSelectedItems<Quota>
    implements QuotaSelectionChangeEvent.QuotaSelectionChangeHandler {

    @Inject
    QuotaMainTabSelectedItems(EventBus eventBus) {
        //This is singleton, so won't leak handlers.
        eventBus.addHandler(QuotaSelectionChangeEvent.getType(), this);
    }

    @Override
    public void onQuotaSelectionChange(QuotaSelectionChangeEvent event) {
        selectedItemsChanged(event.getSelectedItems());
    }
}
