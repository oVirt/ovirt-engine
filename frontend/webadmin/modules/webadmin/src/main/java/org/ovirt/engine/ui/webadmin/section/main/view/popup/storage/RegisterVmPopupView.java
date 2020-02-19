package org.ovirt.engine.ui.webadmin.section.main.view.popup.storage;

import java.util.ArrayList;
import java.util.Collection;

import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.widget.LeftAlignedUiCommandButton;
import org.ovirt.engine.ui.common.widget.UiCommandButton;
import org.ovirt.engine.ui.common.widget.panel.AlertPanel;
import org.ovirt.engine.ui.common.widget.table.column.AbstractCheckboxColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractEnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractImageResourceColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.common.widget.table.header.AbstractCheckboxHeader;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.RegisterVmModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.register.RegisterVmData;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.RegisterVmPopupPresenterWidget;

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.inject.Inject;

public class RegisterVmPopupView extends RegisterEntityPopupView<VM, RegisterVmData, RegisterVmModel>
        implements RegisterVmPopupPresenterWidget.ViewDef {

    interface Driver extends UiCommonEditorDriver<RegisterVmModel, RegisterEntityPopupView<VM, RegisterVmData, RegisterVmModel>> {
    }

    private static final ApplicationResources resources = AssetProvider.getResources();
    private static final ApplicationConstants constants = AssetProvider.getConstants();
    private static final ApplicationTemplates templates = AssetProvider.getTemplates();
    private static final ApplicationMessages messages = AssetProvider.getMessages();

    @Inject
    public RegisterVmPopupView(EventBus eventBus, Driver driver) {
        super(eventBus, driver);
    }

    @Override
    public void edit(final RegisterVmModel model) {
        super.edit(model);

        model.getEntities().getItemsChangedEvent().addListener((ev, sender, args) -> addBadMacsValidationListener(model));
    }

    private void addBadMacsValidationListener(RegisterVmModel model) {
        for(RegisterVmData vmData : model.getEntities().getItems()) {
            vmData.getBadMacsExist().getEntityChangedEvent().addListener((ev, sender, args) -> refreshEntityTable());
        }
    }

    @Override
    protected void createEntityTable(RegisterVmModel model) {
        entityTable.addColumn(new AbstractImageResourceColumn<RegisterVmData>() {
            @Override
            public ImageResource getValue(RegisterVmData registerVmData) {
                if (registerVmData.getError() != null || registerVmData.isNameExistsInSystem()) {
                    return resources.errorImage();
                }
                if (registerVmData.getWarning() != null) {
                    return resources.alertImage();
                }
                return null;
            }
            @Override
            public SafeHtml getTooltip(RegisterVmData registerVmData) {
                String problem;
                if (registerVmData.getError() != null) {
                    problem = registerVmData.getError();
                } else if (registerVmData.isNameExistsInSystem()) {
                    problem = ConstantsManager.getInstance().getConstants().nameMustBeUniqueInvalidReason();
                } else {
                    problem = registerVmData.getWarning();
                }
                return problem == null
                        ? null
                        : new SafeHtmlBuilder().appendEscapedLines(problem).toSafeHtml();
            }
        }, constants.empty(), "30px"); //$NON-NLS-1$
        AbstractTextColumn<RegisterVmData> nameColumn = new AbstractTextColumn<RegisterVmData>() {
            @Override
            public String getValue(RegisterVmData registerVmData) {
                return registerVmData.getVm().getName();
            }
        };
        entityTable.addColumn(nameColumn, constants.nameVm(), "150px"); //$NON-NLS-1$

        AbstractTextColumn<RegisterVmData> originColumn = new AbstractEnumColumn<RegisterVmData, OriginType>() {
            @Override
            protected OriginType getRawValue(RegisterVmData registerVmData) {
                return registerVmData.getVm().getOrigin();
            }
        };
        entityTable.addColumn(originColumn, constants.originVm(), "100px"); //$NON-NLS-1$

        AbstractTextColumn<RegisterVmData> memoryColumn = new AbstractTextColumn<RegisterVmData>() {
            @Override
            public String getValue(RegisterVmData registerVmData) {
                int size = registerVmData.getVm().getVmMemSizeMb();
                return messages.megabytes(String.valueOf(size));
            }
        };
        entityTable.addColumn(memoryColumn, constants.memoryVm(), "100px"); //$NON-NLS-1$

        AbstractTextColumn<RegisterVmData> cpuColumn = new AbstractTextColumn<RegisterVmData>() {
            @Override
            public String getValue(RegisterVmData registerVmData) {
                int numOfCpus = registerVmData.getVm().getNumOfCpus();
                return String.valueOf(numOfCpus);
            }
        };
        entityTable.addColumn(cpuColumn, constants.cpusVm(), "50px"); //$NON-NLS-1$

        AbstractTextColumn<RegisterVmData> archColumn = new AbstractTextColumn<RegisterVmData>() {
            @Override
            public String getValue(RegisterVmData registerVmData) {
                ArchitectureType clusterArch = registerVmData.getVm().getClusterArch();
                return String.valueOf(clusterArch);
            }
        };
        entityTable.addColumn(archColumn, constants.architectureVm(), "100px"); //$NON-NLS-1$

        AbstractTextColumn<RegisterVmData> diskColumn = new AbstractTextColumn<RegisterVmData>() {
            @Override
            public String getValue(RegisterVmData registerVmData) {
                int numOfDisks = registerVmData.getVm().getDiskMap().size();
                return String.valueOf(numOfDisks);
            }
        };
        entityTable.addColumn(diskColumn, constants.disksVm(), "50px"); //$NON-NLS-1$

        entityTable.addColumn(creatReassignMacsColumn(), new ReassignBadMacsHeader(), "150px"); //$NON-NLS-1$

        entityTable.addColumn(createAllowPartialColumn(), constants.allowPartial(), "95px"); //$NON-NLS-1$

        entityTable.addColumn(getClusterColumn(), constants.clusterVm(), "150px"); //$NON-NLS-1$

        if (model.isQuotaEnabled()) {
            entityTable.addColumn(getClusterQuotaColumn(), constants.quotaVm(), "150px"); //$NON-NLS-1$
        }
    }

    private AbstractCheckboxColumn<RegisterVmData> creatReassignMacsColumn() {
        final AbstractCheckboxColumn<RegisterVmData> reassignMacsColumn =
                new AbstractCheckboxColumn<RegisterVmData>() {
                    @Override
                    public Boolean getValue(RegisterVmData registerVmData) {
                        return registerVmData.getReassignMacs().getEntity();
                    }

                    @Override
                    protected boolean canEdit(RegisterVmData registerVmData) {
                        return true;
                    }

                    @Override
                    public void render(Context context, RegisterVmData object, SafeHtmlBuilder sb) {
                        super.render(context, object, sb);
                        sb.append(templates.textForCheckBox(constants.reassignBadMacs()));
                    }
                };

        reassignMacsColumn.setFieldUpdater((index, object, value) -> object.getReassignMacs().setEntity(value));
        return reassignMacsColumn;
    }

    private AbstractCheckboxColumn<RegisterVmData> createAllowPartialColumn() {
        final AbstractCheckboxColumn<RegisterVmData> allowPartialColumn =
                new AbstractCheckboxColumn<RegisterVmData>() {
                    @Override
                    public Boolean getValue(RegisterVmData registerVmData) {
                        return registerVmData.getAllowPartialImport().getEntity();
                    }

                    @Override
                    protected boolean canEdit(RegisterVmData registerVmData) {
                        return true;
                    }

                    @Override
                    public void render(Context context, RegisterVmData object, SafeHtmlBuilder sb) {
                        super.render(context, object, sb);
                    }
                };

        allowPartialColumn.setFieldUpdater((index, object, value) -> {
            object.getAllowPartialImport().setEntity(value);
            updateWarnings(value);
        });
        return allowPartialColumn;
    }

    private void updateWarnings(boolean isAllowPartial) {
        warningPanel.clearMessages();
        if (isAllowPartial) {
            warningPanel.setType(AlertPanel.Type.WARNING);
            warningPanel.addMessage(SafeHtmlUtils.fromSafeConstant(constants.allowPartialVmImportWarning()));
            warningPanel.setVisible(true);
        } else {
            warningPanel.setVisible(false);
        }
    }

    @Override
    protected void createInfoPanel(RegisterVmModel model) {
        registerEntityInfoPanel = new RegisterVmInfoPanel(model);
        entityInfoContainer.add(registerEntityInfoPanel);
    }

    @Override
    protected UiCommandButton createCommandButton(String label, String uniqueId) {
        if (RegisterVmModel.VNIC_PROFILE_MAPPING_COMMAND.equals(uniqueId)) {
            return new LeftAlignedUiCommandButton(label);
        } else {
            return super.createCommandButton(label, uniqueId);
        }
    }

    private Collection<RegisterVmData> getTableItems() {
        final ListModel<RegisterVmData> tableItems = entityTable.asEditor().flush();
        return tableItems == null ? new ArrayList<RegisterVmData>() : tableItems.getItems();
    }

    private class ReassignBadMacsHeader extends AbstractCheckboxHeader {

        @Override
        protected void selectionChanged(Boolean value) {
            for (RegisterVmData tableEntry : getTableItems()) {
                tableEntry.getReassignMacs().setEntity(value);
            }
            refreshEntityTable();
        }

        @Override
        public boolean isEnabled() {
            return true;
        }

        @Override
        public Boolean getValue() {
            for (RegisterVmData tableEntry : getTableItems()) {
                if (!tableEntry.getReassignMacs().getEntity()) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public String getLabel() {
            return constants.reassignAllBadMacs();
        }
    }

    public RegisterVmInfoPanel getInfoPanel() {
        return (RegisterVmInfoPanel) registerEntityInfoPanel;
    }
}
