package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.backup;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.templates.ImportTemplateModel;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjector;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class ImportTemplatePopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<ImportTemplateModel, ImportTemplatePopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<ImportTemplateModel> {
    }

    @Inject
    public ImportTemplatePopupPresenterWidget(EventBus eventBus,
            ViewDef view, ClientGinjector ginjector) {
        super(eventBus, view);
    }

}
