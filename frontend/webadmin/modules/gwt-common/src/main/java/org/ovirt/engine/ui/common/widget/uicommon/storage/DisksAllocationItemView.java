package org.ovirt.engine.ui.common.widget.uicommon.storage;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.idhandler.HasElementId;
import org.ovirt.engine.ui.common.utils.ElementIdUtils;
import org.ovirt.engine.ui.common.view.popup.FocusableComponentsContainer;
import org.ovirt.engine.ui.common.widget.AbstractValidatedWidgetWithLabel;
import org.ovirt.engine.ui.common.widget.HasEditorDriver;
import org.ovirt.engine.ui.common.widget.editor.EntityModelLabelEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.renderer.DiskSizeRenderer;
import org.ovirt.engine.ui.common.widget.renderer.DiskSizeRenderer.DiskSizeUnit;
import org.ovirt.engine.ui.common.widget.renderer.EnumRenderer;
import org.ovirt.engine.ui.common.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.common.widget.renderer.StorageDomainFreeSpaceRenderer;
import org.ovirt.engine.ui.uicommonweb.models.vms.DiskModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class DisksAllocationItemView extends Composite implements HasEditorDriver<DiskModel>, HasElementId, FocusableComponentsContainer {

    interface Driver extends SimpleBeanEditorDriver<DiskModel, DisksAllocationItemView> {
        Driver driver = GWT.create(Driver.class);
    }

    interface ViewUiBinder extends UiBinder<Widget, DisksAllocationItemView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface WidgetStyle extends CssResource {
        String editorContent();

        String editorContentNarrow();

        String editorWrapper();

        String editorLabel();
    }

    @UiField
    WidgetStyle style;

    @UiField
    @Ignore
    EntityModelLabelEditor diskAliasLabel;

    @UiField
    @Path(value = "alias.entity")
    EntityModelTextBoxEditor diskAliasEditor;

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

    public int setTabIndexes(int nextTabIndex) {
        diskAliasEditor.setTabIndex(nextTabIndex++);
        storageListEditor.setTabIndex(nextTabIndex++);
        return nextTabIndex;
    }

    public DisksAllocationItemView(CommonApplicationConstants constants) {
        this.constants = constants;

        initEditors();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        Driver.driver.initialize(this);
    }

    void initEditors() {
        volumeTypeListEditor = new ListModelListBoxEditor<Object>(new EnumRenderer());

        storageListEditor = new ListModelListBoxEditor<Object>(new StorageDomainFreeSpaceRenderer());

        sourceStorageListEditor = new ListModelListBoxEditor<Object>(new StorageDomainFreeSpaceRenderer());

        quotaListEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((Quota) object).getQuotaName();
            }
        });
    }

    void updateStyles(Boolean isNarrowStyle) {
        String editorStyle = isNarrowStyle ? style.editorContentNarrow() : style.editorContent();

        updateEditorStyle(diskAliasLabel, editorStyle);
        updateEditorStyle(diskAliasEditor, editorStyle);
        updateEditorStyle(diskSizeLabel, editorStyle);
        updateEditorStyle(sourceStorageLabel, editorStyle);
        updateEditorStyle(volumeTypeListEditor, editorStyle);
        updateEditorStyle(sourceStorageListEditor, editorStyle);
        updateEditorStyle(storageListEditor, editorStyle);
        updateEditorStyle(quotaListEditor, editorStyle);
    }

    private void updateEditorStyle(AbstractValidatedWidgetWithLabel editor, String contentStyle) {
        editor.setContentWidgetStyleName(contentStyle);
        editor.addWrapperStyleName(style.editorWrapper());
        editor.setLabelStyleName(style.editorLabel());
    }

    public void setIsAliasChangeable(boolean changeable) {
        diskAliasLabel.setVisible(!changeable);
        diskAliasEditor.setVisible(changeable);
    }

    @Override
    public void edit(final DiskModel object) {
        Driver.driver.edit(object);

        diskAliasLabel.asValueBox().setValue(object.getAlias().getEntity());
        diskSizeLabel.asValueBox().setValue((new DiskSizeRenderer<Long>(DiskSizeUnit.GIGABYTE).render(
                (Long) object.getSize().getEntity())));

        object.getVolumeType().setSelectedItem(((DiskImage) object.getDisk()).getvolume_type());

        sourceStorageLabel.getElement().getElementsByTagName("input").getItem(0). //$NON-NLS-1$
                getStyle().setBorderColor("transparent"); //$NON-NLS-1$

        updateStyles(object.getQuota().getIsAvailable());
    }

    @Override
    public DiskModel flush() {
        return Driver.driver.flush();
    }

    @Override
    public void setElementId(String elementId) {
        diskAliasLabel.setElementId(
                ElementIdUtils.createElementId(elementId, "diskName")); //$NON-NLS-1$
        diskAliasEditor.setElementId(
                ElementIdUtils.createElementId(elementId, "diskAlias")); //$NON-NLS-1$
        diskSizeLabel.setElementId(
                ElementIdUtils.createElementId(elementId, "diskSize")); //$NON-NLS-1$
        sourceStorageLabel.setElementId(
                ElementIdUtils.createElementId(elementId, "sourceStorageDomainName")); //$NON-NLS-1$
        volumeTypeListEditor.setElementId(
                ElementIdUtils.createElementId(elementId, "volumeType")); //$NON-NLS-1$
        sourceStorageListEditor.setElementId(
                ElementIdUtils.createElementId(elementId, "sourceStorageDomain")); //$NON-NLS-1$
        storageListEditor.setElementId(
                ElementIdUtils.createElementId(elementId, "storageDomain")); //$NON-NLS-1$
        quotaListEditor.setElementId(
                ElementIdUtils.createElementId(elementId, "quota")); //$NON-NLS-1$
    }

}
