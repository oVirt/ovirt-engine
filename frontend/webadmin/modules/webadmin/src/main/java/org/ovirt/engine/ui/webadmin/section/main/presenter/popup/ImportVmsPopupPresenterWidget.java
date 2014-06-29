package org.ovirt.engine.ui.webadmin.section.main.presenter.popup;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportVmsModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class ImportVmsPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<ImportVmsModel, ImportVmsPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<ImportVmsModel> {
    }

    @Inject
    public ImportVmsPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

    @Override
    public void init(final ImportVmsModel model) {
        super.init(model);
    }
}
