package org.ovirt.engine.ui.webadmin.section.main.view.tab.profile;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.uicommonweb.models.profiles.VnicProfileListModel;
import org.ovirt.engine.ui.uicommonweb.models.profiles.VnicProfileTemplateListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.profile.SubTabVnicProfileTemplatePresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;

import com.google.gwt.core.client.GWT;

public class SubTabVnicProfileTemplateView extends AbstractSubTabTableView<VnicProfileView, VmTemplate, VnicProfileListModel, VnicProfileTemplateListModel>
        implements SubTabVnicProfileTemplatePresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabVnicProfileTemplateView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public SubTabVnicProfileTemplateView(SearchableDetailModelProvider<VmTemplate, VnicProfileListModel, VnicProfileTemplateListModel> modelProvider) {
        super(modelProvider);
        initTable();
        initWidget(getTable());
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    void initTable() {
        getTable().enableColumnResizing();

        AbstractTextColumn<VmTemplate> nameColumn = new AbstractTextColumn<VmTemplate>() {
            @Override
            public String getValue(VmTemplate object) {
                return object.getName();
            }
        };
        nameColumn.makeSortable();
        getTable().addColumn(nameColumn, constants.nameTemplate(), "300px"); //$NON-NLS-1$

        AbstractTextColumn<VmTemplate> versionColumn = new AbstractTextColumn<VmTemplate>() {
            @Override
            public String getValue(VmTemplate object) {
                return String.valueOf(object.getTemplateVersionNumber());
            }
        };
        versionColumn.makeSortable();
        getTable().addColumn(versionColumn, constants.versionTemplate(), "100px"); //$NON-NLS-1$

    }

}
