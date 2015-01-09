package org.ovirt.engine.ui.webadmin.section.main.view.tab.profile;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.models.profiles.VnicProfileListModel;
import org.ovirt.engine.ui.uicommonweb.models.profiles.VnicProfileTemplateListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.profile.SubTabVnicProfileTemplatePresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;

import com.google.gwt.core.client.GWT;

public class SubTabVnicProfileTemplateView extends AbstractSubTabTableView<VnicProfileView, VmTemplate, VnicProfileListModel, VnicProfileTemplateListModel>
        implements SubTabVnicProfileTemplatePresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabVnicProfileTemplateView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private final ApplicationConstants constants;

    @Inject
    public SubTabVnicProfileTemplateView(SearchableDetailModelProvider<VmTemplate, VnicProfileListModel, VnicProfileTemplateListModel> modelProvider,
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

        AbstractTextColumnWithTooltip<VmTemplate> nameColumn = new AbstractTextColumnWithTooltip<VmTemplate>() {
            @Override
            public String getValue(VmTemplate object) {
                return object.getName();
            }
        };
        nameColumn.makeSortable();
        getTable().addColumn(nameColumn, constants.nameTemplate(), "300px"); //$NON-NLS-1$

    }

}
