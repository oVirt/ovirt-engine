package org.ovirt.engine.ui.webadmin.section.main.view.popup.instancetypes;

import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.view.popup.AbstractVmPopupView;
import org.ovirt.engine.ui.common.widget.uicommon.popup.instancetypes.InstanceTypesPopupWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.instancetypes.InstanceTypesPopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class InstanceTypesPopupView extends AbstractVmPopupView implements InstanceTypesPopupPresenterWidget.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<InstanceTypesPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public InstanceTypesPopupView(EventBus eventBus) {
        super(eventBus, new InstanceTypesPopupWidget(eventBus), "710px", "570px"); //$NON-NLS-1$ //$NON-NLS-2$
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }
}
