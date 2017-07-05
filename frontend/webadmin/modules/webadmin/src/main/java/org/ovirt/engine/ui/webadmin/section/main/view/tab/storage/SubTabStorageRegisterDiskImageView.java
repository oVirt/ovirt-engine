package org.ovirt.engine.ui.webadmin.section.main.view.tab.storage;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.uicommon.disks.DisksViewColumns;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageRegisterDiskImageListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStorageRegisterDiskImagePresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;

import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;

public class SubTabStorageRegisterDiskImageView extends AbstractSubTabTableView<StorageDomain, Disk, StorageListModel, StorageRegisterDiskImageListModel>
        implements SubTabStorageRegisterDiskImagePresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabStorageRegisterDiskImageView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public SubTabStorageRegisterDiskImageView(SearchableDetailModelProvider<Disk, StorageListModel, StorageRegisterDiskImageListModel> modelProvider) {
        super(modelProvider);
        initTable();
        initWidget(getTableContainer());
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    void initTable() {
        getTable().enableColumnResizing();

        getTable().ensureColumnVisible(
                DisksViewColumns.getAliasColumn(null), constants.aliasDisk(), true, "150px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                DisksViewColumns.getActualSizeColumn(null), constants.sizeDisk(), true, "150px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                DisksViewColumns.getSizeColumn(null), constants.provisionedSizeDisk(), true, "150px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(DisksViewColumns.getAllocationColumn(null),
                constants.allocationDisk(),
                true, "130px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                DisksViewColumns.getDateCreatedColumn(null), constants.creationDateDisk(), true, "150px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                DisksViewColumns.getDescriptionColumn(null), constants.descriptionDisk(), true, "200px"); //$NON-NLS-1$

        getTable().showRefreshButton();
    }
}
