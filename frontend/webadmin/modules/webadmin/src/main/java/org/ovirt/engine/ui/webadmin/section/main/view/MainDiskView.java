package org.ovirt.engine.ui.webadmin.section.main.view;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskContentType;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.searchbackend.DiskConditionFieldAutoCompleter;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.presenter.FragmentParams;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractDiskSizeColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractImageResourceColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.common.widget.table.column.DiskContainersColumn;
import org.ovirt.engine.ui.common.widget.table.column.StorageDomainsColumn;
import org.ovirt.engine.ui.common.widget.table.header.ImageResourceHeader;
import org.ovirt.engine.ui.common.widget.uicommon.disks.DisksContentTypeSelectionList;
import org.ovirt.engine.ui.common.widget.uicommon.disks.DisksViewColumns;
import org.ovirt.engine.ui.common.widget.uicommon.disks.DisksViewRadioGroup;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterListModel;
import org.ovirt.engine.ui.uicommonweb.models.disks.DiskListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.MainDiskPresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.inject.Inject;

public class MainDiskView extends AbstractMainWithDetailsTableView<Disk, DiskListModel> implements MainDiskPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<MainDiskView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField
    SimplePanel tablePanel;

    private DisksViewRadioGroup disksViewRadioGroup;
    private DisksContentTypeSelectionList disksContentTypeSelectionList;
    private boolean isQuotaVisible;

    private static AbstractTextColumn<Disk> aliasColumn;
    private static AbstractTextColumn<Disk> idColumn;
    private static AbstractDiskSizeColumn<Disk> sizeColumn;
    private static AbstractTextColumn<Disk> allocationColumn;
    private static AbstractTextColumn<Disk> dateCreatedColumn;
    private static AbstractTextColumn<Disk> dateModifiedColumn;
    private static AbstractColumn<Disk, Disk> statusColumn;
    private static AbstractTextColumn<Disk> lunIdColumn;
    private static AbstractTextColumn<Disk> lunSerialColumn;
    private static AbstractTextColumn<Disk> lunVendorIdColumn;
    private static AbstractTextColumn<Disk> lunProductIdColumn;
    private static AbstractTextColumn<Disk> qoutaColumn;
    private static AbstractTextColumn<Disk> diskStorageTypeColumn;
    private static AbstractTextColumn<Disk> contentTypeColumn;
    private static AbstractTextColumn<Disk> descriptionColumn;
    private static AbstractImageResourceColumn<Disk> shareableDiskColumn;
    private static DiskContainersColumn diskContainersColumn;
    private static StorageDomainsColumn storageDomainsColumn;

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    private DataCenterListModel dataCenterListModel;

    @Inject
    public MainDiskView(MainModelProvider<Disk, DiskListModel> modelProvider) {
        super(modelProvider);

        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTableColumns();
        initTableOverhead();
        initWidget(getTable());
    }

    /**
     * Listen to disk type changes from the UI button group - push the change through the model.
     */
    final DisksViewRadioGroup.DisksViewChangeHandler diskViewTypeChange = newType -> {
        getMainModel().getDiskViewType().setEntity(newType);
    };

    /**
     * Listen to disk content type changes from the UI selection list - push the change through the model.
     */
    final DisksContentTypeSelectionList.DisksContentViewChangeHandler diskContentViewTypeChange = newType -> {
        getMainModel().getDiskContentType().setEntity(newType);
    };

    /**
     * Listen to disk type changes from the model adjusting the view as appropriate.
     */
    final IEventListener<EventArgs> diskTypeChangedEventListener = (ev, sender, args) -> {
        DiskStorageType diskType = getMainModel().getDiskViewType().getEntity();
        disksViewRadioGroup.setDiskStorageType(diskType);
        onDiskViewTypeOrContentTypeChanged();
    };

    final IEventListener<EventArgs> diskContentTypeChangedEventListener = (ev, sender, args) -> {
        EntityModel<DiskContentType> diskContentType = (EntityModel<DiskContentType>) sender;
        disksContentTypeSelectionList.setDiskContentType(diskContentType.getEntity());
        onDiskViewTypeOrContentTypeChanged();
    };


    @Override
    public IEventListener<EventArgs> getDiskTypeChangedEventListener() {
        return diskTypeChangedEventListener;
    }

    @Override
    public IEventListener<EventArgs> getDiskContentTypeChangedEventListener() {
        return diskContentTypeChangedEventListener;
    }


    @Override
    public void handleQuotaColumnVisibility() {
        isQuotaVisible = false;
        if (dataCenterListModel.getSelectedItem() != null) {
            StoragePool storagePool = dataCenterListModel.getSelectedItem();
            if (QuotaEnforcementTypeEnum.DISABLED != storagePool.getQuotaEnforcementType()) {
                isQuotaVisible = true;
            }
        }

        onDiskViewTypeOrContentTypeChanged();
    }

    @Override
    public void ensureColumnsVisible(DiskStorageType diskType) {
        boolean all = diskType == null;
        boolean images = diskType == DiskStorageType.IMAGE;
        boolean luns = diskType == DiskStorageType.LUN;
        boolean managedBlock = diskType == DiskStorageType.MANAGED_BLOCK_STORAGE;
        ensureColumnsVisible(all, images, luns, managedBlock);
    }

    private void ensureColumnsVisible(boolean all, boolean images, boolean luns, boolean managedBlock) {
        getTable().ensureColumnVisible(
                aliasColumn, constants.aliasDisk(), all || images || luns || managedBlock,
                "150px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                idColumn, constants.idDisk(), all || images || luns || managedBlock,
                "150px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                shareableDiskColumn,
                new ImageResourceHeader(shareableDiskColumn.getDefaultImage(),
                        SafeHtmlUtils.fromSafeConstant(constants.shareable())),
                all || images || luns || managedBlock, "30px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                DisksViewColumns.diskContainersIconColumn, "", all || images || luns || managedBlock, //$NON-NLS-1$
                "30px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                diskContainersColumn, constants.attachedToDisk(), all || images || luns || managedBlock,
                "125px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                storageDomainsColumn, constants.storageDomainsDisk(), all || images || managedBlock,
                "180px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                sizeColumn, constants.provisionedSizeDisk(), all || images || luns || managedBlock,
                "110px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                allocationColumn, constants.allocationDisk(), images,
                "130px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                dateCreatedColumn, constants.creationDateDisk(), images || managedBlock,
                "130px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                dateModifiedColumn, constants.modificationDateDisk(), images || managedBlock, "120px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                statusColumn, constants.statusDisk(), images || managedBlock || all,
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
                contentTypeColumn, constants.contentDisk(), images,
                "80px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                descriptionColumn, constants.descriptionDisk(), all || images || luns || managedBlock,
                "90px"); //$NON-NLS-1$
    }

    private void onDiskViewTypeOrContentTypeChanged() {
        searchByDiskViewType(disksViewRadioGroup.getDiskStorageType(), disksContentTypeSelectionList.getDiskContentType());
        ensureColumnsVisible(disksViewRadioGroup.getDiskStorageType());
    }

    void initTableColumns() {
        getTable().enableColumnResizing();

        aliasColumn = DisksViewColumns.getAliasColumn((index, disk, value) -> {
            //The link was clicked, now fire an event to switch to details.
            Map<String, String> parameters = new HashMap<>();
            parameters.put(FragmentParams.ID.getName(), disk.getId().toString());
            //The link was clicked, now fire an event to switch to details.
            getPlaceTransitionHandler().handlePlaceTransition(
                    WebAdminApplicationPlaces.diskGeneralSubTabPlace, parameters);
        }, DiskConditionFieldAutoCompleter.ALIAS);

        idColumn = DisksViewColumns.getIdColumn(DiskConditionFieldAutoCompleter.ID);
        sizeColumn = DisksViewColumns.getSizeColumn(DiskConditionFieldAutoCompleter.PROVISIONED_SIZE);
        allocationColumn = DisksViewColumns.getAllocationColumn(null);
        dateCreatedColumn = DisksViewColumns.getDateCreatedColumn(DiskConditionFieldAutoCompleter.CREATION_DATE);
        dateModifiedColumn = DisksViewColumns.getDateModifiedColumn(DiskConditionFieldAutoCompleter.LAST_MODIFIED);
        statusColumn = DisksViewColumns.getStatusColumn(DiskConditionFieldAutoCompleter.STATUS);
        lunIdColumn = DisksViewColumns.getLunIdColumn(null);
        lunSerialColumn = DisksViewColumns.getLunSerialColumn(null);
        lunVendorIdColumn = DisksViewColumns.getLunVendorIdColumn(null);
        lunProductIdColumn = DisksViewColumns.getLunProductIdColumn(null);
        qoutaColumn = DisksViewColumns.getQoutaColumn(DiskConditionFieldAutoCompleter.QUOTA);
        diskStorageTypeColumn = DisksViewColumns.getDiskStorageTypeColumn(DiskConditionFieldAutoCompleter.DISK_TYPE);
        contentTypeColumn = DisksViewColumns.getContentColumn(DiskConditionFieldAutoCompleter.DISK_CONTENT_TYPE);
        descriptionColumn = DisksViewColumns.getDescriptionColumn(DiskConditionFieldAutoCompleter.DESCRIPTION);
        shareableDiskColumn = DisksViewColumns.getShareableDiskColumn();
        diskContainersColumn = DisksViewColumns.getdiskContainersColumn(null);
        storageDomainsColumn = DisksViewColumns.getStorageDomainsColumn(null);
    }

    void initTableOverhead() {
        disksViewRadioGroup = new DisksViewRadioGroup();
        disksViewRadioGroup.addChangeHandler(diskViewTypeChange);

        disksContentTypeSelectionList = new DisksContentTypeSelectionList();
        disksContentTypeSelectionList.addChangeHandler(diskContentViewTypeChange);

        FlowPanel overheadPanel = new FlowPanel();
        overheadPanel.add(disksViewRadioGroup);
        overheadPanel.add(disksContentTypeSelectionList);
        getTable().setTableOverhead(overheadPanel);
    }

    void searchByDiskViewType(DiskStorageType diskViewType, DiskContentType diskContentType) {
        final String disksSearchPrefix = "Disks:"; //$NON-NLS-1$
        final String diskTypeSearchPrefix = "disk_type = "; //$NON-NLS-1$
        final String diskContentTypeSearchPrefix = "disk_content_type = "; //$NON-NLS-1$
        final String searchConjunctionAnd = "and "; //$NON-NLS-1$
        final String searchRegexDisksSearchPrefix = "^\\s*(disk(s)?\\s*(:)+)+\\s*"; //$NON-NLS-1$
        final String searchRegexFlags = "ig"; //$NON-NLS-1$

        final String space = " "; //$NON-NLS-1$
        final String empty = ""; //$NON-NLS-1$

        RegExp searchPatternDisksSearchPrefix = RegExp.compile(searchRegexDisksSearchPrefix, searchRegexFlags);

        String diskTypePostfix = diskViewType != null ?
                diskViewType.name().toLowerCase() + space : null;
        String diskTypeClause = diskTypePostfix != null ?
                diskTypeSearchPrefix + diskTypePostfix : empty;

        String diskContentTypePostfix = diskContentType != null ?
                diskContentType.name().toLowerCase() + space : null;
        String diskContentTypeClause = diskContentTypePostfix != null ?
                diskContentTypeSearchPrefix + diskContentTypePostfix : empty;

        String inputSearchString = this.getMainModel().getSearchString().trim();
        String inputSearchStringPrefix = this.getMainModel().getDefaultSearchString();
        String userSearchString = inputSearchString.substring(inputSearchStringPrefix.length());

        String searchStringPrefix;
        if (diskTypeClause.isEmpty() && diskContentTypeClause.isEmpty()) {
            searchStringPrefix = disksSearchPrefix.trim();
            if (userSearchString.trim().toLowerCase().startsWith(searchConjunctionAnd)) {
                userSearchString = userSearchString.trim().substring(searchConjunctionAnd.length());
            }
        } else {
            boolean filteringBoth = !diskTypeClause.isEmpty() && !diskContentTypeClause.isEmpty();
            searchStringPrefix = disksSearchPrefix + space
                    + (searchPatternDisksSearchPrefix.test(inputSearchStringPrefix) ? empty : searchConjunctionAnd)
                    + diskTypeClause + (filteringBoth ? searchConjunctionAnd : empty) + diskContentTypeClause + space;
            if (!userSearchString.isEmpty() && !userSearchString.trim().toLowerCase().startsWith(searchConjunctionAnd)) {
                userSearchString = searchConjunctionAnd + userSearchString.trim();
            }
        }
        String searchString = searchStringPrefix.trim() + space + userSearchString.trim();

        getTable().getSelectionModel().clear();
        getMainModel().setItems(null);
        getMainModel().setDefaultSearchString(searchStringPrefix.trim());
        getMainModel().setSearchString(searchString.trim());
        getMainModel().search();
    }
}
