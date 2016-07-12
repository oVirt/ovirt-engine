package org.ovirt.engine.ui.webadmin.section.main.view.tab;

import java.util.LinkedList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.searchbackend.DiskConditionFieldAutoCompleter;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.action.ActionButtonDefinition;
import org.ovirt.engine.ui.common.widget.action.CommandLocation;
import org.ovirt.engine.ui.common.widget.table.column.AbstractColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractDiskSizeColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.common.widget.table.header.ImageResourceHeader;
import org.ovirt.engine.ui.common.widget.uicommon.disks.DisksViewColumns;
import org.ovirt.engine.ui.common.widget.uicommon.disks.DisksViewRadioGroup;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.CommonModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemType;
import org.ovirt.engine.ui.uicommonweb.models.disks.DiskListModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabDiskPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractMainTabWithDetailsTableView;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminMenuBarButtonDefinition;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class MainTabDiskView extends AbstractMainTabWithDetailsTableView<Disk, DiskListModel> implements MainTabDiskPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<MainTabDiskView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField
    SimplePanel tablePanel;

    private DisksViewRadioGroup disksViewRadioGroup;
    private boolean isQuotaVisible;

    private static AbstractTextColumn<Disk> aliasColumn;
    private static AbstractTextColumn<Disk> idColumn;
    private static AbstractDiskSizeColumn sizeColumn;
    private static AbstractTextColumn<Disk> allocationColumn;
    private static AbstractTextColumn<Disk> dateCreatedColumn;
    private static AbstractColumn<Disk, Disk> statusColumn;
    private static AbstractTextColumn<Disk> lunIdColumn;
    private static AbstractTextColumn<Disk> lunSerialColumn;
    private static AbstractTextColumn<Disk> lunVendorIdColumn;
    private static AbstractTextColumn<Disk> lunProductIdColumn;
    private static AbstractTextColumn<Disk> qoutaColumn;
    private static AbstractTextColumn<Disk> diskStorageTypeColumn;
    private static AbstractTextColumn<Disk> cinderVolumeTypeColumn;
    private static AbstractTextColumn<Disk> descriptionColumn;

    @Inject
    Provider<CommonModel> commonModelProvider;

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public MainTabDiskView(MainModelProvider<Disk, DiskListModel> modelProvider) {
        super(modelProvider);

        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTableColumns();
        initTableButtons();
        initTableOverhead();
        initWidget(getTable());
    }

    final ClickHandler clickHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            if (((RadioButton) event.getSource()).getValue()) {
                getMainModel().getDiskViewType().setEntity(disksViewRadioGroup.getDiskStorageType());
            }
        }
    };

    final IEventListener<EventArgs> diskTypeChangedEventListener = new IEventListener<EventArgs>() {
        @Override
        public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
            EntityModel diskViewType = (EntityModel) sender;
            disksViewRadioGroup.setDiskStorageType((DiskStorageType) diskViewType.getEntity());
            if (commonModelProvider.get().getSelectedItem() instanceof DiskListModel) {
                onDiskViewTypeChanged();
            }
        }
    };

    @Override
    public IEventListener<EventArgs> getDiskTypeChangedEventListener() {
        return diskTypeChangedEventListener;
    }

    @Override
    public void handleQuotaColumnVisibility() {
        isQuotaVisible = false;
        SystemTreeItemModel treeItem =
                commonModelProvider.get().getSystemTree().getSelectedItem();
        if (treeItem != null
                && SystemTreeItemType.DataCenter == treeItem.getType()) {
            StoragePool storagePool = (StoragePool) treeItem.getEntity();
            if (QuotaEnforcementTypeEnum.DISABLED != storagePool.getQuotaEnforcementType()) {
                isQuotaVisible = true;
            }
        }
        onDiskViewTypeChanged();
    }

    void onDiskViewTypeChanged() {
        boolean all = disksViewRadioGroup.getAllButton().getValue();
        boolean images = disksViewRadioGroup.getImagesButton().getValue();
        boolean luns = disksViewRadioGroup.getLunsButton().getValue();
        boolean cinder = disksViewRadioGroup.getCinderButton().getValue();

        searchByDiskViewType(disksViewRadioGroup.getDiskStorageType());

        getTable().ensureColumnVisible(
                aliasColumn, constants.aliasDisk(), all || images || luns || cinder,
                "120px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                idColumn, constants.idDisk(), all || images || luns || cinder,
                "120px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                DisksViewColumns.shareableDiskColumn,
                new ImageResourceHeader(DisksViewColumns.shareableDiskColumn.getDefaultImage(),
                        SafeHtmlUtils.fromSafeConstant(constants.shareable())),
                all || images || luns || cinder, "30px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                DisksViewColumns.diskContainersIconColumn, "", all || images || luns || cinder, //$NON-NLS-1$
                "30px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                DisksViewColumns.diskContainersColumn, constants.attachedToDisk(), all || images || luns || cinder,
                "125px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                DisksViewColumns.storageDomainsColumn, constants.storageDomainsDisk(), images || cinder,
                "180px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                sizeColumn, constants.provisionedSizeDisk(), all || images || luns || cinder,
                "110px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                allocationColumn, constants.allocationDisk(), images,
                "130px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                cinderVolumeTypeColumn, constants.cinderVolumeTypeDisk(), cinder, "80px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                dateCreatedColumn, constants.creationDateDisk(), images || cinder,
                "130px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                statusColumn, constants.statusDisk(), images || cinder || all,
                "80px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                lunIdColumn, constants.lunIdSanStorage(), luns,
                "100px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                lunSerialColumn, constants.serialSanStorage(), luns,
                "100px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                lunVendorIdColumn, constants.vendorIdSanStorage(), luns,
                "100px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                lunProductIdColumn, constants.productIdSanStorage(), luns,
                "100px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                qoutaColumn, constants.quotaDisk(), images && isQuotaVisible, "120px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                diskStorageTypeColumn, constants.typeDisk(), all, "80px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                descriptionColumn, constants.descriptionDisk(), all || images || luns || cinder,
                "90px"); //$NON-NLS-1$
    }

    void initTableColumns() {
        getTable().enableColumnResizing();

        aliasColumn = DisksViewColumns.getAliasColumn(DiskConditionFieldAutoCompleter.ALIAS);
        idColumn = DisksViewColumns.getIdColumn(DiskConditionFieldAutoCompleter.ID);
        sizeColumn = DisksViewColumns.getSizeColumn(DiskConditionFieldAutoCompleter.PROVISIONED_SIZE);
        allocationColumn = DisksViewColumns.getAllocationColumn(constants.empty());
        dateCreatedColumn = DisksViewColumns.getDateCreatedColumn(DiskConditionFieldAutoCompleter.CREATION_DATE);
        statusColumn = DisksViewColumns.getStatusColumn(DiskConditionFieldAutoCompleter.STATUS);
        lunIdColumn = DisksViewColumns.getLunIdColumn(constants.empty());
        lunSerialColumn = DisksViewColumns.getLunSerialColumn(constants.empty());
        lunVendorIdColumn = DisksViewColumns.getLunVendorIdColumn(constants.empty());
        lunProductIdColumn = DisksViewColumns.getLunProductIdColumn(constants.empty());
        qoutaColumn = DisksViewColumns.getQoutaColumn(DiskConditionFieldAutoCompleter.QUOTA);
        diskStorageTypeColumn = DisksViewColumns.getDiskStorageTypeColumn(DiskConditionFieldAutoCompleter.DISK_TYPE);
        cinderVolumeTypeColumn = DisksViewColumns.getCinderVolumeTypeColumn(null);
        descriptionColumn = DisksViewColumns.getDescriptionColumn(DiskConditionFieldAutoCompleter.DESCRIPTION);
    }

    void initTableOverhead() {
        disksViewRadioGroup = new DisksViewRadioGroup();
        disksViewRadioGroup.setClickHandler(clickHandler);
        disksViewRadioGroup.addStyleName("mtdv_radioGroup_pfly_fix"); //$NON-NLS-1$
        getTable().setTableOverhead(disksViewRadioGroup);
        getTable().setTableTopMargin(20);
    }

    void initTableButtons() {
        getTable().addActionButton(new WebAdminButtonDefinition<Disk>(constants.newDisk()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getNewCommand();
            }
        });

        getTable().addActionButton(new WebAdminButtonDefinition<Disk>(constants.removeDisk()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getRemoveCommand();
            }
        });

        getTable().addActionButton(new WebAdminButtonDefinition<Disk>(constants.moveDisk()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getMoveCommand();
            }
        });

        getTable().addActionButton(new WebAdminButtonDefinition<Disk>(constants.copyDisk()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getCopyCommand();
            }
        });

        getTable().addActionButton(new WebAdminButtonDefinition<Disk>(constants.getDiskAlignment(),
                CommandLocation.OnlyFromContext) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getScanAlignmentCommand();
            }
        });

        getTable().addActionButton(new WebAdminButtonDefinition<Disk>(constants.exportDisk()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getExportCommand();
            }
        });

        getTable().addActionButton(new WebAdminButtonDefinition<Disk>(constants.assignQuota()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getChangeQuotaCommand();
            }
        });

        // Upload operations drop down
        List<ActionButtonDefinition<Disk>> uploadActions = new LinkedList<>();
        uploadActions.add(new WebAdminButtonDefinition<Disk>(constants.uploadImageStart()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getUploadCommand();
            }
        });
        uploadActions.add(new WebAdminButtonDefinition<Disk>(constants.uploadImageCancel()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getCancelUploadCommand();
            }
        });
        uploadActions.add(new WebAdminButtonDefinition<Disk>(constants.uploadImagePause()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getPauseUploadCommand();
            }
        });
        uploadActions.add(new WebAdminButtonDefinition<Disk>(constants.uploadImageResume()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getResumeUploadCommand();
            }
        });
        getTable().addActionButton(new WebAdminMenuBarButtonDefinition<>(
                constants.uploadImage(), uploadActions));
    }

    void searchByDiskViewType(Object diskViewType) {
        final String disksSearchPrefix = "Disks:"; //$NON-NLS-1$
        final String diskTypeSearchPrefix = "disk_type = "; //$NON-NLS-1$
        final String searchConjunctionAnd = "and "; //$NON-NLS-1$
        final String searchRegexDisksSearchPrefix = "^\\s*(disk(s)?\\s*(:)+)+\\s*"; //$NON-NLS-1$
        final String searchRegexDiskTypeClause = "\\s*((and|or)\\s+)?disk_type\\s*=\\s*\\S+"; //$NON-NLS-1$
        final String searchRegexStartConjunction = "^\\s*(and|or)\\s*"; //$NON-NLS-1$
        final String searchRegexFlags = "ig"; //$NON-NLS-1$

        final String space = " "; //$NON-NLS-1$
        final String empty = ""; //$NON-NLS-1$
        final String colon = ":"; //$NON-NLS-1$

        RegExp searchPatternDisksSearchPrefix = RegExp.compile(searchRegexDisksSearchPrefix, searchRegexFlags);
        RegExp searchPatternDiskTypeClause = RegExp.compile(searchRegexDiskTypeClause, searchRegexFlags);
        RegExp searchPatternStartConjunction = RegExp.compile(searchRegexStartConjunction, searchRegexFlags);

        String diskTypePostfix = diskViewType != null ?
                ((DiskStorageType) diskViewType).name().toLowerCase() + space : null;
        String diskTypeClause = diskTypePostfix != null ?
                diskTypeSearchPrefix + diskTypePostfix : empty;

        String inputSearchString = commonModelProvider.get().getSearchString().trim();
        String inputSearchStringPrefix = commonModelProvider.get().getSearchStringPrefix().trim();

        if (!inputSearchString.isEmpty() && inputSearchStringPrefix.isEmpty()) {
            int indexOfColon = inputSearchString.indexOf(colon);
            inputSearchStringPrefix = inputSearchString.substring(0, indexOfColon + 1).trim();
            inputSearchString = inputSearchString.substring(indexOfColon + 1).trim();
        }
        if (inputSearchStringPrefix.isEmpty()) {
            inputSearchStringPrefix = disksSearchPrefix;
            inputSearchString = empty;
        }

        String searchStringPrefixRaw = searchPatternDiskTypeClause
                .replace(inputSearchStringPrefix, empty).trim();

        String searchStringPrefix;
        if (diskTypeClause.equals(empty)) {
            searchStringPrefix = searchStringPrefixRaw + space;
        }
        else {
            searchStringPrefix = searchStringPrefixRaw + space
                    + (searchPatternDisksSearchPrefix.test(searchStringPrefixRaw) ? empty : searchConjunctionAnd)
                    + diskTypeClause;
        }

        inputSearchString = searchPatternDiskTypeClause
                .replace(inputSearchString, empty);
        inputSearchString = searchPatternStartConjunction
                .replace(inputSearchString, empty);

        String searchString;
        if (searchPatternDisksSearchPrefix.test(searchStringPrefix) || inputSearchString.isEmpty()) {
            searchString = inputSearchString;
        }
        else {
            searchString = searchConjunctionAnd + inputSearchString;
        }
        commonModelProvider.get().setSearchStringPrefix(searchStringPrefix);
        commonModelProvider.get().setSearchString(searchString);

        getTable().getSelectionModel().clear();
        getMainModel().setItems(null);
        getMainModel().setSearchString(commonModelProvider.get().getEffectiveSearchString());
        getMainModel().search();
    }
}
