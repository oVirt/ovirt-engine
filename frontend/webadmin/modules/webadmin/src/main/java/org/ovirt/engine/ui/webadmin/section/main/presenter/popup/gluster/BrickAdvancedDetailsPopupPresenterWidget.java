package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.gluster.BrickAdvancedDetailsModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class BrickAdvancedDetailsPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<BrickAdvancedDetailsModel, BrickAdvancedDetailsPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<BrickAdvancedDetailsModel> {
    }

    @Inject
    public BrickAdvancedDetailsPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

}
