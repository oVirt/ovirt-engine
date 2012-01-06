package org.ovirt.engine.ui.webadmin.widget.storage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageVmListModel;
import org.ovirt.engine.ui.webadmin.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.webadmin.widget.renderer.DiskSizeRenderer.DiskSizeUnit;
import org.ovirt.engine.ui.webadmin.widget.table.column.DiskSizeColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.GeneralDateTimeColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.ImageResourceColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.TextColumnWithTooltip;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.CellTable.Resources;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.TreeItem;

public class VMsTree extends AbstractSubTabTree<StorageVmListModel> {

    @Override
    public void updateTree(final StorageVmListModel listModel) {
        listModel.getItemsChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                StorageVmListModel model = (StorageVmListModel) sender;
                List<VM> vms = (List<VM>) model.getItems();
                tree.clear();

                if (vms == null)
                    return;

                for (VM vm : vms) {
                    TreeItem vmItem = getVMNode(vm);

                    for (DiskImage disk : vm.getDiskList()) {
                        TreeItem diskItem = getDiskNode(disk);

                        for (DiskImage snapshot : disk.getSnapshots()) {
                            TreeItem snapshotItem = getSnapshotsNode(snapshot);
                            diskItem.addItem(snapshotItem);
                            styleItem(snapshotItem);
                        }

                        vmItem.addItem(diskItem);
                        styleItem(diskItem);
                    }

                    tree.addItem(vmItem);
                    styleItem(vmItem);
                }
            }
        });
    }

    private void styleItem(TreeItem item) {
        Element tableElm = DOM.getFirstChild(item.getElement());
        tableElm.setAttribute("width", "100%");

        Element col = (Element) tableElm.getElementsByTagName("td").getItem(0);
        col.setAttribute("width", "20px");
    }

    private TreeItem creatEntityItem(EntityModelCellTable<ListModel> table, Object entity) {
        EntityModel entityModel = new EntityModel();
        entityModel.setEntity(entity);
        table.setRowData(new ArrayList<EntityModel>(Arrays.asList(entityModel)));
        table.setWidth("100%");
        TreeItem item = new TreeItem(table);
        return item;
    }

    private TreeItem getVMNode(VM vm) {
        EntityModelCellTable<ListModel> table =
                new EntityModelCellTable<ListModel>(false,
                        (Resources) GWT.create(TreeHeaderlessTableResources.class),
                        true);

        table.addColumn(new ImageResourceColumn<EntityModel>() {
            @Override
            public ImageResource getValue(EntityModel object) {
                return resources.vmImage();
            }
        }, "", "30px");

        TextColumnWithTooltip<EntityModel> nameColumn = new TextColumnWithTooltip<EntityModel>() {
            @Override
            public String getValue(EntityModel object) {
                return ((VM) object.getEntity()).getvm_name();
            }
        };
        table.addColumn(nameColumn, "Name");

        TextColumnWithTooltip<EntityModel> diskColumn = new TextColumnWithTooltip<EntityModel>() {
            @Override
            public String getValue(EntityModel object) {
                return String.valueOf(((VM) object.getEntity()).getDiskMap().size());
            }
        };
        table.addColumn(diskColumn, "Disks", "120px");

        TextColumnWithTooltip<EntityModel> templateColumn = new TextColumnWithTooltip<EntityModel>() {
            @Override
            public String getValue(EntityModel object) {
                return ((VM) object.getEntity()).getvmt_name();
            }
        };
        table.addColumn(templateColumn, "Template", "180px");

        DiskSizeColumn<EntityModel> vSizeColumn = new DiskSizeColumn<EntityModel>(DiskSizeUnit.GIGABYTE) {
            @Override
            protected Long getRawValue(EntityModel object) {
                return (long) ((VM) object.getEntity()).getDiskSize();
            }
        };
        table.addColumn(vSizeColumn, "V-Size", "120px");

        DiskSizeColumn<EntityModel> actualSizeColumn = new DiskSizeColumn<EntityModel>(DiskSizeUnit.GIGABYTE) {
            @Override
            protected Long getRawValue(EntityModel object) {
                return (long) ((VM) object.getEntity()).getActualDiskWithSnapshotsSize();
            }
        };
        table.addColumn(actualSizeColumn, "Actual Size", "120px");

        TextColumnWithTooltip<EntityModel> creationDateColumn = new GeneralDateTimeColumn<EntityModel>() {
            @Override
            protected Date getRawValue(EntityModel object) {
                return ((VM) object.getEntity()).getvm_creation_date();
            }
        };
        table.addColumn(creationDateColumn, "Creation Date", "180px");

        return creatEntityItem(table, vm);
    }

    private TreeItem getDiskOrSnapshotNode(DiskImage disk, final boolean isDisk) {
        EntityModelCellTable<ListModel> table = new EntityModelCellTable<ListModel>(false,
                (Resources) GWT.create(TreeHeaderlessTableResources.class),
                true);

        table.addColumn(new ImageResourceColumn<EntityModel>() {
            @Override
            public ImageResource getValue(EntityModel object) {
                return isDisk ? resources.diskImage() : resources.snapshotImage();
            }
        }, "", "30px");

        TextColumnWithTooltip<EntityModel> nameColumn = new TextColumnWithTooltip<EntityModel>() {
            @Override
            public String getValue(EntityModel object) {
                return isDisk ? "Disk " + ((DiskImage) object.getEntity()).getinternal_drive_mapping()
                        : ((DiskImage) object.getEntity()).getdescription();
            }
        };
        table.addColumn(nameColumn, "Name");

        table.addColumn(new EmptyColumn(), "Disks", "120px");
        table.addColumn(new EmptyColumn(), "Template", "180px");

        DiskSizeColumn<EntityModel> vSizeColumn = new DiskSizeColumn<EntityModel>(DiskSizeUnit.GIGABYTE) {
            @Override
            protected Long getRawValue(EntityModel object) {
                return (long) ((DiskImage) object.getEntity()).getSizeInGigabytes();
            }
        };
        table.addColumn(vSizeColumn, "V-Size", "120px");

        DiskSizeColumn<EntityModel> actualSizeColumn = new DiskSizeColumn<EntityModel>(DiskSizeUnit.GIGABYTE) {
            @Override
            protected Long getRawValue(EntityModel object) {
                return (long) ((DiskImage) object.getEntity()).getActualDiskWithSnapshotsSize();
            }
        };
        table.addColumn(actualSizeColumn, "Actual Size", "120px");

        TextColumnWithTooltip<EntityModel> creationDateColumn = new GeneralDateTimeColumn<EntityModel>() {
            @Override
            protected Date getRawValue(EntityModel object) {
                return ((DiskImage) object.getEntity()).getcreation_date();
            }
        };
        table.addColumn(creationDateColumn, "Creation Date", "180px");

        return creatEntityItem(table, disk);
    }

    private TreeItem getDiskNode(DiskImage disk) {
        return getDiskOrSnapshotNode(disk, true);
    }

    private TreeItem getSnapshotsNode(DiskImage disk) {
        return getDiskOrSnapshotNode(disk, false);
    }
}
