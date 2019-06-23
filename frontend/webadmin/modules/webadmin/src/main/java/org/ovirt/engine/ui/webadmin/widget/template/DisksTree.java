package org.ovirt.engine.ui.webadmin.widget.template;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainSharedStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.utils.SizeConverter;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.label.DiskSizeLabel;
import org.ovirt.engine.ui.common.widget.label.EnumLabel;
import org.ovirt.engine.ui.common.widget.label.StringValueLabel;
import org.ovirt.engine.ui.common.widget.table.column.EmptyColumn;
import org.ovirt.engine.ui.common.widget.tree.AbstractSubTabTree;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateDiskListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.widget.label.FullDateTimeLabel;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.TreeItem;

public class DisksTree extends AbstractSubTabTree<TemplateDiskListModel, DiskImage, StorageDomain> {

    private static final ApplicationResources resources = AssetProvider.getResources();
    private static final ApplicationConstants constants = AssetProvider.getConstants();

    public DisksTree() {
        super();

        setRootSelectionEnabled(true);
    }

    @Override
    protected TreeItem getRootItem(DiskImage disk) {
        HorizontalPanel panel = new HorizontalPanel();
        panel.setSpacing(1);
        panel.setWidth("100%"); //$NON-NLS-1$

        DiskVmElement dve = disk.getDiskVmElements().iterator().next();

        addItemToPanel(panel, new Image(resources.diskImage()), "25px"); //$NON-NLS-1$
        addTextBoxToPanel(panel, new StringValueLabel(), disk.getDiskAlias(), ""); //$NON-NLS-1$
        addItemToPanel(panel, dve.isReadOnly() ? new Image(resources.readOnlyDiskIcon()) : new Image(), "60px"); //$NON-NLS-1$
        addValueLabelToPanel(panel, new DiskSizeLabel<Long>(), disk.getSizeInGigabytes(), "120px"); //$NON-NLS-1$
        addValueLabelToPanel(panel, new DiskSizeLabel<Long>(SizeConverter.SizeUnit.BYTES), disk.getActualSizeInBytes(), "120px"); //$NON-NLS-1$
        addValueLabelToPanel(panel, new EnumLabel<ImageStatus>(), disk.getImageStatus(), "120px"); //$NON-NLS-1$
        addValueLabelToPanel(panel, new EnumLabel<VolumeType>(), disk.getVolumeType(), "120px"); //$NON-NLS-1$
        addValueLabelToPanel(panel, new EnumLabel<DiskInterface>(), dve.getDiskInterface(), "120px"); //$NON-NLS-1$
        addValueLabelToPanel(panel, new EnumLabel<DiskStorageType>(), disk.getDiskStorageType(), "190px"); //$NON-NLS-1$
        addValueLabelToPanel(panel, new FullDateTimeLabel(), disk.getCreationDate(), "140px"); //$NON-NLS-1$
        TreeItem treeItem = new TreeItem(panel);
        treeItem.setUserObject(disk.getId());
        return treeItem;
    }

    @Override
    protected TreeItem getNodeItem(StorageDomain storage) {
        HorizontalPanel panel = new HorizontalPanel();
        panel.setSpacing(1);
        panel.setWidth("100%"); // $NON-NLS-1$

        addItemToPanel(panel, new Image(resources.storageImage()), "25px"); //$NON-NLS-1$
        addTextBoxToPanel(panel, new StringValueLabel(), storage.getStorageName(), ""); //$NON-NLS-1$
        addValueLabelToPanel(panel, new EnumLabel<StorageDomainType>(), storage.getStorageDomainType(), "120px"); //$NON-NLS-1$
        addValueLabelToPanel(panel, new EnumLabel<StorageDomainSharedStatus>(), storage.getStorageDomainSharedStatus(), "120px"); //$NON-NLS-1$
        addValueLabelToPanel(panel, new DiskSizeLabel<Integer>(), storage.getAvailableDiskSize(), "120px"); //$NON-NLS-1$
        addValueLabelToPanel(panel, new DiskSizeLabel<Integer>(), storage.getUsedDiskSize(), "120px"); //$NON-NLS-1$
        addValueLabelToPanel(panel, new DiskSizeLabel<Integer>(), storage.getTotalDiskSize(), "120px"); //$NON-NLS-1$

        TreeItem treeItem = new TreeItem(panel);
        treeItem.setUserObject(storage.getId());
        return treeItem;
    }

    @Override
    protected TreeItem getNodeHeader() {
        EntityModelCellTable<ListModel> table = new EntityModelCellTable<>(false, true);
        table.addColumn(new EmptyColumn(), constants.empty(), "20px"); //$NON-NLS-1$
        table.addColumn(new EmptyColumn(), constants.domainNameDisksTree(), ""); //$NON-NLS-1$
        table.addColumn(new EmptyColumn(), constants.domainTypeDisksTree(), "120px"); //$NON-NLS-1$
        table.addColumn(new EmptyColumn(), constants.statusDisksTree(), "120px"); //$NON-NLS-1$
        table.addColumn(new EmptyColumn(), constants.freeSpaceDisksTree(), "120px"); //$NON-NLS-1$
        table.addColumn(new EmptyColumn(), constants.usedSpaceDisksTree(), "120px"); //$NON-NLS-1$
        table.addColumn(new EmptyColumn(), constants.totalSpaceDisksTree(), "130px"); //$NON-NLS-1$
        table.setRowData(new ArrayList());
        table.setWidth("100%"); // $NON-NLS-1$
        table.setHeight("30px"); // $NON-NLS-1$
        return new TreeItem(table);
    }

    @Override
    protected List<StorageDomain> getNodeObjects(DiskImage disk) {
        return Linq.getStorageDomainsByIds(disk.getStorageIds(), listModel.getStorageDomains());
    }
}
