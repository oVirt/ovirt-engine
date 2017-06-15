package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.profile;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.profiles.DiskProfileBaseModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class DiskProfilePopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<DiskProfileBaseModel, DiskProfilePopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<DiskProfileBaseModel> {
    }

    @Inject
    public DiskProfilePopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

}
