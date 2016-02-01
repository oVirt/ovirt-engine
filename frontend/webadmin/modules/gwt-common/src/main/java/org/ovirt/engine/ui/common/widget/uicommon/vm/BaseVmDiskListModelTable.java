package org.ovirt.engine.ui.common.widget.uicommon.vm;

import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableTableModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractDiskSizeColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.common.widget.table.header.ImageResourceHeader;
import org.ovirt.engine.ui.common.widget.uicommon.AbstractModelBoundTableWidget;
import org.ovirt.engine.ui.common.widget.uicommon.disks.DisksViewColumns;
import org.ovirt.engine.ui.common.widget.uicommon.disks.DisksViewRadioGroup;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmDiskListModelBase;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.RadioButton;

public class BaseVmDiskListModelTable<T extends VmDiskListModelBase<?>> extends AbstractModelBoundTableWidget<Disk, T> {

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();
    private DisksViewRadioGroup disksViewRadioGroup;

    private static AbstractTextColumn<Disk> aliasColumn;
    private static AbstractDiskSizeColumn sizeColumn;
    private static AbstractDiskSizeColumn actualSizeColumn;
    private static AbstractTextColumn<Disk> allocationColumn;
    private static AbstractTextColumn<Disk> dateCreatedColumn;
    private static AbstractColumn<Disk, Disk> statusColumn;
    private static AbstractTextColumn<Disk> lunIdColumn;
    private static AbstractTextColumn<Disk> lunSerialColumn;
    private static AbstractTextColumn<Disk> lunVendorIdColumn;
    private static AbstractTextColumn<Disk> lunProductIdColumn;
    private static AbstractTextColumn<Disk> interfaceColumn;
    private static AbstractTextColumn<Disk> diskStorageTypeColumn;
    private static AbstractTextColumn<Disk> cinderVolumeTypeColumn;
    private static AbstractTextColumn<Disk> descriptionColumn;

    public BaseVmDiskListModelTable(
            SearchableTableModelProvider<Disk, T> modelProvider,
            EventBus eventBus,
            ClientStorage clientStorage) {
        super(modelProvider, eventBus, clientStorage, false);

        disksViewRadioGroup = new DisksViewRadioGroup();
    }

    final ClickHandler clickHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            if (((RadioButton) event.getSource()).getValue()) {
                handleRadioButtonClick(event);
            }
        }
    };

    void initTableOverhead() {
        disksViewRadioGroup.setClickHandler(clickHandler);
        disksViewRadioGroup.addStyleName("dvrg_radioGroup_pfly_fix"); //$NON-NLS-1$
        getTable().setTableOverhead(disksViewRadioGroup);
        getTable().setTableTopMargin(20);
    }

    @Override
    public void initTable() {

        initTableColumns();
        initTableOverhead();
        handleRadioButtonClick(null);

        getModel().getItemsChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                disksViewRadioGroup.setDiskStorageType(getModel().getDiskViewType().getEntity());
            }
        });
    }

    void handleRadioButtonClick(ClickEvent event) {
        boolean all = disksViewRadioGroup.getAllButton().getValue();
        boolean images = disksViewRadioGroup.getImagesButton().getValue();
        boolean luns = disksViewRadioGroup.getLunsButton().getValue();
        boolean cinder = disksViewRadioGroup.getCinderButton().getValue();

        getTable().getSelectionModel().clear();
        getModel().getDiskViewType().setEntity(disksViewRadioGroup.getDiskStorageType());
        getModel().setItems(null);
        getModel().search();

        getTable().ensureColumnVisible(
                DisksViewColumns.diskStatusColumn, constants.empty(), all || images || luns || cinder, "30px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                aliasColumn, constants.aliasDisk(), all || images || luns || cinder, "120px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                DisksViewColumns.bootableDiskColumn,
                new ImageResourceHeader(DisksViewColumns.bootableDiskColumn.getDefaultImage(),
                        SafeHtmlUtils.fromSafeConstant(constants.bootableDisk())),
                        all || images || luns || cinder, "30px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                DisksViewColumns.shareableDiskColumn,
                new ImageResourceHeader(DisksViewColumns.shareableDiskColumn.getDefaultImage(),
                        SafeHtmlUtils.fromSafeConstant(constants.shareable())),
                        all || images || luns || cinder, "30px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                DisksViewColumns.readOnlyDiskColumn,
                new ImageResourceHeader(DisksViewColumns.readOnlyDiskColumn.getDefaultImage(),
                        SafeHtmlUtils.fromSafeConstant(constants.readOnly())),
                        all || images || luns || cinder, "30px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                sizeColumn, constants.provisionedSizeDisk(), all || images || luns || cinder, "110px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                actualSizeColumn, constants.sizeDisk(), images, "110px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                allocationColumn, constants.allocationDisk(), images, "125px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                DisksViewColumns.storageDomainsColumn, constants.storageDomainDisk(), images || cinder, "125px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                DisksViewColumns.storageTypeColumn, constants.storageTypeDisk(), images, "100px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                cinderVolumeTypeColumn, constants.cinderVolumeTypeDisk(), cinder, "80px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                dateCreatedColumn, constants.creationDateDisk(), images || cinder, "120px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                lunIdColumn, constants.lunIdSanStorage(), luns, "130px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                lunSerialColumn, constants.serialSanStorage(), luns, "130px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                lunVendorIdColumn, constants.vendorIdSanStorage(), luns, "130px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                lunProductIdColumn, constants.productIdSanStorage(), luns, "130px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                DisksViewColumns.diskContainersColumn, constants.attachedToDisk(), all || images || luns || cinder, "110px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                interfaceColumn, constants.interfaceDisk(), all || images || luns || cinder, "100px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                DisksViewColumns.diskAlignmentColumn, constants.diskAlignment(), all || images || luns, "100px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                statusColumn, constants.statusDisk(), images || cinder || all, "80px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                diskStorageTypeColumn, constants.typeDisk(), all, "80px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                descriptionColumn, constants.descriptionDisk(), all || images || luns, "90px"); //$NON-NLS-1$

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
        diskStorageTypeColumn = DisksViewColumns.getDiskStorageTypeColumn(null);
        cinderVolumeTypeColumn = DisksViewColumns.getCinderVolumeTypeColumn(null);
        descriptionColumn = DisksViewColumns.getDescriptionColumn(null);
    }
}
