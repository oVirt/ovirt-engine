package org.ovirt.engine.ui.common.widget.uicommon.vm;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.table.column.DiskImageStatusColumn;
import org.ovirt.engine.ui.common.widget.table.column.DiskSizeColumn;
import org.ovirt.engine.ui.common.widget.table.column.EnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.FullDateTimeColumn;
import org.ovirt.engine.ui.common.widget.table.column.RxTxRateColumn;
import org.ovirt.engine.ui.common.widget.table.column.SumUpColumn;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.SnapshotModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmSnapshotListModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;

import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.view.client.NoSelectionModel;

public class VmSnapshotInfoPanel extends TabLayoutPanel {

    private final CommonApplicationConstants constants;
    private final CommonApplicationMessages messages;
    private final CommonApplicationTemplates templates;

    private VmSnapshotListModel vmSnapshotListModel;

    private VmSnapshotInfoGeneral generalForm;
    private EntityModelCellTable<ListModel> disksTable;
    private EntityModelCellTable<ListModel> nicsTable;
    private EntityModelCellTable<ListModel> appsTable;

    public VmSnapshotInfoPanel(VmSnapshotListModel vmSnapshotListModel,
            CommonApplicationConstants constants,
            CommonApplicationMessages messages,
            CommonApplicationTemplates templates) {
        super(20, Unit.PX);

        this.vmSnapshotListModel = vmSnapshotListModel;
        this.constants = constants;
        this.messages = messages;
        this.templates = templates;

        initPanel();
        addStyle();
    }

    private void initPanel() {

        // Initialize Tables
        initGeneralForm();
        initDisksTable();
        initNicsTable();
        initAppsTable();

        // Add Tabs
        add(new ScrollPanel(generalForm), constants.generalLabel());
        add(new ScrollPanel(disksTable), constants.disksLabel());
        add(new ScrollPanel(nicsTable), constants.nicsLabel());
        add(new ScrollPanel(appsTable), constants.applicationsLabel());
    }

    public void updatePanel(Snapshot snapshot) {
        HashMap<Guid, SnapshotModel> snapshotsMap = vmSnapshotListModel.getSnapshotsMap();
        Guid snapshotId = snapshot != null ? snapshot.getId() : null;
        final SnapshotModel snapshotModel = snapshotsMap.get(snapshotId);

        updateTabsData(snapshotModel);

        if (!(Boolean) snapshotModel.getIsPropertiesUpdated().getEntity()) {
            snapshotModel.getIsPropertiesUpdated().getEntityChangedEvent().addListener(new IEventListener() {
                @Override
                public void eventRaised(Event ev, Object sender, EventArgs args) {
                    updateTabsData(snapshotModel);
                }
            });
        }
    }

