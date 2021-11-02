package org.ovirt.engine.ui.common.widget.uicommon.vm;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.gwtbootstrap3.client.ui.Label;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.table.column.AbstractDiskSizeColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractEnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractFullDateTimeColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractRxTxRateColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractSumUpColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.common.widget.table.column.DiskImageStatusColumn;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.SnapshotModel;

import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.view.client.NoSelectionModel;

public class VmSnapshotInfoPanel extends FlowPanel {

    private static final CommonApplicationTemplates templates = AssetProvider.getTemplates();
    private static final CommonApplicationConstants constants = AssetProvider.getConstants();

    private VmSnapshotInfoGeneral generalForm;
    private EntityModelCellTable<ListModel> disksTable;
    private EntityModelCellTable<ListModel> nicsTable;
    private EntityModelCellTable<ListModel> appsTable;

    public VmSnapshotInfoPanel() {
        // Initialize Tables
        initGeneralForm();
        initDisksTable();
        initNicsTable();
        initAppsTable();

        // Add Tabs
        add(new Label(constants.generalLabel()));
        add(generalForm);
        add(new Label(constants.disksLabel()));
        add(disksTable);
        add(new Label(constants.nicsLabel()));
        add(nicsTable);
        add(new Label(constants.applicationsLabel()));
        add(appsTable);
    }

    public void updatePanel(final SnapshotModel snapshotModel) {
        snapshotModel.updateVmConfiguration(returnValue -> updateTabsData(snapshotModel));
    }

