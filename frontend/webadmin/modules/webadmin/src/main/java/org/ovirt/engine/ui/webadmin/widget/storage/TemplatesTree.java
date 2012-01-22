package org.ovirt.engine.ui.webadmin.widget.storage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageTemplateListModel;
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

public class TemplatesTree extends AbstractSubTabTree<StorageTemplateListModel> {

    @Override
    public void updateTree(final StorageTemplateListModel listModel) {
        listModel.getItemsChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                StorageTemplateListModel model = (StorageTemplateListModel) sender;
                List<VmTemplate> templates = (List<VmTemplate>) model.getItems();
                tree.clear();

                if (templates == null)
                    return;

                for (VmTemplate template : templates) {
                    TreeItem vmItem = getTemplateNode(template);

                    Collection<DiskImage> disks = template.getDiskImageMap().values();
                    if (!disks.isEmpty()) {
                        TreeItem snapshotItem = getDiskNode(new ArrayList<DiskImage>(disks));
                        vmItem.addItem(snapshotItem);
                        styleItem(snapshotItem);
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

    private TreeItem getTemplateNode(VmTemplate template) {
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
                return ((VmTemplate) object.getEntity()).getname();
            }
        };
        table.addColumn(nameColumn, "Name");

        TextColumnWithTooltip<EntityModel> diskColumn = new TextColumnWithTooltip<EntityModel>() {
            @Override
            public String getValue(EntityModel object) {
                return String.valueOf(((VmTemplate) object.getEntity()).getDiskMap().size());
            }
        };
        table.addColumn(diskColumn, "Disks", "110px");

        DiskSizeColumn<EntityModel> actualSizeColumn = new DiskSizeColumn<EntityModel>(DiskSizeUnit.GIGABYTE) {
            @Override
            protected Long getRawValue(EntityModel object) {
                return (long) ((VmTemplate) object.getEntity()).getActualDiskSize();
            }
        };
        table.addColumn(actualSizeColumn, "Actual Size", "110px");

        TextColumnWithTooltip<EntityModel> creationDateColumn = new GeneralDateTimeColumn<EntityModel>() {
            @Override
            protected Date getRawValue(EntityModel object) {
                return ((VmTemplate) object.getEntity()).getcreation_date();
            }
        };
        table.addColumn(creationDateColumn, "Creation Date", "140px");

        ArrayList<EntityModel> entityModelList = toEntityModelList(new ArrayList<VmTemplate>(Arrays.asList(template)));
        return createTreeItem(table, entityModelList);
    }

    private TreeItem getDiskNode(ArrayList<DiskImage> disks) {
        EntityModelCellTable<ListModel> table = new EntityModelCellTable<ListModel>(false,
                (Resources) GWT.create(TreeHeaderlessTableResources.class),
                true);

        table.addColumn(new ImageResourceColumn<EntityModel>() {
            @Override
            public ImageResource getValue(EntityModel object) {
                return resources.diskImage();
            }
        }, "", "30px");

        TextColumnWithTooltip<EntityModel> nameColumn = new TextColumnWithTooltip<EntityModel>() {
            @Override
            public String getValue(EntityModel object) {
                return "Disk " + ((DiskImage) object.getEntity()).getinternal_drive_mapping();
            }
        };
        table.addColumn(nameColumn, "Name");

        table.addColumn(new EmptyColumn(), "Disks", "110px");

        DiskSizeColumn<EntityModel> actualSizeColumn = new DiskSizeColumn<EntityModel>(DiskSizeUnit.GIGABYTE) {
            @Override
            protected Long getRawValue(EntityModel object) {
                return (long) ((DiskImage) object.getEntity()).getActualSize();
            }
        };
        table.addColumn(actualSizeColumn, "Actual Size", "110px");

        TextColumnWithTooltip<EntityModel> creationDateColumn = new GeneralDateTimeColumn<EntityModel>() {
            @Override
            protected Date getRawValue(EntityModel object) {
                return ((DiskImage) object.getEntity()).getcreation_date();
            }
        };
        table.addColumn(creationDateColumn, "Creation Date", "140px");

        ArrayList<EntityModel> entityModelList = toEntityModelList(disks);
        return createTreeItem(table, entityModelList);
    }
}
