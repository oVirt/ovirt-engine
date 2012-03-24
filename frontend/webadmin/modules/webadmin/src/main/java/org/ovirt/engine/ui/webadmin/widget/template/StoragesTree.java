package org.ovirt.engine.ui.webadmin.widget.template;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskInterface;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.label.DiskSizeLabel;
import org.ovirt.engine.ui.common.widget.label.EnumLabel;
import org.ovirt.engine.ui.common.widget.label.TextBoxLabel;
import org.ovirt.engine.ui.common.widget.table.column.EmptyColumn;
import org.ovirt.engine.ui.common.widget.tree.AbstractSubTabTree;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageDomainModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateStorageListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.DiskModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;

import com.google.gwt.user.client.ui.DateLabel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.TreeItem;

public class StoragesTree extends AbstractSubTabTree<TemplateStorageListModel, StorageDomainModel, DiskModel> {

    ApplicationResources resources;
    ApplicationConstants constants;

    public StoragesTree(CommonApplicationResources resources, CommonApplicationConstants constants) {
        super(resources, constants);
        this.resources = (ApplicationResources) resources;
        this.constants = (ApplicationConstants) constants;

        setNodeSelectionEnabled(true);
    }

    @Override
    protected TreeItem getRootItem(StorageDomainModel storageDomainModel) {
        HorizontalPanel panel = new HorizontalPanel();
        panel.setSpacing(1);
        panel.setWidth("100%");

        storage_domains storage = storageDomainModel.getStorageDomain();

        addItemToPanel(panel, new Image(resources.storageImage()), "25px");
        addTextBoxToPanel(panel, new TextBoxLabel(), storage.getstorage_name(), "");
        addValueLabelToPanel(panel, new EnumLabel<StorageDomainType>(), storage.getstorage_domain_type(), "120px");
        addValueLabelToPanel(panel, new EnumLabel<StorageDomainStatus>(), storage.getstatus(), "120px");
        addValueLabelToPanel(panel, new DiskSizeLabel<Integer>(), storage.getavailable_disk_size(), "120px");
        addValueLabelToPanel(panel, new DiskSizeLabel<Integer>(), storage.getused_disk_size(), "120px");
        addValueLabelToPanel(panel, new DiskSizeLabel<Integer>(), storage.getTotalDiskSize(), "90px");

        TreeItem treeItem = new TreeItem(panel);
        treeItem.setUserObject(storage.getId());
        return treeItem;
    }

    @Override
    protected TreeItem getNodeItem(DiskModel diskModel) {
        HorizontalPanel panel = new HorizontalPanel();
        panel.setSpacing(1);
        panel.setWidth("100%");

        DiskImage disk = diskModel.getDiskImage();

        addItemToPanel(panel, new Image(resources.diskImage()), "25px");
        addTextBoxToPanel(panel, new TextBoxLabel(), "Disk " + disk.getinternal_drive_mapping(), "");
        addValueLabelToPanel(panel, new DiskSizeLabel<Long>(), disk.getSizeInGigabytes(), "120px");
        addValueLabelToPanel(panel, new EnumLabel<ImageStatus>(), disk.getimageStatus(), "120px");
        addValueLabelToPanel(panel, new EnumLabel<VolumeType>(), disk.getvolume_type(), "120px");
        addValueLabelToPanel(panel, new EnumLabel<DiskInterface>(), disk.getdisk_interface(), "110px");
        addValueLabelToPanel(panel, new DateLabel(), disk.getcreation_date(), "90px");

        TreeItem treeItem = new TreeItem(panel);
        treeItem.setUserObject(getEntityId(diskModel));
        return treeItem;
    }

    @Override
    protected TreeItem getNodeHeader() {
        EntityModelCellTable<ListModel> table = new EntityModelCellTable<ListModel>(false, true);
        table.addColumn(new EmptyColumn(), "", "25px");
        table.addColumn(new EmptyColumn(), "Name", "");
        table.addColumn(new EmptyColumn(), "Size", "120px");
        table.addColumn(new EmptyColumn(), "Type", "120px");
        table.addColumn(new EmptyColumn(), "Allocation", "120px");
        table.addColumn(new EmptyColumn(), "Interface", "110px");
        table.addColumn(new EmptyColumn(), "Creation Date", "100px");
        table.setRowData(new ArrayList());
        table.setWidth("100%", true);

        TreeItem item = new TreeItem(table);
        item.setUserObject(NODE_HEADER);
        item.getElement().getStyle().setBackgroundColor("#F0F2FF");
        return item;
    }

    @Override
    protected ArrayList<DiskModel> getNodeObjects(StorageDomainModel storageDomainModel) {
        return storageDomainModel.getDisksModels();
    }

    protected Object getEntityId(Object entity) {
        DiskModel diskModel = (DiskModel) entity;
        storage_domains storageDomain = (storage_domains) diskModel.getStorageDomain().getSelectedItem();
        return diskModel.getDiskImage().getId().toString() + storageDomain.getId().toString();
    }

    protected ArrayList<Object> getSelectedEntities() {
        ArrayList<Object> selectedEntities = new ArrayList<Object>();
        for (StorageDomainModel storageDomainModel : (ArrayList<StorageDomainModel>) listModel.getItems()) {
            for (DiskModel entity : storageDomainModel.getDisksModels()) {
                if (selectedItems.contains(getEntityId(entity))) {
                    selectedEntities.add(entity);
                }
            }
        }
        return selectedEntities;
    }
}
