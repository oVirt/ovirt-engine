package org.ovirt.engine.ui.webadmin.widget.storage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.LunDisk;
import org.ovirt.engine.core.common.utils.SizeConverter;
import org.ovirt.engine.ui.common.widget.label.DiskSizeLabel;
import org.ovirt.engine.ui.common.widget.label.StringValueLabel;
import org.ovirt.engine.ui.common.widget.tree.AbstractSubTabTree;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.widget.label.FullDateTimeLabel;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;

public class VMsTree<M extends SearchableListModel> extends AbstractSubTabTree<M, VM, Disk> {

    private static final ApplicationResources resources = AssetProvider.getResources();
    private static final ApplicationConstants constants = AssetProvider.getConstants();

    public VMsTree() {
        super();
    }

    @Override
    protected TreeItem getRootItem(VM vm) {
        HorizontalPanel panel = new HorizontalPanel();
        panel.setSpacing(1);
        panel.setWidth("100%"); //$NON-NLS-1$

        addItemToPanel(panel, new Image(resources.vmImage()), "25px"); //$NON-NLS-1$
        addTextBoxToPanel(panel, new StringValueLabel(), vm.getName(), ""); //$NON-NLS-1$
        addTextBoxToPanel(panel, new StringValueLabel(), String.valueOf(vm.getDiskMap().size()), "80px"); //$NON-NLS-1$
        addTextBoxToPanel(panel, new StringValueLabel(), vm.getVmtName(), "160px"); //$NON-NLS-1$
        addValueLabelToPanel(panel, new DiskSizeLabel<Double>(), vm.getDiskSize(), "110px"); //$NON-NLS-1$
        addValueLabelToPanel(panel, new DiskSizeLabel<Double>(), vm.getActualDiskWithSnapshotsSize(), "110px"); //$NON-NLS-1$
        addValueLabelToPanel(panel, new FullDateTimeLabel(), vm.getVmCreationDate(), "140px"); //$NON-NLS-1$

        TreeItem treeItem = new TreeItem(panel);
        treeItem.setUserObject(vm.getId());
        return treeItem;
    }

    @Override
    protected TreeItem getNodeItem(Disk disk) {
        //return getDiskOrSnapshotNode(new ArrayList<Disk>(Arrays.asList(disk)), true);
        return getDiskNode(new ArrayList<>(Arrays.asList(disk)));
    }

    @Override
    protected TreeItem getLeafItem(Disk disk) {
        if (disk.getDiskStorageType() == DiskStorageType.IMAGE) {
            return getSnapshotNode(((DiskImage) disk).getSnapshots());
        } else {
            return null;
        }
    }

    @Override
    protected ArrayList<Disk> getNodeObjects(VM vm) {
        ArrayList<Disk> disks = new ArrayList<>();
        for (Disk disk : vm.getDiskMap().values()) {
            disks.add(disk);
        }
        return disks;
    }

    @Override
    protected boolean getIsNodeEnabled(Disk disk) {
        if (disk != null && disk.getDiskStorageType() == DiskStorageType.IMAGE) {
            if (listModel != null && listModel.getEntity() instanceof Quota) {
                return ((BusinessEntity) listModel.getEntity()).getId().equals(((DiskImage) disk).getQuotaId());
            } else {
                DiskImage diskImage = (DiskImage) disk;
                if (diskImage.getStorageIds() != null && !diskImage.getStorageIds().isEmpty() &&
                        listModel != null && listModel.getEntity() != null) {
                    return diskImage.getStorageIds().get(0).equals(((BusinessEntity) listModel.getEntity()).getId());
                } else {
                    return true;
                }
            }
        } else {
            return true;
        }
    }

    @Override
    protected String getNodeDisabledTooltip() {
        return constants.differentStorageDomainWarning();
    }

    private TreeItem getSnapshotNode(List<DiskImage> disks) {
        VerticalPanel vPanel = new VerticalPanel();
        vPanel.setWidth("100%"); //$NON-NLS-1$

        for (DiskImage disk : disks) {
            HorizontalPanel panel = new HorizontalPanel();

            ImageResource image = resources.snapshotImage();
            String name = disk.getDescription();

            addItemToPanel(panel, new Image(image), "25px"); //$NON-NLS-1$
            addTextBoxToPanel(panel, new StringValueLabel(), name, ""); //$NON-NLS-1$
            addTextBoxToPanel(panel, new StringValueLabel(), "", "80px"); //$NON-NLS-1$ //$NON-NLS-2$
            addTextBoxToPanel(panel, new StringValueLabel(), "", "160px"); //$NON-NLS-1$ //$NON-NLS-2$
            addValueLabelToPanel(panel, new DiskSizeLabel<Long>(), disk.getSizeInGigabytes(), "110px"); //$NON-NLS-1$
            addValueLabelToPanel(panel, new DiskSizeLabel<Double>(SizeConverter.SizeUnit.GiB), disk.getActualSize(), "110px"); //$NON-NLS-1$
            addValueLabelToPanel(panel, new FullDateTimeLabel(), disk.getCreationDate(), "140px"); //$NON-NLS-1$

            panel.setSpacing(1);
            panel.setWidth("100%"); //$NON-NLS-1$

            vPanel.add(panel);
        }

        TreeItem treeItem = new TreeItem(vPanel);
        treeItem.setUserObject(disks.get(0).getId() + "snapshot"); //$NON-NLS-1$
        return treeItem;
    }

    private TreeItem getDiskNode(ArrayList<Disk> disks) {
        if (disks.isEmpty()) {
            return null;
        }

        VerticalPanel vPanel = new VerticalPanel();
        vPanel.setWidth("100%"); //$NON-NLS-1$

        for (Disk disk : disks) {
            HorizontalPanel panel = new HorizontalPanel();

            ImageResource image = resources.diskImage();
            String name = disk.getDiskAlias();

            addItemToPanel(panel, new Image(image), "25px"); //$NON-NLS-1$
            addTextBoxToPanel(panel, new StringValueLabel(), name, ""); //$NON-NLS-1$
            addTextBoxToPanel(panel, new StringValueLabel(), "", "80px"); //$NON-NLS-1$ //$NON-NLS-2$
            addTextBoxToPanel(panel, new StringValueLabel(), "", "160px"); //$NON-NLS-1$ //$NON-NLS-2$

            boolean isDiskImage = disk.getDiskStorageType() == DiskStorageType.IMAGE;
            Double actualSize =
                    isDiskImage ? ((DiskImage) disk).getActualDiskWithSnapshotsSize()
                            : (long) ((LunDisk) disk).getLun().getDeviceSize();
            Long virtualSize = isDiskImage ? disk.getSize() :
                    (long) (((LunDisk) disk).getLun().getDeviceSize() * Math.pow(1024, 3));

            addValueLabelToPanel(panel, new DiskSizeLabel<Long>(SizeConverter.SizeUnit.BYTES), virtualSize, "110px"); //$NON-NLS-1$
            addValueLabelToPanel(panel, new DiskSizeLabel<Double>(SizeConverter.SizeUnit.GiB), actualSize, "110px"); //$NON-NLS-1$
            addValueLabelToPanel(panel, new FullDateTimeLabel(), disk.getDiskStorageType() == DiskStorageType.IMAGE ?
                    ((DiskImage) disk).getCreationDate() : null, "140px"); //$NON-NLS-1$

            panel.setSpacing(1);
            panel.setWidth("100%"); //$NON-NLS-1$

            vPanel.add(panel);
        }

        TreeItem treeItem = new TreeItem(vPanel);
        treeItem.setUserObject(disks.get(0).getId());
        return treeItem;
    }
}
