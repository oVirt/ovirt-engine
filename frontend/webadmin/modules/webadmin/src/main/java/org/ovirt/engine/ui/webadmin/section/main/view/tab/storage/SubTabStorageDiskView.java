package org.ovirt.engine.ui.webadmin.section.main.view.tab.storage;

import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.uicommon.disks.DisksViewColumns;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageDiskListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStorageDiskPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.storage.SubTabStorageDataCenterView.ViewIdHandler;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;

public class SubTabStorageDiskView extends AbstractSubTabTableView<StorageDomain, Disk, StorageListModel, StorageDiskListModel>
        implements SubTabStorageDiskPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabStorageDiskView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public SubTabStorageDiskView(SearchableDetailModelProvider<Disk, StorageListModel, StorageDiskListModel> modelProvider,
            ApplicationResources resources,
            ApplicationConstants constants) {
        super(modelProvider);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable(resources, constants);
        initWidget(getTable());
    }

    void initTable(ApplicationResources resources, final ApplicationConstants constants) {
        getTable().enableColumnResizing();

        getTable().ensureColumnPresent(
                DisksViewColumns.aliasColumn, constants.aliasDisk(), true, "90px"); //$NON-NLS-1$

        getTable().ensureColumnPresent(
                DisksViewColumns.bootableDiskColumn,
                DisksViewColumns.bootableDiskColumn.getHeaderHtml(), true, "30px"); //$NON-NLS-1$

        getTable().ensureColumnPresent(
                DisksViewColumns.shareableDiskColumn,
                DisksViewColumns.shareableDiskColumn.getHeaderHtml(), true, "30px"); //$NON-NLS-1$

        getTable().ensureColumnPresent(
                DisksViewColumns.sizeColumn, constants.provisionedSizeDisk(), true, "100px"); //$NON-NLS-1$

        getTable().ensureColumnPresent(
                DisksViewColumns.actualSizeColumn, constants.sizeDisk(), true, "130px"); //$NON-NLS-1$

        getTable().ensureColumnPresent(
                DisksViewColumns.allocationColumn, constants.allocationDisk(), true, "130px"); //$NON-NLS-1$

        getTable().ensureColumnPresent(
                DisksViewColumns.storageDomainsColumn, constants.storageDomainDisk(), true, "170px"); //$NON-NLS-1$

        getTable().ensureColumnPresent(
                DisksViewColumns.dateCreatedColumn, constants.creationDateDisk(), true, "150px"); //$NON-NLS-1$

        getTable().ensureColumnPresent(
                DisksViewColumns.diskContainersIconColumn, "", true, "30px"); //$NON-NLS-1$ //$NON-NLS-2$

        getTable().ensureColumnPresent(
                DisksViewColumns.diskContainersColumn, constants.attachedToDisk(), true, "120px"); //$NON-NLS-1$

        getTable().ensureColumnPresent(
                DisksViewColumns.diskContainersColumn, constants.diskAlignment(), true, "120px"); //$NON-NLS-1$

        getTable().ensureColumnPresent(
                DisksViewColumns.statusColumn, constants.statusDisk(), true, "100px"); //$NON-NLS-1$

        getTable().ensureColumnPresent(
                DisksViewColumns.descriptionColumn, constants.descriptionDisk(), true, "100px"); //$NON-NLS-1$

        getTable().addActionButton(new WebAdminButtonDefinition<Disk>(constants.removeDisk()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getRemoveCommand();
            }
        });
    }
}
