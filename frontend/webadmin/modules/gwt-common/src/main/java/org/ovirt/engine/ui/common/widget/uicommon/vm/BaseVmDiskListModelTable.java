package org.ovirt.engine.ui.common.widget.uicommon.vm;

import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableTableModelProvider;
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
import com.google.gwt.user.client.ui.RadioButton;

public class BaseVmDiskListModelTable<T extends VmDiskListModelBase> extends AbstractModelBoundTableWidget<Disk, T> {

    private CommonApplicationConstants constants;
    private DisksViewRadioGroup disksViewRadioGroup;

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
    public void initTable(CommonApplicationConstants constants) {
        this.constants = constants;

        initTableOverhead();
        handleRadioButtonClick(null);

        getModel().getItemsChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                disksViewRadioGroup.setDiskStorageType((DiskStorageType) getModel().getDiskViewType().getEntity());
            }
        });
    }

    void handleRadioButtonClick(ClickEvent event) {
        boolean all = disksViewRadioGroup.getAllButton().getValue();
        boolean images = disksViewRadioGroup.getImagesButton().getValue();
        boolean luns = disksViewRadioGroup.getLunsButton().getValue();

        getTable().getSelectionModel().clear();
        getModel().getDiskViewType().setEntity(disksViewRadioGroup.getDiskStorageType());
        getModel().setItems(null);
        getModel().search();

        getTable().enableColumnResizing();

        getTable().ensureColumnPresent(
                DisksViewColumns.diskStatusColumn, constants.empty(), all || images || luns, "30px"); //$NON-NLS-1$

        getTable().ensureColumnPresent(
                DisksViewColumns.getAliasColumn(null), constants.aliasDisk(), all || images || luns, "120px"); //$NON-NLS-1$

        getTable().ensureColumnPresent(
                DisksViewColumns.bootableDiskColumn,
                DisksViewColumns.bootableDiskColumn.getHeaderHtml(), all || images || luns, "30px"); //$NON-NLS-1$

        getTable().ensureColumnPresent(
                DisksViewColumns.shareableDiskColumn,
                DisksViewColumns.shareableDiskColumn.getHeaderHtml(), all || images || luns, "30px"); //$NON-NLS-1$

        getTable().ensureColumnPresent(
                DisksViewColumns.readOnlyDiskColumn,
                DisksViewColumns.readOnlyDiskColumn.getHeaderHtml(), all || images || luns, "30px"); //$NON-NLS-1$

        getTable().ensureColumnPresent(
                DisksViewColumns.lunDiskColumn,
                DisksViewColumns.lunDiskColumn.getHeaderHtml(), all, "30px"); //$NON-NLS-1$

        getTable().ensureColumnPresent(
                DisksViewColumns.getSizeColumn(null), constants.provisionedSizeDisk(), all || images || luns, "110px"); //$NON-NLS-1$

        getTable().ensureColumnPresent(
                DisksViewColumns.getActualSizeColumn(null), constants.sizeDisk(), images, "110px"); //$NON-NLS-1$

        getTable().ensureColumnPresent(
                DisksViewColumns.getAllocationColumn(null), constants.allocationDisk(), images, "125px"); //$NON-NLS-1$

        getTable().ensureColumnPresent(
                DisksViewColumns.storageDomainsColumn, constants.storageDomainDisk(), images, "125px"); //$NON-NLS-1$

        getTable().ensureColumnPresent(
                DisksViewColumns.storageTypeColumn, constants.storageTypeDisk(), images, "100px"); //$NON-NLS-1$

        getTable().ensureColumnPresent(
                DisksViewColumns.getDateCreatedColumn(null), constants.creationDateDisk(), images, "120px"); //$NON-NLS-1$

        getTable().ensureColumnPresent(
                DisksViewColumns.getLunIdColumn(null), constants.lunIdSanStorage(), luns, "130px"); //$NON-NLS-1$

        getTable().ensureColumnPresent(
                DisksViewColumns.getLunSerialColumn(null), constants.serialSanStorage(), luns, "130px"); //$NON-NLS-1$

        getTable().ensureColumnPresent(
                DisksViewColumns.getLunVendorIdColumn(null), constants.vendorIdSanStorage(), luns, "130px"); //$NON-NLS-1$

        getTable().ensureColumnPresent(
                DisksViewColumns.getLunProductIdColumn(null), constants.productIdSanStorage(), luns, "130px"); //$NON-NLS-1$

        getTable().ensureColumnPresent(
                DisksViewColumns.diskContainersColumn, constants.attachedToDisk(), all || images || luns, "110px"); //$NON-NLS-1$

        getTable().ensureColumnPresent(
                DisksViewColumns.getInterfaceColumn(null), constants.interfaceDisk(), all || images || luns, "100px"); //$NON-NLS-1$

        getTable().ensureColumnPresent(
                DisksViewColumns.diskAlignmentColumn, constants.diskAlignment(), all || images || luns, "100px"); //$NON-NLS-1$

        getTable().ensureColumnPresent(
                DisksViewColumns.getStatusColumn(null), constants.statusDisk(), images, "80px"); //$NON-NLS-1$

        getTable().ensureColumnPresent(
                DisksViewColumns.getDescriptionColumn(null), constants.descriptionDisk(), all || images || luns, "90px"); //$NON-NLS-1$

    }
}
