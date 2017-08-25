package org.ovirt.engine.ui.webadmin.widget.template;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainSharedStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.label.DiskSizeLabel;
import org.ovirt.engine.ui.common.widget.label.EnumLabel;
import org.ovirt.engine.ui.common.widget.label.StringValueLabel;
import org.ovirt.engine.ui.common.widget.table.column.EmptyColumn;
import org.ovirt.engine.ui.common.widget.tree.AbstractSubTabTree;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageDomainModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateStorageListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.DiskModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.DateLabel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.TreeItem;

public class StoragesTree extends AbstractSubTabTree<TemplateStorageListModel, StorageDomainModel, DiskModel> {

    private static final ApplicationResources resources = AssetProvider.getResources();
    private static final ApplicationConstants constants = AssetProvider.getConstants();

    public StoragesTree() {
        super();

        setNodeSelectionEnabled(true);
    }

    @Override
    protected TreeItem getRootItem(StorageDomainModel storageDomainModel) {
        HorizontalPanel panel = new HorizontalPanel();
        panel.setSpacing(1);
        panel.setWidth("100%"); // $NON-NLS-1$

        StorageDomain storage = storageDomainModel.getStorageDomain();

        addItemToPanel(panel, new Image(resources.storageImage()), "25px"); //$NON-NLS-1$
        addTextBoxToPanel(panel, new StringValueLabel(), storage.getStorageName(), ""); //$NON-NLS-1$
        addValueLabelToPanel(panel, new EnumLabel<StorageDomainType>(), storage.getStorageDomainType(), "120px"); //$NON-NLS-1$
        addValueLabelToPanel(panel, new EnumLabel<StorageDomainSharedStatus>(), storage.getStorageDomainSharedStatus(), "120px"); //$NON-NLS-1$
        addValueLabelToPanel(panel, new DiskSizeLabel<Integer>(), storage.getAvailableDiskSize(), "120px"); //$NON-NLS-1$
        addValueLabelToPanel(panel, new DiskSizeLabel<Integer>(), storage.getUsedDiskSize(), "120px"); //$NON-NLS-1$
        addValueLabelToPanel(panel, new DiskSizeLabel<Integer>(), storage.getTotalDiskSize(), "90px"); //$NON-NLS-1$

        TreeItem treeItem = new TreeItem(panel);
        treeItem.setUserObject(storage.getId());
        return treeItem;
    }

    @Override
    protected TreeItem getNodeItem(DiskModel diskModel) {
        HorizontalPanel panel = new HorizontalPanel();
        panel.setSpacing(1);
        panel.setWidth("100%"); // $NON-NLS-1$

        DiskImage disk = (DiskImage) diskModel.getDisk();

        addItemToPanel(panel, new Image(resources.diskImage()), "30px"); //$NON-NLS-1$
        addTextBoxToPanel(panel, new StringValueLabel(), disk.getDiskAlias(), ""); //$NON-NLS-1$
        addValueLabelToPanel(panel, new DiskSizeLabel<Long>(), disk.getSizeInGigabytes(), "120px"); //$NON-NLS-1$
        addValueLabelToPanel(panel, new EnumLabel<ImageStatus>(), disk.getImageStatus(), "120px"); //$NON-NLS-1$
        addValueLabelToPanel(panel, new EnumLabel<VolumeType>(), disk.getVolumeType(), "120px"); //$NON-NLS-1$
        addValueLabelToPanel(panel, new EnumLabel<DiskInterface>(), disk.getDiskVmElements().iterator().next().getDiskInterface(), "110px"); //$NON-NLS-1$
        addValueLabelToPanel(panel, new EnumLabel<DiskStorageType>(), disk.getDiskStorageType(), "110px"); //$NON-NLS-1$
        addValueLabelToPanel(panel, new DateLabel(), disk.getCreationDate(), "100px"); //$NON-NLS-1$

        TreeItem treeItem = new TreeItem(panel);
        treeItem.setUserObject(getEntityId(diskModel));
        return treeItem;
    }

    @Override
    protected TreeItem getNodeHeader() {
        EntityModelCellTable<ListModel> table = new EntityModelCellTable<>(false, true);
        table.addColumn(new EmptyColumn(), constants.empty(), "30px"); //$NON-NLS-1$
        table.addColumn(new EmptyColumn(), constants.aliasDisk(), ""); //$NON-NLS-1$
        table.addColumn(new EmptyColumn(), constants.sizeStorageTree(), "120px"); //$NON-NLS-1$
        table.addColumn(new EmptyColumn(), constants.statusStorageTree(), "120px"); //$NON-NLS-1$
        table.addColumn(new EmptyColumn(), constants.allocationStorageTree(), "120px"); //$NON-NLS-1$
        table.addColumn(new EmptyColumn(), constants.interfaceStorageTree(), "110px"); //$NON-NLS-1$
        table.addColumn(new EmptyColumn(), constants.typeDisk(), "110px"); //$NON-NLS-1$
        table.addColumn(new EmptyColumn(), constants.creationDateStorageTree(), "100px"); //$NON-NLS-1$
        table.setRowData(new ArrayList());
        table.setWidth("100%"); // $NON-NLS-1$
        table.setHeight("30px"); // $NON-NLS-1$

        TreeItem item = new TreeItem(table);
        item.setUserObject(NODE_HEADER);
        item.getElement().getStyle().setPadding(0, Unit.PX);
        return item;
    }

    @Override
    protected ArrayList<DiskModel> getNodeObjects(StorageDomainModel storageDomainModel) {
        return storageDomainModel.getDisksModels();
    }

    protected Object getEntityId(Object entity) {
        DiskModel diskModel = (DiskModel) entity;
        StorageDomain storageDomain = diskModel.getStorageDomain().getSelectedItem();
        return ((DiskImage) diskModel.getDisk()).getImageId().toString() + storageDomain.getId().toString();
    }

    protected ArrayList<Object> getSelectedEntities() {
        ArrayList<Object> selectedEntities = new ArrayList<>();
        for (StorageDomainModel storageDomainModel : listModel.getItems()) {
            for (DiskModel entity : storageDomainModel.getDisksModels()) {
                if (selectedItems.contains(getEntityId(entity))) {
                    selectedEntities.add(entity);
                }
            }
        }
        return selectedEntities;
    }
}
