package org.ovirt.engine.ui.webadmin.section.main.view.tab.gluster;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.common.view.AbstractSubTabFormView;
import org.ovirt.engine.ui.common.widget.form.FormBuilder;
import org.ovirt.engine.ui.common.widget.form.FormItem;
import org.ovirt.engine.ui.common.widget.form.GeneralFormPanel;
import org.ovirt.engine.ui.common.widget.label.TextBoxLabel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.VolumeGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.volumes.VolumeListModel;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.gluster.SubTabVolumeGeneralPresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.uibinder.client.UiField;

public class SubTabVolumeGeneralView extends AbstractSubTabFormView<GlusterVolumeEntity, VolumeListModel, VolumeGeneralModel> implements SubTabVolumeGeneralPresenter.ViewDef, Editor<VolumeGeneralModel> {

    interface Driver extends SimpleBeanEditorDriver<VolumeGeneralModel, SubTabVolumeGeneralView> {
        Driver driver = GWT.create(Driver.class);
    }

    // We need this in order to find the icon for alert messages:
    private final ApplicationResources resources;

    @UiField(provided = true)
    GeneralFormPanel formPanel;

    TextBoxLabel name = new TextBoxLabel();
    TextBoxLabel volumeType = new TextBoxLabel();
    TextBoxLabel replicaCount = new TextBoxLabel();
    TextBoxLabel numOfBricks = new TextBoxLabel();

    FormBuilder formBuilder;

    @Inject
    public SubTabVolumeGeneralView(DetailModelProvider<VolumeListModel, VolumeGeneralModel> modelProvider) {
        super(modelProvider);

        // Inject a reference to the resources:
        resources = ClientGinjectorProvider.instance().getApplicationResources();

        // Init form panel:
        formPanel = new GeneralFormPanel();

        initWidget(formPanel);
        Driver.driver.initialize(this);

        // Build a form using the FormBuilder
        formBuilder = new FormBuilder(formPanel, 2, 4);
        formBuilder.addFormItem(new FormItem("Name", name, 0, 0));
        formBuilder.addFormItem(new FormItem("Volume Type", volumeType, 1, 0));
        formBuilder.addFormItem(new FormItem("Replica Count", replicaCount, 2, 0));
        formBuilder.addFormItem(new FormItem("Num of Bricks", numOfBricks, 3, 0));
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setMainTabSelectedItem(GlusterVolumeEntity selectedItem) {
        Driver.driver.edit(getDetailModel());

        formBuilder.showForm(getDetailModel());
    }
}
