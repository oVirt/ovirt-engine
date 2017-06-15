package org.ovirt.engine.ui.common.widget.uicommon.storage;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.widget.HasValidation;
import org.ovirt.engine.ui.common.widget.ValidatedPanelWidget;
import org.ovirt.engine.ui.common.widget.editor.ListModelObjectCellTable;
import org.ovirt.engine.ui.common.widget.table.column.AbstractEditTextColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.storage.ImportFcpStorageModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class ImportFcpStorageView extends AbstractStorageView<ImportFcpStorageModel> implements HasValidation {

    interface Driver extends UiCommonEditorDriver<ImportFcpStorageModel, ImportFcpStorageView> {
    }

    interface ViewUiBinder extends UiBinder<Widget, ImportFcpStorageView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    private final Driver driver = GWT.create(Driver.class);

    @UiField(provided = true)
    @Ignore
    ListModelObjectCellTable<StorageDomain, ListModel> storageDomainsTable;

    @UiField
    ValidatedPanelWidget storageDomainsPanel;

    @UiField
    Label message;

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();


    public ImportFcpStorageView() {
        initViews();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        driver.initialize(this);
    }

    @Override
    public void edit(final ImportFcpStorageModel object) {
        driver.edit(object);

        storageDomainsTable.asEditor().edit(object.getStorageDomains());
        addEventsHandlers(object);
    }

    private void addEventsHandlers(final ImportFcpStorageModel object) {
        object.getPropertyChangedEvent().addListener((ev, sender, args) -> {
            String propName = args.propertyName;
            if (propName.equals("IsValid")) { //$NON-NLS-1$
                onIsValidPropertyChange(object);
            } else if (propName.equals("Message")) { //$NON-NLS-1$
                message.setText(object.getMessage());
            }
        });
    }

    private void initViews() {
        createSotrageDomainsTable();
    }

    private void createSotrageDomainsTable() {
        storageDomainsTable = new ListModelObjectCellTable<>(true, true);
        storageDomainsTable.enableColumnResizing();

        AbstractEditTextColumn<StorageDomain> nameColumn = new AbstractEditTextColumn<StorageDomain>(
                (index, model, value) -> model.setStorageName(value)) {
            @Override
            public String getValue(StorageDomain model) {
                return model.getStorageName();
            }
        };

        storageDomainsTable.addColumn(nameColumn, constants.storageName(), "50%"); //$NON-NLS-1$

        AbstractTextColumn<StorageDomain> storageIdColumn = new AbstractTextColumn<StorageDomain>() {
            @Override
            public String getValue(StorageDomain object) {
                return object.getId().toString();
            }
        };
        storageDomainsTable.addColumn(storageIdColumn, constants.storageIdVgName(), "50%"); //$NON-NLS-1$
    }

    private void onIsValidPropertyChange(Model model) {
        if (model.getIsValid()) {
            markAsValid();
        } else {
            markAsInvalid(model.getInvalidityReasons());
        }
    }

    @Override
    public void markAsValid() {
        storageDomainsPanel.markAsValid();
    }

    @Override
    public void markAsInvalid(List<String> validationHints) {
        storageDomainsPanel.markAsInvalid(validationHints);
    }

    @Override
    public boolean isValid() {
        return storageDomainsPanel.isValid();
    }

    @Override
    public ImportFcpStorageModel flush() {
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }

    @Override
    public void focus() {
    }
}
