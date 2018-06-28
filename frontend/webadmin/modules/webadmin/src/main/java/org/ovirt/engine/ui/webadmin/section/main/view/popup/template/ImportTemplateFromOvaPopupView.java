package org.ovirt.engine.ui.webadmin.section.main.view.popup.template;

import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.template.ImportTemplateFromOvaPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.storage.backup.ImportTemplatePopupView;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class ImportTemplateFromOvaPopupView extends ImportTemplatePopupView implements ImportTemplateFromOvaPopupPresenterWidget.ViewDef {

    @Inject
    public ImportTemplateFromOvaPopupView(EventBus eventBus) {
        super(eventBus);
    }
}
