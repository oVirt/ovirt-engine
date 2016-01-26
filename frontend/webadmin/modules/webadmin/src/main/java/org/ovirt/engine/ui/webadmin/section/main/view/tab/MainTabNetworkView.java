package org.ovirt.engine.ui.webadmin.section.main.view.tab;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.network.NetworkView;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.searchbackend.NetworkConditionFieldAutoCompleter;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractLinkColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractSafeHtmlColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ApplicationModeHelper;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabNetworkPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractMainTabWithDetailsTableView;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.table.column.CommentColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.MultiImageColumnHelper;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.inject.Inject;

public class MainTabNetworkView extends AbstractMainTabWithDetailsTableView<NetworkView, NetworkListModel> implements MainTabNetworkPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<MainTabNetworkView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private final SafeHtml vmImage;
    private final SafeHtml emptyImage;

    private AbstractLinkColumn<NetworkView> providerColumn;

    private static final ApplicationResources resources = AssetProvider.getResources();
    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public MainTabNetworkView(MainModelProvider<NetworkView, NetworkListModel> modelProvider) {
        super(modelProvider);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        vmImage = SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(resources.networkVm()).getHTML());
        emptyImage = SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(resources.networkEmpty()).getHTML());
        initTable();
        initWidget(getTable());
    }

    void initTable() {
        getTable().enableColumnResizing();

        AbstractTextColumn<NetworkView> nameColumn = new AbstractTextColumn<NetworkView>() {
            @Override
            public String getValue(NetworkView object) {
                return object.getName();
            }
        };
        nameColumn.makeSortable(NetworkConditionFieldAutoCompleter.NAME);

        getTable().addColumn(nameColumn, constants.nameNetwork(), "200px"); //$NON-NLS-1$

        CommentColumn<NetworkView> commentColumn = new CommentColumn<>();
        getTable().addColumnWithHtmlHeader(commentColumn,
                SafeHtmlUtils.fromSafeConstant(constants.commentLabel()),
                "75px"); //$NON-NLS-1$
        boolean virtMode = ApplicationModeHelper.isModeSupported(ApplicationMode.VirtOnly);

        AbstractTextColumn<NetworkView> dcColumn = new AbstractTextColumn<NetworkView>() {
            @Override
            public String getValue(NetworkView object) {
                return object.getDataCenterName();
            }
        };
        dcColumn.makeSortable(NetworkConditionFieldAutoCompleter.DATA_CENTER);

        getTable().ensureColumnVisible(dcColumn, constants.dcNetwork(), virtMode, "200px"); //$NON-NLS-1$


        AbstractTextColumn<NetworkView> descriptionColumn = new AbstractTextColumn<NetworkView>() {
            @Override
            public String getValue(NetworkView object) {
                return object.getDescription();
            }
        };
        descriptionColumn.makeSortable(NetworkConditionFieldAutoCompleter.DESCRIPTION);
        getTable().addColumn(descriptionColumn, constants.descriptionNetwork(), "300px"); //$NON-NLS-1$

        AbstractSafeHtmlColumn<NetworkView> roleColumn =
                new AbstractSafeHtmlColumn<NetworkView>() {
                    @Override
                    public SafeHtml getValue(NetworkView networkView) {

                        List<SafeHtml> images = new LinkedList<>();

                        if (networkView.isVmNetwork()) {

                            images.add(vmImage);
                        } else {
                            images.add(emptyImage);
                        }

                        return MultiImageColumnHelper.getValue(images);
                    }

                    @Override
                    public SafeHtml getTooltip(NetworkView networkView) {
                        Map<SafeHtml, String> imagesToText = new LinkedHashMap<>();
                        if (networkView.isVmNetwork()) {
                            imagesToText.put(vmImage, constants.vmItemInfo());

                        }

                        return MultiImageColumnHelper.getTooltip(imagesToText);
                    }
                };

        getTable().addColumn(roleColumn, constants.roleNetwork(), "60px"); //$NON-NLS-1$

         AbstractTextColumn<NetworkView> vlanColumn = new AbstractTextColumn<NetworkView>() {
            @Override
            public String getValue(NetworkView object) {
                return object.getVlanId() == null ? "-" : object.getVlanId().toString(); //$NON-NLS-1$
            }
        };
        vlanColumn.makeSortable(NetworkConditionFieldAutoCompleter.VLAN_ID);
        getTable().ensureColumnVisible(vlanColumn, constants.vlanNetwork(), virtMode, "60px"); //$NON-NLS-1$

        AbstractTextColumn<NetworkView> qosColumn = new AbstractTextColumn<NetworkView>() {
            @Override
            public String getValue(NetworkView object) {
                return object.getQosName() == null ? "-" : object.getQosName().toString();//$NON-NLS-1$
            }
        };
        qosColumn.makeSortable(NetworkConditionFieldAutoCompleter.QOS);
        getTable().ensureColumnVisible(qosColumn, constants.qosName(), virtMode, "60px");//$NON-NLS-1$

        AbstractTextColumn<NetworkView> labelColumn = new AbstractTextColumn<NetworkView>() {
            @Override
            public String getValue(NetworkView object) {
                return object.getLabel() == null ? "-" : object.getLabel(); //$NON-NLS-1$
            }
        };
        labelColumn.makeSortable(NetworkConditionFieldAutoCompleter.LABEL);
        getTable().addColumn(labelColumn, constants.networkLabelNetworksTab(), "200px"); //$NON-NLS-1$

        providerColumn = new AbstractLinkColumn<NetworkView>() {
            @Override
            public String getValue(NetworkView object) {
                return object.getProvidedBy() == null ? "" : object.getProviderName(); //$NON-NLS-1$
            }
        };
        providerColumn.makeSortable(NetworkConditionFieldAutoCompleter.PROVIDER_NAME);
        getTable().ensureColumnVisible(providerColumn, constants.providerNetwork(), virtMode, "200px"); //$NON-NLS-1$

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

    @Override
    public void setProviderClickHandler(final FieldUpdater<NetworkView, String> fieldUpdater) {
        providerColumn.setFieldUpdater(new FieldUpdater<NetworkView, String>() {

            @Override
            public void update(int index, NetworkView object, String value) {
                getTable().getSelectionModel().clear(); // this to avoid problems with a null active details model
                fieldUpdater.update(index, object, value);
            }
        });
    }
}
