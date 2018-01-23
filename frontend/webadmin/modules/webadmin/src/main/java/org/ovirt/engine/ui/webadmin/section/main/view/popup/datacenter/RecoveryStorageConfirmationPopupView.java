package org.ovirt.engine.ui.webadmin.section.main.view.popup.datacenter;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.table.column.AbstractEntityModelTextColumn;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.datacenter.RecoveryStoragePopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;

public class RecoveryStorageConfirmationPopupView extends AbstractModelBoundPopupView<ConfirmationModel> implements RecoveryStoragePopupPresenterWidget.ViewDef {

    interface Driver extends UiCommonEditorDriver<ConfirmationModel, RecoveryStorageConfirmationPopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, RecoveryStorageConfirmationPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<RecoveryStorageConfirmationPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField(provided = true)
    @Path(value = "latch.entity")
    @WithElementId
    EntityModelCheckBoxEditor latch;

    @UiField
    @Ignore
    HTML messageLabel;

    @UiField
    @Ignore
    Label errorLabel;

    @UiField
    @Ignore
    Label selectNewDSDLabel;

    @UiField(provided = true)
    @Ignore
    @WithElementId
    EntityModelCellTable<ListModel> storageDomainItems;

    private final Driver driver = GWT.create(Driver.class);

    private static final ApplicationConstants constants = AssetProvider.getConstants();
    private static final ApplicationMessages messages = AssetProvider.getMessages();

    @Inject
    public RecoveryStorageConfirmationPopupView(EventBus eventBus) {
        super(eventBus);
        storageDomainItems = new EntityModelCellTable<>(false);
        storageDomainItems.setHeight("30%"); //$NON-NLS-1$
        latch = new EntityModelCheckBoxEditor(Align.RIGHT);
        latch.setLabel(constants.approveOperation());
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable();
        driver.initialize(this);
    }

    @Override
    public void edit(final ConfirmationModel object) {
        storageDomainItems.setRowData(new ArrayList<EntityModel>());
        storageDomainItems.asEditor().edit(object);
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

            // Message
            messageLabel.setHTML(constants.dataCenterRecoveryStoragePopupMessageLabel());

            selectNewDSDLabel.setText(constants.dataCenterRecoveryStoragePopupSelectNewDSDLabel());
        });
    }

    @Override
    public void setMessage(String message) {
        if (message != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
            messageLabel.setVisible(false);
        }
    }

    @Override
    public ConfirmationModel flush() {
        storageDomainItems.flush();
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }

    private void initTable() {
        AbstractEntityModelTextColumn<StorageDomain> nameColumn = new AbstractEntityModelTextColumn<StorageDomain>() {
            @Override
            public String getText(StorageDomain storage) {
                return storage.getStorageName();
            }
        };

        storageDomainItems.addColumn(nameColumn, constants.nameStorage(), "280px"); //$NON-NLS-1$

        AbstractEntityModelTextColumn<StorageDomain> freeSpaceColumn = new AbstractEntityModelTextColumn<StorageDomain>() {
            @Override
            public String getText(StorageDomain storage) {
                if (storage.getAvailableDiskSize() == null || storage.getAvailableDiskSize() < 1) {
                    return messages.gibibytes("< 1"); //$NON-NLS-1$
                }
                return messages.gibibytes(String.valueOf(storage.getAvailableDiskSize()));
            }
        };

        storageDomainItems.addColumn(freeSpaceColumn, constants.freeSpaceStorage(), "80px"); //$NON-NLS-1$
    }

}
