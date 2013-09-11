package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.gluster.RemoveBrickModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class RemoveBrickPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<RemoveBrickModel, RemoveBrickPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<RemoveBrickModel> {
    }

    @Inject
    public RemoveBrickPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

}
