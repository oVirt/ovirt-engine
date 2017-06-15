package org.ovirt.engine.ui.common.widget.uicommon.storage;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.widget.HasValidation;
import org.ovirt.engine.ui.common.widget.ValidatedPanelWidget;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.editor.ListModelObjectCellTable;
import org.ovirt.engine.ui.common.widget.table.column.AbstractCheckboxColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractEditTextColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.common.widget.table.header.AbstractSelectAllCheckBoxHeader;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.storage.ImportIscsiStorageModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.SanTargetModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionModel;

public class ImportIscsiStorageView extends AbstractStorageView<ImportIscsiStorageModel> implements HasValidation {

    interface Driver extends UiCommonEditorDriver<ImportIscsiStorageModel, ImportIscsiStorageView> {
    }

    interface ViewUiBinder extends UiBinder<Widget, ImportIscsiStorageView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    private final Driver driver = GWT.create(Driver.class);

    @UiField
    WidgetStyle style;

    @UiField(provided = true)
    @Ignore
    IscsiDiscoverTargetsView iscsiDiscoverTargetsView;

    @UiField(provided = true)
    SplitLayoutPanel splitLayoutPanel;

    @UiField(provided = true)
    @Ignore
    EntityModelCellTable<ListModel<SanTargetModel>> targetsTable;

    @UiField(provided = true)
    @Ignore
    ListModelObjectCellTable<StorageDomain, ListModel> storageDomainsTable;

    @UiField
    ValidatedPanelWidget storageDomainsPanel;

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();

    public ImportIscsiStorageView() {
        initViews();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        addStyles();
        driver.initialize(this);
    }

    void addStyles() {
        iscsiDiscoverTargetsView.setLoginButtonStyle(style.loginButton());
    }

    @Override
    public void edit(final ImportIscsiStorageModel object) {
        driver.edit(object);

        iscsiDiscoverTargetsView.edit(object);
        targetsTable.asEditor().edit(object.getTargets());
        storageDomainsTable.asEditor().edit(object.getStorageDomains());

        addEventsHandlers(object);
    }

    private void addEventsHandlers(final ImportIscsiStorageModel object) {
        object.getPropertyChangedEvent().addListener((ev, sender, args) -> {
            String propName = args.propertyName;
            if (propName.equals("IsValid")) { //$NON-NLS-1$
                onIsValidPropertyChange(object);
            }
        });
        object.getTargets().getSelectedItemsChangedEvent().addListener((ev, sender, args) -> {
            if (object.getTargets().getSelectedItems() != null && object.getTargets().getSelectedItems().isEmpty()) {
                // Clear items selection
                ((MultiSelectionModel) targetsTable.getSelectionModel()).clear();
            }
        });
    }

    private void initViews() {
        // Create split layout panel
        splitLayoutPanel = new SplitLayoutPanel(4);

        // Create discover panel
        iscsiDiscoverTargetsView = new IscsiDiscoverTargetsView();

        // Create tables
        createTargetsTable();
        createSotrageDomainsTable();
    }

    private void createTargetsTable() {
        targetsTable = new EntityModelCellTable<>(true, true);
        targetsTable.enableColumnResizing();

        addTargetsSelectionColumn();

        AbstractTextColumn<SanTargetModel> iqnColumn = new AbstractTextColumn<SanTargetModel>() {
            @Override
            public String getValue(SanTargetModel model) {
                return model.getEntity().getIqn();
            }
        };
        targetsTable.addColumn(iqnColumn, constants.iqn(), "60%"); //$NON-NLS-1$

        AbstractTextColumn<SanTargetModel> addressColumn = new AbstractTextColumn<SanTargetModel>() {
            @Override
            public String getValue(SanTargetModel model) {
                return model.getEntity().getConnection();
            }
        };
        targetsTable.addColumn(addressColumn, constants.addressSanStorage(), "130px"); //$NON-NLS-1$

        AbstractTextColumn<SanTargetModel> portColumn = new AbstractTextColumn<SanTargetModel>() {
            @Override
            public String getValue(SanTargetModel model) {
                return model.getEntity().getPort();
            }
        };
        targetsTable.addColumn(portColumn, constants.portSanStorage(), "70px"); //$NON-NLS-1$
    }

    private void addTargetsSelectionColumn() {
        AbstractSelectAllCheckBoxHeader<SanTargetModel> selectAllHeader = new AbstractSelectAllCheckBoxHeader<SanTargetModel>() {
            @Override
            protected void selectionChanged(Boolean value) {
                ListModel listModel = targetsTable.asEditor().flush();
                if (listModel == null || listModel.getItems() == null) {
                    return;
                }
                handleSelection(value, listModel, targetsTable.getSelectionModel());
            }

            @Override
            public void handleSelection(Boolean value, ListModel listModel, SelectionModel selectionModel) {
                if (!listModel.getItems().iterator().hasNext()) {
                    return;
                }
                ArrayList<SanTargetModel> selectedItems = new ArrayList<>();
                for (SanTargetModel entity : (Iterable<SanTargetModel>) listModel.getItems()) {
                    if (!entity.getIsLoggedIn()) {
                        if (value) {
                            selectedItems.add(entity);
                        }
                        selectionModel.setSelected(entity, value);
                    }
                }
                listModel.setSelectedItems(selectedItems);
            }

            @Override
            public Boolean getValue() {
                ListModel listModel = targetsTable.asEditor().flush();
                if (listModel == null || listModel.getItems() == null) {
                    return false;
                }
                return getCheckValue(listModel.getItems(), targetsTable.getSelectionModel());
            }
        };
        AbstractCheckboxColumn<SanTargetModel> checkColumn = new AbstractCheckboxColumn<SanTargetModel>() {
            @Override
            protected boolean canEdit(SanTargetModel object) {
                return !object.getIsLoggedIn();
            }

            @Override
            public Boolean getValue(SanTargetModel object) {
                return targetsTable.getSelectionModel().isSelected(object) || object.getIsLoggedIn();
            }
        };
        targetsTable.addColumn(checkColumn, selectAllHeader, "25px"); //$NON-NLS-1$
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
    public boolean isSubViewFocused() {
        return iscsiDiscoverTargetsView.isDiscoverPanelFocused();
    }

    @Override
    public ImportIscsiStorageModel flush() {
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }

    @Override
    public void focus() {
    }

    interface WidgetStyle extends CssResource {
        String loginButton();
    }

}
