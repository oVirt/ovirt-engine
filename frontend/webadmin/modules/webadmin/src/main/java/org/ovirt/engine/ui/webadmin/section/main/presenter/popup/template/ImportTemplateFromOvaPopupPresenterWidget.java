package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.template;

import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.backup.ImportTemplatePopupPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class ImportTemplateFromOvaPopupPresenterWidget extends ImportTemplatePopupPresenterWidget {

    public interface ViewDef extends ImportTemplatePopupPresenterWidget.ViewDef {
    }

    @Inject
    public ImportTemplateFromOvaPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

}
