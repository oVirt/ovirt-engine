package org.ovirt.engine.ui.webadmin.widget.template;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskInterface;
import org.ovirt.engine.core.common.businessentities.DiskType;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.label.DiskSizeLabel;
import org.ovirt.engine.ui.common.widget.label.TextBoxLabel;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateDiskListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.DiskModel;
import org.ovirt.engine.ui.webadmin.widget.label.EnumLabel;
import org.ovirt.engine.ui.webadmin.widget.tree.AbstractSubTabTree;

import com.google.gwt.user.client.ui.DateLabel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.TreeItem;

public class DisksTree extends AbstractSubTabTree<TemplateDiskListModel, DiskModel, storage_domains> {

    public DisksTree() {
        enableRootSelection();
    }

    @Override
    protected TreeItem getRootItem(DiskModel diskModel) {
        HorizontalPanel panel = new HorizontalPanel();
        panel.setSpacing(1);
        panel.setWidth("100%");

        DiskImage disk = diskModel.getDiskImage();

        addItemToPanel(panel, new Image(resources.diskImage()), "25px");
        addTextBoxToPanel(panel, new TextBoxLabel(), "Disk " + disk.getinternal_drive_mapping(), "");
        addValueLabelToPanel(panel, new DiskSizeLabel<Long>(), disk.getSizeInGigabytes(), "120px");
        addValueLabelToPanel(panel, new EnumLabel<DiskType>(), disk.getdisk_type(), "120px");
        addValueLabelToPanel(panel, new EnumLabel<VolumeType>(), disk.getvolume_type(), "120px");
        addValueLabelToPanel(panel, new EnumLabel<DiskInterface>(), disk.getdisk_interface(), "120px");
        addValueLabelToPanel(panel, new DateLabel(), disk.getcreation_date(), "90px");

        TreeItem treeItem = new TreeItem(panel);
        treeItem.setUserObject(disk);
        return treeItem;
    }

    @Override
    protected TreeItem getNodeItem(storage_domains storage) {
        HorizontalPanel panel = new HorizontalPanel();
        panel.setSpacing(1);
        panel.setWidth("100%");

        addItemToPanel(panel, new Image(resources.storageImage()), "25px");
        addTextBoxToPanel(panel, new TextBoxLabel(), storage.getstorage_name(), "");
        addValueLabelToPanel(panel, new EnumLabel<StorageDomainType>(), storage.getstorage_domain_type(), "120px");
        addValueLabelToPanel(panel, new EnumLabel<StorageDomainStatus>(), storage.getstatus(), "120px");
        addValueLabelToPanel(panel, new DiskSizeLabel<Integer>(), storage.getavailable_disk_size(), "120px");
        addValueLabelToPanel(panel, new DiskSizeLabel<Integer>(), storage.getused_disk_size(), "120px");
        addValueLabelToPanel(panel, new DiskSizeLabel<Integer>(), storage.getTotalDiskSize(), "120px");

        TreeItem treeItem = new TreeItem(panel);
        treeItem.setUserObject(storage);
        return treeItem;
    }

    @Override
    protected TreeItem getNodeHeader() {
        EntityModelCellTable<ListModel> table = new EntityModelCellTable<ListModel>(false, true);
        table.addColumn(new EmptyColumn(), "", "20px");
        table.addColumn(new EmptyColumn(), "Domain Name", "");
        table.addColumn(new EmptyColumn(), "Domain Type", "120px");
        table.addColumn(new EmptyColumn(), "Status", "120px");
        table.addColumn(new EmptyColumn(), "Free Space", "120px");
        table.addColumn(new EmptyColumn(), "Used Space", "120px");
        table.addColumn(new EmptyColumn(), "Total Space", "130px");
        table.setRowData(new ArrayList());
        table.setWidth("100%", true);
        return new TreeItem(table);
    }

    @Override
    protected ArrayList<storage_domains> getNodeObjects(DiskModel diskModel) {
        return Linq.<storage_domains> Cast(diskModel.getStorageDomain().getItems());
    }
}
