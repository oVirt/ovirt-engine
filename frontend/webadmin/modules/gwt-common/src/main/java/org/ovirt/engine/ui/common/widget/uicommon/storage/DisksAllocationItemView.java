package org.ovirt.engine.ui.common.widget.uicommon.storage;

import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.widget.HasEditorDriver;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.label.DiskSizeLabel;
import org.ovirt.engine.ui.common.widget.label.TextBoxLabel;
import org.ovirt.engine.ui.common.widget.renderer.EnumRenderer;
import org.ovirt.engine.ui.common.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.uicommonweb.models.vms.DiskModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
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
    HorizontalPanel diskNameLabelPanel;

    @Ignore
    TextBoxLabel diskNameLabel;

    @Ignore
    DiskSizeLabel<Long> diskSizeLabel;

    @UiField(provided = true)
    @Path(value = "volumeType.selectedItem")
    ListModelListBoxEditor<Object> volumeTypeListEditor;

    @UiField(provided = true)
    @Path(value = "storageDomain.selectedItem")
    ListModelListBoxEditor<Object> storageListEditor;

    @UiField(provided = true)
    @Path(value = "sourceStorageDomain.selectedItem")
    ListModelListBoxEditor<Object> sourceStorageListEditor;

    CommonApplicationConstants constants;

    public DisksAllocationItemView(CommonApplicationConstants constants) {
        this.constants = constants;

        initListBoxEditors();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        initLabels();
        addStyles();
        Driver.driver.initialize(this);
    }

    void initListBoxEditors() {
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
    }

    void initLabels() {
        diskNameLabel = new TextBoxLabel();
        diskSizeLabel = new DiskSizeLabel<Long>();
        diskNameLabelPanel.add(diskNameLabel);
        diskNameLabelPanel.add(diskSizeLabel);
    }

    void addStyles() {
        diskNameLabel.addStyleName(style.diskNameLabel());
        diskSizeLabel.addStyleName(style.diskSizeLabel());
        diskNameLabelPanel.addStyleName(style.diskNameLabelPanel());

        volumeTypeListEditor.addContentWidgetStyleName(style.editorContent());
        sourceStorageListEditor.addContentWidgetStyleName(style.editorContent());
        storageListEditor.addContentWidgetStyleName(style.editorContent());

        volumeTypeListEditor.addWrapperStyleName(style.editorWrapper());
        sourceStorageListEditor.addWrapperStyleName(style.editorWrapper());
        storageListEditor.addWrapperStyleName(style.editorWrapper());

        volumeTypeListEditor.hideLabel();
        sourceStorageListEditor.hideLabel();
        storageListEditor.hideLabel();
    }

    @Override
    public void edit(DiskModel object) {
        Driver.driver.edit(object);

        diskNameLabel.setText(constants.diskNamePrefix() + object.getName());
        diskSizeLabel.setValue((Long) object.getSize().getEntity());
    }

    @Override
    public DiskModel flush() {
        return Driver.driver.flush();
    }

    interface WidgetStyle extends CssResource {
        String diskNameLabel();

        String diskNameLabelPanel();

        String diskSizeLabel();

        String editorContent();

        String editorWrapper();
    }

}
