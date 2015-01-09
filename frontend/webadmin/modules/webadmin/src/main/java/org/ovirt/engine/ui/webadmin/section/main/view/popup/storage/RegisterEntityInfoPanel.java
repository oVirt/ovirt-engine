package org.ovirt.engine.ui.webadmin.section.main.view.popup.storage;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.view.client.NoSelectionModel;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.compat.Guid;
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
import org.ovirt.engine.ui.uicommonweb.models.storage.RegisterEntityModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportEntityData;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.TabLayoutPanel;

import org.ovirt.engine.ui.webadmin.widget.table.cell.CustomSelectionCell;

public abstract class RegisterEntityInfoPanel extends TabLayoutPanel {

    protected static final ApplicationConstants constants = GWT.create(ApplicationConstants.class);
    protected static final ApplicationTemplates templates = GWT.create(ApplicationTemplates.class);
    protected static final ApplicationResources resources = GWT.create(ApplicationResources.class);

    protected EntityModelCellTable<ListModel> disksTable;
    protected EntityModelCellTable<ListModel> nicsTable;
    protected EntityModelCellTable<ListModel> appsTable;

    protected RegisterEntityModel registerEntityModel;

    public RegisterEntityInfoPanel(RegisterEntityModel registerEntityModel) {
        super(templates.TAB_BAR_HEIGHT, Style.Unit.PX);
        this.registerEntityModel = registerEntityModel;

        init();
        addStyles();
    }

    protected abstract void init();

    public abstract void updateTabsData(ImportEntityData importEntityData);

    private void addStyles() {
        getElement().getStyle().setPosition(Style.Position.STATIC);
    }

    protected void initDisksTable() {
        disksTable = new EntityModelCellTable<ListModel>(false, true);
        disksTable.enableColumnResizing();

        disksTable.addColumn(new DiskImageStatusColumn(), constants.empty(), "30px"); //$NON-NLS-1$

        TextColumnWithTooltip<DiskImage> aliasColumn = new TextColumnWithTooltip<DiskImage>() {
            @Override
            public String getValue(DiskImage object) {
                return object.getDiskAlias();
            }
        };
        disksTable.addColumn(aliasColumn, constants.aliasDisk(), "80px"); //$NON-NLS-1$

        DiskSizeColumn<DiskImage> sizeColumn = new DiskSizeColumn<DiskImage>() {
            @Override
            protected Long getRawValue(DiskImage object) {
                return object.getSize();
            }
        };
        disksTable.addColumn(sizeColumn, constants.provisionedSizeDisk(), "80px"); //$NON-NLS-1$

        DiskSizeColumn<DiskImage> actualSizeColumn = new DiskSizeColumn<DiskImage>() {
            @Override
            protected Long getRawValue(DiskImage object) {
                return object.getActualSizeInBytes();
            }
        };
        disksTable.addColumn(actualSizeColumn, constants.sizeDisk(), "80px"); //$NON-NLS-1$

        TextColumnWithTooltip<DiskImage> allocationColumn = new EnumColumn<DiskImage, VolumeType>() {
            @Override
            protected VolumeType getRawValue(DiskImage object) {
                return VolumeType.forValue(object.getVolumeType().getValue());
            }
        };
        disksTable.addColumn(allocationColumn, constants.allocationDisk(), "110px"); //$NON-NLS-1$

        TextColumnWithTooltip<DiskImage> interfaceColumn = new TextColumnWithTooltip<DiskImage>() {
            @Override
            public String getValue(DiskImage object) {
                return object.getDiskInterface().toString();
            }
        };
        disksTable.addColumn(interfaceColumn, constants.interfaceDisk(), "95px"); //$NON-NLS-1$

        TextColumnWithTooltip<DiskImage> statusColumn = new EnumColumn<DiskImage, ImageStatus>() {
            @Override
            protected ImageStatus getRawValue(DiskImage object) {
                return object.getImageStatus();
            }
        };
        disksTable.addColumn(statusColumn, constants.statusDisk(), "65px"); //$NON-NLS-1$

        TextColumnWithTooltip<DiskImage> dateCreatedColumn = new FullDateTimeColumn<DiskImage>() {
            @Override
            protected Date getRawValue(DiskImage object) {
                return object.getCreationDate();
            }
        };
        disksTable.addColumn(dateCreatedColumn, constants.creationDateDisk(), "100px"); //$NON-NLS-1$

        TextColumnWithTooltip<DiskImage> descriptionColumn = new TextColumnWithTooltip<DiskImage>() {
            @Override
            public String getValue(DiskImage object) {
                return object.getDiskDescription();
            }
        };
        disksTable.addColumn(descriptionColumn, constants.descriptionDisk(), "100px"); //$NON-NLS-1$

        if (registerEntityModel.isQuotaEnabled()) {
            disksTable.addColumn(getDiskQuotaColumn(), constants.quotaVm(), "100px"); //$NON-NLS-1$
        }

        disksTable.setRowData(new ArrayList<EntityModel>());
        disksTable.setWidth("100%", true); //$NON-NLS-1$
        disksTable.setSelectionModel(new NoSelectionModel());
    }

