package org.ovirt.engine.ui.webadmin.section.main.view;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.searchbackend.StorageDomainFieldAutoCompleter;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.presenter.FragmentParams;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractEnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractLinkColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractStorageSizeColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.uicompat.EnumTranslator;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.MainStoragePresenter;
import org.ovirt.engine.ui.webadmin.widget.table.column.CommentColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.StorageDomainAdditionalStatusColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.StorageDomainSharedStatusColumn;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.inject.Inject;

public class MainStorageView extends AbstractMainWithDetailsTableView<StorageDomain, StorageListModel> implements MainStoragePresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<MainStorageView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public MainStorageView(MainModelProvider<StorageDomain, StorageListModel> modelProvider) {
        super(modelProvider);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable();
        initWidget(getTable());
    }

    void initTable() {
        getTable().enableColumnResizing();

        StorageDomainSharedStatusColumn sharedStatusColumn = new StorageDomainSharedStatusColumn();
        sharedStatusColumn.setContextMenuTitle(constants.sharedStatusStorage());
        getTable().addColumn(sharedStatusColumn, constants.storageDomainStatus(), "45px"); //$NON-NLS-1$

        StorageDomainAdditionalStatusColumn additionalStatusColumn = new StorageDomainAdditionalStatusColumn();
        additionalStatusColumn.setContextMenuTitle(constants.additionalStatusStorage());
        getTable().addColumn(additionalStatusColumn, constants.empty(), "60px"); //$NON-NLS-1$

        AbstractTextColumn<StorageDomain> nameColumn = new AbstractLinkColumn<StorageDomain>(
                new FieldUpdater<StorageDomain, String>() {

            @Override
            public void update(int index, StorageDomain storageDomain, String value) {
                Map<String, String> parameters = new HashMap<>();
                parameters.put(FragmentParams.NAME.getName(), storageDomain.getName());
                //The link was clicked, now fire an event to switch to details.
                getPlaceTransitionHandler().handlePlaceTransition(
                        WebAdminApplicationPlaces.storageGeneralSubTabPlace, parameters);
            }

        }) {
            @Override
            public String getValue(StorageDomain object) {
                return object.getName();
            }
        };
        nameColumn.makeSortable(StorageDomainFieldAutoCompleter.NAME);
        getTable().addColumn(nameColumn, constants.domainNameStorage(), "150px"); //$NON-NLS-1$

        CommentColumn<StorageDomain> commentColumn = new CommentColumn<>();
        getTable().addColumnWithHtmlHeader(commentColumn,
                SafeHtmlUtils.fromSafeConstant(constants.commentLabel()),
                "75px"); //$NON-NLS-1$

        AbstractTextColumn<StorageDomain> domainTypeColumn = new AbstractEnumColumn<StorageDomain, StorageDomainType>() {
            @Override
            protected StorageDomainType getRawValue(StorageDomain object) {
                return object.getStorageDomainType();
            }
        };
        getTable().addColumn(domainTypeColumn, constants.domainTypeStorage(), "150px"); //$NON-NLS-1$

        AbstractTextColumn<StorageDomain> storageTypeColumn = new AbstractEnumColumn<StorageDomain, StorageType>() {
            @Override
            protected StorageType getRawValue(StorageDomain object) {
                return object.getStorageType();
            }
        };
        storageTypeColumn.makeSortable(StorageDomainFieldAutoCompleter.TYPE);
        getTable().addColumn(storageTypeColumn, constants.storageTypeStorage(), "150px"); //$NON-NLS-1$

        AbstractTextColumn<StorageDomain> formatColumn = new AbstractEnumColumn<StorageDomain, StorageFormatType>() {
            @Override
            protected StorageFormatType getRawValue(StorageDomain object) {
                return object.getStorageFormat();
            }
        };
        getTable().addColumn(formatColumn, constants.formatStorage(), "140px"); //$NON-NLS-1$

        AbstractTextColumn<StorageDomain> crossDataCenterStatusColumn =
                new AbstractTextColumn<StorageDomain>() {
                    @Override
                    public String getValue(StorageDomain object) {
                        if (object.getStorageDomainType() == StorageDomainType.ISO) {
                            return EnumTranslator.getInstance().translate(object.getStorageDomainSharedStatus());
                        } else {
                            return EnumTranslator.getInstance().translate(object.getStatus());
                        }
                    }
                };
        crossDataCenterStatusColumn.makeSortable(StorageDomainFieldAutoCompleter.STATUS);
        getTable().addColumn(crossDataCenterStatusColumn, constants.crossDcStatusStorage(), "210px"); //$NON-NLS-1$

        AbstractStorageSizeColumn<StorageDomain> totalSpaceColumn = new AbstractStorageSizeColumn<StorageDomain>() {
            @Override
            public Long getRawValue(StorageDomain object) {
                Integer totalSpace = object.getTotalDiskSize();
                return totalSpace == null ? null : Long.valueOf(totalSpace);
            }
        };
        getTable().addColumn(totalSpaceColumn, constants.totalSpaceStorage(), "130px"); //$NON-NLS-1$

        AbstractStorageSizeColumn<StorageDomain> freeSpaceColumn = new AbstractStorageSizeColumn<StorageDomain>() {
            @Override
            public Long getRawValue(StorageDomain object) {
                Integer availableDiskSize = object.getAvailableDiskSize();
                return availableDiskSize == null ? null : Long.valueOf(availableDiskSize);
            }
        };
        freeSpaceColumn.makeSortable(StorageDomainFieldAutoCompleter.FREE_SIZE);
        getTable().addColumn(freeSpaceColumn, constants.freeSpaceStorage(), "130px"); //$NON-NLS-1$

        AbstractStorageSizeColumn<StorageDomain> confirmedFreeSpaceColumn = new AbstractStorageSizeColumn<StorageDomain>() {
            @Override
            public Long getRawValue(StorageDomain object) {
                Integer confirmedAvailableSize = object.getConfirmedAvailableDiskSize();
                Long availableDiskSize = object.getAvailableDiskSize() == null ? null : Long.valueOf(object.getAvailableDiskSize());
                return confirmedAvailableSize == null ? availableDiskSize : Long.valueOf(confirmedAvailableSize);
            }

            @Override
            public SafeHtml getTooltip(StorageDomain object) {
                if (object.getConfirmedAvailableDiskSize() == null) {
                    return SafeHtmlUtils.fromString(constants.confirmedFreeSpaceStorageNonThinTooltip());
                }
                return SafeHtmlUtils.fromString(constants.confirmedFreeSpaceStorageThinTooltip());
            }
        };
        getTable().addColumn(confirmedFreeSpaceColumn, constants.confirmedFreeSpaceStorage(), "180px"); //$NON-NLS-1$

        AbstractTextColumn<StorageDomain> descriptionColumn = new AbstractTextColumn<StorageDomain>() {
            @Override
            public String getValue(StorageDomain object) {
                return object.getDescription();
            }
        };
        descriptionColumn.makeSortable(StorageDomainFieldAutoCompleter.DESCRIPTION);
        getTable().addColumn(descriptionColumn, constants.domainDescriptionStorage(), "200px"); //$NON-NLS-1$
    }

}
