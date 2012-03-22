package org.ovirt.engine.ui.common.widget.uicommon.vm;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotStatus;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.core.compat.StringFormat;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.form.FormBuilder;
import org.ovirt.engine.ui.common.widget.form.FormItem;
import org.ovirt.engine.ui.common.widget.form.GeneralFormPanel;
import org.ovirt.engine.ui.common.widget.label.EnumLabel;
import org.ovirt.engine.ui.common.widget.label.TextBoxLabel;
import org.ovirt.engine.ui.common.widget.table.column.DiskImageStatusColumn;
import org.ovirt.engine.ui.common.widget.table.column.DiskSizeColumn;
import org.ovirt.engine.ui.common.widget.table.column.EnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.FullDateTimeColumn;
import org.ovirt.engine.ui.common.widget.table.column.RxTxRateColumn;
import org.ovirt.engine.ui.common.widget.table.column.SnapshotDetailColumn;
import org.ovirt.engine.ui.common.widget.table.column.SnapshotStatusColumn;
import org.ovirt.engine.ui.common.widget.table.column.SumUpColumn;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.common.widget.tree.AbstractSubTabTree;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.SnapshotDetailModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.SnapshotModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmSnapshotListModel;

import com.google.gwt.dom.client.Style.FontStyle;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.DateLabel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.view.client.NoSelectionModel;

public class SnapshotsTree<L extends ListWithDetailsModel> extends AbstractSubTabTree<VmSnapshotListModel, Snapshot, SnapshotDetailModel> {

    public SnapshotsTree(SearchableDetailModelProvider<Snapshot, L, VmSnapshotListModel> modelProvider,
            EventBus eventBus,
            CommonApplicationResources resources,
            CommonApplicationConstants constants) {

        super(resources, constants);

        setSelectionEnabled(true);
        setMultiSelection(false);
    }

