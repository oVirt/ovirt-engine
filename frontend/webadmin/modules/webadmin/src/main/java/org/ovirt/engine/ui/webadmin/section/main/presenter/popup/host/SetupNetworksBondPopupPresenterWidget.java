package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.hosts.SetupNetworksBondModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class SetupNetworksBondPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<SetupNetworksBondModel, SetupNetworksBondPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<SetupNetworksBondModel> {

    }

    @Inject
    public SetupNetworksBondPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

}
