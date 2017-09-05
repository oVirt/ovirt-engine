package org.ovirt.engine.ui.webadmin.section.main.view.popup.vm.register;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable.SelectionMode;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.renderer.NameRenderer;
import org.ovirt.engine.ui.common.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.common.widget.table.column.AbstractEntityModelTextColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractListModelListBoxColumn;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.register.VnicProfileMappingEntity;
import org.ovirt.engine.ui.uicommonweb.models.vms.register.VnicProfileMappingItem;
import org.ovirt.engine.ui.uicommonweb.models.vms.register.VnicProfileMappingModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.register.VnicProfileMappingPopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.inject.Inject;

public class VnicProfileMappingPopupView
        extends AbstractModelBoundPopupView<VnicProfileMappingModel>
        implements VnicProfileMappingPopupPresenterWidget.ViewDef {

    interface Driver extends UiCommonEditorDriver<VnicProfileMappingModel, VnicProfileMappingPopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, VnicProfileMappingPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<VnicProfileMappingPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final ApplicationConstants constants = AssetProvider.getConstants();
    private static final CommonApplicationMessages messages = AssetProvider.getMessages();

    @UiField(provided = true)
    @Ignore
    EntityModelCellTable<ListModel<VnicProfileMappingItem>> vnicProfileMappingTable;

    @UiField(provided = true)
    @Path("targetCluster.selectedItem")
    final ListModelListBoxEditor<Cluster> cluster;

    private final Driver driver = GWT.create(Driver.class);

    @Inject
    public VnicProfileMappingPopupView(EventBus eventBus) {
        super(eventBus);

        this.cluster = new ListModelListBoxEditor<>(new NameRenderer<Cluster>());

        initTable();

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));

        ViewIdHandler.idHandler.generateAndSetIds(this);

        driver.initialize(this);
    }

    private void initTable() {
        vnicProfileMappingTable = new EntityModelCellTable<>(SelectionMode.NONE);
        vnicProfileMappingTable.addColumn(new AbstractEntityModelTextColumn<VnicProfileMappingEntity>() {
            @Override
            protected String getText(VnicProfileMappingEntity model) {
                return model.getExternalNetworkName();
            }
        }, constants.externalLogicalNetwork());
        vnicProfileMappingTable.addColumn(new AbstractEntityModelTextColumn<VnicProfileMappingEntity>() {
            @Override
            protected String getText(VnicProfileMappingEntity model) {
                return model.getExternalNetworkProfileName();
            }
        }, constants.externalVnicProfile());

        final NullSafeRenderer<VnicProfileView> vnicProfileRenderer = new NullSafeRenderer<VnicProfileView>() {
            @Override
            protected String renderNullSafe(VnicProfileView profile) {
                return (profile == VnicProfileView.EMPTY) ? messages.emptyProfile().asString()
                        : messages.profileAndNetworkSelected(profile.getName(), profile.getNetworkName());
            }
        };
        vnicProfileMappingTable.addColumn(
                new AbstractListModelListBoxColumn<VnicProfileMappingItem, VnicProfileView>(vnicProfileRenderer) {
                    @Override
                    public ListModel getValue(VnicProfileMappingItem object) {
                        return object.getTargetVnicProfile();
                    }
                },
                constants.targetVnicProfile());
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }

    private void refreshMappingsTable(ListModel<VnicProfileMappingItem> mappingModelRows) {
        vnicProfileMappingTable.asEditor().edit(mappingModelRows);
    }

    @Override
    public void edit(final VnicProfileMappingModel model) {

        driver.edit(model);

        refreshMappingsTable(model.getMappingModelRows());

        model.getMappingModelRows().getItemsChangedEvent().addListener((ev, sender, args) -> refreshMappingsTable(model.getMappingModelRows()));
        cluster.setLabel(constants.importVm_destCluster());
    }

    @Override
    public VnicProfileMappingModel flush() {
        return driver.flush();
    }
}
