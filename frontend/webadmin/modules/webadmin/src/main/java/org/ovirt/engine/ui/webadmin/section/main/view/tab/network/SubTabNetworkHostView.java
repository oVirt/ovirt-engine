package org.ovirt.engine.ui.webadmin.section.main.view.tab.network;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.comparators.LexoNumericComparator;
import org.ovirt.engine.core.common.businessentities.network.NetworkView;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.utils.PairQueryable;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.presenter.FragmentParams;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.view.ViewRadioGroup;
import org.ovirt.engine.ui.common.widget.renderer.RxTxTotalRenderer;
import org.ovirt.engine.ui.common.widget.table.column.AbstractImageResourceColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractLinkColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractRenderedTextColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractRxTxRateColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractSafeHtmlColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkHostFilter;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkHostListModel;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.network.SubTabNetworkHostPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.widget.host.InterfaceStatusImage;
import org.ovirt.engine.ui.webadmin.widget.table.column.HostStatusColumn;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

public class SubTabNetworkHostView extends AbstractSubTabTableView<NetworkView, PairQueryable<VdsNetworkInterface, VDS>, NetworkListModel, NetworkHostListModel>
        implements SubTabNetworkHostPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabNetworkHostView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private final ViewRadioGroup<NetworkHostFilter> viewRadioGroup;
    private final SafeHtml labelImage;

    private static final ApplicationTemplates templates = AssetProvider.getTemplates();
    private static final ApplicationResources resources = AssetProvider.getResources();
    private static final ApplicationConstants constants = AssetProvider.getConstants();
    private static final RxTxTotalRenderer totalRenderer = new RxTxTotalRenderer();

    @Inject
    public SubTabNetworkHostView(SearchableDetailModelProvider<PairQueryable<VdsNetworkInterface, VDS>, NetworkListModel, NetworkHostListModel> modelProvider) {
        super(modelProvider);

        viewRadioGroup = new ViewRadioGroup<>(Arrays.asList(NetworkHostFilter.values()));
        viewRadioGroup.setSelectedValue(NetworkHostFilter.attached);
        viewRadioGroup.addChangeHandler(selected -> onRadioButtonChange(selected));

        labelImage = SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(resources.tagImage()).getHTML());

        initTable();
        initWidget(getTableContainer());
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    void initTableOverhead() {
        getTable().setTableOverhead(viewRadioGroup);
    }

    private final HostStatusColumn<PairQueryable<VdsNetworkInterface, VDS>> hostStatus = new HostStatusColumn<PairQueryable<VdsNetworkInterface, VDS>>() {
        {
            setContextMenuTitle(constants.statusHost());
        }
    };

    private final AbstractTextColumn<PairQueryable<VdsNetworkInterface, VDS>> nameColumn =
            new AbstractLinkColumn<PairQueryable<VdsNetworkInterface, VDS>>(
                    new FieldUpdater<PairQueryable<VdsNetworkInterface, VDS>, String>() {
        @Override
        public void update(int index, PairQueryable<VdsNetworkInterface, VDS> host, String value) {
            Map<String, String> parameters = new HashMap<>();
            parameters.put(FragmentParams.NAME.getName(), host.getSecond().getName());
            getPlaceTransitionHandler().handlePlaceTransition(
                    WebAdminApplicationPlaces.hostGeneralSubTabPlace, parameters);
        }
    }) {
        @Override
        public String getValue(PairQueryable<VdsNetworkInterface, VDS> object) {
            return object.getSecond().getName();
        }
    };


    private final AbstractTextColumn<PairQueryable<VdsNetworkInterface, VDS>> clusterColumn = new AbstractTextColumn<PairQueryable<VdsNetworkInterface, VDS>>() {
        @Override
        public String getValue(PairQueryable<VdsNetworkInterface, VDS> object) {
            return object.getSecond().getClusterName();
        }
    };

    private final AbstractTextColumn<PairQueryable<VdsNetworkInterface, VDS>> dcColumn = new AbstractTextColumn<PairQueryable<VdsNetworkInterface, VDS>>() {
        @Override
        public String getValue(PairQueryable<VdsNetworkInterface, VDS> object) {
            return object.getSecond().getStoragePoolName();
        }
    };

    AbstractSafeHtmlColumn<PairQueryable<VdsNetworkInterface, VDS>> nicStatusColumn = new AbstractSafeHtmlColumn<PairQueryable<VdsNetworkInterface, VDS>>(){

        @Override
        public SafeHtml getValue(PairQueryable<VdsNetworkInterface, VDS> object) {
            if (object.getFirst() != null) {
                ImageResource imageResource
                        = InterfaceStatusImage.getResource(object.getFirst().getStatistics().getStatus());

                SafeHtml nicStatus
                        = SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(imageResource).getHTML());

                if (isNetworkUpdating(object)) {
                    return templates.networkDeviceStatusImgAndNetworkOperationInProgress(nicStatus,
                            constants.networksUpdating());

                } else {
                    return templates.networkDeviceStatusImg(nicStatus);
                }
            } else if (isNetworkUpdating(object)) {
                return templates.networkOperationInProgressDiv(constants.networksUpdating());
            }
            return null;
        }

        @Override
        public SafeHtml getTooltip(PairQueryable<VdsNetworkInterface, VDS> object) {
            return isNetworkUpdating(object) ? SafeHtmlUtils.fromTrustedString(constants.networksUpdating()) : null;
        }

        private boolean isNetworkUpdating(PairQueryable<VdsNetworkInterface, VDS> object) {
            return object.getSecond() != null && object.getSecond().isNetworkOperationInProgress();
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

    private final AbstractRenderedTextColumn<PairQueryable<VdsNetworkInterface, VDS>, Number> totalRxColumn =
            new AbstractRenderedTextColumn<PairQueryable<VdsNetworkInterface, VDS>, Number>(totalRenderer) {
                @Override
                protected Number getRawValue(PairQueryable<VdsNetworkInterface, VDS> object) {
                    return object.getFirst() == null ? null : object.getFirst().getStatistics().getReceivedBytes();
                }
            };

    private final AbstractRenderedTextColumn<PairQueryable<VdsNetworkInterface, VDS>, Number> totalTxColumn =
            new AbstractRenderedTextColumn<PairQueryable<VdsNetworkInterface, VDS>, Number>(totalRenderer) {
                @Override
                protected Number getRawValue(PairQueryable<VdsNetworkInterface, VDS> object) {
                    return object.getFirst() == null ? null : object.getFirst().getStatistics().getTransmittedBytes();
                }
            };

    private void onRadioButtonChange(NetworkHostFilter selected) {
        getDetailModel().setViewFilterType(selected);

        boolean attached = selected == NetworkHostFilter.attached;

        getTable().ensureColumnVisible(hostStatus, constants.empty(), true, "30px"); //$NON-NLS-1$
        getTable().ensureColumnVisible(nameColumn, constants.nameHost(), true, "200px"); //$NON-NLS-1$
        getTable().ensureColumnVisible(clusterColumn, constants.clusterHost(), true, "200px"); //$NON-NLS-1$
        getTable().ensureColumnVisible(dcColumn, constants.dcHost(), true, "200px"); //$NON-NLS-1$
        getTable().ensureColumnVisible(nicStatusColumn, constants.statusNetworkHost(), attached, "175px"); //$NON-NLS-1$
        getTable().ensureColumnVisible(hostOutOfSync, constants.hostOutOfSync(), attached, "95px"); //$NON-NLS-1$
        getTable().ensureColumnVisible(nicColumn, constants.nicNetworkHost(), attached, "140px"); //$NON-NLS-1$
        getTable().ensureColumnVisible(speedColumn,
                templates.sub(constants.speedNetworkHost(), constants.mbps()).asString(),
                attached,
                "100px"); //$NON-NLS-1$
        getTable().ensureColumnVisible(nicRxColumn,
                templates.sub(constants.rxNetworkHost(), constants.mbps()).asString(),
                attached,
                "100px"); //$NON-NLS-1$
        getTable().ensureColumnVisible(nicTxColumn,
                templates.sub(constants.txNetworkHost(), constants.mbps()).asString(),
                attached,
                "100px"); //$NON-NLS-1$
        getTable().ensureColumnVisible(totalRxColumn,
                templates.sub(constants.rxTotal(), constants.bytes()).asString(),
                attached,
                "150px"); //$NON-NLS-1$
        getTable().ensureColumnVisible(totalTxColumn,
                templates.sub(constants.txTotal(), constants.bytes()).asString(),
                attached,
                "150px"); //$NON-NLS-1$
    }

    void initTable() {
        getTable().enableColumnResizing();
        initTableOverhead();
        onRadioButtonChange(viewRadioGroup.getSelectedValue());
        initSorting();
    }

    private void initSorting() {
        hostStatus.makeSortable();
        nameColumn.makeSortable();
        hostOutOfSync.makeSortable(Comparator.comparing((PairQueryable<VdsNetworkInterface, VDS> p) -> p.getFirst(), Comparator.comparing(i -> i != null && i.getNetworkImplementationDetails().isInSync())));

        clusterColumn.makeSortable();
        dcColumn.makeSortable();
        nicStatusColumn.makeSortable(Comparator.comparing(
            (PairQueryable<VdsNetworkInterface, VDS> p) -> p.getFirst(), Comparator.comparing(i -> i.getStatistics().getStatus())));
        nicColumn.makeSortable(Comparator.comparing(o -> o.getFirst() == null ? null : o.getFirst().getName(),
                new LexoNumericComparator()));

        speedColumn.makeSortable();
        nicRxColumn.makeSortable();
        nicTxColumn.makeSortable();
        totalRxColumn.makeSortable();
        totalTxColumn.makeSortable();
    }
}

