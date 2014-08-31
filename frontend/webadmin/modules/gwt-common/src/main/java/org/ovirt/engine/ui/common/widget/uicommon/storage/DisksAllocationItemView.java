package org.ovirt.engine.ui.common.widget.uicommon.storage;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.businessentities.profiles.DiskProfile;
import org.ovirt.engine.core.common.utils.SizeConverter;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.idhandler.HasElementId;
import org.ovirt.engine.ui.common.utils.ElementIdUtils;
import org.ovirt.engine.ui.common.view.popup.FocusableComponentsContainer;
import org.ovirt.engine.ui.common.widget.AbstractValidatedWidgetWithLabel;
import org.ovirt.engine.ui.common.widget.HasEditorDriver;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelLabelEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.renderer.DiskSizeRenderer;
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
    StringEntityModelLabelEditor diskAliasLabel;

    @UiField
    @Path(value = "alias.entity")
    StringEntityModelTextBoxEditor diskAliasEditor;

    @UiField
    @Ignore
    StringEntityModelLabelEditor diskSizeLabel;

    @UiField
    @Path(value = "sourceStorageDomainName.entity")
    StringEntityModelLabelEditor sourceStorageLabel;

    @UiField(provided = true)
    @Path(value = "volumeType.selectedItem")
    ListModelListBoxEditor<VolumeType> volumeTypeListEditor;

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

    private final CommonApplicationConstants constants;

    @Override
    public int setTabIndexes(int nextTabIndex) {
        diskAliasEditor.setTabIndex(nextTabIndex++);
        storageListEditor.setTabIndex(nextTabIndex++);
        diskProfileListEditor.setTabIndex(nextTabIndex++);
        return nextTabIndex;
    }

    public DisksAllocationItemView(CommonApplicationConstants constants) {
        this.constants = constants;

        initEditors();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        driver.initialize(this);
    }

    void initEditors() {
        volumeTypeListEditor = new ListModelListBoxEditor<VolumeType>(new EnumRenderer<VolumeType>());

        storageListEditor = new ListModelListBoxEditor<StorageDomain>(new StorageDomainFreeSpaceRenderer<StorageDomain>());

        sourceStorageListEditor = new ListModelListBoxEditor<StorageDomain>(new StorageDomainFreeSpaceRenderer<StorageDomain>());

        diskProfileListEditor = new ListModelListBoxEditor<DiskProfile>(new NullSafeRenderer<DiskProfile>() {
            @Override
            protected String renderNullSafe(DiskProfile object) {
                return object.getName();
            }
        });

        quotaListEditor = new ListModelListBoxEditor<Quota>(new NullSafeRenderer<Quota>() {
            @Override
            public String renderNullSafe(Quota quota) {
                return quota.getQuotaName();
            }
        });
    }

    void updateStyles(Boolean isNarrowStyle) {
        String editorStyle = style.editorContentNarrow();

        updateEditorStyle(diskAliasLabel, editorStyle);
        updateEditorStyle(diskAliasEditor, editorStyle);
        updateEditorStyle(diskSizeLabel, editorStyle);
        updateEditorStyle(sourceStorageLabel, editorStyle);
        updateEditorStyle(volumeTypeListEditor, editorStyle);
        updateEditorStyle(sourceStorageListEditor, editorStyle);
        updateEditorStyle(storageListEditor, editorStyle);
        updateEditorStyle(diskProfileListEditor, editorStyle);
        updateEditorStyle(quotaListEditor, editorStyle);
    }

    private void updateEditorStyle(AbstractValidatedWidgetWithLabel editor, String contentStyle) {
        editor.setContentWidgetStyleName(contentStyle);
        editor.addContentWidgetStyleName("avw_contentWidget_pfly_fix"); //$NON-NLS-1$
        editor.addWrapperStyleName(style.editorWrapper());
        editor.setLabelStyleName(style.editorLabel());
    }

    public void setIsAliasChangeable(boolean changeable) {
        diskAliasLabel.setVisible(!changeable);
        diskAliasEditor.setVisible(changeable);
    }

    @Override
    public void edit(final DiskModel object) {
        driver.edit(object);

        diskAliasLabel.asValueBox().setValue(object.getAlias().getEntity());
        diskSizeLabel.asValueBox().setValue((new DiskSizeRenderer<Integer>(SizeConverter.SizeUnit.GB).render(
                object.getSize().getEntity())));

        object.getVolumeType().setSelectedItem(((DiskImage) object.getDisk()).getVolumeType());

        sourceStorageLabel.getElement().getElementsByTagName("input").getItem(0). //$NON-NLS-1$
                getStyle().setBorderColor("transparent"); //$NON-NLS-1$

        updateStyles(object.getQuota().getIsAvailable());
    }

    @Override
    public DiskModel flush() {
        return driver.flush();
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
        diskProfileListEditor.setElementId(
                ElementIdUtils.createElementId(elementId, "diskProfile")); //$NON-NLS-1$
        quotaListEditor.setElementId(
                ElementIdUtils.createElementId(elementId, "quota")); //$NON-NLS-1$
    }

}
