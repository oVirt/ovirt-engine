package org.ovirt.engine.ui.webadmin.section.main.view.tab.disk;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.utils.SizeConverter;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.common.view.AbstractSubTabFormView;
import org.ovirt.engine.ui.common.widget.form.FormBuilder;
import org.ovirt.engine.ui.common.widget.form.FormItem;
import org.ovirt.engine.ui.common.widget.form.GeneralFormPanel;
import org.ovirt.engine.ui.common.widget.label.BooleanLabel;
import org.ovirt.engine.ui.common.widget.label.DiskSizeLabel;
import org.ovirt.engine.ui.common.widget.label.StringValueLabel;
import org.ovirt.engine.ui.uicommonweb.models.disks.DiskGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.disks.DiskListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.disk.SubTabDiskGeneralPresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;

public class SubTabDiskGeneralView extends AbstractSubTabFormView<Disk, DiskListModel, DiskGeneralModel> implements SubTabDiskGeneralPresenter.ViewDef, Editor<DiskGeneralModel> {

    interface ViewUiBinder extends UiBinder<Widget, SubTabDiskGeneralView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<SubTabDiskGeneralView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    interface Driver extends UiCommonEditorDriver<DiskGeneralModel, SubTabDiskGeneralView> {
    }

    StringValueLabel alias = new StringValueLabel();
    StringValueLabel description = new StringValueLabel();
    StringValueLabel diskId = new StringValueLabel();
    StringValueLabel lunId = new StringValueLabel();
    StringValueLabel diskProfileName = new StringValueLabel();
    StringValueLabel quotaName = new StringValueLabel();
    BooleanLabel wipeAfterDelete = new BooleanLabel(constants.yes(), constants.no());
    DiskSizeLabel<Long> virtualSize = new DiskSizeLabel<>(SizeConverter.SizeUnit.GiB);
    DiskSizeLabel<Double> actualSize = new DiskSizeLabel<>(SizeConverter.SizeUnit.GiB);

    @UiField(provided = true)
    @WithElementId
    GeneralFormPanel formPanel;

    FormBuilder formBuilder;

    private final Driver driver = GWT.create(Driver.class);

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public SubTabDiskGeneralView(DetailModelProvider<DiskListModel, DiskGeneralModel> modelProvider) {
        super(modelProvider);

        // Init formPanel
        formPanel = new GeneralFormPanel();

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        driver.initialize(this);

        generateIds();

        // Build a form using the FormBuilder
        formBuilder = new FormBuilder(formPanel, 1, 10);

        formBuilder.addFormItem(new FormItem(constants.aliasDisk(), alias, 0, 0), 2, 10);
        formBuilder.addFormItem(new FormItem(constants.descriptionDisk(), description, 1, 0), 2, 10);
        formBuilder.addFormItem(new FormItem(constants.idDisk(), diskId, 2, 0), 2, 10);
        formBuilder.addFormItem(new FormItem(constants.lunIdSanStorage(), lunId, 3, 0) {
            @Override
            public boolean getIsAvailable() {
                return getDetailModel().isLun();
            }
        }, 2, 10);
        formBuilder.addFormItem(new FormItem(constants.diskProfile(), diskProfileName, 4, 0) {
            @Override
            public boolean getIsAvailable() {
                return !getDetailModel().isLun();
            }
        }, 2, 10);
        formBuilder.addFormItem(new FormItem(constants.quota(), quotaName, 5, 0) {
            @Override
            public boolean getIsAvailable() {
                return getDetailModel().isQuotaAvailable();
            }
        }, 2, 10);
        formBuilder.addFormItem(new FormItem(constants.wipeAfterDelete(), wipeAfterDelete, 6, 0) {
            @Override
            public boolean getIsAvailable() {
                return getDetailModel().isImage();
            }
        }, 2, 10);
        formBuilder.addFormItem(new FormItem(constants.diskVirtualSize(), virtualSize, 7, 0), 2, 10);
        formBuilder.addFormItem(new FormItem(constants.diskActualSize(), actualSize, 8, 0) {
            @Override
            public boolean getIsAvailable() {
                return getDetailModel().isImage();
            }
        }, 2, 10);
        formBuilder.setRelativeColumnWidth(0, 12);
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    @Override
    public void setMainSelectedItem(Disk selectedItem) {
        driver.edit(getDetailModel());
        formBuilder.update(getDetailModel());
    }

}
