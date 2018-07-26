package org.ovirt.engine.ui.common.widget.uicommon.storage;

import org.ovirt.engine.core.common.businessentities.storage.DiskContentType;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.utils.SizeConverter;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.widget.form.FormItem;
import org.ovirt.engine.ui.common.widget.label.BooleanLabel;
import org.ovirt.engine.ui.common.widget.label.DiskSizeLabel;
import org.ovirt.engine.ui.common.widget.label.EnumTextBoxLabel;
import org.ovirt.engine.ui.common.widget.uicommon.AbstractModelBoundFormWidget;
import org.ovirt.engine.ui.uicommonweb.models.storage.ImageInfoModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiConstructor;

public class ImageInfoForm extends AbstractModelBoundFormWidget<ImageInfoModel> {

    interface Driver extends UiCommonEditorDriver<ImageInfoModel, ImageInfoForm> {
    }

    private final Driver driver = GWT.create(Driver.class);

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();

    private ImageInfoModel imageInfoModel;

    EnumTextBoxLabel<VolumeFormat> format = new EnumTextBoxLabel<>();
    DiskSizeLabel<Long> actualSize = new DiskSizeLabel<>(SizeConverter.SizeUnit.BYTES);
    DiskSizeLabel<Long> virtualSize = new DiskSizeLabel<>(SizeConverter.SizeUnit.BYTES);
    EnumTextBoxLabel<DiskContentType> contentType = new EnumTextBoxLabel<>();
    EnumTextBoxLabel<ImageInfoModel.QemuCompat> qcowCompat = new EnumTextBoxLabel<>();
    BooleanLabel backingFile = new BooleanLabel(constants.yes(), constants.no());


    @UiConstructor
    public ImageInfoForm() {
        super(null, 2, 3);
    }

    public void initialize(ImageInfoModel model) {
        setModel(model);
        driver.initialize(this);

        formBuilder.addFormItem(new FormItem(constants.imageFormat(), format, 0, 0){
            @Override
            public boolean getIsAvailable() {
                return getModel().getFileLoaded();
            }
        });
        formBuilder.addFormItem(new FormItem(constants.imageActualSize(), actualSize, 1, 0){
            @Override
            public boolean getIsAvailable() {
                return getModel().getFileLoaded();
            }
        });
        formBuilder.addFormItem(new FormItem(constants.imageVirtualSize(), virtualSize, 2, 0) {
            @Override
            public boolean getIsAvailable() {
                return getModel().getFormat() == VolumeFormat.COW;
            }
        });
        formBuilder.addFormItem(new FormItem(constants.imageContent(), contentType, 0, 1) {
            @Override
            public boolean getIsAvailable() {
                return getModel().getFileLoaded();
            }
        });
        formBuilder.addFormItem(new FormItem(constants.imageQcowCompat(), qcowCompat, 1, 1) {
            @Override
            public boolean getIsAvailable() {
                return getModel().getFormat() == VolumeFormat.COW;
            }
        });
        formBuilder.addFormItem(new FormItem(constants.imageBackingFile(), backingFile, 2, 1) {
            @Override
            public boolean getIsAvailable() {
                return getModel().getFormat() == VolumeFormat.COW;
            }
        });

        formBuilder.setRelativeColumnWidth(0, 5);
        formBuilder.setRelativeColumnWidth(1, 5);

        getModel().getEntityChangedEvent().addListener((ev, sender, args) -> update());
    }

    @Override
    protected void doEdit(ImageInfoModel model) {
        driver.edit(model);
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }

    @Override
    protected ImageInfoModel getModel() {
        return imageInfoModel;
    }

    public void setModel(ImageInfoModel imageInfoModel) {
        this.imageInfoModel = imageInfoModel;
    }
}
