package org.ovirt.engine.ui.webadmin.section.main.view.tab.network;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.RadioButton;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.comparators.LexoNumericComparator;
import org.ovirt.engine.core.common.businessentities.network.NetworkView;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.utils.PairQueryable;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.view.ViewRadioGroup;
import org.ovirt.engine.ui.common.widget.table.column.AbstractImageResourceColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractNullableNumberColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractRxTxRateColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractSafeHtmlColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.common.widget.table.column.SimpleStatusColumnComparator;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkHostFilter;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkHostListModel;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.network.SubTabNetworkHostPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.host.InterfaceStatusImage;
import org.ovirt.engine.ui.webadmin.widget.table.column.HostStatusColumn;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Comparator;

public class SubTabNetworkHostView extends AbstractSubTabTableView<NetworkView, PairQueryable<VdsNetworkInterface, VDS>, NetworkListModel, NetworkHostListModel>
        implements SubTabNetworkHostPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabNetworkHostView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private final ViewRadioGroup<NetworkHostFilter> viewRadioGroup;
    private final SafeHtml labelImage;

    private final static ApplicationTemplates templates = AssetProvider.getTemplates();
    private final static ApplicationResources resources = AssetProvider.getResources();
    private final static ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public SubTabNetworkHostView(SearchableDetailModelProvider<PairQueryable<VdsNetworkInterface, VDS>, NetworkListModel, NetworkHostListModel> modelProvider) {
        super(modelProvider);
        viewRadioGroup = new ViewRadioGroup<NetworkHostFilter>(Arrays.asList(NetworkHostFilter.values()));
        viewRadioGroup.setSelectedValue(NetworkHostFilter.attached);
        viewRadioGroup.addStyleName("stnhv_radioGroup_pfly_fix"); //$NON-NLS-1$
        labelImage =
                SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(resources.tagImage()).getHTML());
        initTable();
        initWidget(getTable());
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    void initTableOverhead() {
        viewRadioGroup.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (((RadioButton) event.getSource()).getValue()) {
                    handleRadioButtonClick(event);
                }
            }
        });

        getTable().setTableOverhead(viewRadioGroup);
        getTable().setTableTopMargin(20);
    }

    private final HostStatusColumn<PairQueryable<VdsNetworkInterface, VDS>> hostStatus = new HostStatusColumn<PairQueryable<VdsNetworkInterface, VDS>>();

    private final AbstractTextColumn<PairQueryable<VdsNetworkInterface, VDS>> nameColumn = new AbstractTextColumn<PairQueryable<VdsNetworkInterface, VDS>>() {
        @Override
        public String getValue(PairQueryable<VdsNetworkInterface, VDS> object) {
            return object.getSecond().getName();
        }
    };

    private final AbstractTextColumn<PairQueryable<VdsNetworkInterface, VDS>> clusterColumn = new AbstractTextColumn<PairQueryable<VdsNetworkInterface, VDS>>() {
        @Override
        public String getValue(PairQueryable<VdsNetworkInterface, VDS> object) {
            return object.getSecond().getVdsGroupName();
        }
    };

    private final AbstractTextColumn<PairQueryable<VdsNetworkInterface, VDS>> dcColumn = new AbstractTextColumn<PairQueryable<VdsNetworkInterface, VDS>>() {
        @Override
        public String getValue(PairQueryable<VdsNetworkInterface, VDS> object) {
            return object.getSecond().getStoragePoolName();
        }
    };

    AbstractImageResourceColumn<PairQueryable<VdsNetworkInterface, VDS>> nicStatusColumn = new AbstractImageResourceColumn<PairQueryable<VdsNetworkInterface, VDS>>(){

        @Override
        public ImageResource getValue(PairQueryable<VdsNetworkInterface, VDS> object) {
            if (object.getFirst() != null){
                return InterfaceStatusImage.getResource(object.getFirst().getStatistics().getStatus());
            }
            return null;
        }
    };

    private final AbstractImageResourceColumn<PairQueryable<VdsNetworkInterface, VDS>> hostOutOfSync =
            new AbstractImageResourceColumn<PairQueryable<VdsNetworkInterface, VDS>>() {

                @Override
                public ImageResource getValue(PairQueryable<VdsNetworkInterface, VDS> object) {
                    return (object.getFirst() == null || object.getFirst().getNetworkImplementationDetails().isInSync()) ? null
                            : resources.networkNotSyncImage();
                }

            };


    private final AbstractSafeHtmlColumn<PairQueryable<VdsNetworkInterface, VDS>> nicColumn =
            new AbstractSafeHtmlColumn<PairQueryable<VdsNetworkInterface, VDS>>() {
                @Override
                public SafeHtml getValue(PairQueryable<VdsNetworkInterface, VDS> object) {
                    if (object.getFirst() != null) {
                        VdsNetworkInterface nic = object.getFirst();
                        return getDetailModel().isInterfaceAttachedByLabel(nic) ? templates.textImageLabels(nic.getName(),
                                labelImage)
                                : SafeHtmlUtils.fromTrustedString(nic.getName());
                    }
                    return null;
                }

                @Override
                public SafeHtml getTooltip(PairQueryable<VdsNetworkInterface, VDS> object) {
                    return object.getFirst() != null
                            && getDetailModel().isInterfaceAttachedByLabel(object.getFirst()) ? SafeHtmlUtils.fromTrustedString(getDetailModel().getEntity()
                            .getLabel())
                            : null;
                }
            };

    private final AbstractTextColumn<PairQueryable<VdsNetworkInterface, VDS>> speedColumn =
            new AbstractTextColumn<PairQueryable<VdsNetworkInterface, VDS>>() {
                @Override
                public String getValue(PairQueryable<VdsNetworkInterface, VDS> object) {
                    if (object.getFirst() != null && object.getFirst().getSpeed() != null) {
                        return String.valueOf(object.getFirst().getSpeed());
                    }
                    return null;
                }
            };

    private final AbstractTextColumn<PairQueryable<VdsNetworkInterface, VDS>> nicRxColumn = new AbstractRxTxRateColumn<PairQueryable<VdsNetworkInterface, VDS>>() {
        @Override
        protected Double getRate(PairQueryable<VdsNetworkInterface, VDS> object) {
            if (object.getFirst() != null){
                return object.getFirst().getStatistics().getReceiveRate();
            }
            return null;
        }

        @Override
        protected Double getSpeed(PairQueryable<VdsNetworkInterface, VDS> object) {
            if (object.getFirst() != null && object.getFirst().getSpeed() != null) {
                return object.getFirst().getSpeed().doubleValue();
            } else {
                return null;
            }
        }
    };

    private final AbstractTextColumn<PairQueryable<VdsNetworkInterface, VDS>> nicTxColumn = new AbstractRxTxRateColumn<PairQueryable<VdsNetworkInterface, VDS>>() {
        @Override
        protected Double getRate(PairQueryable<VdsNetworkInterface, VDS> object) {
            if (object.getFirst() != null){
                return object.getFirst().getStatistics().getTransmitRate();
            }else{
                return null;
            }
        }

        @Override
        protected Double getSpeed(PairQueryable<VdsNetworkInterface, VDS> object) {
            if (object.getFirst() != null && object.getFirst().getSpeed() != null) {
                return object.getFirst().getSpeed().doubleValue();
            } else {
                return null;
            }
        }
    };

    private final AbstractTextColumn<PairQueryable<VdsNetworkInterface, VDS>> totalRxColumn =
            new AbstractNullableNumberColumn<PairQueryable<VdsNetworkInterface, VDS>>() {
                @Override
                protected Number getRawValue(PairQueryable<VdsNetworkInterface, VDS> object) {
                    return object.getFirst() == null ? null : object.getFirst().getStatistics().getReceivedBytes();
                }
            };

    private final AbstractTextColumn<PairQueryable<VdsNetworkInterface, VDS>> totalTxColumn =
            new AbstractNullableNumberColumn<PairQueryable<VdsNetworkInterface, VDS>>() {
                @Override
                protected Number getRawValue(PairQueryable<VdsNetworkInterface, VDS> object) {
                    return object.getFirst() == null ? null : object.getFirst().getStatistics().getTransmittedBytes();
                }
            };

    private void handleRadioButtonClick(ClickEvent event) {
        getDetailModel().setViewFilterType((viewRadioGroup.getSelectedValue()));

        boolean attached = viewRadioGroup.getSelectedValue() == NetworkHostFilter.attached;

        getTable().ensureColumnPresent(hostStatus, constants.empty(), true, "30px"); //$NON-NLS-1$
        getTable().ensureColumnPresent(nameColumn, constants.nameHost(), true, "200px"); //$NON-NLS-1$
        getTable().ensureColumnPresent(clusterColumn, constants.clusterHost(), true, "200px"); //$NON-NLS-1$
        getTable().ensureColumnPresent(dcColumn, constants.dcHost(), true, "200px"); //$NON-NLS-1$
        getTable().ensureColumnPresent(nicStatusColumn, constants.statusNetworkHost(), attached, "140px"); //$NON-NLS-1$
        getTable().ensureColumnPresent(hostOutOfSync, constants.hostOutOfSync(), attached, "75px"); //$NON-NLS-1$
        getTable().ensureColumnPresent(nicColumn, constants.nicNetworkHost(), attached, "100px"); //$NON-NLS-1$
        getTable().ensureColumnPresent(speedColumn,
                templates.sub(constants.speedNetworkHost(), constants.mbps()).asString(),
                attached,
                "100px"); //$NON-NLS-1$
        getTable().ensureColumnPresent(nicRxColumn,
                templates.sub(constants.rxNetworkHost(), constants.mbps()).asString(),
                attached,
                "100px"); //$NON-NLS-1$
        getTable().ensureColumnPresent(nicTxColumn,
                templates.sub(constants.txNetworkHost(), constants.mbps()).asString(),
                attached,
                "100px"); //$NON-NLS-1$
        getTable().ensureColumnPresent(totalRxColumn,
                templates.sub(constants.rxTotal(), constants.bytes()).asString(),
                attached,
                "150px"); //$NON-NLS-1$
        getTable().ensureColumnPresent(totalTxColumn,
                templates.sub(constants.txTotal(), constants.bytes()).asString(),
                attached,
                "150px"); //$NON-NLS-1$
    }

    void initTable() {
        getTable().enableColumnResizing();
        initTableOverhead();
        handleRadioButtonClick(null);
        initSorting();

        getTable().addActionButton(new WebAdminButtonDefinition<PairQueryable<VdsNetworkInterface, VDS>>(constants.setupHostNetworksInterface()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getSetupNetworksCommand();
            }
        });
    }

    private void initSorting() {
        hostStatus.makeSortable();
        nameColumn.makeSortable();
        hostOutOfSync.makeSortable((new Comparator<PairQueryable<VdsNetworkInterface, VDS>>() {

            @Override
            public int compare(PairQueryable<VdsNetworkInterface, VDS> o1,
                    PairQueryable<VdsNetworkInterface, VDS> o2) {
                boolean syncStatus1 =
                        (o1.getFirst() == null) ? false : o1.getFirst().getNetworkImplementationDetails().isInSync();
                boolean syncStatus2 =
                        (o2.getFirst() == null) ? false : o2.getFirst().getNetworkImplementationDetails().isInSync();
                return Boolean.compare(syncStatus1, syncStatus2);
            }

        }));

        clusterColumn.makeSortable();
        dcColumn.makeSortable();
        nicStatusColumn.makeSortable(new SimpleStatusColumnComparator<PairQueryable<VdsNetworkInterface, VDS>>(nicStatusColumn));
        nicColumn.makeSortable(new Comparator<PairQueryable<VdsNetworkInterface, VDS>>() {

            private final LexoNumericComparator lexoNumeric = new LexoNumericComparator();

            @Override
            public int compare(PairQueryable<VdsNetworkInterface, VDS> o1, PairQueryable<VdsNetworkInterface, VDS> o2) {
                String name1 = (o1.getFirst() == null) ? null : o1.getFirst().getName();
                String name2 = (o2.getFirst() == null) ? null : o2.getFirst().getName();
                return lexoNumeric.compare(name1, name2);
            }
        });
        speedColumn.makeSortable();
        nicRxColumn.makeSortable();
        nicTxColumn.makeSortable();
        totalRxColumn.makeSortable();
        totalTxColumn.makeSortable();
    }
}

