package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.template;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.ui.common.presenter.AbstractMainTabSelectedItems;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.TemplateSelectionChangeEvent;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class TemplateMainTabSelectedItems extends AbstractMainTabSelectedItems<VmTemplate>
    implements TemplateSelectionChangeEvent.TemplateSelectionChangeHandler {

    @Inject
    TemplateMainTabSelectedItems(EventBus eventBus) {
        //This is singleton, so won't leak handlers.
        eventBus.addHandler(TemplateSelectionChangeEvent.getType(), this);
    }

    @Override
    public void onTemplateSelectionChange(TemplateSelectionChangeEvent event) {
        selectedItemsChanged(event.getSelectedItems());
    }
}
