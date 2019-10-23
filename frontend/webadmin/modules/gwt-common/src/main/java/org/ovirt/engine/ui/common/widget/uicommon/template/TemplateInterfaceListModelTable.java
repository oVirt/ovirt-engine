package org.ovirt.engine.ui.common.widget.uicommon.template;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.presenter.FragmentParams;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableTableModelProvider;
import org.ovirt.engine.ui.common.widget.action.TemplateInterfaceActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.widget.table.column.AbstractBooleanColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractCheckboxColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractEnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractLinkColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.common.widget.table.column.NicActivateStatusColumn;
import org.ovirt.engine.ui.common.widget.uicommon.AbstractModelBoundTableWidget;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateInterfaceListModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.event.shared.EventBus;

public class TemplateInterfaceListModelTable extends AbstractModelBoundTableWidget<VmTemplate, VmNetworkInterface, TemplateInterfaceListModel> {

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();

    private final TemplateListModel templateListModel;

    public TemplateInterfaceListModelTable(
            SearchableTableModelProvider<VmNetworkInterface, TemplateInterfaceListModel> modelProvider,
            EventBus eventBus, TemplateInterfaceActionPanelPresenterWidget actionPanel, ClientStorage clientStorage,
            TemplateListModel templateListModel) {
        super(modelProvider, eventBus, actionPanel, clientStorage, false);
        this.templateListModel = templateListModel;
    }

    @Override
    public void initTable() {
        NicActivateStatusColumn<VmNetworkInterface> statusColumn = new NicActivateStatusColumn<>();
        statusColumn.setContextMenuTitle(constants.vnicStatusNetworkVM());
        getTable().addColumn(statusColumn, constants.empty(), "30px"); //$NON-NLS-1$

        AbstractTextColumn<VmNetworkInterface> nameColumn = new AbstractTextColumn<VmNetworkInterface>() {
            @Override
            public String getValue(VmNetworkInterface object) {
                return object.getName();
            }
        };
        getTable().addColumn(nameColumn, constants.nameInterface(), "200px"); //$NON-NLS-1$

        AbstractCheckboxColumn<VmNetworkInterface> pluggedColumn = new AbstractCheckboxColumn<VmNetworkInterface>() {
            @Override
            public Boolean getValue(VmNetworkInterface object) {
                return object.isPlugged();
            }

            @Override
            protected boolean canEdit(VmNetworkInterface object) {
                return false;
            }
        };

        getTable().addColumn(pluggedColumn, constants.plugged(), "90px"); //$NON-NLS-1$

        AbstractTextColumn<VmNetworkInterface> networkNameColumn = new AbstractLinkColumn<VmNetworkInterface>(
                new FieldUpdater<VmNetworkInterface, String>() {
            @Override
            public void update(int index, VmNetworkInterface networkInterface, String value) {
                Map<String, String> parameters = new HashMap<>();
                parameters.put(FragmentParams.NAME.getName(), networkInterface.getNetworkName());
                parameters.put(FragmentParams.DATACENTER.getName(),
                        templateListModel.getSelectedItem().getStoragePoolName());
                getPlaceTransitionHandler().handlePlaceTransition(
                        WebAdminApplicationPlaces.networkGeneralSubTabPlace, parameters);
            }
        }) {
            @Override
            public String getValue(VmNetworkInterface object) {
                return object.getNetworkName();
            }
        };

        getTable().addColumn(networkNameColumn, constants.networkNameInterface(), "200px"); //$NON-NLS-1$

        AbstractTextColumn<VmNetworkInterface> profileNameColumn = new AbstractLinkColumn<VmNetworkInterface>(
                new FieldUpdater<VmNetworkInterface, String>() {
            @Override
            public void update(int index, VmNetworkInterface networkInterface, String value) {
                Map<String, String> parameters = new HashMap<>();
                parameters.put(FragmentParams.NAME.getName(), networkInterface.getVnicProfileName());
                parameters.put(FragmentParams.NETWORK.getName(), networkInterface.getNetworkName());
                parameters.put(FragmentParams.DATACENTER.getName(),
                        templateListModel.getSelectedItem().getStoragePoolName());
                getPlaceTransitionHandler().handlePlaceTransition(
                        WebAdminApplicationPlaces.vnicProfileVmSubTabPlace, parameters);
            }
        }) {
            @Override
            public String getValue(VmNetworkInterface object) {
                return object.getVnicProfileName();
            }
        };
        getTable().addColumn(profileNameColumn, constants.profileNameInterface(), "150px"); //$NON-NLS-1$

        AbstractBooleanColumn<VmNetworkInterface> linkStateColumn =
                new AbstractBooleanColumn<VmNetworkInterface>(constants.linkedNetworkInterface(),
                        constants.unlinkedNetworkInterface()) {
                    @Override
                    protected Boolean getRawValue(VmNetworkInterface object) {
                        return object.isLinked();
                    }
                };

        getTable().addColumn(linkStateColumn, constants.linkStateNetworkInterface(), "90px"); //$NON-NLS-1$

        AbstractTextColumn<VmNetworkInterface> typeColumn = new AbstractEnumColumn<VmNetworkInterface, VmInterfaceType>() {
            @Override
            protected VmInterfaceType getRawValue(VmNetworkInterface object) {
                return VmInterfaceType.forValue(object.getType());
            }
        };
        getTable().addColumn(typeColumn, constants.typeInterface(), "100px"); //$NON-NLS-1$
    }

}
