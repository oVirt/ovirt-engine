package org.ovirt.engine.ui.webadmin.section.main.view.popup.instancetypes;

import org.ovirt.engine.core.common.businessentities.InstanceType;
import org.ovirt.engine.ui.common.MainTableHeaderlessResources;
import org.ovirt.engine.ui.common.MainTableResources;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.DetailTabModelProvider;
import org.ovirt.engine.ui.common.widget.table.SimpleActionTable;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.common.widget.uicommon.popup.instancetypes.InstanceTypeGeneralModelForm;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.configure.instancetypes.InstanceTypeGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.instancetypes.InstanceTypeListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.InstanceTypeModelProvider;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable.Resources;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.inject.Inject;

public class InstanceTypesView extends Composite {

    interface ViewUiBinder extends UiBinder<FlowPanel, InstanceTypesView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    SimplePanel instanceTypesTabContent;

    private SimpleActionTable<InstanceType> table;
    private InstanceTypeGeneralModelForm detailTable;
    private SplitLayoutPanel splitLayoutPanel;

    private final InstanceTypeModelProvider instanceTypeModelProvider;
    private final DetailTabModelProvider<InstanceTypeListModel, InstanceTypeGeneralModel> instanceTypeGeneralModelProvider;

    private final EventBus eventBus;

    private final ClientStorage clientStorage;

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public InstanceTypesView(InstanceTypeModelProvider instanceTypeModelProvider,
            EventBus eventBus, ClientStorage clientStorage,
            DetailTabModelProvider<InstanceTypeListModel, InstanceTypeGeneralModel> instanceTypeGeneralModelProvider) {
        this.instanceTypeModelProvider = instanceTypeModelProvider;
        this.eventBus = eventBus;
        this.clientStorage = clientStorage;
        this.instanceTypeGeneralModelProvider = instanceTypeGeneralModelProvider;
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));

        initSplitLayoutPanel();

        initMainTable();
        initSubtabTable();
    }

    private void initSplitLayoutPanel() {
        splitLayoutPanel = new SplitLayoutPanel();
        splitLayoutPanel.setHeight("100%"); //$NON-NLS-1$
        splitLayoutPanel.setWidth("100%"); //$NON-NLS-1$
        instanceTypesTabContent.add(splitLayoutPanel);
    }

    public void setSubTabVisibility(boolean visible) {
        splitLayoutPanel.clear();
        if (visible) {
            splitLayoutPanel.addSouth(detailTable, 150);
        }
        splitLayoutPanel.add(table);
    }

    private void initMainTable() {
        table = new SimpleActionTable<>(instanceTypeModelProvider,
                getTableHeaderlessResources(), getTableResources(), eventBus, clientStorage);

        AbstractTextColumn<InstanceType> nameColumn = new AbstractTextColumn<InstanceType>() {
            @Override
            public String getValue(InstanceType object) {
                return object.getName();
            }
        };
        table.addColumn(nameColumn, constants.instanceTypeName(), "100px"); //$NON-NLS-1$


        table.addActionButton(new WebAdminButtonDefinition<InstanceType>(constants.newInstanceType()) {
            @Override
            protected UICommand resolveCommand() {
                return instanceTypeModelProvider.getModel().getNewInstanceTypeCommand();
            }
        });

        table.addActionButton(new WebAdminButtonDefinition<InstanceType>(constants.editInstanceType()) {
            @Override
            protected UICommand resolveCommand() {
                return instanceTypeModelProvider.getModel().getEditInstanceTypeCommand();
            }
        });

        table.addActionButton(new WebAdminButtonDefinition<InstanceType>(constants.removeInstanceType()) {
            @Override
            protected UICommand resolveCommand() {
                return instanceTypeModelProvider.getModel().getDeleteInstanceTypeCommand();
            }
        });

        splitLayoutPanel.add(table);

        table.getSelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                instanceTypeModelProvider.setSelectedItems(table.getSelectionModel().getSelectedList());
                if (table.getSelectionModel().getSelectedList().size() > 0) {
                    setSubTabVisibility(true);
                    detailTable.update();
                } else {
                    setSubTabVisibility(false);
                }
            }
        });

    }

    private void initSubtabTable() {
        detailTable = new InstanceTypeGeneralModelForm(instanceTypeGeneralModelProvider);
    }

    protected Resources getTableHeaderlessResources() {
        return GWT.create(MainTableHeaderlessResources.class);
    }

    protected Resources getTableResources() {
        return GWT.create(MainTableResources.class);
    }

}
