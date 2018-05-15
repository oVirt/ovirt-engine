package org.ovirt.engine.ui.webadmin.section.main.view.popup.storage;

import org.gwtbootstrap3.client.ui.Alert;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.uicommon.storage.DisksAllocationView;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.DisksAllocationModel;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.DisksAllocationPopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.inject.Inject;

public class DisksAllocationPopupView extends AbstractModelBoundPopupView<DisksAllocationModel> implements DisksAllocationPopupPresenterWidget.ViewDef {

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, DisksAllocationPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface Driver extends UiCommonEditorDriver<DisksAllocationModel, DisksAllocationPopupView> {
    }

    final Driver driver = GWT.create(Driver.class);

    @UiField
    Alert warningMessage;

    @UiField
    Alert dynamicWarningMessage;

    @UiField(provided = true)
    @Ignore
    DisksAllocationView disksAllocationView;

    DisksAllocationModel disksAllocationModel;

    @Inject
    public DisksAllocationPopupView(EventBus eventBus) {
        super(eventBus);

        disksAllocationView = new DisksAllocationView();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        disksAllocationView.setUsePatternFly(true);

        driver.initialize(this);
    }

    @Override
    public void edit(DisksAllocationModel object) {
        driver.edit(object);

        disksAllocationView.edit(object);
        disksAllocationModel = object;

        object.getDynamicWarning().getPropertyChangedEvent().addListener((ev, sender, args) -> {
            EntityModel ownerModel = (EntityModel) sender;
            String propName = ((PropertyChangedEventArgs) args).propertyName;

            if ("IsAvailable".equals(propName)) { //$NON-NLS-1$
                dynamicWarningMessage.setVisible(ownerModel.getIsAvailable());
            }
        });

        object.getDynamicWarning().getEntityChangedEvent().addListener((ev, sender, args) -> {
            EntityModel ownerModel = (EntityModel) sender;
            dynamicWarningMessage.setText((String) ownerModel.getEntity());
        });
    }

    @Override
    public DisksAllocationModel flush() {
        driver.flush();
        return disksAllocationView.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }

    @Override
    public void setMessage(String message) {
        super.setMessage(message);

        if (message != null && !message.isEmpty()) {
            warningMessage.setText(message);
        }

        warningMessage.setVisible(message != null && !message.isEmpty());
    }

    interface WidgetStyle extends CssResource {
        String messagePanel();
    }
}
