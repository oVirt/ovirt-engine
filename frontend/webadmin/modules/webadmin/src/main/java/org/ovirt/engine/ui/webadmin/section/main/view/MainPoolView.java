package org.ovirt.engine.ui.webadmin.section.main.view;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.businessentities.VmPoolType;
import org.ovirt.engine.core.searchbackend.PoolConditionFieldAutoCompleter;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.presenter.FragmentParams;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractEnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractLinkColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.MainPoolPresenter;
import org.ovirt.engine.ui.webadmin.widget.table.column.CommentColumn;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.inject.Inject;

public class MainPoolView extends AbstractMainWithDetailsTableView<VmPool, PoolListModel> implements MainPoolPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<MainPoolView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public MainPoolView(MainModelProvider<VmPool, PoolListModel> modelProvider) {
        super(modelProvider);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable();
        initWidget(getTable());
    }

    void initTable() {
        getTable().enableColumnResizing();

        AbstractTextColumn<VmPool> nameColumn = new AbstractLinkColumn<VmPool>(new FieldUpdater<VmPool, String>() {

            @Override
            public void update(int index, VmPool pool, String value) {
                Map<String, String> parameters = new HashMap<>();
                parameters.put(FragmentParams.NAME.getName(), pool.getName());
                //The link was clicked, now fire an event to switch to details.
                getPlaceTransitionHandler().handlePlaceTransition(
                        WebAdminApplicationPlaces.poolGeneralSubTabPlace, parameters);
            }

        }) {
            @Override
            public String getValue(VmPool object) {
                return object.getName();
            }
        };
        nameColumn.makeSortable(PoolConditionFieldAutoCompleter.NAME);
        getTable().addColumn(nameColumn, constants.namePool(), "150px"); //$NON-NLS-1$

        CommentColumn<VmPool> commentColumn = new CommentColumn<>();
        getTable().addColumnWithHtmlHeader(commentColumn,
                SafeHtmlUtils.fromSafeConstant(constants.commentLabel()),
                "75px"); //$NON-NLS-1$

        AbstractTextColumn<VmPool> assignedColumn = new AbstractLinkColumn<VmPool>(new FieldUpdater<VmPool, String>() {
            @Override
            public void update(int index, VmPool pool, String value) {
                Map<String, String> parameters = new HashMap<>();
                parameters.put(FragmentParams.SEARCH.getName(), "pool=" + pool.getName()); //$NON-NLS-1$
                getPlaceTransitionHandler().handlePlaceTransition(
                        WebAdminApplicationPlaces.virtualMachineMainPlace, parameters);
            }
        }) {
            @Override
            public String getValue(VmPool object) {
                return Integer.toString(object.getAssignedVmsCount());
            }
        };

        assignedColumn.makeSortable(PoolConditionFieldAutoCompleter.ASSIGNED_VM_COUNT);
        getTable().addColumn(assignedColumn, constants.assignVmsPool(), "150px"); //$NON-NLS-1$

        AbstractTextColumn<VmPool> runningColumn = new AbstractLinkColumn<VmPool>(new FieldUpdater<VmPool, String>() {
            @Override
            public void update(int index, VmPool pool, String value) {
                Map<String, String> parameters = new HashMap<>();
                parameters.put(FragmentParams.SEARCH.getName(), "pool=" + pool.getName() //$NON-NLS-1$
                        + " AND status=up"); //$NON-NLS-1$
                getPlaceTransitionHandler().handlePlaceTransition(
                        WebAdminApplicationPlaces.virtualMachineMainPlace, parameters);
            }
        }) {
            @Override
            public String getValue(VmPool object) {
                return Integer.toString(object.getRunningVmsCount());
            }
        };
        runningColumn.makeSortable(PoolConditionFieldAutoCompleter.RUNNING_VM_COUNT);
        getTable().addColumn(runningColumn, constants.runningVmsPool(), "150px"); //$NON-NLS-1$

        AbstractTextColumn<VmPool> typeColumn = new AbstractEnumColumn<VmPool, VmPoolType>() {
            @Override
            public VmPoolType getRawValue(VmPool object) {
                return object.getVmPoolType();
            }
        };
        typeColumn.makeSortable(PoolConditionFieldAutoCompleter.TYPE);
        getTable().addColumn(typeColumn, constants.typePool(), "150px"); //$NON-NLS-1$

        AbstractTextColumn<VmPool> descColumn = new AbstractTextColumn<VmPool>() {
            @Override
            public String getValue(VmPool object) {
                return object.getVmPoolDescription();
            }
        };
        descColumn.makeSortable(PoolConditionFieldAutoCompleter.DESCRIPTION);
        getTable().addColumn(descColumn, constants.descriptionPool(), "300px"); //$NON-NLS-1$
    }

}
