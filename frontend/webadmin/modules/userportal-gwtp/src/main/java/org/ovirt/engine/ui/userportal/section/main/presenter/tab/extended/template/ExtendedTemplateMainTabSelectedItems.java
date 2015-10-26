package org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.template;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.ui.common.presenter.AbstractMainTabSelectedItems;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.ExtendedTemplateSelectionChangeEvent;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class ExtendedTemplateMainTabSelectedItems extends AbstractMainTabSelectedItems<VmTemplate>
    implements ExtendedTemplateSelectionChangeEvent.ExtendedTemplateSelectionChangeHandler {

    @Inject
    public ExtendedTemplateMainTabSelectedItems(EventBus eventBus) {
        //This is singleton, so won't leak handlers.
        eventBus.addHandler(ExtendedTemplateSelectionChangeEvent.getType(), this);
    }

    @Override
    public void onExtendedTemplateSelectionChange(ExtendedTemplateSelectionChangeEvent event) {
        selectedItemsChanged(event.getSelectedItems());
    }
}
