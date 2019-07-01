package org.ovirt.engine.ui.webadmin.section.main.view.tab;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractBooleanColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.scheduling.affinity_groups.list.AffinityGroupListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.widget.table.column.AffinityGroupStatusColumn;

public abstract class AbstractSubTabAffinityGroupsView<I, M extends ListWithDetailsModel, T extends AffinityGroupListModel<?>> extends AbstractSubTabTableView<I, AffinityGroup, M, T> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    public AbstractSubTabAffinityGroupsView(SearchableDetailModelProvider<AffinityGroup, M, T> modelProvider) {
        super(modelProvider);
        generateIds();
        initTable();
        initWidget(getTableContainer());
    }

    private void initTable() {
        getTable().enableColumnResizing();

        AffinityGroupStatusColumn statusColumn = new AffinityGroupStatusColumn();
        statusColumn.makeSortable();
        getTable().addColumn(statusColumn, constants.statusAffinityGroup(), "50px"); //$NON-NLS-1$

        AbstractTextColumn<AffinityGroup> nameColumn = new AbstractTextColumn<AffinityGroup>() {
            @Override
            public String getValue(AffinityGroup object) {
                return object.getName();
            }
        };
        nameColumn.makeSortable();
        getTable().addColumn(nameColumn, constants.nameAffinityGroup(), "150px"); //$NON-NLS-1$

        AbstractTextColumn<AffinityGroup> descColumn = new AbstractTextColumn<AffinityGroup>() {
            @Override
            public String getValue(AffinityGroup object) {
                return object.getDescription();
            }
        };
        descColumn.makeSortable();
        getTable().addColumn(descColumn, constants.descriptionAffinityGroup(), "150px"); //$NON-NLS-1$

        AbstractTextColumn<AffinityGroup> priorityColumn = new AbstractTextColumn<AffinityGroup>() {
            @Override
            public String getValue(AffinityGroup group) {
                return Double.toString(group.getPriorityAsDouble());
            }
        };
        priorityColumn.makeSortable(Comparator.comparing(AffinityGroup::getPriority));
        getTable().addColumn(priorityColumn, constants.priorityAffinityGroup(), "100px"); //$NON-NLS-1$

        AbstractBooleanColumn<AffinityGroup> vmPolarityColumn =
                new AbstractBooleanColumn<AffinityGroup>(constants.positiveAffinity(), constants.negativeAffinity()) {

            @Override
            protected Boolean getRawValue(AffinityGroup object) {
                return object.getVmPolarityBooleanObject();
            }
        };
        vmPolarityColumn.makeSortable();
        getTable().addColumn(vmPolarityColumn, constants.polarityAffinityGroup(), "100px"); //$NON-NLS-1$

        AbstractBooleanColumn<AffinityGroup> vmEnforceColumn =
                new AbstractBooleanColumn<AffinityGroup>(constants.hardEnforcingAffinity(), constants.softEnforcingAffinity()) {

                    @Override
                    protected Boolean getRawValue(AffinityGroup object) {
                        return object.isVmEnforcing();
                    }
                };
        vmEnforceColumn.makeSortable();
        getTable().addColumn(vmEnforceColumn, constants.enforceAffinityGroup(), "100px"); //$NON-NLS-1$

        AbstractBooleanColumn<AffinityGroup> hostPolarityColumn =
                new AbstractBooleanColumn<AffinityGroup>(constants.positiveAffinity(), constants.negativeAffinity()) {

                    @Override
                    protected Boolean getRawValue(AffinityGroup object) {
                        return object.getVdsPolarityBooleanObject();
                    }
                };
        hostPolarityColumn.makeSortable();
        getTable().addColumn(hostPolarityColumn, constants.hostPolarityAffinityGroup(), "100px"); //$NON-NLS-1$

        AbstractBooleanColumn<AffinityGroup> hostEnforceColumn =
                new AbstractBooleanColumn<AffinityGroup>(constants.hardEnforcingAffinity(), constants.softEnforcingAffinity()) {

                    @Override
                    protected Boolean getRawValue(AffinityGroup object) {
                        return object.isVdsEnforcing();
                    }
                };
        hostEnforceColumn.makeSortable();
        getTable().addColumn(hostEnforceColumn, constants.hostEnforceAffinityGroup(), "100px"); //$NON-NLS-1$


        getTable().addColumn(createListTextColumn(this::getVmNames, constants.noMembersAffinityGroup()),
                constants.vmMembersAffinityGroup(),
                "400px"); //$NON-NLS-1$

        getTable().addColumn(createListTextColumn(AffinityGroup::getVmLabelNames, constants.noLabelsAffinityGroup()),
                constants.vmLabelsAffinityGroup(),
                "300px"); //$NON-NLS-1$

        getTable().addColumn(createListTextColumn(this::getHostNames, constants.noMembersAffinityGroup()),
                constants.hostMembersAffinityGroup(),
                "400px"); //$NON-NLS-1$

        getTable().addColumn(createListTextColumn(AffinityGroup::getHostLabelNames, constants.noLabelsAffinityGroup()),
                constants.hostLabelsAffinityGroup(),
                "300px"); //$NON-NLS-1$
    }

    protected List<String> getVmNames(AffinityGroup group) {
        return group.getVmEntityNames();
    }

    protected List<String> getHostNames(AffinityGroup group) {
        return group.getVdsEntityNames();
    }

    protected abstract void generateIds();

    private AbstractTextColumn<AffinityGroup> createListTextColumn(Function<AffinityGroup, List<String>> extractor,
            String noMembers) {
        AbstractTextColumn<AffinityGroup> column = new AbstractTextColumn<AffinityGroup>() {
            @Override
            public String getValue(AffinityGroup group) {
                String vmLabels = String.join(", ", extractor.apply(group)); //$NON-NLS-1$
                if (vmLabels.isEmpty()) {
                    return noMembers;
                }
                return vmLabels;
            }
        };
        column.makeSortable();
        return column;
    }
}
