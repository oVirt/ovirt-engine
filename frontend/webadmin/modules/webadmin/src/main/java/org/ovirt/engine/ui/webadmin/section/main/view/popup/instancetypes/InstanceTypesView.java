package org.ovirt.engine.ui.webadmin.section.main.view.popup.instancetypes;

import org.gwtbootstrap3.client.ui.Container;
import org.ovirt.engine.core.common.businessentities.InstanceType;
import org.ovirt.engine.ui.common.MainTableResources;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.DetailTabModelProvider;
import org.ovirt.engine.ui.common.widget.table.SimpleActionTable;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.common.widget.uicommon.popup.instancetypes.InstanceTypeGeneralModelForm;
import org.ovirt.engine.ui.uicommonweb.models.configure.instancetypes.InstanceTypeGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.instancetypes.InstanceTypeListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.InstanceTypeModelProvider;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.DataGrid.Resources;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.inject.Inject;

public class InstanceTypesView extends Composite {

    interface ViewUiBinder extends UiBinder<Container, InstanceTypesView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    SplitLayoutPanel splitLayoutPanel;

    @UiField
    FlowPanel typePanel;

    @UiField
    FlowPanel detailsPanel;

    private SimpleActionTable<Void, InstanceType> typeTable;
    private InstanceTypeGeneralModelForm detailsWidget;

    private final InstanceTypeModelProvider instanceTypeModelProvider;
    private final DetailTabModelProvider<InstanceTypeListModel, InstanceTypeGeneralModel> instanceTypeGeneralModelProvider;

    private final EventBus eventBus;
    private final ClientStorage clientStorage;

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public InstanceTypesView(EventBus eventBus, ClientStorage clientStorage,
            InstanceTypeModelProvider instanceTypeModelProvider,
            InstanceTypesActionPanelPresenterWidget actionPanel,
            DetailTabModelProvider<InstanceTypeListModel, InstanceTypeGeneralModel> instanceTypeGeneralModelProvider) {
        this.eventBus = eventBus;
        this.clientStorage = clientStorage;

        this.instanceTypeModelProvider = instanceTypeModelProvider;
        this.instanceTypeGeneralModelProvider = instanceTypeGeneralModelProvider;

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        initMainTable(actionPanel);
        initDetailsWidget();

        setSubTabVisibility(false);
    }

    public void setSubTabVisibility(boolean visible) {
        splitLayoutPanel.clear();
        if (visible) {
            splitLayoutPanel.addSouth(detailsPanel, 150);
        }
        splitLayoutPanel.add(typePanel);
    }

    private void initMainTable(InstanceTypesActionPanelPresenterWidget actionPanel) {
        typeTable = new SimpleActionTable<>(instanceTypeModelProvider,
                getTableResources(), eventBus, clientStorage);

        AbstractTextColumn<InstanceType> nameColumn = new AbstractTextColumn<InstanceType>() {
            @Override
            public String getValue(InstanceType object) {
                return object.getName();
            }
        };
        typeTable.addColumn(nameColumn, constants.instanceTypeName(), "100px"); //$NON-NLS-1$

        typeTable.getSelectionModel().addSelectionChangeHandler(event -> {
            instanceTypeModelProvider.setSelectedItems(typeTable.getSelectionModel().getSelectedObjects());
            if (typeTable.getSelectionModel().getSelectedObjects().size() > 0) {
                setSubTabVisibility(true);
                detailsWidget.update();
            } else {
                setSubTabVisibility(false);
            }
        });

        typePanel.add(actionPanel);
        typePanel.add(typeTable);
    }

    private void initDetailsWidget() {
        detailsWidget = new InstanceTypeGeneralModelForm(instanceTypeGeneralModelProvider);

        detailsPanel.add(detailsWidget);
    }

    protected Resources getTableResources() {
        return GWT.create(MainTableResources.class);
    }

}
