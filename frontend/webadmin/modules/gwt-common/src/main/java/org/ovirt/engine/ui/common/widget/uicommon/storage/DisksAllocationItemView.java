package org.ovirt.engine.ui.common.widget.uicommon.storage;

import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.widget.AbstractValidatedWidgetWithLabel;
import org.ovirt.engine.ui.common.widget.HasEditorDriver;
import org.ovirt.engine.ui.common.widget.editor.EntityModelLabelEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelRenderer;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.parser.EntityModelParser;
import org.ovirt.engine.ui.common.widget.renderer.DiskSizeRenderer;
import org.ovirt.engine.ui.common.widget.renderer.DiskSizeRenderer.DiskSizeUnit;
import org.ovirt.engine.ui.common.widget.renderer.EnumRenderer;
import org.ovirt.engine.ui.common.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.uicommonweb.models.vms.DiskModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class DisksAllocationItemView extends Composite implements HasEditorDriver<DiskModel> {

    interface Driver extends SimpleBeanEditorDriver<DiskModel, DisksAllocationItemView> {
        Driver driver = GWT.create(Driver.class);
    }

    interface ViewUiBinder extends UiBinder<Widget, DisksAllocationItemView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    WidgetStyle style;

    @UiField(provided = true)
    @Path(value = "volumeType.selectedItem")
    ListModelListBoxEditor<Object> volumeTypeListEditor;

    @UiField(provided = true)
    @Path(value = "storageDomain.selectedItem")
    ListModelListBoxEditor<Object> storageListEditor;

    @UiField(provided = true)
    @Path(value = "sourceStorageDomain.selectedItem")
    ListModelListBoxEditor<Object> sourceStorageListEditor;

    @UiField
    @Path(value = "sourceStorageDomainName.entity")
    EntityModelLabelEditor sourceStorageLabel;

    @UiField
    @Ignore
    EntityModelLabelEditor diskNameLabel;

    @UiField
    @Ignore
    EntityModelLabelEditor diskSizeLabel;

    CommonApplicationConstants constants;

    public DisksAllocationItemView(CommonApplicationConstants constants) {
        this.constants = constants;

        initEditors();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        addStyles();
        Driver.driver.initialize(this);
    }

    void initEditors() {
        volumeTypeListEditor = new ListModelListBoxEditor<Object>(new EnumRenderer());

        storageListEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((storage_domains) object).getstorage_name();
            }
        });

        sourceStorageListEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((storage_domains) object).getstorage_name();
            }
        });

        diskSizeLabel = new EntityModelLabelEditor(
                new EntityModelRenderer(), new EntityModelParser());
    }

    void addStyles() {
        updateEditorStyle(diskNameLabel);
        updateEditorStyle(diskSizeLabel);
        updateEditorStyle(sourceStorageLabel);
        updateEditorStyle(volumeTypeListEditor);
        updateEditorStyle(sourceStorageListEditor);
        updateEditorStyle(storageListEditor);
    }

    private void updateEditorStyle(AbstractValidatedWidgetWithLabel editor) {
        editor.addContentWidgetStyleName(style.editorContent());
        editor.addWrapperStyleName(style.editorWrapper());
        editor.setLabelStyleName(style.editorLabel());
    }

    @Override
    public void edit(DiskModel object) {
        Driver.driver.edit(object);

        diskNameLabel.asValueBox().setValue(constants.diskNamePrefix() + object.getName());
        diskSizeLabel.asValueBox().setValue(
                (new DiskSizeRenderer<Long>(DiskSizeUnit.GIGABYTE).render((Long) object.getSize().getEntity())));
    }

    @Override
    public DiskModel flush() {
        return Driver.driver.flush();
    }

    interface WidgetStyle extends CssResource {
        String editorContent();

        String editorWrapper();

        String editorLabel();
    }

}
