package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.hosts.VfsConfigModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class VfsConfigPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<VfsConfigModel, VfsConfigPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<VfsConfigModel> {
    }

    @Inject
    public VfsConfigPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }
}
