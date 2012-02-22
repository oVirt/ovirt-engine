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
import org.ovirt.engine.ui.common.widget.label.TextBoxLabel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageDomainModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateDiskListModel;
import org.ovirt.engine.ui.webadmin.widget.label.DiskSizeLabel;
import org.ovirt.engine.ui.webadmin.widget.label.EnumLabel;
import org.ovirt.engine.ui.webadmin.widget.storage.AbstractSubTabTree;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.TreeItem;

public class StoragesTree extends AbstractSubTabTree<TemplateDiskListModel, StorageDomainModel, DiskImage> {

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

        return new TreeItem(panel);
    }

    @Override
    protected TreeItem getNodeItem(DiskImage disk) {
        HorizontalPanel panel = new HorizontalPanel();
        panel.setSpacing(1);
        panel.setWidth("100%");

        addItemToPanel(panel, new Image(resources.diskImage()), "25px");
        addTextBoxToPanel(panel, new TextBoxLabel(), "Disk " + disk.getinternal_drive_mapping(), "");
        addValueLabelToPanel(panel, new DiskSizeLabel<Long>(), disk.getSizeInGigabytes(), "120px");
        addValueLabelToPanel(panel, new EnumLabel<DiskType>(), disk.getdisk_type(), "120px");
        addValueLabelToPanel(panel, new EnumLabel<VolumeType>(), disk.getvolume_type(), "120px");
        addValueLabelToPanel(panel, new EnumLabel<DiskInterface>(), disk.getdisk_interface(), "110px");

        return new TreeItem(panel);
    }

    @Override
    protected TreeItem getNodeHeader() {
        EntityModelCellTable<ListModel> table = new EntityModelCellTable<ListModel>(false, true);
        table.addColumn(new EmptyColumn(), "", "25px");
        table.addColumn(new EmptyColumn(), "Name", "");
        table.addColumn(new EmptyColumn(), "Size", "120px");
        table.addColumn(new EmptyColumn(), "Type", "120px");
        table.addColumn(new EmptyColumn(), "Allocation", "120px");
        table.addColumn(new EmptyColumn(), "Interface", "120px");
        table.setRowData(new ArrayList());
        table.setWidth("100%", true);
        return new TreeItem(table);
    }

    @Override
    protected ArrayList<DiskImage> getNodeObjects(StorageDomainModel storageDomainModel) {
        return storageDomainModel.getDisks();
    }
}
