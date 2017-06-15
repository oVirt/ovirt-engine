package org.ovirt.engine.ui.webadmin.section.main.view.popup.host;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.hosts.SetupNetworksLabelModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class SetupNetworksLabelPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<SetupNetworksLabelModel, SetupNetworksLabelPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<SetupNetworksLabelModel> {
    }

    @Inject
    public SetupNetworksLabelPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

}
