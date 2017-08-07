package org.ovirt.engine.ui.webadmin.section.main.view.tab;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.Label;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.labels.list.AffinityLabelListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;

public abstract class AbstractSubTabAffinityLabelsView<I, M extends ListWithDetailsModel, T extends AffinityLabelListModel<?>> extends AbstractSubTabTableView<I, Label, M, T> {

    protected static final ApplicationConstants constants = AssetProvider.getConstants();

    private final Map<Guid, String> entitiesNameMap = new HashMap<>();

    public AbstractSubTabAffinityLabelsView(SearchableDetailModelProvider<Label, M, T> modelProvider) {
        super(modelProvider);
        generateIds();
        updateEntitiesNameMap();
        getDetailModel().getEntitiesNameMap().getEntityChangedEvent().addListener((ev, sender, args) -> {
            updateEntitiesNameMap();
        });
        initTable();
        initWidget(getTableContainer());
    }

    private void updateEntitiesNameMap() {
        entitiesNameMap.putAll(getDetailModel().getEntitiesNameMap().getEntity());
    }

    protected void initTable() {
        getTable().enableColumnResizing();

        // Name Column
        AbstractTextColumn<Label> nameColumn = new AbstractTextColumn<Label>() {
            @Override
            public String getValue(Label label) {
                return label.getName();
            }
        };
        nameColumn.makeSortable();
        getTable().addColumn(nameColumn, constants.affinityLabelsNameColumnLabel(), "150px"); //$NON-NLS-1$

        // VM Members Column
        AbstractTextColumn<Label> vmMembersColumn = new AbstractTextColumn<Label>() {
            @Override
            public String getValue(Label label) {
                String vmNames = String.join(", ", getVmNames(label)); //$NON-NLS-1$
                if (vmNames.isEmpty()) {
                    return constants.affinityLabelsNoMembers();
                }
                return vmNames;
            }
        };
        vmMembersColumn.makeSortable();
        getTable().addColumn(vmMembersColumn, constants.affinityLabelsVmsColumnLabel(), "500px"); //$NON-NLS-1$

        // Host Members Column
        AbstractTextColumn<Label> hostMembersColumn = new AbstractTextColumn<Label>() {
            @Override
            public String getValue(Label label) {
                String hostNames = String.join(", ", getHostNames(label)); //$NON-NLS-1$
                if (hostNames.isEmpty()) {
                    return constants.affinityLabelsNoMembers();
                }
                return hostNames;
            }
        };
        hostMembersColumn.makeSortable();
        getTable().addColumn(hostMembersColumn, constants.affinityLabelsHostsColumnLabel(), "500px"); //$NON-NLS-1$
    }

    protected List<String> getVmNames(Label label) {
        List<String> vmNames = new ArrayList<>();

        if (!entitiesNameMap.isEmpty() && !label.getVms().isEmpty()) {
            label.getVms().forEach(id -> {
                String vmName = entitiesNameMap.get(id);
                if (vmName != null && !vmName.isEmpty()) {
                    vmNames.add(vmName);
                }
            });

            Collections.sort(vmNames);
        }

        return vmNames;
    }

    protected List<String> getHostNames(Label label) {
        List<String> hostNames = new ArrayList<>();

        if (!entitiesNameMap.isEmpty() && !label.getHosts().isEmpty()) {
            label.getHosts().forEach(id -> {
                String hostName = entitiesNameMap.get(id);
                if (hostName != null && !hostName.isEmpty()) {
                    hostNames.add(hostName);
                }
            });

            Collections.sort(hostNames);
        }

        return hostNames;
    }

    protected abstract void generateIds();
}
