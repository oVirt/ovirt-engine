package org.ovirt.engine.ui.webadmin.section.main.view.tab.storage;

import java.util.LinkedList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.searchbackend.DiskConditionFieldAutoCompleter;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.action.ActionButtonDefinition;
import org.ovirt.engine.ui.common.widget.table.column.AbstractColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractDiskSizeColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.common.widget.table.header.ImageResourceHeader;
import org.ovirt.engine.ui.common.widget.uicommon.disks.DisksViewColumns;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageDiskListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStorageDiskPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminMenuBarButtonDefinition;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.inject.Inject;

public class SubTabStorageDiskView extends AbstractSubTabTableView<StorageDomain, Disk, StorageListModel, StorageDiskListModel>
        implements SubTabStorageDiskPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabStorageDiskView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    private static AbstractTextColumn<Disk> aliasColumn;
    private static AbstractDiskSizeColumn<Disk> sizeColumn;
    private static AbstractDiskSizeColumn<Disk> actualSizeColumn;
    private static AbstractTextColumn<Disk> allocationColumn;
    private static AbstractTextColumn<Disk> dateCreatedColumn;
    private static AbstractColumn<Disk, Disk> statusColumn;
    private static AbstractTextColumn<Disk> typeColumn;
    private static AbstractTextColumn<Disk> cinderVolumeTypeColumn;
    private static AbstractTextColumn<Disk> descriptionColumn;

    @Inject
    public SubTabStorageDiskView(SearchableDetailModelProvider<Disk, StorageListModel, StorageDiskListModel> modelProvider) {
        super(modelProvider);
        initWidget(getTable());
        initTableColumns();
        initTableActionButtons();
    }

    @Override
    public void setMainTabSelectedItem(StorageDomain storageDomain) {
        initTable(storageDomain);
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    void initTable(StorageDomain storageDomain) {
        if (storageDomain == null) {
            return;
        }

        boolean isDataStorage = storageDomain.getStorageDomainType().isDataDomain();
        boolean isCinderStorage = storageDomain.getStorageType().isCinderDomain();

        getTable().enableColumnResizing();

        getTable().ensureColumnVisible(aliasColumn, constants.aliasDisk(), true, "90px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                DisksViewColumns.bootableDiskColumn,
                new ImageResourceHeader(DisksViewColumns.bootableDiskColumn.getDefaultImage(),
                        SafeHtmlUtils.fromSafeConstant(constants.bootableDisk())),
                true, "30px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                DisksViewColumns.shareableDiskColumn,
                new ImageResourceHeader(DisksViewColumns.shareableDiskColumn.getDefaultImage(),
                        SafeHtmlUtils.fromSafeConstant(constants.shareable())),
                true, "30px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(sizeColumn, constants.provisionedSizeDisk(), true, "100px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(actualSizeColumn, constants.sizeDisk(), isDataStorage, "130px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(allocationColumn, constants.allocationDisk(), isDataStorage, "130px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                DisksViewColumns.storageDomainsColumn, constants.storageDomainDisk(), true, "170px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(cinderVolumeTypeColumn, constants.cinderVolumeTypeDisk(), isCinderStorage,
                "90px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(dateCreatedColumn, constants.creationDateDisk(), true, "150px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                DisksViewColumns.diskContainersIconColumn, "", true, "30px"); //$NON-NLS-1$ //$NON-NLS-2$

        getTable().ensureColumnVisible(
                DisksViewColumns.diskContainersColumn, constants.attachedToDisk(), true, "120px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(
                DisksViewColumns.diskAlignmentColumn, constants.diskAlignment(), isDataStorage, "120px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(statusColumn, constants.statusDisk(), true, "100px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(typeColumn, constants.typeDisk(), true, "80px"); //$NON-NLS-1$

        getTable().ensureColumnVisible(descriptionColumn, constants.descriptionDisk(), true, "100px"); //$NON-NLS-1$
    }

    void initTableColumns() {
        getTable().enableColumnResizing();

        aliasColumn = DisksViewColumns.getAliasColumn(DiskConditionFieldAutoCompleter.ALIAS);
        sizeColumn = DisksViewColumns.getSizeColumn(DiskConditionFieldAutoCompleter.PROVISIONED_SIZE);
        actualSizeColumn = DisksViewColumns.getActualSizeColumn(null);
        allocationColumn = DisksViewColumns.getAllocationColumn(constants.empty());
        dateCreatedColumn = DisksViewColumns.getDateCreatedColumn(DiskConditionFieldAutoCompleter.CREATION_DATE);
        statusColumn = DisksViewColumns.getStatusColumn(DiskConditionFieldAutoCompleter.STATUS);
        typeColumn = DisksViewColumns.getDiskStorageTypeColumn(DiskConditionFieldAutoCompleter.DISK_TYPE);
        cinderVolumeTypeColumn = DisksViewColumns.getCinderVolumeTypeColumn(null);
        descriptionColumn = DisksViewColumns.getDescriptionColumn(DiskConditionFieldAutoCompleter.DESCRIPTION);
    }

    void initTableActionButtons() {
        getTable().addActionButton(new WebAdminButtonDefinition<Disk>(constants.removeDisk()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getRemoveCommand();
            }
        });

        // Upload operations drop down
        List<ActionButtonDefinition<Disk>> uploadActions = new LinkedList<>();
        uploadActions.add(new WebAdminButtonDefinition<Disk>(constants.uploadImageStart()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getUploadCommand();
            }
        });
        uploadActions.add(new WebAdminButtonDefinition<Disk>(constants.uploadImageCancel()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getCancelUploadCommand();
            }
        });
        uploadActions.add(new WebAdminButtonDefinition<Disk>(constants.uploadImagePause()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getPauseUploadCommand();
            }
        });
        uploadActions.add(new WebAdminButtonDefinition<Disk>(constants.uploadImageResume()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getResumeUploadCommand();
            }
        });
        getTable().addActionButton(new WebAdminMenuBarButtonDefinition<>(
                constants.uploadImage(), uploadActions));
    }
}
