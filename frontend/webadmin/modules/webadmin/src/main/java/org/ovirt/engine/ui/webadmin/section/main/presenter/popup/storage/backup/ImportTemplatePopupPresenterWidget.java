package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.backup;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class ImportTemplatePopupPresenterWidget extends ImportVmPopupPresenterWidget {

    public interface ViewDef extends ImportVmPopupPresenterWidget.ViewDef {
    }

    @Inject
    public ImportTemplatePopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

}