    private void addStyle() {
        getElement().getStyle().setPosition(Position.STATIC);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void updateTabsData(SnapshotModel snapshotModel) {
        generalForm.update(snapshotModel.getVm());
        disksTable.setRowData((List) snapshotModel.getDisks());
        nicsTable.setRowData((List) snapshotModel.getNics());
        appsTable.setRowData((List) snapshotModel.getApps());
    }

    private void initGeneralForm() {
        generalForm = new VmSnapshotInfoGeneral();
    }

    private void initDisksTable() {
        disksTable = new EntityModelCellTable<>(false, true);
        disksTable.enableColumnResizing();

        disksTable.addColumn(new DiskImageStatusColumn(), constants.empty(), "30px"); //$NON-NLS-1$

        AbstractTextColumn<DiskImage> aliasColumn = new AbstractTextColumn<DiskImage>() {
            @Override
            public String getValue(DiskImage object) {
                return object.getDiskAlias();
            }
        };
        disksTable.addColumn(aliasColumn, constants.aliasDisk(), "70px"); //$NON-NLS-1$

        AbstractDiskSizeColumn<DiskImage> sizeColumn = new AbstractDiskSizeColumn<DiskImage>() {
            @Override
            protected Long getRawValue(DiskImage object) {
                return object.getSize();
            }
        };
        disksTable.addColumn(sizeColumn, constants.provisionedSizeDisk(), "70px"); //$NON-NLS-1$

        AbstractDiskSizeColumn<DiskImage> actualSizeColumn = new AbstractDiskSizeColumn<DiskImage>() {
            @Override
            protected Long getRawValue(DiskImage object) {
                return object.getActualSizeInBytes();
            }
        };
        disksTable.addColumn(actualSizeColumn, constants.sizeDisk(), "70px"); //$NON-NLS-1$

        AbstractTextColumn<DiskImage> allocationColumn = new AbstractEnumColumn<DiskImage, VolumeType>() {
            @Override
            protected VolumeType getRawValue(DiskImage object) {
                return VolumeType.forValue(object.getVolumeType().getValue());
            }
        };
        disksTable.addColumn(allocationColumn, constants.allocationDisk(), "110px"); //$NON-NLS-1$

        AbstractTextColumn<DiskImage> interfaceColumn = new AbstractTextColumn<DiskImage>() {
            @Override
            public String getValue(DiskImage object) {
                if (object.getDiskVmElements().size() == 1) {
                    return object.getDiskVmElements().iterator().next().getDiskInterface().toString();
                }
                return null;
            }
        };
        disksTable.addColumn(interfaceColumn, constants.interfaceDisk(), "95px"); //$NON-NLS-1$

        AbstractTextColumn<DiskImage> statusColumn = new AbstractEnumColumn<DiskImage, ImageStatus>() {
            @Override
            protected ImageStatus getRawValue(DiskImage object) {
                return object.getImageStatus();
            }
        };
        disksTable.addColumn(statusColumn, constants.statusDisk(), "65px"); //$NON-NLS-1$

        AbstractTextColumn<DiskImage> dateCreatedColumn = new AbstractFullDateTimeColumn<DiskImage>() {
            @Override
            protected Date getRawValue(DiskImage object) {
                return object.getCreationDate();
            }
        };
        disksTable.addColumn(dateCreatedColumn, constants.creationDateDisk(), "80px"); //$NON-NLS-1$

        AbstractTextColumn<DiskImage> diskSnapshotIDColumn = new AbstractTextColumn<DiskImage>() {
            @Override
            public String getValue(DiskImage diskImage) {
                return diskImage.getImageId().toString();
            }
        };
        disksTable.addColumn(diskSnapshotIDColumn, constants.diskSnapshotIDDisk(), "260px"); //$NON-NLS-1$

        AbstractTextColumn<Disk> diskStorageTypeColumn = new AbstractEnumColumn<Disk, DiskStorageType>() {
            @Override
            protected DiskStorageType getRawValue(Disk object) {
                return object.getDiskStorageType();
            }
        };
        disksTable.addColumn(diskStorageTypeColumn, constants.typeDisk(), "80px"); //$NON-NLS-1$

        AbstractTextColumn<DiskImage> descriptionColumn = new AbstractTextColumn<DiskImage>() {
            @Override
            public String getValue(DiskImage object) {
                return object.getDiskDescription();
            }
        };
        disksTable.addColumn(descriptionColumn, constants.descriptionDisk(), "80px"); //$NON-NLS-1$

        disksTable.setRowData(new ArrayList<EntityModel>());
        disksTable.setWidth("100%"); // $NON-NLS-1$
        disksTable.setSelectionModel(new NoSelectionModel());
    }

    private void initNicsTable() {
        nicsTable = new EntityModelCellTable<>(false, true);
        nicsTable.enableColumnResizing();

        AbstractTextColumn<VmNetworkInterface> nameColumn = new AbstractTextColumn<VmNetworkInterface>() {
            @Override
            public String getValue(VmNetworkInterface object) {
                return object.getName();
            }
        };
        nicsTable.addColumn(nameColumn, constants.nameInterface(), "80px"); //$NON-NLS-1$

        AbstractTextColumn<VmNetworkInterface> networkNameColumn = new AbstractTextColumn<VmNetworkInterface>() {
            @Override
            public String getValue(VmNetworkInterface object) {
                return object.getNetworkName();
            }
        };
        nicsTable.addColumn(networkNameColumn, constants.networkNameInterface(), "80px"); //$NON-NLS-1$

        AbstractTextColumn<VmNetworkInterface> profileNameColumn = new AbstractTextColumn<VmNetworkInterface>() {
            @Override
            public String getValue(VmNetworkInterface object) {
                return object.getVnicProfileName();
            }
        };
        nicsTable.addColumn(profileNameColumn, constants.profileNameInterface(), "80px"); //$NON-NLS-1$

        AbstractTextColumn<VmNetworkInterface> typeColumn = new AbstractEnumColumn<VmNetworkInterface, VmInterfaceType>() {
            @Override
            protected VmInterfaceType getRawValue(VmNetworkInterface object) {
                return VmInterfaceType.forValue(object.getType());
            }
        };
        nicsTable.addColumn(typeColumn, constants.typeInterface(), "80px"); //$NON-NLS-1$

        AbstractTextColumn<VmNetworkInterface> macColumn = new AbstractTextColumn<VmNetworkInterface>() {
            @Override
            public String getValue(VmNetworkInterface object) {
                return object.getMacAddress();
            }
        };
        nicsTable.addColumn(macColumn, constants.macInterface(), "80px"); //$NON-NLS-1$

        AbstractTextColumn<VmNetworkInterface> speedColumn = new AbstractTextColumn<VmNetworkInterface>() {
            @Override
            public String getValue(VmNetworkInterface object) {
                if (object.getSpeed() != null) {
                    return object.getSpeed().toString();
                } else {
                    return null;
                }
            }
        };
        nicsTable.addColumn(speedColumn, templates.sub(constants.speedInterface(), constants.mbps()), "80px"); //$NON-NLS-1$

        AbstractTextColumn<VmNetworkInterface> rxColumn = new AbstractRxTxRateColumn<VmNetworkInterface>() {
            @Override
            protected Double getRate(VmNetworkInterface object) {
                return object.getStatistics().getReceiveRate();
            }

            @Override
            protected Double getSpeed(VmNetworkInterface object) {
                if (object.getSpeed() != null) {
                    return object.getSpeed().doubleValue();
                } else {
                    return null;
                }
            }
        };
        nicsTable.addColumn(rxColumn, templates.sub(constants.rxRate(), constants.mbps()), "80px"); //$NON-NLS-1$

        AbstractTextColumn<VmNetworkInterface> txColumn = new AbstractRxTxRateColumn<VmNetworkInterface>() {
            @Override
            protected Double getRate(VmNetworkInterface object) {
                return object.getStatistics().getTransmitRate();
            }

            @Override
            protected Double getSpeed(VmNetworkInterface object) {
                if (object.getSpeed() != null) {
                    return object.getSpeed().doubleValue();
                } else {
                    return null;
                }
            }
        };
        nicsTable.addColumn(txColumn, templates.sub(constants.txRate(), constants.mbps()), "80px"); //$NON-NLS-1$

        AbstractTextColumn<VmNetworkInterface> dropsColumn = new AbstractSumUpColumn<VmNetworkInterface>() {
            @Override
            protected Double[] getRawValue(VmNetworkInterface object) {
                Double receiveDrops = object != null ? object.getStatistics().getReceiveDrops().doubleValue() : null;
                Double transmitDrops = object != null ? object.getStatistics().getTransmitDrops().doubleValue() : null;
                return new Double[] { receiveDrops, transmitDrops };
            }
        };
        nicsTable.addColumn(dropsColumn, templates.sub(constants.dropsInterface(), constants.pkts()), "80px"); //$NON-NLS-1$

        nicsTable.setRowData(new ArrayList<EntityModel>());
        nicsTable.setWidth("100%"); // $NON-NLS-1$
        nicsTable.setSelectionModel(new NoSelectionModel());
    }

    private void initAppsTable() {
        appsTable = new EntityModelCellTable<>(false, true);

        AbstractTextColumn<String> appNameColumn = new AbstractTextColumn<String>() {
            @Override
            public String getValue(String appName) {
                return appName;
            }
        };
        appsTable.addColumn(appNameColumn, constants.nameSnapshot());

        appsTable.setRowData(new ArrayList<EntityModel>());
        appsTable.setWidth("100%"); // $NON-NLS-1$
        appsTable.setSelectionModel(new NoSelectionModel());
    }
}
