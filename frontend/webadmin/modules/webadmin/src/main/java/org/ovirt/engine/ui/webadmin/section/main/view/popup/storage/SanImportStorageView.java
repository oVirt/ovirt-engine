package org.ovirt.engine.ui.webadmin.section.main.view.popup.storage;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.table.column.EntityModelEnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.EntityModelTextColumn;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.ImportSanStorageModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.SanStorageModelBase;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.CellTable.Resources;
import com.google.gwt.user.client.ui.Label;

public class SanImportStorageView extends AbstractSanStorageView {

    @UiField
    @Path(value = "error")
    Label errorMessage;

    EntityModelCellTable<ListModel> table;

    @Override
    protected void initLists(SanStorageModelBase object) {
        table = new EntityModelCellTable<ListModel>(false, (Resources) GWT.create(LunTableResources.class));

        table.addColumn(new EntityModelTextColumn<storage_domains>() {
            @Override
            public String getValue(storage_domains storage) {
                return storage.getstorage_name();
            }
        }, "Name");

        table.addColumn(new EntityModelEnumColumn<storage_domains, StorageFormatType>() {

            @Override
            protected StorageFormatType getRawValue(storage_domains storage) {
                return
                storage.getStorageStaticData().getStorageFormat();
            }
        }, "Format", "80px");

        table.setColumnWidth(table.getColumn(0), "20px");

        listPanel.add(table);
    }

    @Override
    public void edit(SanStorageModelBase object) {
        super.edit(object);

        table.setRowData(new ArrayList<EntityModel>());
        table.edit(((ImportSanStorageModel) object).getCandidatesList());
    }

    public interface LunTableResources extends CellTable.Resources {
        interface TableStyle extends CellTable.Style {
        }

        @Override
        @Source({ CellTable.Style.DEFAULT_CSS, "org/ovirt/engine/ui/webadmin/css/SanStorageListHeader.css" })
        TableStyle cellTableStyle();
    }
}
