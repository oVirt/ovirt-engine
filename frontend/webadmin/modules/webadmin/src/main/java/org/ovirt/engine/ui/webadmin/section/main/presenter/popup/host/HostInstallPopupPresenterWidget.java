package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.hosts.InstallModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class HostInstallPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<InstallModel, HostInstallPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<InstallModel> {
    }

    @Inject
    public HostInstallPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

}
