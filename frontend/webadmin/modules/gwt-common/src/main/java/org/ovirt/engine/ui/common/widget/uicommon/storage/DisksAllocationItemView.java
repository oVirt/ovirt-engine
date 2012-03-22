package org.ovirt.engine.ui.common.widget.uicommon.storage;

import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.widget.AbstractValidatedWidgetWithLabel;
import org.ovirt.engine.ui.common.widget.HasEditorDriver;
import org.ovirt.engine.ui.common.widget.editor.EntityModelLabelEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
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

    @UiField
    @Ignore
    EntityModelLabelEditor diskNameLabel;

    @UiField
    @Ignore
    EntityModelLabelEditor diskSizeLabel;

    @UiField
    @Path(value = "sourceStorageDomainName.entity")
    EntityModelLabelEditor sourceStorageLabel;

    @UiField(provided = true)
    @Path(value = "volumeType.selectedItem")
    ListModelListBoxEditor<Object> volumeTypeListEditor;

    @UiField(provided = true)
    @Path(value = "sourceStorageDomain.selectedItem")
    ListModelListBoxEditor<Object> sourceStorageListEditor;

    @UiField(provided = true)
    @Path(value = "storageDomain.selectedItem")
    ListModelListBoxEditor<Object> storageListEditor;

    @UiField(provided = true)
    @Path(value = "quota.selectedItem")
    ListModelListBoxEditor<Object> quotaListEditor;

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

        quotaListEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((storage_domains) object).getstorage_name();
            }
        });
    }

    void addStyles() {
        updateEditorStyle(diskNameLabel, style.editorLabelContent());
        updateEditorStyle(diskSizeLabel, style.editorLabelContent());
        updateEditorStyle(sourceStorageLabel, style.editorContent());
        updateEditorStyle(volumeTypeListEditor, style.editorContent());
        updateEditorStyle(sourceStorageListEditor, style.editorContent());
        updateEditorStyle(storageListEditor, style.editorContent());
        updateEditorStyle(quotaListEditor, style.editorContent());
    }

    private void updateEditorStyle(AbstractValidatedWidgetWithLabel editor, String contentStyle) {
        editor.addContentWidgetStyleName(contentStyle);
        editor.addWrapperStyleName(style.editorWrapper());
        editor.setLabelStyleName(style.editorLabel());
    }

    @Override
    public void edit(final DiskModel object) {
        Driver.driver.edit(object);

        diskNameLabel.asValueBox().setValue(constants.diskNamePrefix() + object.getName());
        diskSizeLabel.asValueBox().setValue((new DiskSizeRenderer<Long>(DiskSizeUnit.GIGABYTE).render(
                (Long) object.getSize().getEntity())));

        object.getVolumeType().setSelectedItem(object.getDiskImage().getvolume_type());

        sourceStorageLabel.getElement().getElementsByTagName("input").getItem(0).
                getStyle().setBorderColor("transparent");
    }

    @Override
    public DiskModel flush() {
        return Driver.driver.flush();
    }

    interface WidgetStyle extends CssResource {
        String editorContent();

        String editorLabelContent();

        String editorWrapper();

        String editorLabel();
    }

}
