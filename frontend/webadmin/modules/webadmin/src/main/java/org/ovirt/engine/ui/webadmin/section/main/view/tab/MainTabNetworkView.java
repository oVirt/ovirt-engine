package org.ovirt.engine.ui.webadmin.section.main.view.tab;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.network.NetworkView;
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
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabNetworkPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractMainTabWithDetailsTableView;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.table.column.CommentColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.NetworkRoleColumnHelper;

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
            (String) AsyncDataProvider.getConfigValuePreConverted(ConfigurationValues.ManagementNetwork);

    private final ApplicationConstants constants;

    private final SafeHtml mgmtImage;
    private final SafeHtml vmImage;
    private final SafeHtml emptyImage;

    @Inject
    public MainTabNetworkView(MainModelProvider<NetworkView, NetworkListModel> modelProvider,
            ApplicationConstants constants,
            ApplicationResources resources) {
        super(modelProvider);
        this.constants = constants;
        ViewIdHandler.idHandler.generateAndSetIds(this);
        mgmtImage = SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(resources.mgmtNetwork()).getHTML());
        vmImage = SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(resources.networkVm()).getHTML());
        emptyImage = SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(resources.networkEmpty()).getHTML());
        initTable();
        initWidget(getTable());
    }

    void initTable() {
        getTable().enableColumnResizing();

        TextColumnWithTooltip<NetworkView> nameColumn = new TextColumnWithTooltip<NetworkView>() {
            @Override
            public String getValue(NetworkView object) {
                return object.getName();
            }
        };

        getTable().addColumn(nameColumn, constants.nameNetwork(), "200px"); //$NON-NLS-1$

        TextColumnWithTooltip<NetworkView> dcColumn = new TextColumnWithTooltip<NetworkView>() {
            @Override
            public String getValue(NetworkView object) {
                return object.getDataCenterName();
            }
        };

        getTable().addColumn(dcColumn, constants.dcNetwork(), "200px"); //$NON-NLS-1$

        TextColumnWithTooltip<NetworkView> descriptionColumn = new TextColumnWithTooltip<NetworkView>() {
            @Override
            public String getValue(NetworkView object) {
                return object.getDescription();
            }
        };
        getTable().addColumn(descriptionColumn, constants.descriptionNetwork(), "300px"); //$NON-NLS-1$

        getTable().addColumn(new CommentColumn<NetworkView>(), constants.commentLabel(), "100px"); //$NON-NLS-1$

        SafeHtmlWithSafeHtmlTooltipColumn<NetworkView> roleColumn =
                new SafeHtmlWithSafeHtmlTooltipColumn<NetworkView>() {
                    @Override
                    public SafeHtml getValue(NetworkView networkView) {

                        List<SafeHtml> images = new LinkedList<SafeHtml>();

                        if (ENGINE_NETWORK_NAME.equals(networkView.getName())) {

                            images.add(mgmtImage);
                        } else {
                            images.add(emptyImage);
                        }

                        if (networkView.isVmNetwork()) {

                            images.add(vmImage);
                        } else {
                            images.add(emptyImage);
                        }

                        return NetworkRoleColumnHelper.getValue(images);
                    }

                    @Override
                    public SafeHtml getTooltip(NetworkView networkView) {
                        Map<SafeHtml, String> imagesToText = new LinkedHashMap<SafeHtml, String>();
                        if (ENGINE_NETWORK_NAME.equals(networkView.getName())) {
                            imagesToText.put(mgmtImage, constants.managementItemInfo());
                        }

                        if (networkView.isVmNetwork()) {
                            imagesToText.put(vmImage, constants.vmItemInfo());

                        }

                        return NetworkRoleColumnHelper.getTooltip(imagesToText);
                    }
                };

        getTable().addColumn(roleColumn, constants.roleNetwork(), "60px"); //$NON-NLS-1$

        TextColumnWithTooltip<NetworkView> vlanColumn = new TextColumnWithTooltip<NetworkView>() {
            @Override
            public String getValue(NetworkView object) {
                return object.getVlanId() == null ? "-" : object.getVlanId().toString(); //$NON-NLS-1$
            }
        };
        getTable().addColumn(vlanColumn, constants.vlanNetwork(), "200px"); //$NON-NLS-1$

        TextColumnWithTooltip<NetworkView> providerColumn = new TextColumnWithTooltip<NetworkView>() {
            @Override
            public String getValue(NetworkView object) {
                return object.getProvidedBy() == null ? "-" : object.getProviderName(); // $NON-NLS-1$
            }
        };
        getTable().addColumn(providerColumn, constants.providerNetwork(), "200px"); //$NON-NLS-1$

        getTable().addActionButton(new WebAdminButtonDefinition<NetworkView>(constants.newNetwork()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getNewCommand();
            }
        });
        getTable().addActionButton(new WebAdminButtonDefinition<NetworkView>(constants.importNetwork()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getImportCommand();
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
