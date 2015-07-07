package org.ovirt.engine.ui.userportal.section.main.view.tab.extended.vm;

import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.AbstractSubTabFormView;
import org.ovirt.engine.ui.common.widget.uicommon.vm.VmGeneralModelForm;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmGeneralModel;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.vm.SubTabExtendedVmGeneralPresenter;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalDetailModelProvider;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SubTabExtendedVmGeneralView extends AbstractSubTabFormView<UserPortalItemModel, UserPortalListModel, VmGeneralModel>
        implements SubTabExtendedVmGeneralPresenter.ViewDef {

    interface ViewUiBinder extends UiBinder<Widget, SubTabExtendedVmGeneralView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<SubTabExtendedVmGeneralView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField(provided = true)
    @WithElementId
    VmGeneralModelForm form;

    @Inject
    public SubTabExtendedVmGeneralView(UserPortalDetailModelProvider<UserPortalListModel, VmGeneralModel> modelProvider) {
        super(modelProvider);
        form = new VmGeneralModelForm(modelProvider);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));

        generateIds();

        form.initialize();
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    @Override
    public void setMainTabSelectedItem(UserPortalItemModel selectedItem) {
        update();
    }

    @Override
    public void update() {
        form.update();
    }

}
