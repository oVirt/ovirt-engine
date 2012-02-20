package org.ovirt.engine.ui.webadmin.section.main.view.popup;

import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractVmPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;
import org.ovirt.engine.ui.webadmin.ApplicationResources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.inject.Inject;

public abstract class AbstractVmPopupView extends WebAdminModelBoundPopupView<UnitVmModel> {

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, AbstractVmPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField(provided = true)
    AbstractVmPopupWidget popupWidget;

    @Inject
    public AbstractVmPopupView(EventBus eventBus, ApplicationResources resources, AbstractVmPopupWidget popupWidget) {
        super(eventBus, resources);
        this.popupWidget = popupWidget;
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
    }

    @Override
    public void focusInput() {
        popupWidget.nameEditor.setFocus(true);
    }

    @Override
    public void edit(UnitVmModel object) {
        popupWidget.edit(object);
    }

    @Override
    public UnitVmModel flush() {
        return popupWidget.flush();
    }

}