    @Override
    public void updateTree(final VmSnapshotListModel listModel) {
        super.updateTree(listModel);

        listModel.getCanSelectSnapshot().getEntityChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                boolean canSelectSnapshot = (Boolean) listModel.getCanSelectSnapshot().getEntity();
                setSelectionEnabled(canSelectSnapshot);
            }
        });
    }

    @Override
    protected TreeItem getRootItem(Snapshot snapshot) {
        HorizontalPanel panel = new HorizontalPanel();
        panel.setSpacing(1);
        panel.setWidth("100%");

        addItemToPanel(panel, new Image(new SnapshotStatusColumn().getValue(snapshot)), "30px");
        if (snapshot.getType() == SnapshotType.ACTIVE) {
            addTextBoxToPanel(panel, new TextBoxLabel(), constants.currentSnapshotLabel(), "150px");
        }
        else if (snapshot.getType() == SnapshotType.PREVIEW) {
            addTextBoxToPanel(panel, new TextBoxLabel(), constants.previousCurrentSnapshotLabel(), "150px");
        }
        else {
            DateTimeFormat dateTimeFormat = DateTimeFormat.getFormat("yyyy-MMM-dd, HH:mm:ss");
            addValueLabelToPanel(panel, new DateLabel(dateTimeFormat), snapshot.getCreationDate(), "150px");
        }

        addValueLabelToPanel(panel, new EnumLabel<SnapshotStatus>(), snapshot.getStatus(), "150px");

        TextBoxLabel descriptionLabel = new TextBoxLabel();
        String description = snapshot.getDescription();

        addTextBoxToPanel(panel, descriptionLabel, description, "");

        if (snapshot.getStatus() == SnapshotStatus.IN_PREVIEW) {
            descriptionLabel.setValue(description + " (" + constants.previewModelLabel() + ")");
            descriptionLabel.getElement().getStyle().setColor("orange");
        }
        else if (snapshot.getType() == SnapshotType.STATELESS) {
            descriptionLabel.setValue(description + " (" + constants.readonlyLabel() + ")");
            descriptionLabel.getElement().getStyle().setFontStyle(FontStyle.ITALIC);
        }
        else if (snapshot.getType() == SnapshotType.ACTIVE || snapshot.getType() == SnapshotType.PREVIEW) {
            descriptionLabel.getElement().getStyle().setColor("gray");
        }

        TreeItem treeItem = new TreeItem(panel);
        treeItem.setUserObject(snapshot.isVmConfigurationAvailable() ? snapshot.getId() : true);

        return treeItem;
    }

    @Override
    protected TreeItem getNodeItem(SnapshotDetailModel snapshotDetailModel) {
        HorizontalPanel panel = new HorizontalPanel();
        panel.setSpacing(1);
        panel.setWidth("100%");

        TreeItem treeItem = new TreeItem(panel);

        if (snapshotDetailModel.getEntity() == null) {
            panel.add(new Label(constants.loadingLabel()));
        }
        else {
            addItemToPanel(panel, new Image(new SnapshotDetailColumn().getValue(snapshotDetailModel)), "30px");
            addTextBoxToPanel(panel, new TextBoxLabel(), snapshotDetailModel.getName(), "");

            Snapshot snapshot = snapshotDetailModel.getSnapshot();
            treeItem.setUserObject(snapshot.getId() + snapshotDetailModel.getName());
        }

        return treeItem;
    }

    @Override
    protected ArrayList<SnapshotDetailModel> getNodeObjects(Snapshot snapshot) {

        ArrayList<SnapshotDetailModel> nodes = new ArrayList<SnapshotDetailModel>();
        SnapshotModel snapshotModel = listModel.getSnapshotsMap().get(snapshot.getId());

        if (!(Boolean) snapshotModel.getIsPropertiesUpdated().getEntity()) {
            addRefershListener(snapshotModel);
            nodes.add(new SnapshotDetailModel());
            return nodes;
        }

        SnapshotDetailModel general = new SnapshotDetailModel();
        general.setSnapshot(snapshot);
        general.setEntity(snapshotModel.getVm());
        general.setName(constants.generalLabel());

        SnapshotDetailModel disks = new SnapshotDetailModel();
        disks.setSnapshot(snapshot);
        disks.setEntity(snapshotModel.getDisks());
        disks.setName(constants.disksLabel());

        SnapshotDetailModel nics = new SnapshotDetailModel();
        nics.setSnapshot(snapshot);
        nics.setEntity(snapshotModel.getNics());
        nics.setName(constants.nicsLabel());

        SnapshotDetailModel apps = new SnapshotDetailModel();
        apps.setSnapshot(snapshot);
        apps.setEntity(snapshotModel.getApps());
        apps.setName(constants.applicationsLabel());

        nodes.add(general);
        nodes.add(disks);
        nodes.add(nics);
        nodes.add(apps);

        return nodes;
    }

    @Override
    protected TreeItem getLeafItem(SnapshotDetailModel snapshotDetailModel) {
        if (snapshotDetailModel == null) {
            return null;
        }

        String name = snapshotDetailModel.getName();

        if (name.equals(constants.generalLabel())) {
            return getGeneralTreeItem(snapshotDetailModel);
        }
        else if (name.equals(constants.disksLabel())) {
            return getDisksTreeItem(snapshotDetailModel);
        }
        else if (name.equals(constants.nicsLabel())) {
            return getNicsTreeItem(snapshotDetailModel);
        }
        else if (name.equals(constants.applicationsLabel())) {
            return getAppsTreeItem(snapshotDetailModel);
        }

        return null;
    }

    @Override
    protected void onTreeItemOpen(TreeItem treeItem) {
        // Root node
        if (treeItem.getParentItem() == null) {
            Guid snapshotId = (Guid) treeItem.getUserObject();
            SnapshotModel snapshotModel = listModel.getSnapshotsMap().get(snapshotId);

            if (!(Boolean) snapshotModel.getIsPropertiesUpdated().getEntity()) {
                listModel.UpdateVmConfigurationBySnapshot(snapshotId);
            }
        }
    }

    private void addRefershListener(final SnapshotModel snapshotModel) {
        snapshotModel.getIsPropertiesUpdated().getEntityChangedEvent().addListener(
                new IEventListener() {
                    @Override
                    public void eventRaised(Event ev, Object sender, EventArgs args) {
                        if ((Boolean) snapshotModel.getIsPropertiesUpdated().getEntity()) {
                            refreshTree();
                        }
                    }
                });
    }

    private TreeItem getGeneralTreeItem(SnapshotDetailModel snapshotDetailModel) {
        VM vm = (VM) snapshotDetailModel.getEntity();

        GeneralFormPanel formPanel = new GeneralFormPanel();
        FormBuilder formBuilder = new FormBuilder(formPanel, 1, 3);

        TextBoxLabel definedMemory = new TextBoxLabel(vm.getvm_mem_size_mb() + " MB");
        TextBoxLabel minAllocatedMemory = new TextBoxLabel(vm.getMinAllocatedMem() + " MB");
        TextBoxLabel cpuInfo = new TextBoxLabel(StringFormat.format(
                constants.cpuInfoLabel(), vm.getnum_of_cpus(), vm.getnum_of_sockets(), vm.getcpu_per_socket()));

        formBuilder.setColumnsWidth("100%");
        formBuilder.addFormItem(new FormItem("Defined Memory", definedMemory, 0, 0));
        formBuilder.addFormItem(new FormItem("Physical Memory Guaranteed", minAllocatedMemory, 1, 0));
        formBuilder.addFormItem(new FormItem("Number of CPU Cores", cpuInfo, 2, 0));

        formBuilder.showForm(snapshotDetailModel);
        formPanel.setWidth("100%");

        return new TreeItem(formPanel);
    }

    private TreeItem getDisksTreeItem(SnapshotDetailModel snapshotDetailModel) {
        EntityModelCellTable<ListModel> table = new EntityModelCellTable<ListModel>(false, true);

        table.addColumn(new DiskImageStatusColumn(), "", "30px");

        TextColumnWithTooltip<DiskImage> nameColumn = new TextColumnWithTooltip<DiskImage>() {
            @Override
            public String getValue(DiskImage object) {
                return "Disk " + object.getinternal_drive_mapping();
            }
        };
        table.addColumn(nameColumn, "Name");

        DiskSizeColumn<DiskImage> sizeColumn = new DiskSizeColumn<DiskImage>() {
            @Override
            protected Long getRawValue(DiskImage object) {
                return object.getsize();
            }
        };
        table.addColumn(sizeColumn, "Size");

        DiskSizeColumn<DiskImage> actualSizeColumn = new DiskSizeColumn<DiskImage>() {
            @Override
            protected Long getRawValue(DiskImage object) {
                return object.getactual_size();
            }
        };
        table.addColumn(actualSizeColumn, "Actual Size");

        TextColumnWithTooltip<DiskImage> allocationColumn = new EnumColumn<DiskImage, VolumeType>() {
            @Override
            protected VolumeType getRawValue(DiskImage object) {
                return VolumeType.forValue(object.getvolume_type().getValue());
            }
        };
        table.addColumn(allocationColumn, "Allocation", "120px");

        TextColumnWithTooltip<DiskImage> interfaceColumn = new TextColumnWithTooltip<DiskImage>() {
            @Override
            public String getValue(DiskImage object) {
                return object.getdisk_interface().toString();
            }
        };
        table.addColumn(interfaceColumn, "Interface", "120px");

        TextColumnWithTooltip<DiskImage> statusColumn = new EnumColumn<DiskImage, ImageStatus>() {
            @Override
            protected ImageStatus getRawValue(DiskImage object) {
                return object.getimageStatus();
            }
        };
        table.addColumn(statusColumn, "Status", "120px");

        TextColumnWithTooltip<DiskImage> dateCreatedColumn = new FullDateTimeColumn<DiskImage>() {
            @Override
            protected Date getRawValue(DiskImage object) {
                return object.getcreation_date();
            }
        };
        table.addColumn(dateCreatedColumn, "Creation Date", "140px");

        table.setRowData((List) snapshotDetailModel.getEntity());
        table.setWidth("100%", true);
        table.setSelectionModel(new NoSelectionModel());
        return new TreeItem(table);
    }

    private TreeItem getNicsTreeItem(SnapshotDetailModel snapshotDetailModel) {
        EntityModelCellTable<ListModel> table = new EntityModelCellTable<ListModel>(false, true);

        TextColumnWithTooltip<VmNetworkInterface> nameColumn = new TextColumnWithTooltip<VmNetworkInterface>() {
            @Override
            public String getValue(VmNetworkInterface object) {
                return object.getName();
            }
        };
        table.addColumn(nameColumn, "Name");

        TextColumnWithTooltip<VmNetworkInterface> networkNameColumn = new TextColumnWithTooltip<VmNetworkInterface>() {
            @Override
            public String getValue(VmNetworkInterface object) {
                return object.getNetworkName();
            }
        };
        table.addColumn(networkNameColumn, "Network Name");

        TextColumnWithTooltip<VmNetworkInterface> typeColumn = new EnumColumn<VmNetworkInterface, VmInterfaceType>() {
            @Override
            protected VmInterfaceType getRawValue(VmNetworkInterface object) {
                return VmInterfaceType.forValue(object.getType());
            }
        };
        table.addColumn(typeColumn, "Type");

        TextColumnWithTooltip<VmNetworkInterface> macColumn = new TextColumnWithTooltip<VmNetworkInterface>() {
            @Override
            public String getValue(VmNetworkInterface object) {
                return object.getMacAddress();
            }
        };
        table.addColumn(macColumn, "MAC");

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
        table.addColumn(speedColumn, "Speed (Mbps)");

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
        table.addColumn(rxColumn, "Rx (Mbps)");

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
        table.addColumn(txColumn, "Tx (Mbps)");

        TextColumnWithTooltip<VmNetworkInterface> dropsColumn = new SumUpColumn<VmNetworkInterface>() {
            @Override
            protected Double[] getRawValue(VmNetworkInterface object) {
                return new Double[] { object.getStatistics().getReceiveDropRate(),
                        object.getStatistics().getTransmitDropRate() };
            }
        };
        table.addColumn(dropsColumn, "Drops (Pkts)");

        table.setRowData((List) snapshotDetailModel.getEntity());
        table.setWidth("100%", true);
        table.setSelectionModel(new NoSelectionModel());
        return new TreeItem(table);
    }

    private TreeItem getAppsTreeItem(SnapshotDetailModel snapshotDetailModel) {
        EntityModelCellTable<ListModel> table = new EntityModelCellTable<ListModel>(false, true);

        TextColumnWithTooltip<String> appNameColumn = new TextColumnWithTooltip<String>() {
            @Override
            public String getValue(String appName) {
                return appName;
            }
        };
        table.addColumn(appNameColumn, "Name");

        table.setRowData((List) snapshotDetailModel.getEntity());
        table.setWidth("100%", true);
        table.setSelectionModel(new NoSelectionModel());
        return new TreeItem(table);
    }

}
