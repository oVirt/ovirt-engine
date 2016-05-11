package org.ovirt.engine.ui.webadmin.section.main.view.tab.host;

import org.ovirt.engine.core.common.businessentities.HostDeviceView;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.view.AbstractSubTabTableWidgetView;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.editor.BaseEntityModelCheckbox;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.hostdev.HostDeviceListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host.SubTabHostDevicePresenter;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventBus;

import com.google.inject.Inject;

public class SubTabHostDeviceView extends AbstractSubTabTableWidgetView<VDS, HostDeviceView, HostListModel<Void>, HostDeviceListModel> implements SubTabHostDevicePresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabHostDeviceView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @WithElementId
    private EntityModelCheckBoxEditor doFilterEditor = new EntityModelCheckBoxEditor(Align.RIGHT);

    @Inject
    public SubTabHostDeviceView(
            SearchableDetailModelProvider<HostDeviceView, HostListModel<Void>, HostDeviceListModel> modelProvider,
            EventBus eventBus,
            ClientStorage clientStorage) {
        super(new HostDeviceModelTable(modelProvider, eventBus, clientStorage));
        modelProvider.getModel().setUseListFilter(true);
        BaseEntityModelCheckbox<Boolean> filterCheckBox = doFilterEditor.getContentWidget();
        filterCheckBox.setValue(true);
        execListFilter(filterCheckBox, modelProvider);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable();
        initWidget(getModelBoundTableWidget());
    }

    @Override
    public void initTable() {
        super.initTable();
        table.getBarPanel().add(doFilterEditor);
        doFilterEditor.setLabel("启用过滤");//$NON-NLS-1$
    }

    public void execListFilter(final BaseEntityModelCheckbox<Boolean> filterCheckBox,
            final SearchableDetailModelProvider<HostDeviceView, HostListModel<Void>, HostDeviceListModel> modelProvider) {
        filterCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                modelProvider.getModel().execFilterOrNot(filterCheckBox.getValue());
            }
        });
    }
}
