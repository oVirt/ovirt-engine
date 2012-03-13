package org.ovirt.engine.ui.common.widget.uicommon.popup.vm;

import org.ovirt.engine.core.common.businessentities.DiskImageBase;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.renderer.EnumRenderer;
import org.ovirt.engine.ui.common.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractModelBoundPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.DiskModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

public class VmDiskPopupWidget extends AbstractModelBoundPopupWidget<DiskModel> {

    interface Driver extends SimpleBeanEditorDriver<DiskModel, VmDiskPopupWidget> {
        Driver driver = GWT.create(Driver.class);
    }

    interface ViewUiBinder extends UiBinder<FlowPanel, VmDiskPopupWidget> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    @Path("size.entity")
    EntityModelTextBoxEditor sizeEditor;

    @UiField(provided = true)
    @Path("storageDomain.selectedItem")
    ListModelListBoxEditor<Object> storageDomainEditor;

    @UiField(provided = true)
    @Path("preset.selectedItem")
    ListModelListBoxEditor<Object> presetEditor;

    @UiField(provided = true)
    @Path("interface.selectedItem")
    ListModelListBoxEditor<Object> interfaceEditor;

    @UiField(provided = true)
    @Path("volumeType.selectedItem")
    ListModelListBoxEditor<Object> volumeTypeEditor;

    @UiField(provided = true)
    @Path("wipeAfterDelete.entity")
    EntityModelCheckBoxEditor wipeAfterDeleteEditor;

    @UiField(provided = true)
    @Path("isBootable.entity")
    EntityModelCheckBoxEditor isBootableEditor;

    @UiField(provided = true)
    @Path("isPlugged.entity")
    EntityModelCheckBoxEditor isPluggedEditor;

    @UiField
    Label message;

    public VmDiskPopupWidget(CommonApplicationConstants constants) {
        initManualWidgets();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize(constants);
        Driver.driver.initialize(this);
    }

    // TODO: Localize
    private void localize(CommonApplicationConstants constants) {
        sizeEditor.setLabel("Size(GB)");
        storageDomainEditor.setLabel("Storage Domain");
        presetEditor.setLabel("Disk Type");
        interfaceEditor.setLabel("Interface");
        volumeTypeEditor.setLabel("Format");
        wipeAfterDeleteEditor.setLabel("Wipe after delete");
        isBootableEditor.setLabel("Is bootable");
        isPluggedEditor.setLabel("Activate");
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void initManualWidgets() {
        storageDomainEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((storage_domains) object).getstorage_name();
            }
        });

        presetEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((DiskImageBase) object).getdisk_type().name();
            }
        });

        interfaceEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return object.toString();
            }
        });

        volumeTypeEditor = new ListModelListBoxEditor<Object>(new EnumRenderer());

        wipeAfterDeleteEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        isBootableEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        isPluggedEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
    }

    @Override
    public void focusInput() {
        sizeEditor.setFocus(true);
    }

    @Override
    public void edit(DiskModel disk) {
        Driver.driver.edit(disk);
    }

    @Override
    public DiskModel flush() {
        return Driver.driver.flush();
    }

}
