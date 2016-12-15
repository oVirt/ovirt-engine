package org.ovirt.engine.ui.webadmin.section.main.view.popup.storage;

import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.widget.AbstractUiCommandButton;
import org.ovirt.engine.ui.common.widget.LeftAlignedUiCommandButton;
import org.ovirt.engine.ui.common.widget.table.column.AbstractCheckboxColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractEnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractImageResourceColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.uicommonweb.models.storage.RegisterVmModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.register.RegisterVmData;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.RegisterVmPopupPresenterWidget;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.inject.Inject;

public class RegisterVmPopupView extends RegisterEntityPopupView<VM, RegisterVmData, RegisterVmModel>
        implements RegisterVmPopupPresenterWidget.ViewDef {

    interface Driver extends UiCommonEditorDriver<RegisterVmModel, RegisterEntityPopupView<VM, RegisterVmData, RegisterVmModel>> {
    }

    private static final ApplicationResources resources = AssetProvider.getResources();
    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public RegisterVmPopupView(EventBus eventBus, Driver driver) {
        super(eventBus, driver);
    }

    @Override
    public void edit(final RegisterVmModel model) {
        super.edit(model);

        model.getEntities().getItemsChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                addBadMacsValidationListener(model);
            }
        });
    }

    private void addBadMacsValidationListener(RegisterVmModel model) {
        for(RegisterVmData vmData : model.getEntities().getItems()) {
            vmData.getBadMacsExist().getEntityChangedEvent().addListener(new IEventListener<EventArgs>() {
                @Override
                public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                    refreshEntityTable();
                }
            });
        }
    }

    @Override
    protected void createEntityTable(RegisterVmModel model) {
        entityTable.addColumn(new AbstractImageResourceColumn<RegisterVmData>() {
            @Override
            public ImageResource getValue(RegisterVmData registerVmData) {
                if (registerVmData.getError() != null) {
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
                } else {
                    problem = registerVmData.getWarning();
                }
                return problem == null
                        ? null
                        : new SafeHtmlBuilder().appendEscapedLines(problem).toSafeHtml();
            }
        }, constants.empty(), "20px"); //$NON-NLS-1$
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
                return size + " MB"; //$NON-NLS-1$
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

        final AbstractCheckboxColumn<RegisterVmData> reassignMacsColumn = new AbstractCheckboxColumn<RegisterVmData>() {
            @Override
            public Boolean getValue(RegisterVmData registerVmData) {
                return registerVmData.getReassignMacs().getEntity();
            }

            @Override
            protected boolean canEdit(RegisterVmData registerVmData) {
                return true;
            }
        };
        reassignMacsColumn.setFieldUpdater(new FieldUpdater<RegisterVmData, Boolean>() {
            @Override
            public void update(int index, RegisterVmData object, Boolean value) {
                object.getReassignMacs().setEntity(value);
            }
        });
        entityTable.addColumn(reassignMacsColumn, constants.reassignBadMacs());

        entityTable.addColumn(getClusterColumn(), constants.clusterVm(), "150px"); //$NON-NLS-1$

        if (model.isQuotaEnabled()) {
            entityTable.addColumn(getClusterQuotaColumn(), constants.quotaVm(), "150px"); //$NON-NLS-1$
        }
    }

    @Override
    protected void createInfoPanel(RegisterVmModel model) {
        registerEntityInfoPanel = new RegisterVmInfoPanel(model);
        entityInfoContainer.add(registerEntityInfoPanel);
    }

    @Override
    protected AbstractUiCommandButton createCommandButton(String label, String uniqueId) {
        if (RegisterVmModel.VNIC_PROFILE_MAPPING_COMMAND.equals(uniqueId)) {
            return new LeftAlignedUiCommandButton(label);
        } else {
            return super.createCommandButton(label, uniqueId);
        }
    }
}
