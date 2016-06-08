package org.ovirt.engine.ui.common.widget.uicommon.storage;

import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.profiles.DiskProfile;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.utils.SizeConverter;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.idhandler.HasElementId;
import org.ovirt.engine.ui.common.utils.ElementIdUtils;
import org.ovirt.engine.ui.common.view.popup.FocusableComponentsContainer;
import org.ovirt.engine.ui.common.widget.AbstractValidatedWidgetWithLabel;
import org.ovirt.engine.ui.common.widget.HasEditorDriver;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.label.EnableableFormLabel;
import org.ovirt.engine.ui.common.widget.renderer.DiskSizeRenderer;
import org.ovirt.engine.ui.common.widget.renderer.EnumRenderer;
import org.ovirt.engine.ui.common.widget.renderer.NameRenderer;
import org.ovirt.engine.ui.common.widget.renderer.StorageDomainFreeSpaceRenderer;
import org.ovirt.engine.ui.uicommonweb.models.vms.DiskModel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class DisksAllocationItemView extends Composite implements HasEditorDriver<DiskModel>, HasElementId, FocusableComponentsContainer {

    interface Driver extends UiCommonEditorDriver<DiskModel, DisksAllocationItemView> {
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
    EnableableFormLabel diskAliasLabel;

    @UiField
    @Path(value = "alias.entity")
    StringEntityModelTextBoxEditor diskAliasEditor;

    @UiField
    @Ignore
    EnableableFormLabel diskSizeLabel;

    @UiField
    @Path(value = "sourceStorageDomainName.entity")
    EnableableFormLabel sourceStorageLabel;

    @UiField(provided = true)
    @Path(value = "volumeType.selectedItem")
    ListModelListBoxEditor<VolumeType> volumeTypeListEditor;

    @UiField(provided = true)
    @Path(value = "volumeFormat.selectedItem")
    ListModelListBoxEditor<VolumeFormat> volumeFormatListEditor;

    @UiField(provided = true)
    @Path(value = "sourceStorageDomain.selectedItem")
    ListModelListBoxEditor<StorageDomain> sourceStorageListEditor;

    @UiField(provided = true)
    @Path(value = "storageDomain.selectedItem")
    ListModelListBoxEditor<StorageDomain> storageListEditor;

    @UiField(provided = true)
    @Path(value = "diskProfile.selectedItem")
    ListModelListBoxEditor<DiskProfile> diskProfileListEditor;

    @UiField(provided = true)
    @Path(value = "quota.selectedItem")
    ListModelListBoxEditor<Quota> quotaListEditor;

    private final Driver driver = GWT.create(Driver.class);

    @Override
    public int setTabIndexes(int nextTabIndex) {
        diskAliasEditor.setTabIndex(nextTabIndex++);
        storageListEditor.setTabIndex(nextTabIndex++);
        diskProfileListEditor.setTabIndex(nextTabIndex++);
        return nextTabIndex;
    }

    public DisksAllocationItemView() {

        initEditors();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        driver.initialize(this);
        diskAliasEditor.hideLabel();
    }

    void initEditors() {
        volumeTypeListEditor = new ListModelListBoxEditor<>(new EnumRenderer<VolumeType>());
        volumeTypeListEditor.hideLabel();

        storageListEditor = new ListModelListBoxEditor<>(new StorageDomainFreeSpaceRenderer<>());
        storageListEditor.hideLabel();

        volumeFormatListEditor = new ListModelListBoxEditor<>(new EnumRenderer<VolumeFormat>());
        volumeFormatListEditor.hideLabel();

        sourceStorageListEditor = new ListModelListBoxEditor<>(new StorageDomainFreeSpaceRenderer<>());
        sourceStorageListEditor.hideLabel();

        diskProfileListEditor = new ListModelListBoxEditor<>(new NameRenderer<DiskProfile>());
        diskProfileListEditor.hideLabel();

        quotaListEditor = new ListModelListBoxEditor<>(new NameRenderer<Quota>());
        quotaListEditor.hideLabel();
    }

    void updateStyles(Boolean isNarrowStyle) {
        String editorStyle = isNarrowStyle ? style.editorContentNarrow() : style.editorContent();

        updateLabelStyle(diskAliasLabel, editorStyle);
        updateEditorStyle(diskAliasEditor, editorStyle);
        updateLabelStyle(diskSizeLabel, editorStyle);
        updateLabelStyle(sourceStorageLabel, editorStyle);
        updateEditorStyle(volumeTypeListEditor, editorStyle);
        updateEditorStyle(volumeFormatListEditor, editorStyle);
        updateEditorStyle(sourceStorageListEditor, editorStyle);
        updateEditorStyle(storageListEditor, editorStyle);
        updateEditorStyle(diskProfileListEditor, editorStyle);
        updateEditorStyle(quotaListEditor, editorStyle);
    }

    private void updateEditorStyle(AbstractValidatedWidgetWithLabel<?, ?> editor, String contentStyle) {
        editor.setContentWidgetContainerStyleName(contentStyle);
        editor.addWrapperStyleName(style.editorWrapper());
    }

    private void updateLabelStyle(EnableableFormLabel label, String contentStyle) {
        label.addStyleName(contentStyle);
    }

    public void setIsAliasChangeable(boolean changeable) {
        diskAliasLabel.setVisible(!changeable);
        diskAliasEditor.setVisible(changeable);
    }

    @Override
    public void edit(final DiskModel object) {
        driver.edit(object);

        diskAliasLabel.setText(object.getAlias().getEntity());

        diskSizeLabel.setText(new DiskSizeRenderer<Integer>(SizeConverter.SizeUnit.GiB).render(
                object.getSize().getEntity()));

        object.getVolumeType().setSelectedItem(((DiskImage) object.getDisk()).getVolumeType());
        object.getVolumeFormat().setSelectedItem(((DiskImage) object.getDisk()).getVolumeFormat());

        StorageDomain sourceDomain = object.getSourceStorageDomain().getSelectedItem();
        if (sourceDomain != null) {
            sourceStorageLabel.setText(object.getSourceStorageDomain().getSelectedItem().getName());
        }

        updateStyles(object.getQuota().getIsAvailable());
    }

    @Override
    public DiskModel flush() {
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }

    @Override
    public void setElementId(String elementId) {
        diskAliasLabel.setId(
                ElementIdUtils.createElementId(elementId, "diskName")); //$NON-NLS-1$
        diskAliasEditor.setElementId(
                ElementIdUtils.createElementId(elementId, "diskAlias")); //$NON-NLS-1$
        diskSizeLabel.setId(
                ElementIdUtils.createElementId(elementId, "diskSize")); //$NON-NLS-1$
        sourceStorageLabel.setId(
                ElementIdUtils.createElementId(elementId, "sourceStorageDomainName")); //$NON-NLS-1$
        volumeTypeListEditor.setElementId(
                ElementIdUtils.createElementId(elementId, "volumeType")); //$NON-NLS-1$
        volumeFormatListEditor.setElementId(
                ElementIdUtils.createElementId(elementId, "volumeFormat")); //$NON-NLS-1$
        sourceStorageListEditor.setElementId(
                ElementIdUtils.createElementId(elementId, "sourceStorageDomain")); //$NON-NLS-1$
        storageListEditor.setElementId(
                ElementIdUtils.createElementId(elementId, "storageDomain")); //$NON-NLS-1$
        diskProfileListEditor.setElementId(
                ElementIdUtils.createElementId(elementId, "diskProfile")); //$NON-NLS-1$
        quotaListEditor.setElementId(
                ElementIdUtils.createElementId(elementId, "quota")); //$NON-NLS-1$
    }

}
