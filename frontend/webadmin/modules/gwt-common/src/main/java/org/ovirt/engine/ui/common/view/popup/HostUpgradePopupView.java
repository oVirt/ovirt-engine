package org.ovirt.engine.ui.common.view.popup;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.presenter.popup.HostUpgradePopupPresenterWidget;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.label.WarningNotificationLabel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.UpgradeConfirmationModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;

public class HostUpgradePopupView extends AbstractModelBoundPopupView<UpgradeConfirmationModel>
    implements HostUpgradePopupPresenterWidget.ViewDef {

    interface Driver extends UiCommonEditorDriver<UpgradeConfirmationModel, HostUpgradePopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, HostUpgradePopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<HostUpgradePopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField
    WarningNotificationLabel tsxRemovalInsecureCpuWarning;

    @UiField(provided = true)
    @Path(value = "reboot.entity")
    @WithElementId("reboot")
    public EntityModelCheckBoxEditor reboot;

    @UiField
    @Ignore
    Label messageLabel;

    private final Driver driver = GWT.create(Driver.class);

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public HostUpgradePopupView(EventBus eventBus) {
        super(eventBus);

        reboot = new EntityModelCheckBoxEditor(Align.RIGHT);
        reboot.setLabel(constants.hostRestartAfterUpgrade());

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        driver.initialize(this);
    }

    @Override
    public void edit(UpgradeConfirmationModel model) {
        driver.edit(model);

        tsxRemovalInsecureCpuWarning.setVisible(model.isClusterCpuInsecureAndAffectedByTsxRemoval());
    }

    @Override
    public void setMessage(String message) {
        messageLabel.setText(message);
    }

    @Override
    public UpgradeConfirmationModel flush() {
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }
}
