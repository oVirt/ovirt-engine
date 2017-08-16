package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.template;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.ui.common.presenter.AbstractMainSelectedItems;
import org.ovirt.engine.ui.webadmin.section.main.presenter.TemplateSelectionChangeEvent;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class TemplateMainSelectedItems extends AbstractMainSelectedItems<VmTemplate>
    implements TemplateSelectionChangeEvent.TemplateSelectionChangeHandler {

    @Inject
    TemplateMainSelectedItems(EventBus eventBus) {
        //This is singleton, so won't leak handlers.
        eventBus.addHandler(TemplateSelectionChangeEvent.getType(), this);
    }

    @Override
    public void onTemplateSelectionChange(TemplateSelectionChangeEvent event) {
        selectedItemsChanged(event.getSelectedItems());
    }
}