    private void addStyle() {
        getElement().getStyle().setPosition(Position.STATIC);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void updateTabsData(SnapshotModel snapshotModel) {
        generalForm.update(snapshotModel.getVm());
        disksTable.setRowData((List) snapshotModel.getDisks());
        nicsTable.setRowData((List) snapshotModel.getNics());
        appsTable.setRowData((List) snapshotModel.getApps());
    }

    private void initGeneralForm() {
        generalForm = new VmSnapshotInfoGeneral(constants, messages);
    }

    private void initDisksTable() {
        disksTable = new EntityModelCellTable<ListModel>(false, true);

        disksTable.addColumn(new DiskImageStatusColumn(), constants.empty(), "30px"); //$NON-NLS-1$

        TextColumnWithTooltip<DiskImage> aliasColumn = new TextColumnWithTooltip<DiskImage>() {
            @Override
            public String getValue(DiskImage object) {
                return object.getDiskAlias();
            }
        };
        disksTable.addColumn(aliasColumn, constants.aliasDisk());

        DiskSizeColumn<DiskImage> sizeColumn = new DiskSizeColumn<DiskImage>() {
            @Override
            protected Long getRawValue(DiskImage object) {
                return object.getSize();
            }
        };
        disksTable.addColumn(sizeColumn, constants.provisionedSizeDisk());

        DiskSizeColumn<DiskImage> actualSizeColumn = new DiskSizeColumn<DiskImage>() {
            @Override
            protected Long getRawValue(DiskImage object) {
                return object.getActualSizeFromDiskImage();
            }
        };
        disksTable.addColumn(actualSizeColumn, constants.sizeDisk());

        TextColumnWithTooltip<DiskImage> allocationColumn = new EnumColumn<DiskImage, VolumeType>() {
            @Override
            protected VolumeType getRawValue(DiskImage object) {
                return VolumeType.forValue(object.getVolumeType().getValue());
            }
        };
        disksTable.addColumn(allocationColumn, constants.allocationDisk(), "60px"); //$NON-NLS-1$

        TextColumnWithTooltip<DiskImage> interfaceColumn = new TextColumnWithTooltip<DiskImage>() {
            @Override
            public String getValue(DiskImage object) {
                return object.getDiskInterface().toString();
            }
        };
        disksTable.addColumn(interfaceColumn, constants.interfaceDisk(), "60px"); //$NON-NLS-1$

        TextColumnWithTooltip<DiskImage> statusColumn = new EnumColumn<DiskImage, ImageStatus>() {
            @Override
            protected ImageStatus getRawValue(DiskImage object) {
                return object.getImageStatus();
            }
        };
        disksTable.addColumn(statusColumn, constants.statusDisk(), "60px"); //$NON-NLS-1$

        TextColumnWithTooltip<DiskImage> dateCreatedColumn = new FullDateTimeColumn<DiskImage>() {
            @Override
            protected Date getRawValue(DiskImage object) {
                return object.getCreationDate();
            }
        };
        disksTable.addColumn(dateCreatedColumn, constants.creationDateDisk(), "80px"); //$NON-NLS-1$

        TextColumnWithTooltip<DiskImage> descriptionColumn = new TextColumnWithTooltip<DiskImage>() {
            @Override
            public String getValue(DiskImage object) {
                return object.getDiskDescription();
            }
        };
        disksTable.addColumn(descriptionColumn, constants.descriptionDisk());

        disksTable.setRowData(new ArrayList<EntityModel>());
        disksTable.setWidth("100%", true); //$NON-NLS-1$
        disksTable.setSelectionModel(new NoSelectionModel());
    }

    private void initNicsTable() {
        nicsTable = new EntityModelCellTable<ListModel>(false, true);

        TextColumnWithTooltip<VmNetworkInterface> nameColumn = new TextColumnWithTooltip<VmNetworkInterface>() {
            @Override
            public String getValue(VmNetworkInterface object) {
                return object.getName();
            }
        };
        nicsTable.addColumn(nameColumn, constants.nameInterface());

        TextColumnWithTooltip<VmNetworkInterface> networkNameColumn = new TextColumnWithTooltip<VmNetworkInterface>() {
            @Override
            public String getValue(VmNetworkInterface object) {
                return object.getNetworkName();
            }
        };
        nicsTable.addColumn(networkNameColumn, constants.networkNameInterface());

        TextColumnWithTooltip<VmNetworkInterface> typeColumn = new EnumColumn<VmNetworkInterface, VmInterfaceType>() {
            @Override
            protected VmInterfaceType getRawValue(VmNetworkInterface object) {
                return VmInterfaceType.forValue(object.getType());
            }
        };
        nicsTable.addColumn(typeColumn, constants.typeInterface());

        TextColumnWithTooltip<VmNetworkInterface> macColumn = new TextColumnWithTooltip<VmNetworkInterface>() {
            @Override
            public String getValue(VmNetworkInterface object) {
                return object.getMacAddress();
            }
        };
        nicsTable.addColumn(macColumn, constants.macInterface());

        TextColumnWithTooltip<VmNetworkInterface> speedColumn = new TextColumnWithTooltip<VmNetworkInterface>() {
            @Override
            public String getValue(VmNetworkInterface object) {
                if (object.getSpeed() != null) {
                    return object.getSpeed().toString();
                } else {
                    return null;
                }
            }
        };
        nicsTable.addColumn(speedColumn, templates.sub(constants.speedInterface(), constants.mbps()));

        TextColumnWithTooltip<VmNetworkInterface> rxColumn = new RxTxRateColumn<VmNetworkInterface>() {
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
        nicsTable.addColumn(rxColumn, templates.sub(constants.rxInterface(), constants.mbps()));

        TextColumnWithTooltip<VmNetworkInterface> txColumn = new RxTxRateColumn<VmNetworkInterface>() {
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
        nicsTable.addColumn(txColumn, templates.sub(constants.txInterface(), constants.mbps()));

        TextColumnWithTooltip<VmNetworkInterface> dropsColumn = new SumUpColumn<VmNetworkInterface>() {
            @Override
            protected Double[] getRawValue(VmNetworkInterface object) {
                return new Double[] { object.getStatistics().getReceiveDropRate(),
                        object.getStatistics().getTransmitDropRate() };
            }
        };
        nicsTable.addColumn(dropsColumn, templates.sub(constants.dropsInterface(), constants.pkts()));

        nicsTable.setRowData(new ArrayList<EntityModel>());
        nicsTable.setWidth("100%", true); //$NON-NLS-1$
        nicsTable.setSelectionModel(new NoSelectionModel());
    }

    private void initAppsTable() {
        appsTable = new EntityModelCellTable<ListModel>(false, true);

        TextColumnWithTooltip<String> appNameColumn = new TextColumnWithTooltip<String>() {
            @Override
            public String getValue(String appName) {
                return appName;
            }
        };
        appsTable.addColumn(appNameColumn, constants.nameSnapshot());

        appsTable.setRowData(new ArrayList<EntityModel>());
        appsTable.setWidth("100%", true); //$NON-NLS-1$
        appsTable.setSelectionModel(new NoSelectionModel());
    }
}
