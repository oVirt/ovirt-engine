package org.ovirt.engine.ui.webadmin.section.main.view.tab.profile;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.models.profiles.VnicProfileListModel;
import org.ovirt.engine.ui.uicommonweb.models.profiles.VnicProfileVmListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.profile.SubTabVnicProfileVmPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;

import com.google.gwt.core.client.GWT;

public class SubTabVnicProfileVmView extends AbstractSubTabTableView<VnicProfileView, VM, VnicProfileListModel, VnicProfileVmListModel>
        implements SubTabVnicProfileVmPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabVnicProfileVmView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private final ApplicationConstants constants;

    @Inject
    public SubTabVnicProfileVmView(SearchableDetailModelProvider<VM, VnicProfileListModel, VnicProfileVmListModel> modelProvider,
            ApplicationConstants constants) {
        super(modelProvider);
        this.constants = constants;
        initTable();
        initWidget(getTable());
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    void initTable() {
        getTable().enableColumnResizing();

        AbstractTextColumnWithTooltip<VM> nameColumn = new AbstractTextColumnWithTooltip<VM>() {
            @Override
            public String getValue(VM object) {
                return object.getName();
            }
        };
        nameColumn.makeSortable();
        getTable().addColumn(nameColumn, constants.nameVm(), "300px"); //$NON-NLS-1$

    }

}
