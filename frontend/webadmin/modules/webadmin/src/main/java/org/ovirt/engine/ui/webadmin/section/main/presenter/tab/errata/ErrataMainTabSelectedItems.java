package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.errata;

import org.ovirt.engine.core.common.businessentities.Erratum;
import org.ovirt.engine.ui.common.presenter.AbstractMainTabSelectedItems;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.ErrataSelectionChangeEvent;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class ErrataMainTabSelectedItems extends AbstractMainTabSelectedItems<Erratum>
    implements ErrataSelectionChangeEvent.ErrataSelectionChangeHandler {

    @Inject
    ErrataMainTabSelectedItems(EventBus eventBus) {
        //This is singleton, so won't leak handlers.
        eventBus.addHandler(ErrataSelectionChangeEvent.getType(), this);
    }

    @Override
    public void onErrataSelectionChange(ErrataSelectionChangeEvent event) {
        selectedItemsChanged(event.getSelectedItems());
    }
}
