package org.ovirt.engine.ui.common.view.popup;

import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractVmPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public abstract class AbstractVmPopupView extends AbstractModelBoundWidgetPopupView<UnitVmModel> {

    @Inject
    public AbstractVmPopupView(EventBus eventBus, CommonApplicationResources resources,
            AbstractVmPopupWidget popupWidget) {
        super(eventBus, resources, popupWidget, "670px", "480px");
    }

}
