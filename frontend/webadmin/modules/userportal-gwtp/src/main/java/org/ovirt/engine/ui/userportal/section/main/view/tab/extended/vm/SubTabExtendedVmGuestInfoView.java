package org.ovirt.engine.ui.userportal.section.main.view.tab.extended.vm;

import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.view.AbstractSubTabFormView;
import org.ovirt.engine.ui.common.widget.uicommon.vm.VmGuestInfoModelForm;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmGuestInfoModel;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.vm.SubTabExtendedVmGuestInfoPresenter;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalDetailModelProvider;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SubTabExtendedVmGuestInfoView
    extends AbstractSubTabFormView<UserPortalItemModel, UserPortalListModel, VmGuestInfoModel>
    implements SubTabExtendedVmGuestInfoPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabExtendedVmGuestInfoView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    interface ViewUiBinder extends UiBinder<Widget, SubTabExtendedVmGuestInfoView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField(provided = true)
    VmGuestInfoModelForm form;

    @Inject
    public SubTabExtendedVmGuestInfoView(
            UserPortalDetailModelProvider<UserPortalListModel, VmGuestInfoModel> modelProvider) {
        super(modelProvider);

        form = new VmGuestInfoModelForm(modelProvider);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
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
