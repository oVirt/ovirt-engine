package org.ovirt.engine.ui.webadmin.section.main.view.tab;

import java.util.List;

import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.BooleanColumn;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.scheduling.affinity_groups.list.AffinityGroupListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

public abstract class AbstractSubTabAffinityGroupsView<I, M extends ListWithDetailsModel, T extends AffinityGroupListModel<?>> extends AbstractSubTabTableView<I, AffinityGroup, M, T> {

    public AbstractSubTabAffinityGroupsView(SearchableDetailModelProvider<AffinityGroup, M, T> modelProvider,
            ApplicationConstants constants) {
        super(modelProvider);
        generateIds();
        initTable(constants);
        initWidget(getTable());
    }

    private void initTable(final ApplicationConstants constants) {
        getTable().enableColumnResizing();

        TextColumnWithTooltip<AffinityGroup> nameColumn = new TextColumnWithTooltip<AffinityGroup>() {
            @Override
            public String getValue(AffinityGroup object) {
                return object.getName();
            }
        };
        nameColumn.makeSortable();
        getTable().addColumn(nameColumn, constants.nameAffinityGroup(), "150px"); //$NON-NLS-1$

        TextColumnWithTooltip<AffinityGroup> descColumn = new TextColumnWithTooltip<AffinityGroup>() {
            @Override
            public String getValue(AffinityGroup object) {
                return object.getDescription();
            }
        };
        descColumn.makeSortable();
        getTable().addColumn(descColumn, constants.descriptionAffinityGroup(), "150px"); //$NON-NLS-1$

        BooleanColumn<AffinityGroup> polarityColumn =
                new BooleanColumn<AffinityGroup>(constants.positiveAffinity(), constants.negativeAffinity()) {

            @Override
            protected Boolean getRawValue(AffinityGroup object) {
                return object.isPositive();
            }
        };
        polarityColumn.makeSortable();
        getTable().addColumn(polarityColumn, constants.polarityAffinityGroup(), "100px"); //$NON-NLS-1$

        BooleanColumn<AffinityGroup> enforceColumn =
                new BooleanColumn<AffinityGroup>(constants.hardEnforcingAffinity(), constants.softEnforcingAffinity()) {

                    @Override
                    protected Boolean getRawValue(AffinityGroup object) {
                        return object.isEnforcing();
                    }
                };
        enforceColumn.makeSortable();
        getTable().addColumn(enforceColumn, constants.enforceAffinityGroup(), "100px"); //$NON-NLS-1$

        TextColumnWithTooltip<AffinityGroup> membersColumn = new TextColumnWithTooltip<AffinityGroup>() {
            @Override
            public String getValue(AffinityGroup object) {
                String join = join(getEntityNames(object), ", "); //$NON-NLS-1$
                if (join.isEmpty()) {
                    return constants.noMembersAffinityGroup();
                }
                return join;
            }
        };
        membersColumn.makeSortable();
        getTable().addColumn(membersColumn, constants.membersAffinityGroup(), "500px"); //$NON-NLS-1$

        getTable().addActionButton(new WebAdminButtonDefinition<AffinityGroup>(constants.newAffinityGroupLabel()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getNewCommand();
            }
        });

        getTable().addActionButton(new WebAdminButtonDefinition<AffinityGroup>(constants.editAffinityGroupLabel()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getEditCommand();
            }
        });

        getTable().addActionButton(new WebAdminButtonDefinition<AffinityGroup>(constants.removeAffinityGroupLabel()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getRemoveCommand();
            }
        });
    }

    protected List<String> getEntityNames(AffinityGroup object) {
        return object.getEntityNames();
    }

    protected String join(List<String> strings, String separator) {
        StringBuffer result = new StringBuffer();
        if (strings == null) {
            return result.toString();
        }
        for (String s : strings) {
            if (result.length() != 0) {
                result.append(separator);
            }
            result.append(s);
        }

        return result.toString();
    }

    protected abstract void generateIds();
}
