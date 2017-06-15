package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.scheduling;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.configure.scheduling.ManagePolicyUnitModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class ManagePolicyUnitPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<ManagePolicyUnitModel, ManagePolicyUnitPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<ManagePolicyUnitModel> {
    }

    @Inject
    public ManagePolicyUnitPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

}