    private Column getDiskQuotaColumn() {
        CustomSelectionCell customSelectionCell = new CustomSelectionCell(new ArrayList<String>());
        customSelectionCell.setStyle("input-group col-xs-11"); //$NON-NLS-1$

        Column column = new Column<DiskImage, String>(customSelectionCell) {
            @Override
            public String getValue(DiskImage disk) {
                List<Quota> quotas = (List<Quota>) registerEntityModel.getStorageQuota().getItems();
                if (quotas == null || quotas.isEmpty()) {
                    return constants.empty();
                }

                Map<Guid, Quota> diskQuotaMap = registerEntityModel.getDiskQuotaMap().getEntity();
                if (diskQuotaMap.get(disk.getId()) == null) {
                    diskQuotaMap.put(disk.getId(), quotas.get(0));
                    ((CustomSelectionCell) getCell()).setOptions(registerEntityModel.getQuotaNames(quotas));
                }

                return diskQuotaMap.get(disk.getId()).getQuotaName();
            }
        };
        column.setFieldUpdater(new FieldUpdater<DiskImage, String>() {
            @Override
            public void update(int index, DiskImage disk, String value) {
                Quota quota = registerEntityModel.getQuotaByName(value, (List<Quota>) registerEntityModel.getStorageQuota().getItems());
                registerEntityModel.getDiskQuotaMap().getEntity().put(disk.getId(), quota);
            }
        });

        return column;
    }

    protected void initNicsTable() {
        nicsTable = new EntityModelCellTable<ListModel>(false, true);
        nicsTable.enableColumnResizing();

        TextColumnWithTooltip<VmNetworkInterface> nameColumn = new TextColumnWithTooltip<VmNetworkInterface>() {
            @Override
            public String getValue(VmNetworkInterface object) {
                return object.getName();
            }
        };
        nicsTable.addColumn(nameColumn, constants.nameInterface(), "90px"); //$NON-NLS-1$

        TextColumnWithTooltip<VmNetworkInterface> networkNameColumn = new TextColumnWithTooltip<VmNetworkInterface>() {
            @Override
            public String getValue(VmNetworkInterface object) {
                return object.getNetworkName();
            }
        };
        nicsTable.addColumn(networkNameColumn, constants.networkNameInterface(), "90px"); //$NON-NLS-1$

        TextColumnWithTooltip<VmNetworkInterface> profileNameColumn = new TextColumnWithTooltip<VmNetworkInterface>() {
            @Override
            public String getValue(VmNetworkInterface object) {
                return object.getVnicProfileName();
            }
        };
        nicsTable.addColumn(profileNameColumn, constants.profileNameInterface(), "90px"); //$NON-NLS-1$

        TextColumnWithTooltip<VmNetworkInterface> typeColumn = new EnumColumn<VmNetworkInterface, VmInterfaceType>() {
            @Override
            protected VmInterfaceType getRawValue(VmNetworkInterface object) {
                return VmInterfaceType.forValue(object.getType());
            }
        };
        nicsTable.addColumn(typeColumn, constants.typeInterface(), "90px"); //$NON-NLS-1$

        TextColumnWithTooltip<VmNetworkInterface> macColumn = new TextColumnWithTooltip<VmNetworkInterface>() {
            @Override
            public String getValue(VmNetworkInterface object) {
                return object.getMacAddress();
            }
        };
        nicsTable.addColumn(macColumn, constants.macInterface(), "90px"); //$NON-NLS-1$

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
        nicsTable.addColumn(speedColumn, templates.sub(constants.speedInterface(), constants.mbps()), "90px"); //$NON-NLS-1$

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
        nicsTable.addColumn(rxColumn, templates.sub(constants.rxRate(), constants.mbps()), "90px"); //$NON-NLS-1$

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
        nicsTable.addColumn(txColumn, templates.sub(constants.txRate(), constants.mbps()), "90px"); //$NON-NLS-1$

        TextColumnWithTooltip<VmNetworkInterface> dropsColumn = new SumUpColumn<VmNetworkInterface>() {
            @Override
            protected Double[] getRawValue(VmNetworkInterface object) {
                Double receiveDropRate = object != null ? object.getStatistics().getReceiveDropRate() : null;
                Double transmitDropRate = object != null ? object.getStatistics().getTransmitDropRate() : null;
                return new Double[] { receiveDropRate, transmitDropRate };
            }
        };
        nicsTable.addColumn(dropsColumn, templates.sub(constants.dropsInterface(), constants.pkts()), "90px"); //$NON-NLS-1$

        nicsTable.setRowData(new ArrayList<EntityModel>());
        nicsTable.setWidth("100%", true); //$NON-NLS-1$
        nicsTable.setSelectionModel(new NoSelectionModel());
    }

    protected void initAppsTable() {
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
