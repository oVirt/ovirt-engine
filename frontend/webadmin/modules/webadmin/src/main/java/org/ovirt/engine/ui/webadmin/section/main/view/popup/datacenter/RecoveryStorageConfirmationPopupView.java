package org.ovirt.engine.ui.webadmin.section.main.view.popup.datacenter;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.datacenter.RecoveryStoragePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.WebAdminModelBoundPopupView;
import org.ovirt.engine.ui.webadmin.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.webadmin.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.webadmin.widget.table.column.EntityModelTextColumn;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.inject.Inject;

public class RecoveryStorageConfirmationPopupView extends WebAdminModelBoundPopupView<ConfirmationModel> implements RecoveryStoragePopupPresenterWidget.ViewDef {

    interface Driver extends SimpleBeanEditorDriver<ConfirmationModel, RecoveryStorageConfirmationPopupView> {
        Driver driver = GWT.create(Driver.class);
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
    Label warningLabel;

    @UiField
    @Ignore
    HTML messageLabel;

    @UiField
    @Ignore
    Label selectNewDSDLabel;

    @UiField(provided = true)
    @Ignore
    @WithElementId
    EntityModelCellTable<ListModel> storageDomainItems;

    @UiField
    ScrollPanel sdItemsScrollPanel;

    private final ApplicationConstants applicationConstants;

    @Inject
    public RecoveryStorageConfirmationPopupView(EventBus eventBus,
            ApplicationResources resources,
            ApplicationConstants constants) {
        super(eventBus, resources);
        storageDomainItems = new EntityModelCellTable<ListModel>(false);
        storageDomainItems.setHeight("30%");
        this.applicationConstants = constants;
        latch = new EntityModelCheckBoxEditor(Align.RIGHT);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable();
        Driver.driver.initialize(this);
    }

    @Override
    public void edit(final ConfirmationModel object) {
        storageDomainItems.setRowData(new ArrayList<EntityModel>());
        storageDomainItems.edit(object);
        Driver.driver.edit(object);

        // Bind "Latch.IsAvailable"
        object.getLatch().getPropertyChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                if ("IsAvailable".equals(((PropertyChangedEventArgs) args).PropertyName)) {
                    EntityModel entity = (EntityModel) sender;
                    if (entity.getIsAvailable()) {
                        latch.setVisible(true);
                    }
                }
            }
        });

        object.getItemsChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                // Warning
                warningLabel.setText(applicationConstants.dataCenterRecoveryStoragePopupWarningLabel());

                // Message
                messageLabel.setHTML(applicationConstants.dataCenterRecoveryStoragePopupMessageLabel());

                selectNewDSDLabel.setText(applicationConstants.dataCenterRecoveryStoragePopupSelectNewDSDLabel());
            }
        });
    }

    @Override
    public ConfirmationModel flush() {
        storageDomainItems.flush();
        return Driver.driver.flush();
    }

    private void initTable() {
        EntityModelTextColumn<EntityModel> nameColumn = new EntityModelTextColumn<EntityModel>() {
            @Override
            public String getValue(EntityModel model) {
                if (model.getEntity() instanceof storage_domains) {
                    return ((storage_domains) model.getEntity()).getstorage_name();
                } else {
                    return "";
                }
            }
        };

        storageDomainItems.setColumnWidth(nameColumn, "280px");
        storageDomainItems.addEntityModelColumn(nameColumn, "Name");

        EntityModelTextColumn<EntityModel> freeSpaceColumn = new EntityModelTextColumn<EntityModel>() {
            @Override
            public String getValue(EntityModel model) {
                if (model.getEntity() instanceof storage_domains) {
                    storage_domains storage = (storage_domains) model.getEntity();
                    if (storage.getavailable_disk_size() == null || storage.getavailable_disk_size() < 1) {
                        return "< 1 GB";
                    }
                    return storage.getavailable_disk_size() + " GB";
                } else {
                    return "";
                }
            }
        };

        storageDomainItems.setColumnWidth(freeSpaceColumn, "80px");
        storageDomainItems.addEntityModelColumn(freeSpaceColumn, "Free Space");
    }
}
