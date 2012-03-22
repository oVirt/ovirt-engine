package org.ovirt.engine.ui.common.widget.uicommon.storage;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.QuotaEnforcmentTypeEnum;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.PopupSimpleTableResources;
import org.ovirt.engine.ui.common.widget.AbstractValidatedWidgetWithLabel;
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
import com.google.gwt.user.client.ui.SimplePanel;
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

    @UiField(provided = true)
    @Path(value = "quota.selectedItem")
    ListModelListBoxEditor<Object> singleQuotaEditor;

    @UiField
    FlowPanel singleStoragePanel;

    @UiField
    FlowPanel diskListPanel;

    @UiField
    SimplePanel diskListHeaderPanel;

    @Ignore
    EntityModelCellTable<ListModel> listHeader;

    boolean showVolumeType;
    boolean showSource;
    boolean showQuota;

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

        singleQuotaEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((storage_domains) object).getstorage_name();
            }
        });
    }

    void updateListHeader(DisksAllocationModel model) {
        listHeader = new EntityModelCellTable(false, (Resources) GWT.create(
                PopupSimpleTableResources.class), true);
        listHeader.addColumn(new EmptyColumn(), "Alias");
        listHeader.addColumn(new EmptyColumn(), "Size");

        if (showVolumeType)
            listHeader.addColumn(new EmptyColumn(), "Allocation", "125px");

        if (showSource)
            listHeader.addColumn(new EmptyColumn(), "Source", "125px");

        listHeader.addColumn(new EmptyColumn(), "Destination", "125px");

        if (showQuota)
            listHeader.addColumn(new EmptyColumn(), "Quota", "125px");

        listHeader.setRowData(new ArrayList());
        listHeader.setWidth("100%", true);

        diskListHeaderPanel.setWidget(listHeader);
    }

    void updateColumnsAvailability(DisksAllocationModel model) {
        setShowVolumeType(model.getIsVolumeFormatAvailable());
        setShowQuota(model.getQuotaEnforcementType() != QuotaEnforcmentTypeEnum.DISABLED);
        localize(constants);
    }

    void addStyles() {
        isSingleStorageEditor.addContentWidgetStyleName(style.isSingleStorageEditorContent());

        updateEditorStyle(singleStorageEditor);
        updateEditorStyle(singleQuotaEditor);
    }

    private void updateEditorStyle(AbstractValidatedWidgetWithLabel editor) {
        editor.addContentWidgetStyleName(style.editorContent());
        editor.addWrapperStyleName(style.editorWrapper());
        editor.setLabelStyleName(style.editorLabel());
    }

    void localize(CommonApplicationConstants constants) {
        isSingleStorageEditor.setLabel(!showQuota ? constants.singleDestinationStorage() :
                constants.singleDestinationStorage() + constants.singleQuota());
    }

    @Override
    public void edit(DisksAllocationModel model) {
        Driver.driver.edit(model);
        initListerners(model);
        InitStorageTable(model.getIsSingleDiskCopy());
        updateColumnsAvailability(model);
        updateListHeader(model);
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
                    else if ("QuotaEnforcmentType".equals(changedArgs.PropertyName)) {
                        updateColumnsAvailability(model);
                        updateListHeader(model);
                    }
                }
            }
        });
    }

    void addDiskList(DisksAllocationModel model) {
        diskListPanel.clear();

        for (final DiskModel diskModel : model.getDisks()) {
            DisksAllocationItemView disksAllocationItemView = new DisksAllocationItemView(constants);
            disksAllocationItemView.edit(diskModel);
            diskListPanel.add(disksAllocationItemView);
        }

        if (model.getIsSingleDiskMove() || model.getIsSingleDiskCopy()) {
            singleStoragePanel.setVisible(false);
            model.getIsSingleStorageDomain().setEntity(false);
        }
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

    public void setListWidth(String listWidth) {
        diskListPanel.setWidth(listWidth);
    }

    public void setShowVolumeType(boolean showVolumeType) {
        this.showVolumeType = showVolumeType;
    }

    public void setShowSource(boolean showSource) {
        this.showSource = showSource;
    }

    public void setShowQuota(boolean showQuota) {
        this.showQuota = showQuota;
    }

    public void setEnabled(boolean enabled) {
        isSingleStorageEditor.setEnabled(enabled);
        singleStorageEditor.setEnabled(enabled);
    }

    public void addDiskListPanelStyle(String style) {
        diskListPanel.addStyleName(style);
    }

    interface WidgetStyle extends CssResource {
        String isSingleStorageEditorContent();

        String editorLabel();

        String editorContent();

        String editorWrapper();
    }

}
