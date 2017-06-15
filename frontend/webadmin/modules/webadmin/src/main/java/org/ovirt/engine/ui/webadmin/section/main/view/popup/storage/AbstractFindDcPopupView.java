package org.ovirt.engine.ui.webadmin.section.main.view.popup.storage;

import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.table.column.AbstractEntityModelTextColumn;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;

public class AbstractFindDcPopupView extends AbstractModelBoundPopupView<ListModel> {

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, AbstractFindDcPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField(provided = true)
    EntityModelCellTable<ListModel> table;

    @UiField
    Label messageLabel;

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    public AbstractFindDcPopupView(EventBus eventBus, boolean multiSelection) {
        super(eventBus);
        table = new EntityModelCellTable<>(multiSelection);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));

        // Table Entity Columns
        table.addColumn(new AbstractEntityModelTextColumn<StoragePool>() {
            @Override
            public String getText(StoragePool storage) {
                return storage.getName();
            }
        }, constants.nameDc());

        table.addColumn(new AbstractEntityModelTextColumn<StoragePool>() {
            @Override
            protected String getText(StoragePool entity) {
                return entity.isLocal() ? constants.storageTypeLocal() : constants.storageTypeShared();
            }
        }
        , constants.storgeTypeDc());
    }

    @Override
    public void edit(ListModel object) {
        table.asEditor().edit(object);
    }

    @Override
    public void setMessage(String message) {
        super.setMessage(message);
        // Hide table in case of message
        if (message != null && message.length() > 0) {
            table.setVisible(false);
            messageLabel.setText(message);
        }
    }

    @Override
    public ListModel flush() {
        return table.asEditor().flush();
    }

    @Override
    public void cleanup() {
        // TODO clean up stuff if needed
    }
}
