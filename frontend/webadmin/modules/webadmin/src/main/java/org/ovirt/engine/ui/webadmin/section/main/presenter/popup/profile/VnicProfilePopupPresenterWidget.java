package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.profile;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.profiles.VnicProfileModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class VnicProfilePopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<VnicProfileModel, VnicProfilePopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<VnicProfileModel> {
    }

    @Inject
    public VnicProfilePopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }
}
