package org.ovirt.engine.ui.webadmin.section.main.view.tab;

import org.ovirt.engine.core.common.businessentities.NetworkView;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.SafeHtmlWithSafeHtmlTooltipColumn;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabNetworkPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractMainTabWithDetailsTableView;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.inject.Inject;

public class MainTabNetworkView extends AbstractMainTabWithDetailsTableView<NetworkView, NetworkListModel> implements MainTabNetworkPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<MainTabNetworkView> {

        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private final String ENGINE_NETWORK_NAME =
            (String) AsyncDataProvider.GetConfigValuePreConverted(ConfigurationValues.ManagementNetwork);

    private final ApplicationConstants constants;
    private final ApplicationTemplates templates;

    private final SafeHtml mgmtImage;
    private final SafeHtml vmImage;
    private final SafeHtml emptyImage;

    @Inject
    public MainTabNetworkView(MainModelProvider<NetworkView, NetworkListModel> modelProvider,
            ApplicationConstants constants,
            ApplicationTemplates templates,
            ApplicationResources resources) {
        super(modelProvider);
        this.constants = constants;
        this.templates = templates;
        ViewIdHandler.idHandler.generateAndSetIds(this);
        mgmtImage = SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(resources.mgmtNetwork()).getHTML());
        vmImage = SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(resources.networkVm()).getHTML());
        emptyImage = SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(resources.networkEmpty()).getHTML());
        initTable();
        initWidget(getTable());
    }

    void initTable() {

        TextColumnWithTooltip<NetworkView> nameColumn = new TextColumnWithTooltip<NetworkView>() {
            @Override
            public String getValue(NetworkView object) {
                return object.getName();
            }
        };

        getTable().addColumn(nameColumn, constants.nameNetwork());

        TextColumnWithTooltip<NetworkView> dcColumn = new TextColumnWithTooltip<NetworkView>() {
            @Override
            public String getValue(NetworkView object) {
                return object.getStoragePoolName();
            }
        };

        getTable().addColumn(dcColumn, constants.dcNetwork());

        TextColumnWithTooltip<NetworkView> descriptionColumn = new TextColumnWithTooltip<NetworkView>(40) {
            @Override
            public String getValue(NetworkView object) {
                return object.getdescription();
            }
        };
        getTable().addColumn(descriptionColumn, constants.descriptionNetwork());

        SafeHtmlWithSafeHtmlTooltipColumn<NetworkView> roleColumn =
                new SafeHtmlWithSafeHtmlTooltipColumn<NetworkView>() {
                    @Override
                    public SafeHtml getValue(NetworkView networkView) {

                        String images = ""; //$NON-NLS-1$

                        if (ENGINE_NETWORK_NAME.equals(networkView.getName())) {

                            images = images.concat(mgmtImage.asString());
                        } else {
                            images = images.concat(emptyImage.asString());
                        }

                        if (networkView.isVmNetwork()) {

                            images = images.concat(vmImage.asString());
                        } else {
                            images = images.concat(emptyImage.asString());
                        }

                        return templates.image(SafeHtmlUtils.fromTrustedString(images));
                    }

                    @Override
                    public SafeHtml getTooltip(NetworkView networkView) {
                        String tooltip = ""; //$NON-NLS-1$
                        if (ENGINE_NETWORK_NAME.equals(networkView.getName())) {
                            tooltip =
                                    tooltip.concat(templates.imageTextSetupNetwork(mgmtImage,
                                            constants.managementItemInfo()).asString());
                        }

                        if (networkView.isVmNetwork()) {
                            if (!"".equals(tooltip)) //$NON-NLS-1$
                            {
                                tooltip = tooltip.concat("<BR>"); //$NON-NLS-1$
                            }
                            tooltip =
                                    tooltip.concat(templates.imageTextSetupNetwork(vmImage, constants.vmItemInfo())
                                            .asString());

                        }

                        return SafeHtmlUtils.fromTrustedString(tooltip);
                    }
                };

        getTable().addColumn(roleColumn, constants.roleNetwork(), "60px"); //$NON-NLS-1$

        TextColumnWithTooltip<NetworkView> vlanColumn = new TextColumnWithTooltip<NetworkView>() {
            @Override
            public String getValue(NetworkView object) {
                return object.getvlan_id() == null ? "-" : object.getvlan_id().toString(); //$NON-NLS-1$
            }
        };
        getTable().addColumn(vlanColumn, constants.vlanNetwork());

        getTable().addActionButton(new WebAdminButtonDefinition<NetworkView>(constants.newNetwork()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getNewCommand();
            }
        });
        getTable().addActionButton(new WebAdminButtonDefinition<NetworkView>(constants.editNetwork()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getEditCommand();
            }
        });
        getTable().addActionButton(new WebAdminButtonDefinition<NetworkView>(constants.removeNetwork()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getRemoveCommand();
            }
        });

    }
}
