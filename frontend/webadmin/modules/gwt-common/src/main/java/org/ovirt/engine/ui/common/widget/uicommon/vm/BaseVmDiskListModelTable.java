package org.ovirt.engine.ui.common.widget.uicommon.vm;

import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.presenter.DetailActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableTableModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractDiskSizeColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractImageResourceColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.common.widget.table.column.DiskContainersColumn;
import org.ovirt.engine.ui.common.widget.table.column.StorageDomainsColumn;
import org.ovirt.engine.ui.common.widget.table.header.ImageResourceHeader;
import org.ovirt.engine.ui.common.widget.uicommon.AbstractModelBoundTableWidget;
import org.ovirt.engine.ui.common.widget.uicommon.disks.DisksViewColumns;
import org.ovirt.engine.ui.common.widget.uicommon.disks.DisksViewRadioGroup;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmDiskListModelBase;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmListModel;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

public class BaseVmDiskListModelTable<E, T extends VmDiskListModelBase<?>> extends AbstractModelBoundTableWidget<E, Disk, T> {

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();
    private DisksViewRadioGroup disksViewRadioGroup;

    private static AbstractTextColumn<Disk> aliasColumn;
    private static AbstractDiskSizeColumn<Disk> sizeColumn;
    private static AbstractDiskSizeColumn<Disk> actualSizeColumn;
    private static AbstractTextColumn<Disk> allocationColumn;
    private static AbstractTextColumn<Disk> dateCreatedColumn;
    private static AbstractColumn<Disk, Disk> statusColumn;
    private static AbstractTextColumn<Disk> lunIdColumn;
    private static AbstractTextColumn<Disk> lunSerialColumn;
    private static AbstractTextColumn<Disk> lunVendorIdColumn;
    private static AbstractTextColumn<Disk> lunProductIdColumn;
    private static AbstractTextColumn<Disk> interfaceColumn;
    private static AbstractTextColumn<Disk> logicalNameColumn;
    private static AbstractTextColumn<Disk> diskStorageTypeColumn;
    private static AbstractTextColumn<Disk> descriptionColumn;
    private static AbstractImageResourceColumn<Disk> shareableDiskColumn;
    private static DiskContainersColumn diskContainersColumn;
    private static StorageDomainsColumn storageDomainsColumn;


    public BaseVmDiskListModelTable(
            SearchableTableModelProvider<Disk, T> modelProvider,
            EventBus eventBus,
            DetailActionPanelPresenterWidget<?, Disk, VmListModel<Void>, T> actionPanel,
            ClientStorage clientStorage) {
        super(modelProvider, eventBus, actionPanel, clientStorage, false);
    }

    @Override
    public void initTable() {
        initTableColumns();
        initTableOverhead();
        onDiskViewTypeChanged(null);

        getModel().getDiskViewType().getEntityChangedEvent().addListener((ev, sender, args) -> {
            DiskStorageType diskType = getModel().getDiskViewType().getEntity();
            disksViewRadioGroup.setDiskStorageType(diskType);
            onDiskViewTypeChanged(diskType);
        });
    }

    void initTableOverhead() {
        disksViewRadioGroup = new DisksViewRadioGroup();
        disksViewRadioGroup.addChangeHandler(newType -> getModel().getDiskViewType().setEntity(newType));
        getTable().setTableOverhead(disksViewRadioGroup);
    }

    void onDiskViewTypeChanged(DiskStorageType diskType) {
        boolean all = diskType == null;
        boolean images = diskType == DiskStorageType.IMAGE;
        boolean luns = diskType == DiskStorageType.LUN;
        boolean managedBlock = diskType == DiskStorageType.MANAGED_BLOCK_STORAGE;

        getTable().getSelectionModel().clear();
        getModel().setItems(null);
        getModel().search();

        getTable().ensureColumnVisible(
                DisksViewColumns.diskStatusColumn, constants.empty(), all || images || luns || managedBlock, "30px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                aliasColumn, constants.aliasDisk(), all || images || luns || managedBlock, "120px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                DisksViewColumns.bootableDiskColumn,
                new ImageResourceHeader(DisksViewColumns.bootableDiskColumn.getDefaultImage(),
                        SafeHtmlUtils.fromSafeConstant(constants.bootableDisk())),
                        all || images || luns || managedBlock, "30px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                shareableDiskColumn,
                new ImageResourceHeader(shareableDiskColumn.getDefaultImage(),
                        SafeHtmlUtils.fromSafeConstant(constants.shareable())),
                all || images || luns || managedBlock, "30px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                DisksViewColumns.readOnlyDiskColumn,
                new ImageResourceHeader(DisksViewColumns.readOnlyDiskColumn.getDefaultImage(),
                        SafeHtmlUtils.fromSafeConstant(constants.readOnly())),
                        all || images || luns || managedBlock, "30px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                sizeColumn, constants.provisionedSizeDisk(), all || images || luns || managedBlock, "110px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                actualSizeColumn, constants.sizeDisk(), images || managedBlock, "110px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                allocationColumn, constants.allocationDisk(), images || managedBlock, "125px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                storageDomainsColumn, constants.storageDomainDisk(), images || managedBlock, "125px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                DisksViewColumns.storageTypeColumn, constants.storageTypeDisk(), images || managedBlock, "100px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                dateCreatedColumn, constants.creationDateDisk(), images || managedBlock, "120px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                lunIdColumn, constants.lunIdSanStorage(), luns, "130px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                lunSerialColumn, constants.serialSanStorage(), luns, "130px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                lunVendorIdColumn, constants.vendorIdSanStorage(), luns, "130px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                lunProductIdColumn, constants.productIdSanStorage(), luns, "130px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                diskContainersColumn, constants.attachedToDisk(), all || images || luns || managedBlock, "110px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                interfaceColumn, constants.interfaceDisk(), all || images || luns || managedBlock, "100px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                logicalNameColumn, constants.logicalNameDisk(), all || images || luns || managedBlock, "100px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                statusColumn, constants.statusDisk(), images || managedBlock || all, "80px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                diskStorageTypeColumn, constants.typeDisk(), all, "80px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                descriptionColumn, constants.descriptionDisk(), all || images || luns || managedBlock, "90px"); //$NON-NLS-1$

    }

    void initTableColumns() {
        getTable().enableColumnResizing();

        aliasColumn = DisksViewColumns.getAliasColumn(null);
        sizeColumn = DisksViewColumns.getSizeColumn(null);
        actualSizeColumn = DisksViewColumns.getActualSizeColumn(null);
        allocationColumn = DisksViewColumns.getAllocationColumn(null);
        dateCreatedColumn = DisksViewColumns.getDateCreatedColumn(null);
        statusColumn = DisksViewColumns.getStatusColumn(null);
        lunIdColumn = DisksViewColumns.getLunIdColumn(null);
        lunSerialColumn = DisksViewColumns.getLunSerialColumn(null);
        lunVendorIdColumn = DisksViewColumns.getLunVendorIdColumn(null);
        lunProductIdColumn = DisksViewColumns.getLunProductIdColumn(null);
        interfaceColumn = DisksViewColumns.getInterfaceColumn(null);
        logicalNameColumn = DisksViewColumns.getLogicalNameColumn(null);
        diskStorageTypeColumn = DisksViewColumns.getDiskStorageTypeColumn(null);
        descriptionColumn = DisksViewColumns.getDescriptionColumn(null);
        shareableDiskColumn = DisksViewColumns.getShareableDiskColumn();
        diskContainersColumn = DisksViewColumns.getdiskContainersColumn(null);
        storageDomainsColumn = DisksViewColumns.getStorageDomainsColumn(null);
    }
}
