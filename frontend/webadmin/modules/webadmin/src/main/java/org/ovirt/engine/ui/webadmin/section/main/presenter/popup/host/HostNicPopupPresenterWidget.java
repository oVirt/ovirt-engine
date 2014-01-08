package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostNicModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class HostNicPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<HostNicModel, HostNicPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<HostNicModel> {
    }

    @Inject
    public HostNicPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

}
