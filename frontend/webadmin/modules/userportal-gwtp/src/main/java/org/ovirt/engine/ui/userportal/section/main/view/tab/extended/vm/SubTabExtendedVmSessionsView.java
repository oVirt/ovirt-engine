package org.ovirt.engine.ui.userportal.section.main.view.tab.extended.vm;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.view.AbstractSubTabFormView;
import org.ovirt.engine.ui.common.widget.uicommon.vm.VmSessionsModelForm;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmSessionsModel;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.vm.SubTabExtendedVmSessionsPresenter;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalDetailModelProvider;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SubTabExtendedVmSessionsView
    extends AbstractSubTabFormView<UserPortalItemModel, UserPortalListModel, VmSessionsModel>
    implements SubTabExtendedVmSessionsPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabExtendedVmSessionsView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    interface ViewUiBinder extends UiBinder<Widget, SubTabExtendedVmSessionsView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField(provided = true)
    VmSessionsModelForm form;

    @Inject
    public SubTabExtendedVmSessionsView(
            UserPortalDetailModelProvider<UserPortalListModel, VmSessionsModel> modelProvider,
            CommonApplicationConstants constants) {
        super(modelProvider);

        form = new VmSessionsModelForm(modelProvider, constants);
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
