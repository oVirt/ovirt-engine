package org.ovirt.engine.ui.webadmin.section.main.view.tab.network;

import java.util.Arrays;
import java.util.Comparator;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.comparators.LexoNumericComparator;
import org.ovirt.engine.core.common.businessentities.network.NetworkView;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.utils.PairQueryable;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.view.ViewRadioGroup;
import org.ovirt.engine.ui.common.widget.table.column.RxTxRateColumn;
import org.ovirt.engine.ui.common.widget.table.column.SafeHtmlWithSafeHtmlTooltipColumn;
import org.ovirt.engine.ui.common.widget.table.column.SimpleStatusColumnComparator;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkHostFilter;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkHostListModel;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.network.SubTabNetworkHostPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.host.InterfaceStatusImage;
import org.ovirt.engine.ui.webadmin.widget.table.column.HostStatusColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.WebAdminImageResourceColumn;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.RadioButton;

public class SubTabNetworkHostView extends AbstractSubTabTableView<NetworkView, PairQueryable<VdsNetworkInterface, VDS>, NetworkListModel, NetworkHostListModel>
        implements SubTabNetworkHostPresenter.ViewDef {

    private final ViewRadioGroup<NetworkHostFilter> viewRadioGroup;
    private final ApplicationConstants constants;
    private final ApplicationTemplates templates;

    private final SafeHtml labelImage;

    @Inject
    public SubTabNetworkHostView(SearchableDetailModelProvider<PairQueryable<VdsNetworkInterface, VDS>, NetworkListModel, NetworkHostListModel> modelProvider,
            ApplicationConstants constants,
            ApplicationTemplates templates,
            ApplicationResources resources) {
        super(modelProvider);
        this.constants = constants;
        this.templates = templates;
        viewRadioGroup = new ViewRadioGroup<NetworkHostFilter>(Arrays.asList(NetworkHostFilter.values()));
        viewRadioGroup.setSelectedValue(NetworkHostFilter.attached);
        viewRadioGroup.addStyleName("stnhv_radioGroup_pfly_fix"); //$NON-NLS-1$
        labelImage =
                SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(resources.tagImage()).getHTML());
        initTable();
        initWidget(getTable());
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

    private final TextColumnWithTooltip<PairQueryable<VdsNetworkInterface, VDS>> nameColumn = new TextColumnWithTooltip<PairQueryable<VdsNetworkInterface, VDS>>() {
        @Override
        public String getValue(PairQueryable<VdsNetworkInterface, VDS> object) {
            return object.getSecond().getName();
        }
    };

    private final TextColumnWithTooltip<PairQueryable<VdsNetworkInterface, VDS>> clusterColumn = new TextColumnWithTooltip<PairQueryable<VdsNetworkInterface, VDS>>() {
        @Override
        public String getValue(PairQueryable<VdsNetworkInterface, VDS> object) {
            return object.getSecond().getVdsGroupName();
        }
    };

    private final TextColumnWithTooltip<PairQueryable<VdsNetworkInterface, VDS>> dcColumn = new TextColumnWithTooltip<PairQueryable<VdsNetworkInterface, VDS>>() {
        @Override
        public String getValue(PairQueryable<VdsNetworkInterface, VDS> object) {
            return object.getSecond().getStoragePoolName();
        }
    };

    WebAdminImageResourceColumn<PairQueryable<VdsNetworkInterface, VDS>> nicStatusColumn = new WebAdminImageResourceColumn<PairQueryable<VdsNetworkInterface, VDS>>(){

        @Override
        public ImageResource getValue(PairQueryable<VdsNetworkInterface, VDS> object) {
            if (object.getFirst() != null){
                return InterfaceStatusImage.getResource(object.getFirst().getStatistics().getStatus());
            }
            return null;
        }
    };

    private final SafeHtmlWithSafeHtmlTooltipColumn<PairQueryable<VdsNetworkInterface, VDS>> nicColumn =
            new SafeHtmlWithSafeHtmlTooltipColumn<PairQueryable<VdsNetworkInterface, VDS>>() {
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

    private final TextColumnWithTooltip<PairQueryable<VdsNetworkInterface, VDS>> speedColumn =
            new TextColumnWithTooltip<PairQueryable<VdsNetworkInterface, VDS>>() {
                @Override
                public String getValue(PairQueryable<VdsNetworkInterface, VDS> object) {
                    if (object.getFirst() != null && object.getFirst().getSpeed() != null) {
                        return String.valueOf(object.getFirst().getSpeed());
                    }
                    return null;
                }
            };

    private final TextColumnWithTooltip<PairQueryable<VdsNetworkInterface, VDS>> nicRxColumn = new RxTxRateColumn<PairQueryable<VdsNetworkInterface, VDS>>() {
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

    private final TextColumnWithTooltip<PairQueryable<VdsNetworkInterface, VDS>> nicTxColumn = new RxTxRateColumn<PairQueryable<VdsNetworkInterface, VDS>>() {
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

    private void handleRadioButtonClick(ClickEvent event) {
        getDetailModel().setViewFilterType((viewRadioGroup.getSelectedValue()));

        boolean attached = viewRadioGroup.getSelectedValue() == NetworkHostFilter.attached;

        getTable().ensureColumnPresent(hostStatus, constants.empty(), true, "30px"); //$NON-NLS-1$
        getTable().ensureColumnPresent(nameColumn, constants.nameHost(), true, "200px"); //$NON-NLS-1$
        getTable().ensureColumnPresent(clusterColumn, constants.clusterHost(), true, "200px"); //$NON-NLS-1$
        getTable().ensureColumnPresent(dcColumn, constants.dcHost(), true, "200px"); //$NON-NLS-1$
        getTable().ensureColumnPresent(nicStatusColumn, constants.statusNetworkHost(), attached, "140px"); //$NON-NLS-1$
        getTable().ensureColumnPresent(nicColumn, constants.nicNetworkHost(), attached, "200px"); //$NON-NLS-1$
        getTable().ensureColumnPresent(speedColumn, constants.speedNetworkHost(), attached, "200px"); //$NON-NLS-1$
        getTable().ensureColumnPresent(nicRxColumn,
                templates.sub(constants.rxNetworkHost(), constants.mbps()).asString(),
                attached,
                "200px"); //$NON-NLS-1$
        getTable().ensureColumnPresent(nicTxColumn,
                templates.sub(constants.txNetworkHost(), constants.mbps()).asString(),
                attached,
                "200px"); //$NON-NLS-1$
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
        clusterColumn.makeSortable();
        dcColumn.makeSortable();
        nicStatusColumn.makeSortable(new SimpleStatusColumnComparator<PairQueryable<VdsNetworkInterface, VDS>>(nicStatusColumn));
        nicColumn.makeSortable(new Comparator<PairQueryable<VdsNetworkInterface, VDS>>() {

            private LexoNumericComparator lexoNumeric = new LexoNumericComparator();

            @Override
            public int compare(PairQueryable<VdsNetworkInterface, VDS> o1, PairQueryable<VdsNetworkInterface, VDS> o2) {
                String name1 = (o1.getFirst() == null) ? null : o1.getFirst().getName();
                String name2 = (o2.getFirst() == null) ? null : o2.getFirst().getName();
                return lexoNumeric.compare(name1, name2);
            }
        });
        speedColumn.makeSortable();
    }
}

