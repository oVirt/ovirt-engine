package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.cluster;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ManageGlusterSwiftModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class ManageGlusterSwiftPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<ManageGlusterSwiftModel, ManageGlusterSwiftPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<ManageGlusterSwiftModel> {
    }

    @Inject
    public ManageGlusterSwiftPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

    @Override
    public void init(ManageGlusterSwiftModel model) {
        super.init(model);
    }

}
