package org.ovirt.engine.ui.webadmin.section.main.view.popup.cluster;

import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.table.column.AbstractEntityModelTextColumn;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.cluster.AddDataCenterClusterPopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.inject.Inject;

public class AddDataCenterClusterPopupView extends AbstractModelBoundPopupView<ListModel<EntityModel<StoragePool>>>
    implements AddDataCenterClusterPopupPresenterWidget.ViewDef {

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, AddDataCenterClusterPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @UiField(provided = true)
    EntityModelCellTable<ListModel<EntityModel<StoragePool>>> dataCentersTable;

    @Inject
    public AddDataCenterClusterPopupView(EventBus eventBus) {
        super(eventBus);
        dataCentersTable = new EntityModelCellTable<>(false, false);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        initTableColumns();
    }

    private void initTableColumns() {
        dataCentersTable.addColumn(new AbstractEntityModelTextColumn<StoragePool>() {
            @Override
            public String getText(StoragePool entity) {
                return entity.getName();
            }
        }, constants.dataCenter());

        dataCentersTable.addColumn(new AbstractEntityModelTextColumn<StoragePool>() {
            @Override
            public String getText(StoragePool entity) {
                return entity.getCompatibilityVersion().getValue();
            }
        }, constants.comptVersDc());
    }

    @Override
    public void edit(ListModel<EntityModel<StoragePool>> object) {
        dataCentersTable.asEditor().edit(object);
    }

    @Override
    public ListModel<EntityModel<StoragePool>> flush() {
        return dataCentersTable.asEditor().flush();
    }

    @Override
    public void cleanup() {
        // TODO clean up stuff if needed
    }
}
