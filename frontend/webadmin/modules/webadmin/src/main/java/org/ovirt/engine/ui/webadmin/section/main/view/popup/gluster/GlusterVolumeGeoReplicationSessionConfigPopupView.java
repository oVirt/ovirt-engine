package org.ovirt.engine.ui.webadmin.section.main.view.popup.gluster;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSessionConfiguration;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.table.column.AbstractCheckboxColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractEntityModelTextColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractScrollableTextColumn;
import org.ovirt.engine.ui.common.widget.table.column.GlusterConfigAwareColumn;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.GlusterVolumeGeoReplicationSessionConfigModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.GlusterVolumeGeoReplicationSessionConfigPopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.view.client.NoSelectionModel;
import com.google.inject.Inject;

public class GlusterVolumeGeoReplicationSessionConfigPopupView extends AbstractModelBoundPopupView<GlusterVolumeGeoReplicationSessionConfigModel> implements GlusterVolumeGeoReplicationSessionConfigPopupPresenterWidget.ViewDef {

    interface Driver extends UiCommonEditorDriver<GlusterVolumeGeoReplicationSessionConfigModel, GlusterVolumeGeoReplicationSessionConfigPopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, GlusterVolumeGeoReplicationSessionConfigPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<GlusterVolumeGeoReplicationSessionConfigPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField(provided = true)
    @Ignore
    @WithElementId
    EntityModelCellTable<ListModel<EntityModel<Pair<Boolean, GlusterGeoRepSessionConfiguration>>>> geoReplicationConfigTable;

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    private final Driver driver = GWT.create(Driver.class);

    @Inject
    public GlusterVolumeGeoReplicationSessionConfigPopupView(EventBus eventBus) {
        super(eventBus);
        initConfigTable();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        driver.initialize(this);
    }

    private void initConfigTable() {
        geoReplicationConfigTable = new EntityModelCellTable<>(false, true);
        geoReplicationConfigTable.setSelectionModel(new NoSelectionModel());

        geoReplicationConfigTable.addColumn(new AbstractEntityModelTextColumn<Pair<Boolean, GlusterGeoRepSessionConfiguration>>() {
            @Override
            protected String getText(Pair<Boolean, GlusterGeoRepSessionConfiguration> entity) {
                return entity.getSecond().getKey();
            }
        },
                constants.optionKeyVolumeParameter(), "150px");//$NON-NLS-1$

        geoReplicationConfigTable.addColumn(new AbstractScrollableTextColumn<EntityModel<Pair<Boolean, GlusterGeoRepSessionConfiguration>>>() {
            @Override
            public String getValue(EntityModel<Pair<Boolean, GlusterGeoRepSessionConfiguration>> object) {
                return object.getEntity().getSecond().getDescription() == null ? constants.notAvailableLabel()
                        : object.getEntity().getSecond().getDescription();
            }
        },
                constants.descriptionVolumeParameter(),
                "300px");//$NON-NLS-1$

        geoReplicationConfigTable.addColumn(new GlusterConfigAwareColumn(),
                constants.optionValueVolumeParameter(),
                "200px");//$NON-NLS-1$

        geoReplicationConfigTable.addColumn(new AbstractCheckboxColumn<EntityModel<Pair<Boolean, GlusterGeoRepSessionConfiguration>>>(true,
                (index, object, value) -> object.getEntity().setFirst(value)) {

            @Override
            protected boolean canEdit(EntityModel<Pair<Boolean, GlusterGeoRepSessionConfiguration>> object) {
                return true;
            }

            @Override
            public Boolean getValue(EntityModel<Pair<Boolean, GlusterGeoRepSessionConfiguration>> object) {
                return object.getEntity().getFirst();
            }
        }, constants.resetGeoRepSessionConfig());
    }

    @Override
    public void edit(GlusterVolumeGeoReplicationSessionConfigModel object) {
        driver.edit(object);
        geoReplicationConfigTable.asEditor().edit(object.getConfigsModel());
    }

    @Override
    public GlusterVolumeGeoReplicationSessionConfigModel flush() {
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }
}
