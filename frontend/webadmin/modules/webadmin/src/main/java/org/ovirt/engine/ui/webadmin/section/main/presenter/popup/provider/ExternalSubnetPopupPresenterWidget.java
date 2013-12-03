package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.provider;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.providers.NewExternalSubnetModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class ExternalSubnetPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<NewExternalSubnetModel, ExternalSubnetPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<NewExternalSubnetModel> {
    }

    @Inject
    public ExternalSubnetPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }
}
