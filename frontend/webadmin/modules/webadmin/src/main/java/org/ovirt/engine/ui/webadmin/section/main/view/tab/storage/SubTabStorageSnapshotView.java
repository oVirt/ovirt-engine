package org.ovirt.engine.ui.webadmin.section.main.view.tab.storage;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.uicommon.disks.DisksViewColumns;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageSnapshotListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStorageSnapshotPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;

import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;

public class SubTabStorageSnapshotView extends AbstractSubTabTableView<StorageDomain, Disk, StorageListModel, StorageSnapshotListModel>
        implements SubTabStorageSnapshotPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabStorageSnapshotView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public SubTabStorageSnapshotView(SearchableDetailModelProvider<Disk, StorageListModel,
            StorageSnapshotListModel> modelProvider) {
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
                DisksViewColumns.getSnapshotSizeColumn(null), constants.diskSnapshotSize(), true, "130px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                DisksViewColumns.getDateCreatedColumn(null), constants.creationDateDisk(), true, "130px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                DisksViewColumns.getSnapshotCreationDateColumn(null), constants.diskSnapshotCreationDate(), true, "130px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                DisksViewColumns.getAliasColumn(null), constants.diskSnapshotAlias(), true, "130px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                DisksViewColumns.getSnapshotDescriptionColumn(null), constants.diskSnapshotDescription(), true, "160px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                DisksViewColumns.getdiskContainersColumn(null), constants.attachedToDisk(), true, "130px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                DisksViewColumns.getStatusOnlyColumn(null), constants.statusDisk(), true, "80px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                DisksViewColumns.getDiskSnapshotIDColumn(null), constants.diskSnapshotIDDisk(),
                true, "260px"); //$NON-NLS-1$
    }
}
