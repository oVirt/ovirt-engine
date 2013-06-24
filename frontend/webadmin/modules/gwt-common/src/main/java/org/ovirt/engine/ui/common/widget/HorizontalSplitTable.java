package org.ovirt.engine.ui.common.widget;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;

public class HorizontalSplitTable extends Composite {

    interface WidgetUiBinder extends UiBinder<Widget, HorizontalSplitTable> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    interface Style extends CssResource {
    }

    private final MultiSelectionModel<EntityModel> topSelectionModel;
    private final MultiSelectionModel<EntityModel> bottomSelectionModel;

    @UiField
    protected Button downButton;

    @UiField
    protected Button upButton;

    @UiField(provided = true)
    protected EntityModelCellTable<ListModel> topTable;

    @UiField(provided = true)
    protected EntityModelCellTable<ListModel> bottomTable;

    @SuppressWarnings("unchecked")
    public HorizontalSplitTable(EntityModelCellTable<ListModel> topTable,
            EntityModelCellTable<ListModel> bottomTable,
            CommonApplicationConstants constants) {

        this.topTable = topTable;
        this.bottomTable = bottomTable;
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));

        topSelectionModel = (MultiSelectionModel<EntityModel>) topTable.getSelectionModel();
        bottomSelectionModel = (MultiSelectionModel<EntityModel>) bottomTable.getSelectionModel();

        downButton.setText(constants.horizontalSplitTableDown());
        upButton.setText(constants.horizontalSplitTableUp());
        downButton.setEnabled(false);
        upButton.setEnabled(false);

        addSelectionHandler(downButton);
        addSelectionHandler(upButton);
        addClickHandler(downButton);
        addClickHandler(upButton);
    }

    private void addSelectionHandler(final Button button) {
        final MultiSelectionModel<EntityModel> selectionModel =
                (button == downButton) ? topSelectionModel : bottomSelectionModel;
        selectionModel.addSelectionChangeHandler(new Handler() {

            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                button.setEnabled(!selectionModel.getSelectedSet().isEmpty());
            }
        });
    }

    private void addClickHandler(final Button button) {
        button.addClickHandler(new ClickHandler() {

            @SuppressWarnings("unchecked")
            @Override
            public void onClick(ClickEvent event) {
                MultiSelectionModel<EntityModel> sourceSelectionModel = getSelectionModelForButton(button);
                EntityModelCellTable<ListModel> sourceTable = (button == downButton) ? topTable : bottomTable;
                EntityModelCellTable<ListModel> targetTable = (button == downButton) ? bottomTable : topTable;

                Set<EntityModel> selectedItems = sourceSelectionModel.getSelectedSet();
                ((Collection<EntityModel>) sourceTable.flush().getItems()).removeAll(selectedItems);
                ListModel targetListModel = targetTable.flush();
                Collection<EntityModel> targetItems = (Collection<EntityModel>) targetListModel.getItems();
                if (targetItems == null) {
                    targetItems = new LinkedList<EntityModel>();
                    targetListModel.setItems(targetItems);
                }
                targetItems.addAll(selectedItems);
                refresh();
            }
        });
    }

    private MultiSelectionModel<EntityModel> getSelectionModelForButton(Button button) {
        return (button == downButton) ? topSelectionModel : bottomSelectionModel;
    }

    private void refresh() {
        topSelectionModel.clear();
        bottomSelectionModel.clear();
        topTable.edit(topTable.flush());
        bottomTable.edit(bottomTable.flush());
    }

}
