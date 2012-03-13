package org.ovirt.engine.ui.common.widget.uicommon.storage;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.PopupSimpleTableResources;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.HasEditorDriver;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.IVdcQueryableCellTable;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.common.widget.table.column.EmptyColumn;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.DisksAllocationModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.DiskModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable.Resources;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class DisksAllocationView extends Composite implements HasEditorDriver<DisksAllocationModel> {

    interface Driver extends SimpleBeanEditorDriver<DisksAllocationModel, DisksAllocationView> {
        Driver driver = GWT.create(Driver.class);
    }

    interface ViewUiBinder extends UiBinder<Widget, DisksAllocationView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    WidgetStyle style;

    @UiField(provided = true)
    @Path(value = "isSingleStorageDomain.entity")
    EntityModelCheckBoxEditor isSingleStorageEditor;

    @UiField(provided = true)
    @Path(value = "storageDomain.selectedItem")
    ListModelListBoxEditor<Object> singleStorageEditor;

    @UiField
    FlowPanel singleStoragePanel;

    @UiField
    ScrollPanel diskListPanel;

    @UiField
    SimplePanel diskListHeaderPanel;

    @Ignore
    EntityModelCellTable<ListModel> listHeader;

    boolean showListHeader;
    boolean showVolumeType;

    @Ignore
    IVdcQueryableCellTable<storage_domains, ListModel> storageTable;

    CommonApplicationConstants constants;

    @UiConstructor
    public DisksAllocationView() {
    }

    public DisksAllocationView(CommonApplicationConstants constants) {
        this.constants = constants;
        initListBoxEditors();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        initListHeader();
        localize(constants);
        addStyles();
        Driver.driver.initialize(this);
    }

    void initListBoxEditors() {
        isSingleStorageEditor = new EntityModelCheckBoxEditor(Align.RIGHT);

        singleStorageEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((storage_domains) object).getstorage_name();
            }
        });
    }

    void initListHeader() {
        listHeader =
                new EntityModelCellTable(false, (Resources) GWT.create(PopupSimpleTableResources.class), true);
        listHeader.addColumn(new EmptyColumn(), "Alias", "100px");
        listHeader.addColumn(new EmptyColumn(), "Provisioned Size", "100px");
        listHeader.addColumn(new EmptyColumn(), "Source", "100px");
        listHeader.addColumn(new EmptyColumn(), "Destination", "110px");
        listHeader.setRowData(new ArrayList());
        listHeader.setWidth("100%", true);
    }

    void addStyles() {
        isSingleStorageEditor.addContentWidgetStyleName(style.isSingleStorageEditorContent());

        singleStorageEditor.setLabelStyleName(style.editorLabel());
        singleStorageEditor.addContentWidgetStyleName(style.editorContent());
        singleStorageEditor.addWrapperStyleName(style.editorWrapper());
    }

    void localize(CommonApplicationConstants constants) {
        isSingleStorageEditor.setLabel(constants.singleDestinationStorage());
    }

    @Override
    public void edit(DisksAllocationModel model) {
        Driver.driver.edit(model);
        initListerners(model);
        InitStorageTable(model.getIsSingleDiskCopy());

        if (showListHeader && !model.getIsSingleDiskMove() && !model.getIsSingleDiskCopy()) {
            diskListHeaderPanel.setWidget(listHeader);
        }
    }

    private void initListerners(final DisksAllocationModel model) {
        model.getPropertyChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                if (args instanceof PropertyChangedEventArgs) {
                    PropertyChangedEventArgs changedArgs = (PropertyChangedEventArgs) args;
                    if ("Disks".equals(changedArgs.PropertyName)) {
                        addDiskList(model);
                    }
                }
            }
        });
    }

    void addDiskList(DisksAllocationModel model) {
        VerticalPanel container = new VerticalPanel();
        container.setWidth("100%");

        if (model.getIsSingleDiskMove() || model.getIsSingleDiskCopy()) {
            singleStoragePanel.setVisible(false);
            storageTable.edit(model.getDisks().get(0).getStorageDomain());
            container.add(storageTable);
            diskListPanel.addStyleName(style.listPanelSingleDisk());
        } else {
            for (final DiskModel diskModel : model.getDisks()) {
                DisksAllocationItemView disksAllocationItemView = new DisksAllocationItemView(constants);
                disksAllocationItemView.edit(diskModel);
                container.add(disksAllocationItemView);
            }
            setListHeight("170px");
        }

        diskListPanel.clear();
        diskListPanel.add(container);
    }

    public void InitStorageTable(boolean multiSelection) {
        storageTable = new IVdcQueryableCellTable<storage_domains, ListModel>(multiSelection);

        // Table Entity Columns
        storageTable.addColumn(new TextColumnWithTooltip<storage_domains>() {
            @Override
            public String getValue(storage_domains storage) {
                return storage.getstorage_name();
            }
        }, "Name");

        storageTable.addColumn(new TextColumnWithTooltip<storage_domains>() {
            @Override
            public String getValue(storage_domains storage) {
                if (storage.getavailable_disk_size() == null || storage.getavailable_disk_size() < 1) {
                    return "< 1 GB";
                }
                return storage.getavailable_disk_size() + " GB";
            }
        }, "Free Space");

        storageTable.setWidth("100%", true);
    }

    @Override
    public DisksAllocationModel flush() {
        return Driver.driver.flush();
    }

    public void setListHeight(String listHeight) {
        diskListPanel.setHeight(listHeight);
    }

    public void setShowListHeader(boolean showListHeader) {
        this.showListHeader = showListHeader;
    }

    public void setShowVolumeType(boolean showVolumeType) {
        this.showVolumeType = showVolumeType;
    }

    public void setEnabled(boolean enabled) {
        isSingleStorageEditor.setEnabled(enabled);
        singleStorageEditor.setEnabled(enabled);
    }

    interface WidgetStyle extends CssResource {
        String isSingleStorageEditorContent();

        String editorLabel();

        String editorContent();

        String editorWrapper();

        String listPanelSingleDisk();
    }

}
