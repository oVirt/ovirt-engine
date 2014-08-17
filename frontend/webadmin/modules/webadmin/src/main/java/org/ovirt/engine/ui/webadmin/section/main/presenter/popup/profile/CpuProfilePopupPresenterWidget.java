package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.profile;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.profiles.CpuProfileBaseModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class CpuProfilePopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<CpuProfileBaseModel, CpuProfilePopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<CpuProfileBaseModel> {
    }

    @Inject
    public CpuProfilePopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

}
