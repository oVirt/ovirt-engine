package org.ovirt.engine.ui.webadmin.widget.storage;

import java.util.ArrayList;
import java.util.Arrays;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.widget.label.DiskSizeLabel;
import org.ovirt.engine.ui.common.widget.label.TextBoxLabel;
import org.ovirt.engine.ui.common.widget.tree.AbstractSubTabTree;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.widget.label.FullDateTimeLabel;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;

public class VMsTree<M extends SearchableListModel> extends AbstractSubTabTree<M, VM, DiskImage> {

    ApplicationResources resources;
    ApplicationConstants constants;

    public VMsTree(CommonApplicationResources resources, CommonApplicationConstants constants) {
        super(resources, constants);
        this.resources = (ApplicationResources) resources;
        this.constants = (ApplicationConstants) constants;
    }

    @Override
    protected TreeItem getRootItem(VM vm) {
        HorizontalPanel panel = new HorizontalPanel();
        panel.setSpacing(1);
        panel.setWidth("100%");

        addItemToPanel(panel, new Image(resources.vmImage()), "25px");
        addTextBoxToPanel(panel, new TextBoxLabel(), vm.getvm_name(), "");
        addTextBoxToPanel(panel, new TextBoxLabel(), String.valueOf(vm.getDiskMap().size()), "80px");
        addTextBoxToPanel(panel, new TextBoxLabel(), vm.getvmt_name(), "160px");
        addValueLabelToPanel(panel, new DiskSizeLabel<Double>(), vm.getDiskSize(), "110px");
        addValueLabelToPanel(panel, new DiskSizeLabel<Double>(), vm.getActualDiskWithSnapshotsSize(), "110px");
        addValueLabelToPanel(panel, new FullDateTimeLabel(), vm.getvm_creation_date(), "140px");

        TreeItem treeItem = new TreeItem(panel);
        treeItem.setUserObject(vm.getId());
        return treeItem;
    }

    @Override
    protected TreeItem getNodeItem(DiskImage disk) {
        return getDiskOrSnapshotNode(new ArrayList<DiskImage>(Arrays.asList(disk)), true);
    }

    @Override
    protected TreeItem getLeafItem(DiskImage disk) {
        return getDiskOrSnapshotNode(disk.getSnapshots(), false);
    }

    @Override
    protected ArrayList<DiskImage> getNodeObjects(VM vm) {
        return new ArrayList<DiskImage>(vm.getDiskMap().values());
    }

    @Override
    protected boolean getIsNodeEnabled(DiskImage disk) {
        return disk.getstorage_ids().get(0).equals(((BusinessEntity) listModel.getEntity()).getId());
    }

    @Override
    protected String getNodeDisabledTooltip() {
        return constants.differentStorageDomainWarning();
    }

    private TreeItem getDiskOrSnapshotNode(ArrayList<DiskImage> disks, final boolean isDisk) {
        if (disks.isEmpty()) {
            return null;
        }

        VerticalPanel vPanel = new VerticalPanel();
        vPanel.setWidth("100%");

        for (DiskImage disk : disks) {
            HorizontalPanel panel = new HorizontalPanel();

            ImageResource image =
                    isDisk ? resources.diskImage() : resources.snapshotImage();
            String name = isDisk ? "Disk " + disk.getinternal_drive_mapping() : disk.getdescription();

            addItemToPanel(panel, new Image(image), "25px");
            addTextBoxToPanel(panel, new TextBoxLabel(), name, "");
            addTextBoxToPanel(panel, new TextBoxLabel(), "", "80px");
            addTextBoxToPanel(panel, new TextBoxLabel(), "", "160px");
            addValueLabelToPanel(panel, new DiskSizeLabel<Long>(), disk.getSizeInGigabytes(), "110px");
            addValueLabelToPanel(panel, new DiskSizeLabel<Double>(), disk.getActualDiskWithSnapshotsSize(), "110px");
            addValueLabelToPanel(panel, new FullDateTimeLabel(), disk.getcreation_date(), "140px");

            panel.setSpacing(1);
            panel.setWidth("100%");

            vPanel.add(panel);
        }

        TreeItem treeItem = new TreeItem(vPanel);
        treeItem.setUserObject(disks.get(0).getId());
        return treeItem;
    }

    private TreeItem getDiskNode(DiskImage disk) {
        return getDiskOrSnapshotNode(new ArrayList<DiskImage>(Arrays.asList(disk)), true);
    }

    private TreeItem getSnapshotsNode(ArrayList<DiskImage> disks) {
        return getDiskOrSnapshotNode(disks, false);
    }

}
