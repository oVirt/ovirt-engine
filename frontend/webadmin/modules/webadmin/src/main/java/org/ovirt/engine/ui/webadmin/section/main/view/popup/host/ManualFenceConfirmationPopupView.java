package org.ovirt.engine.ui.webadmin.section.main.view.popup.host;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VdsSpmStatus;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.ManualFencePopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;

public class ManualFenceConfirmationPopupView extends AbstractModelBoundPopupView<ConfirmationModel> implements ManualFencePopupPresenterWidget.ViewDef {

    interface Driver extends UiCommonEditorDriver<ConfirmationModel, ManualFenceConfirmationPopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, ManualFenceConfirmationPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<ManualFenceConfirmationPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField(provided = true)
    @Path(value = "latch.entity")
    @WithElementId
    EntityModelCheckBoxEditor latch;

    @UiField
    @Ignore
    Label warningLabel;

    @UiField
    @Ignore
    Label spmWarningLabel;

    @UiField
    @Ignore
    Label messageLabel;

    private final Driver driver = GWT.create(Driver.class);

    private static final ApplicationConstants constants = AssetProvider.getConstants();
    private static final ApplicationMessages messages = AssetProvider.getMessages();

    @Inject
    public ManualFenceConfirmationPopupView(EventBus eventBus) {
        super(eventBus);
        latch = new EntityModelCheckBoxEditor(Align.RIGHT);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        addStyles();
        driver.initialize(this);
    }

    private void addStyles() {
        latch.addLabelStyleName("confirmCheckBoxLabel"); //$NON-NLS-1$
    }

    @Override
    public void edit(final ConfirmationModel object) {
        driver.edit(object);

        // Bind "Latch.IsAvailable"
        object.getLatch().getPropertyChangedEvent().addListener((ev, sender, args) -> {
            if ("IsAvailable".equals(args.propertyName)) { //$NON-NLS-1$
                EntityModel entity = (EntityModel) sender;
                if (entity.getIsAvailable()) {
                    latch.setVisible(true);
                }
            }
        });

        object.getItemsChangedEvent().addListener((ev, sender, args) -> {
            VDS vds = (VDS) object.getItems().iterator().next();

            // Message
            messageLabel.setText(messages.manaulFencePopupMessageLabel(vds.getName()));

            // Spm warning
            VdsSpmStatus spmStatus = vds.getSpmStatus();
            if (spmStatus == VdsSpmStatus.None) {
                spmWarningLabel.setText(constants.manaulFencePopupNoneSpmWarningLabel());
            } else if (spmStatus == VdsSpmStatus.SPM) {
                spmWarningLabel.setText(constants.manaulFencePopupSpmWarningLabel());
            } else if (spmStatus == VdsSpmStatus.Contending) {
                spmWarningLabel.setText(constants.manaulFencePopupContendingSpmWarningLabel());

            }

            // Warning
            warningLabel.setText(constants.manaulFencePopupWarningLabel());
        });
    }

    @Override
    public ConfirmationModel flush() {
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }
}
