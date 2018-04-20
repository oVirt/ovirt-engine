package org.ovirt.engine.ui.webadmin.section.main.view.tab.cluster;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServerService;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServiceStatus;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.common.view.AbstractSubTabFormView;
import org.ovirt.engine.ui.common.widget.UiCommandButton;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.renderer.EnumRenderer;
import org.ovirt.engine.ui.common.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.common.widget.table.column.AbstractEntityModelTextColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractEnumColumn;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterListModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterServiceModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster.SubTabClusterServicePresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;

public class SubTabClusterServiceView extends AbstractSubTabFormView<Cluster, ClusterListModel<Void>, ClusterServiceModel>
        implements SubTabClusterServicePresenter.ViewDef, Editor<ClusterServiceModel> {

    interface Driver extends UiCommonEditorDriver<ClusterServiceModel, SubTabClusterServiceView> {
    }

    interface ViewIdHandler extends ElementIdHandler<SubTabClusterServiceView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    interface ViewUiBinder extends UiBinder<Widget, SubTabClusterServiceView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField(provided = true)
    @Path(value = "hostList.selectedItem")
    @WithElementId("hostList")
    ListModelListBoxEditor<Object> hostEditor;

    @UiField(provided = true)
    @Path(value = "serviceTypeList.selectedItem")
    @WithElementId
    ListModelListBoxEditor<Object> serviceTypeEditor;

    @UiField
    @WithElementId
    UiCommandButton filterButton;

    @UiField
    @WithElementId
    UiCommandButton clearButton;

    @UiField(provided = true)
    @Ignore
    @WithElementId
    EntityModelCellTable<ListModel> servicesTable;

    private final Driver driver = GWT.create(Driver.class);

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public SubTabClusterServiceView(final DetailModelProvider<ClusterListModel<Void>, ClusterServiceModel> modelProvider) {
        super(modelProvider);
        servicesTable = new EntityModelCellTable<>(false, true);
        initListBoxEditors();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize();
        initTableColumns();
        initButtons();
        driver.initialize(this);
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    private void initListBoxEditors() {
        hostEditor = new ListModelListBoxEditor<>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                if (object != null) {
                    return ((VDS) object).getHostName();
                } else {
                    return ""; //$NON-NLS-1$
                }
            }
        });
        serviceTypeEditor = new ListModelListBoxEditor<>(new EnumRenderer() {
            @Override
            public String render(Enum object) {
                if (object != null) {
                    return super.render(object);
                } else {
                    return ""; //$NON-NLS-1$
                }
            }
        });
    }

    private void localize() {
        hostEditor.setLabel(constants.hostService());
        serviceTypeEditor.setLabel(constants.nameService());
        filterButton.setLabel(constants.filterService());
        clearButton.setLabel(constants.showAllService());
    }

    protected void initTableColumns() {
        // Table Entity Columns
        servicesTable.addColumn(new AbstractEntityModelTextColumn<GlusterServerService>() {
            @Override
            public String getText(GlusterServerService entity) {
                return entity.getHostName();
            }
        }, constants.hostService());

        servicesTable.addColumn(new AbstractEntityModelTextColumn<GlusterServerService>() {
            @Override
            public String getText(GlusterServerService entity) {
                return entity.getServiceType().name();
            }
        }, constants.nameService());

        servicesTable.addColumn(new AbstractEnumColumn<EntityModel, GlusterServiceStatus>() {
            @Override
            protected GlusterServiceStatus getRawValue(EntityModel object) {
                return ((GlusterServerService)object.getEntity()).getStatus();
            }
        }, constants.statusService());

        servicesTable.addColumn(new AbstractEntityModelTextColumn<GlusterServerService>() {
            @Override
            public String getText(GlusterServerService entity) {
                if (entity.getPort() != null && entity.getPort() > 0) {
                    return String.valueOf(entity.getPort());
                } else {
                    return constants.notAvailableLabel();
                }
            }
        }, constants.portService());

        servicesTable.addColumn(new AbstractEntityModelTextColumn<GlusterServerService>() {
            @Override
            public String getText(GlusterServerService entity) {
                if (entity.getRdmaPort() != null && entity.getRdmaPort() > 0) {
                    return String.valueOf(entity.getRdmaPort());
                } else {
                    return constants.notAvailableLabel();
                }
            }
        }, constants.rdmaPortService());

        servicesTable.addColumn(new AbstractEntityModelTextColumn<GlusterServerService>() {
            @Override
            public String getText(GlusterServerService entity) {
                return String.valueOf(entity.getPid());
            }
        }, constants.pidService());
    }

    private void initButtons() {
        filterButton.addClickHandler(event -> getDetailModel().executeCommand(getDetailModel().getFilterServicesCommand()));

        clearButton.addClickHandler(event -> getDetailModel().executeCommand(getDetailModel().getClearFilterServicesCommand()));
    }

    @Override
    public void setMainSelectedItem(Cluster selectedItem) {
        servicesTable.asEditor().edit(getDetailModel().getServiceList());
        driver.edit(getDetailModel());
    }

}
