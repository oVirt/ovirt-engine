package org.ovirt.engine.ui.webadmin.section.main.view.tab.datacenter;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.utils.SizeConverter;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.presenter.FragmentParams;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractDiskSizeColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractEnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractLinkColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterStorageListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter.SubTabDataCenterStoragePresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.widget.table.column.StorageDomainAdditionalStatusColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.StorageDomainStatusColumn;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;

public class SubTabDataCenterStorageView extends AbstractSubTabTableView<StoragePool, StorageDomain, DataCenterListModel, DataCenterStorageListModel>
        implements SubTabDataCenterStoragePresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabDataCenterStorageView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public SubTabDataCenterStorageView(SearchableDetailModelProvider<StorageDomain, DataCenterListModel, DataCenterStorageListModel> modelProvider) {
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

        StorageDomainStatusColumn statusIconColumn = new StorageDomainStatusColumn();
        statusIconColumn.setContextMenuTitle(constants.statusIconStorage());
        getTable().addColumn(statusIconColumn, constants.empty(), "30px"); //$NON-NLS-1$

        StorageDomainAdditionalStatusColumn additionalStatusColumn = new StorageDomainAdditionalStatusColumn();
        additionalStatusColumn.setContextMenuTitle(constants.additionalStatusStorage());
        getTable().addColumn(additionalStatusColumn, constants.empty(), "60px"); //$NON-NLS-1$

        AbstractTextColumn<StorageDomain> nameColumn = new AbstractLinkColumn<StorageDomain>(
                new FieldUpdater<StorageDomain, String>() {
            @Override
            public void update(int index, StorageDomain sd, String value) {
                Map<String, String> parameters = new HashMap<>();
                parameters.put(FragmentParams.NAME.getName(), sd.getName());
                getPlaceTransitionHandler().handlePlaceTransition(
                        WebAdminApplicationPlaces.storageGeneralSubTabPlace, parameters);
            }
        }) {
            @Override
            public String getValue(StorageDomain object) {
                return object.getName();
            }
        };
        nameColumn.makeSortable();
        getTable().addColumn(nameColumn, constants.domainNameStorage(), "160px"); //$NON-NLS-1$

        AbstractTextColumn<StorageDomain> typeColumn = new AbstractEnumColumn<StorageDomain, StorageDomainType>() {
            @Override
            public StorageDomainType getRawValue(StorageDomain object) {
                return object.getStorageDomainType();
            }
        };
        typeColumn.makeSortable();
        getTable().addColumn(typeColumn, constants.domainTypeStorage(), "160px"); //$NON-NLS-1$

        AbstractTextColumn<StorageDomain> statusColumn = new AbstractEnumColumn<StorageDomain, StorageDomainStatus>() {
            @Override
            public StorageDomainStatus getRawValue(StorageDomain object) {
                return object.getStatus();
            }
        };
        statusColumn.makeSortable();
        getTable().addColumn(statusColumn, constants.statusStorage(), "160px"); //$NON-NLS-1$

        AbstractDiskSizeColumn<StorageDomain> freeColumn = new AbstractDiskSizeColumn<StorageDomain>(SizeConverter.SizeUnit.GiB) {
            @Override
            public Long getRawValue(StorageDomain object) {
                return object.getAvailableDiskSize() != null ? object.getAvailableDiskSize() : 0L;
            }
        };
        freeColumn.makeSortable();
        getTable().addColumn(freeColumn, constants.freeSpaceStorage(), "160px"); //$NON-NLS-1$

        AbstractDiskSizeColumn<StorageDomain> usedColumn = new AbstractDiskSizeColumn<StorageDomain>(SizeConverter.SizeUnit.GiB) {
            @Override
            public Long getRawValue(StorageDomain object) {
                return object.getUsedDiskSize() != null ? object.getUsedDiskSize() : 0L;
            }
        };
        usedColumn.makeSortable();
        getTable().addColumn(usedColumn, constants.usedSpaceStorage(), "160px"); //$NON-NLS-1$

        AbstractDiskSizeColumn<StorageDomain> totalColumn = new AbstractDiskSizeColumn<StorageDomain>(SizeConverter.SizeUnit.GiB) {
            @Override
            public Long getRawValue(StorageDomain object) {
                return object.getTotalDiskSize() != null ? object.getTotalDiskSize() : 0L;
            }
        };
        totalColumn.makeSortable();
        getTable().addColumn(totalColumn, constants.totalSpaceStorage(), "160px"); //$NON-NLS-1$

        AbstractTextColumn<StorageDomain> descriptionColumn = new AbstractTextColumn<StorageDomain>() {
            @Override
            public String getValue(StorageDomain object) {
                return object.getDescription();
            }
        };
        descriptionColumn.makeSortable();
        getTable().addColumn(descriptionColumn, constants.domainDescriptionStorage(), "160px"); //$NON-NLS-1$
    }
}
