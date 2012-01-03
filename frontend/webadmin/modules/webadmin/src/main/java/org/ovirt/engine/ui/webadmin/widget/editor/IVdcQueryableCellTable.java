package org.ovirt.engine.ui.webadmin.widget.editor;

import java.util.ArrayList;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.webadmin.widget.HasEditorDriver;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;

public class IVdcQueryableCellTable<IVdcQueryable, M extends ListModel> extends CellTable<IVdcQueryable> implements HasEditorDriver<M> {

    private static final int DEFAULT_PAGESIZE = 1000;

    private M listModel;

    public IVdcQueryableCellTable() {
        super(DEFAULT_PAGESIZE, (Resources) GWT.create(IVdcQueryableCellTableResources.class));

        SingleSelectionModel<IVdcQueryable> selectionModel = new SingleSelectionModel<IVdcQueryable>();
        setSelectionModel(selectionModel);

        getSelectionModel().addSelectionChangeHandler(new Handler() {

            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                listModel.setSelectedItem(((SingleSelectionModel) getSelectionModel()).getSelectedObject());
            }
        });
    }

    @Override
    public void addColumn(Column<IVdcQueryable, ?> column, String headerText, String width) {
        addColumn(column, headerText);
        setColumnWidth(column, width);
    }

    public void addColumnAt(Column<IVdcQueryable, ?> column, String headerText, String width, int position) {
        insertColumn(position, column, headerText);
        setColumnWidth(column, width);
    }

    @Override
    public void edit(M object) {
        this.listModel = object;
        if (listModel.getItems() != null) {
            if (listModel.getItems() != null) {
                setRowData((ArrayList<IVdcQueryable>) listModel.getItems());
            }
            else {
                setRowData(new ArrayList<IVdcQueryable>());
            }
        }
        listModel.getItemsChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                if (listModel.getItems() != null) {
                    setRowData((ArrayList<IVdcQueryable>) listModel.getItems());
                }
                else {
                    setRowData(new ArrayList<IVdcQueryable>());
                }

            }
        });
        listModel.getSelectedItemChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {

            }
        });
    }

    @Override
    public M flush() {
        return listModel;
    }

    public interface IVdcQueryableCellTableResources extends CellTable.Resources {
        interface TableStyle extends CellTable.Style {
        }

        @Override
        @Source({ CellTable.Style.DEFAULT_CSS, "org/ovirt/engine/ui/webadmin/css/PopupCellTable.css" })
        TableStyle cellTableStyle();
    }

}
