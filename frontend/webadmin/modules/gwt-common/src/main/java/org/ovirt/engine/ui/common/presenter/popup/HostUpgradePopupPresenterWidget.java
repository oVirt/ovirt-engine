package org.ovirt.engine.ui.common.presenter.popup;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.hosts.UpgradeConfirmationModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class HostUpgradePopupPresenterWidget
    extends AbstractModelBoundPopupPresenterWidget<UpgradeConfirmationModel, HostUpgradePopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<UpgradeConfirmationModel> {
    }

    @Inject
    public HostUpgradePopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

}
