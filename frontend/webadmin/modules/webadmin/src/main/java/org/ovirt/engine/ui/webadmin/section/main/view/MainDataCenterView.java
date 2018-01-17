package org.ovirt.engine.ui.webadmin.section.main.view;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.searchbackend.StoragePoolFieldAutoCompleter;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.presenter.FragmentParams;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractBooleanColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractEnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractLinkColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.MainDataCenterPresenter;
import org.ovirt.engine.ui.webadmin.widget.table.column.CommentColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.DcAdditionalStatusColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.DcStatusColumn;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.inject.Inject;

public class MainDataCenterView extends AbstractMainWithDetailsTableView<StoragePool, DataCenterListModel> implements MainDataCenterPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<MainDataCenterView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public MainDataCenterView(MainModelProvider<StoragePool, DataCenterListModel> modelProvider) {
        super(modelProvider);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable();
        initWidget(getTable());
    }

    void initTable() {
        getTable().enableColumnResizing();

        DcStatusColumn statusIconColumn = new DcStatusColumn();
        statusIconColumn.setContextMenuTitle(constants.statusIconDc());
        getTable().addColumn(statusIconColumn, constants.empty(), "30px"); //$NON-NLS-1$

        DcAdditionalStatusColumn additionalStatusColumn = new DcAdditionalStatusColumn();
        additionalStatusColumn.setContextMenuTitle(constants.additionalStatusDataCenter());
        getTable().addColumn(additionalStatusColumn, constants.empty(), "30px"); //$NON-NLS-1$

        AbstractTextColumn<StoragePool> nameColumn = new AbstractLinkColumn<StoragePool>(new FieldUpdater<StoragePool, String>() {

                @Override
                public void update(int index, StoragePool storagePool, String value) {
                    Map<String, String> parameters = new HashMap<>();
                    parameters.put(FragmentParams.NAME.getName(), storagePool.getName());
                    //The link was clicked, now fire an event to switch to details.
                    getPlaceTransitionHandler().handlePlaceTransition(
                            WebAdminApplicationPlaces.dataCenterStorageSubTabPlace, parameters);
                }
            }) {
            @Override
            public String getValue(StoragePool object) {
                return object.getName();
            }
        };
        nameColumn.makeSortable(StoragePoolFieldAutoCompleter.NAME);
        getTable().addColumn(nameColumn, constants.nameDc(), "150px"); //$NON-NLS-1$

        CommentColumn<StoragePool> commentColumn = new CommentColumn<>();
        getTable().addColumnWithHtmlHeader(commentColumn,
                SafeHtmlUtils.fromSafeConstant(constants.commentLabel()),
                "75px"); //$NON-NLS-1$

        AbstractTextColumn<StoragePool> storageTypeColumn = new AbstractBooleanColumn<StoragePool>(
                constants.storageTypeLocal(), constants.storageTypeShared()) {
            @Override
            protected Boolean getRawValue(StoragePool object) {
                return object.isLocal();
            }
        };
        storageTypeColumn.makeSortable(StoragePoolFieldAutoCompleter.LOCAL);
        getTable().addColumn(storageTypeColumn, constants.storgeTypeDc(), "150px"); //$NON-NLS-1$

        AbstractTextColumn<StoragePool> statusColumn = new AbstractEnumColumn<StoragePool, StoragePoolStatus>() {
            @Override
            public StoragePoolStatus getRawValue(StoragePool object) {
                return object.getStatus();
            }
        };
        statusColumn.makeSortable(StoragePoolFieldAutoCompleter.STATUS);
        getTable().addColumn(statusColumn, constants.statusDc(), "150px"); //$NON-NLS-1$

        AbstractTextColumn<StoragePool> versionColumn = new AbstractTextColumn<StoragePool>() {
            @Override
            public String getValue(StoragePool object) {
                return object.getCompatibilityVersion().getValue();
            }
        };
        versionColumn.makeSortable(StoragePoolFieldAutoCompleter.COMPATIBILITY_VERSION);
        getTable().addColumn(versionColumn, constants.comptVersDc(), "150px"); //$NON-NLS-1$

        AbstractTextColumn<StoragePool> descColumn = new AbstractTextColumn<StoragePool>() {
            @Override
            public String getValue(StoragePool object) {
                return object.getdescription();
            }
        };
        descColumn.makeSortable(StoragePoolFieldAutoCompleter.DESCRIPTION);
        getTable().addColumn(descColumn, constants.descriptionDc(), "300px"); //$NON-NLS-1$
    }
}
