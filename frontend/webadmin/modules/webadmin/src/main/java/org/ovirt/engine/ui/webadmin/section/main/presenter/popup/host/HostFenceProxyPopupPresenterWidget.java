package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.hosts.FenceProxyModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class HostFenceProxyPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<FenceProxyModel,
    HostFenceProxyPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<FenceProxyModel> {
    }

    @Inject
    public HostFenceProxyPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }
}
