package org.ovirt.engine.ui.webadmin.section.main.view.popup.cluster;

import java.util.ArrayList;
import java.util.Collections;

import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable.SelectionMode;
import org.ovirt.engine.ui.common.widget.table.column.CheckboxColumn;
import org.ovirt.engine.ui.common.widget.table.column.SafeHtmlWithSafeHtmlTooltipColumn;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.common.widget.table.header.CheckboxHeader;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterNetworkManageModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterNetworkModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.cluster.ClusterManageNetworkPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.widget.table.column.NetworkRoleColumnHelper;

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.inject.Inject;

public class ClusterManageNetworkPopupView extends AbstractModelBoundPopupView<ClusterNetworkManageModel>
        implements ClusterManageNetworkPopupPresenterWidget.ViewDef {

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, ClusterManageNetworkPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField(provided = true)
    EntityModelCellTable<ClusterNetworkManageModel> networks;

    private final ApplicationConstants constants;
    private final ApplicationTemplates templates;
    private final SafeHtml vmImage;
    private final SafeHtml emptyImage;

    @Inject
    public ClusterManageNetworkPopupView(
            EventBus eventBus,
            ApplicationResources resources,
            ApplicationConstants constants,
            ApplicationTemplates templates) {
        super(eventBus, resources);

        this.constants = constants;
        this.templates = templates;
        this.networks = new EntityModelCellTable<ClusterNetworkManageModel>(SelectionMode.NONE, true);
        vmImage = SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(resources.networkVm()).getHTML());
        emptyImage = SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(resources.networkEmpty()).getHTML());
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
    }

    Iterable<ClusterNetworkModel> getNetworksTableItems() {
        ClusterNetworkManageModel tableModel = networks.asEditor().flush();
        return tableModel != null ? tableModel.getItems() : new ArrayList<ClusterNetworkModel>();
    }

    void refreshNetworksTable() {
        networks.asEditor().edit(networks.asEditor().flush());
    }

    void initEntityModelCellTable(final ApplicationConstants constants, final ApplicationTemplates templates) {
        networks.enableColumnResizing();
        boolean multipleSelectionAllowed = networks.asEditor().flush().isMultiCluster();

        networks.addColumn(new NetworkNameTextColumnWithTooltip(), constants.nameNetwork(), "85px"); //$NON-NLS-1$

        networks.addColumn(
                new AttachedIndicatorCheckboxColumn(new AttachedIndicatorFieldUpdater()),
                new AttachedIndicatorCheckboxHeader(templates.textForCheckBoxHeader(constants.assignAll())), "90px"); //$NON-NLS-1$

        networks.addColumn(
                new RequiredIndicatorCheckboxColumn(new RequiredIndicatorFieldUpdater()),
                new RequiredAllCheckboxHeader(templates.textForCheckBoxHeader(constants.requiredAll())), "110px"); //$NON-NLS-1$

        networks.addColumn(
                new VmNetworkImageSafeHtmlWithSafeHtmlTooltipColumn(constants),
                constants.vmNetwork(), "80px"); //$NON-NLS-1$

        final SafeHtml displayNetworkColumnHeader = templates.textWithTooltip(
                constants.displayNetwork(),
                constants.changeDisplayNetworkWarning());
        networks.addColumn(
                new DisplayNetworkIndicatorCheckboxColumn(multipleSelectionAllowed,
                        new DisplayNetworkIndicatorFieldUpdater()),
                displayNetworkColumnHeader, "100px"); //$NON-NLS-1$

        networks.addColumn(
                new MigrationNetworkIndicatorCheckboxColumn(multipleSelectionAllowed,
                        new MigrationNetworkIndicatorFieldUpdater()),
                constants.migrationNetwork(), "105px"); //$NON-NLS-1$
    }

    @Override
    public void edit(ClusterNetworkManageModel clusterNetworkManageModel) {
        networks.asEditor().edit(clusterNetworkManageModel);
        initEntityModelCellTable(constants, templates);
    }

    @Override
    public ClusterNetworkManageModel flush() {
        return networks.asEditor().flush();
    }

    private void changeIsAttached(ClusterNetworkModel clusterNetworkModel, Boolean value) {
        clusterNetworkModel.setAttached(value);
        if (!value && clusterNetworkModel.isDisplayNetwork()) {
            clusterNetworkModel.setDisplayNetwork(false);
        }
        if (!value && clusterNetworkModel.isRequired()) {
            clusterNetworkModel.setRequired(false);
        }
    }

    private boolean canEditAssign(ClusterNetworkModel clusterNetworkModel) {
        return !clusterNetworkModel.isManagement();
    }

    private boolean canEditRequired(ClusterNetworkModel clusterNetworkModel) {
        return clusterNetworkModel.isAttached() && !clusterNetworkModel.isManagement()
                && !clusterNetworkModel.isExternal();
    }

    private static final class NetworkNameTextColumnWithTooltip extends TextColumnWithTooltip<ClusterNetworkModel> {
        @Override
        public String getValue(ClusterNetworkModel clusterNetworkModel) {
            return clusterNetworkModel.getDisplayedName();
        }
    }

    private final class VmNetworkImageSafeHtmlWithSafeHtmlTooltipColumn
            extends SafeHtmlWithSafeHtmlTooltipColumn<ClusterNetworkModel> {
        private final ApplicationConstants constants;

        private VmNetworkImageSafeHtmlWithSafeHtmlTooltipColumn(ApplicationConstants constants) {
            this.constants = constants;
        }

        @Override
        public SafeHtml getValue(ClusterNetworkModel clusterNetworkModel) {
            return NetworkRoleColumnHelper.getValue(Collections.singletonList(clusterNetworkModel.isVmNetwork() ?
                    vmImage : emptyImage));
        }

        @Override
        public SafeHtml getTooltip(ClusterNetworkModel clusterNetworkModel) {
            return NetworkRoleColumnHelper.getTooltip(clusterNetworkModel.isVmNetwork() ?
                    Collections.singletonMap(vmImage, constants.vmItemInfo())
                    : Collections.<SafeHtml, String> emptyMap());
        }
    }

    private final class RequiredIndicatorCheckboxColumn extends CheckboxColumn<ClusterNetworkModel> {

        private RequiredIndicatorCheckboxColumn(RequiredIndicatorFieldUpdater requiredIndicatorFieldUpdater) {
            super(requiredIndicatorFieldUpdater);
        }

        @Override
        public Boolean getValue(ClusterNetworkModel clusterNetworkModel) {
            return clusterNetworkModel.isRequired();
        }

        @Override
        protected boolean canEdit(ClusterNetworkModel clusterNetworkModel) {
            return canEditRequired(clusterNetworkModel);
        }

        @Override
        public void render(Context context, ClusterNetworkModel object, SafeHtmlBuilder sb) {
            super.render(context, object, sb);
            sb.append(templates.textForCheckBox(constants.required()));
        }
    }

    private final class RequiredIndicatorFieldUpdater implements FieldUpdater<ClusterNetworkModel, Boolean> {
        @Override
        public void update(int index, ClusterNetworkModel clusterNetworkModel, Boolean value) {
            clusterNetworkModel.setRequired(value);
            refreshNetworksTable();
        }
    }

    private final class RequiredAllCheckboxHeader extends CheckboxHeader {
        private RequiredAllCheckboxHeader(SafeHtml title) {
            super(title);
        }

        @Override
        protected void selectionChanged(Boolean value) {
            for (ClusterNetworkModel clusterNetworkModel : getNetworksTableItems()) {
                if (canEditRequired(clusterNetworkModel)) {
                    clusterNetworkModel.setRequired(value);
                }
                refreshNetworksTable();
            }
        }

        @Override
        public Boolean getValue() {
            boolean allEntriesDisabled = !isEnabled();
            for (ClusterNetworkModel clusterNetworkModel : getNetworksTableItems()) {
                if (allEntriesDisabled || canEditRequired(clusterNetworkModel)) {
                    if (!clusterNetworkModel.isRequired()) {
                        return false;
                    }
                }
            }
            return true;
        }

        @Override
        public boolean isEnabled() {
            for (ClusterNetworkModel clusterNetworkModel : getNetworksTableItems()) {
                if (canEditRequired(clusterNetworkModel)) {
                    return true;
                }
            }
            return false;
        }
    }

    private final class AttachedIndicatorCheckboxColumn extends CheckboxColumn<ClusterNetworkModel> {
        private AttachedIndicatorCheckboxColumn(AttachedIndicatorFieldUpdater attachedIndicatorFieldUpdater) {
            super(attachedIndicatorFieldUpdater);
        }

        @Override
        public Boolean getValue(ClusterNetworkModel clusterNetworkModel) {
            return clusterNetworkModel.isAttached();
        }

        @Override
        protected boolean canEdit(ClusterNetworkModel clusterNetworkModel) {
            return canEditAssign(clusterNetworkModel);
        }

        @Override
        public void render(Context context, ClusterNetworkModel object, SafeHtmlBuilder sb) {
            super.render(context, object, sb);
            sb.append(templates.textForCheckBox(constants.assign()));
        }
    }

    private final class AttachedIndicatorFieldUpdater implements FieldUpdater<ClusterNetworkModel, Boolean> {
        @Override
        public void update(int index, ClusterNetworkModel clusterNetworkModel, Boolean value) {
            changeIsAttached(clusterNetworkModel, value);
            refreshNetworksTable();
        }
    }

    private final class AttachedIndicatorCheckboxHeader extends CheckboxHeader {
        private AttachedIndicatorCheckboxHeader(SafeHtml title) {
            super(title);
        }

        @Override
        protected void selectionChanged(Boolean value) {
            for (ClusterNetworkModel clusterNetworkModel : getNetworksTableItems()) {
                if (canEditAssign(clusterNetworkModel)) {
                    changeIsAttached(clusterNetworkModel, value);
                }
            }
            refreshNetworksTable();
        }

        @Override
        public Boolean getValue() {
            boolean allEntriesDisabled = !isEnabled();
            for (ClusterNetworkModel clusterNetworkModel : getNetworksTableItems()) {
                if (allEntriesDisabled || canEditAssign(clusterNetworkModel)) {
                    if (!clusterNetworkModel.isAttached()) {
                        return false;
                    }
                }
            }
            return true;
        }

        @Override
        public boolean isEnabled() {
            for (ClusterNetworkModel clusterNetworkModel : getNetworksTableItems()) {
                if (canEditAssign(clusterNetworkModel)) {
                    return true;
                }
            }
            return false;
        }
    }

    private static final class MigrationNetworkIndicatorCheckboxColumn extends CheckboxColumn<ClusterNetworkModel> {
        private MigrationNetworkIndicatorCheckboxColumn(boolean multipleSelectionAllowed,
                MigrationNetworkIndicatorFieldUpdater migrationNetworkIndicatorFieldUpdater) {
            super(multipleSelectionAllowed, migrationNetworkIndicatorFieldUpdater);
        }

        @Override
        public Boolean getValue(ClusterNetworkModel clusterNetworkModel) {
            return clusterNetworkModel.isMigrationNetwork();
        }

        @Override
        protected boolean canEdit(ClusterNetworkModel clusterNetworkModel) {
            Boolean migrationNetworkEnabled =
                    (Boolean) AsyncDataProvider.getConfigValuePreConverted(ConfigurationValues.MigrationNetworkEnabled,
                            clusterNetworkModel.getCluster().getcompatibility_version().toString());
            return migrationNetworkEnabled && clusterNetworkModel.isAttached() && !clusterNetworkModel.isExternal();
        }
    }

    private final class MigrationNetworkIndicatorFieldUpdater implements FieldUpdater<ClusterNetworkModel, Boolean> {
        @Override
        public void update(int index, ClusterNetworkModel clusterNetworkModel, Boolean value) {

            networks.asEditor().flush().setMigrationNetwork(clusterNetworkModel, value);
            refreshNetworksTable();
        }
    }

    private final class DisplayNetworkIndicatorFieldUpdater implements FieldUpdater<ClusterNetworkModel, Boolean> {
        @Override
        public void update(int index, ClusterNetworkModel clusterNetworkModel, Boolean value) {
            networks.asEditor().flush().setDisplayNetwork(clusterNetworkModel, value);
            refreshNetworksTable();
        }
    }

    private final static class DisplayNetworkIndicatorCheckboxColumn extends CheckboxColumn<ClusterNetworkModel> {
        private DisplayNetworkIndicatorCheckboxColumn(boolean multipleSelectionAllowed,
                DisplayNetworkIndicatorFieldUpdater displayNetworkIndicatorFieldUpdater) {
            super(multipleSelectionAllowed, displayNetworkIndicatorFieldUpdater);
        }

        @Override
        public Boolean getValue(ClusterNetworkModel clusterNetworkModel) {
            return clusterNetworkModel.isDisplayNetwork();
        }

        @Override
        protected boolean canEdit(ClusterNetworkModel clusterNetworkModel) {
            return clusterNetworkModel.isAttached() && !clusterNetworkModel.isExternal();
        }
    }
}
