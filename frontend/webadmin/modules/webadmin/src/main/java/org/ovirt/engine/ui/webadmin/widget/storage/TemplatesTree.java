package org.ovirt.engine.ui.webadmin.widget.storage;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.widget.label.DiskSizeLabel;
import org.ovirt.engine.ui.common.widget.label.TextBoxLabel;
import org.ovirt.engine.ui.common.widget.tree.AbstractSubTabTree;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageTemplateListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.widget.label.FullDateTimeLabel;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.TreeItem;

public class TemplatesTree extends AbstractSubTabTree<StorageTemplateListModel, VmTemplate, DiskImage> {

    ApplicationResources resources;
    ApplicationConstants constants;

    public TemplatesTree(CommonApplicationResources resources, CommonApplicationConstants constants) {
        super(resources, constants);
        this.resources = (ApplicationResources) resources;
        this.constants = (ApplicationConstants) constants;
    }

    @Override
    protected TreeItem getRootItem(VmTemplate template) {
        HorizontalPanel panel = new HorizontalPanel();
        panel.setSpacing(1);
        panel.setWidth("100%");

        addItemToPanel(panel, new Image(resources.vmImage()), "25px");
        addTextBoxToPanel(panel, new TextBoxLabel(), template.getname(), "");
        addTextBoxToPanel(panel, new TextBoxLabel(), String.valueOf(template.getDiskMap().size()), "110px");
        addValueLabelToPanel(panel, new DiskSizeLabel<Double>(), template.getActualDiskSize(), "110px");
        addValueLabelToPanel(panel, new FullDateTimeLabel(), template.getcreation_date(), "140px");

        TreeItem treeItem = new TreeItem(panel);
        treeItem.setUserObject(template.getId());
        return treeItem;
    }

    @Override
    protected TreeItem getNodeItem(DiskImage disk) {
        HorizontalPanel panel = new HorizontalPanel();
        panel.setSpacing(1);
        panel.setWidth("100%");

        addItemToPanel(panel, new Image(resources.diskImage()), "25px");
        addTextBoxToPanel(panel, new TextBoxLabel(), disk.getinternal_drive_mapping(), "");
        addTextBoxToPanel(panel, new TextBoxLabel(), "", "110px");
        addValueLabelToPanel(panel, new DiskSizeLabel<Double>(), disk.getActualSize(), "110px");
        addValueLabelToPanel(panel, new FullDateTimeLabel(), disk.getcreation_date(), "140px");

        TreeItem treeItem = new TreeItem(panel);
        treeItem.setUserObject(disk.getId());
        return treeItem;
    }

    @Override
    protected ArrayList<DiskImage> getNodeObjects(VmTemplate template) {
        return new ArrayList<DiskImage>(template.getDiskImageMap().values());
    }

    @Override
    protected boolean getIsNodeEnabled(DiskImage disk) {
        return disk.getstorage_ids().contains(listModel.getEntity().getId());
    }
}
