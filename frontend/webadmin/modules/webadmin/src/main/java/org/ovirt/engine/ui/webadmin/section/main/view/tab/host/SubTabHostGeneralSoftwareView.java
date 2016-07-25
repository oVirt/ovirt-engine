package org.ovirt.engine.ui.webadmin.section.main.view.tab.host;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.common.view.AbstractSubTabFormView;
import org.ovirt.engine.ui.common.widget.form.FormBuilder;
import org.ovirt.engine.ui.common.widget.form.FormItem;
import org.ovirt.engine.ui.common.widget.form.GeneralFormPanel;
import org.ovirt.engine.ui.common.widget.label.StringValueLabel;
import org.ovirt.engine.ui.uicommonweb.models.ApplicationModeHelper;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host.SubTabHostGeneralSoftwarePresenter;
import org.ovirt.engine.ui.webadmin.widget.label.VersionValueLabel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SubTabHostGeneralSoftwareView extends AbstractSubTabFormView<VDS, HostListModel<Void>, HostGeneralModel>
    implements SubTabHostGeneralSoftwarePresenter.ViewDef, Editor<HostGeneralModel> {

    interface Driver extends SimpleBeanEditorDriver<HostGeneralModel, SubTabHostGeneralSoftwareView> {
    }

    interface ViewIdHandler extends ElementIdHandler<SubTabHostGeneralSoftwareView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    interface ViewUiBinder extends UiBinder<Widget, SubTabHostGeneralSoftwareView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    private final Driver driver = GWT.create(Driver.class);

    FormBuilder formBuilder;

    @Path("OS")
    StringValueLabel oS = new StringValueLabel();
    StringValueLabel osPrettyName = new StringValueLabel();
    StringValueLabel kvmVersion = new StringValueLabel();
    VersionValueLabel libvirtVersion = new VersionValueLabel();
    StringValueLabel spiceVersion = new StringValueLabel();
    StringValueLabel kernelVersion = new StringValueLabel();
    VersionValueLabel glusterVersion = new VersionValueLabel();
    VersionValueLabel vdsmVersion = new VersionValueLabel();
    VersionValueLabel librbdVersion = new VersionValueLabel();

    @UiField(provided = true)
    @WithElementId
    GeneralFormPanel formPanel;

    @Inject
    public SubTabHostGeneralSoftwareView(DetailModelProvider<HostListModel<Void>, HostGeneralModel> modelProvider) {
        super(modelProvider);

        // Init form panel:
        formPanel = new GeneralFormPanel();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        driver.initialize(this);
        buildForm();
    }

    private void buildForm() {
        generateIds();

        boolean virtSupported = ApplicationModeHelper.isModeSupported(ApplicationMode.VirtOnly);
        boolean glusterSupported = ApplicationModeHelper.isModeSupported(ApplicationMode.GlusterOnly);

        // Build a form using the FormBuilder
        formBuilder = new FormBuilder(formPanel, 1, 9);
        formBuilder.setRelativeColumnWidth(0, 12);
        formBuilder.addFormItem(new FormItem(constants.osVersionHostGeneral(), oS, 0).withAutoPlacement(), 2, 10);
        formBuilder.addFormItem(new FormItem(constants.osPrettyName(), osPrettyName, 0).withAutoPlacement(), 2, 10);
        formBuilder.addFormItem(new FormItem(constants.kernelVersionHostGeneral(), kernelVersion,
                0).withAutoPlacement(), 2, 10);
        formBuilder.addFormItem(new FormItem(constants.kvmVersionHostGeneral(), kvmVersion, 0,
                virtSupported).withAutoPlacement(), 2, 10);
        formBuilder.addFormItem(new FormItem(constants.libvirtVersionHostGeneral(), libvirtVersion, 0,
                virtSupported).withAutoPlacement(), 2, 10);
        formBuilder.addFormItem(new FormItem(constants.vdsmVersionHostGeneral(), vdsmVersion,
                0).withAutoPlacement(), 2, 10);
        formBuilder.addFormItem(new FormItem(constants.spiceVersionHostGeneral(), spiceVersion, 0,
                virtSupported).withAutoPlacement(), 2, 10);
        formBuilder.addFormItem(new FormItem(constants.glusterVersionHostGeneral(), glusterVersion, 0,
                glusterSupported).withAutoPlacement(), 2, 10);
        formBuilder.addFormItem(new FormItem(constants.cephVersionHostGeneral(), librbdVersion, 0,
                virtSupported).withAutoPlacement(), 2, 10);
    }

    @Override
    public void setMainTabSelectedItem(VDS selectedItem) {
        driver.edit(getDetailModel());
        formBuilder.update(getDetailModel());
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

}
