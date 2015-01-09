package org.ovirt.engine.ui.userportal.section.main.view.tab.extended;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.utils.ElementIdUtils;
import org.ovirt.engine.ui.common.widget.table.SimpleActionTable;
import org.ovirt.engine.ui.uicommonweb.ErrorPopupManager;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.VmConsoles;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalListModel;
import org.ovirt.engine.ui.userportal.ApplicationConstants;
import org.ovirt.engine.ui.userportal.ApplicationResources;
import org.ovirt.engine.ui.userportal.ApplicationTemplates;
import org.ovirt.engine.ui.userportal.gin.ClientGinjectorProvider;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.SideTabExtendedVirtualMachinePresenter;
import org.ovirt.engine.ui.userportal.section.main.view.AbstractSideTabWithDetailsView;
import org.ovirt.engine.ui.userportal.uicommon.model.vm.UserPortalListProvider;
import org.ovirt.engine.ui.userportal.widget.action.UserPortalButtonDefinition;
import org.ovirt.engine.ui.userportal.widget.basic.MainTabBasicListItemMessagesTranslator;
import org.ovirt.engine.ui.userportal.widget.extended.vm.AbstractConsoleButtonCell;
import org.ovirt.engine.ui.userportal.widget.extended.vm.BorderedCompositeCell;
import org.ovirt.engine.ui.userportal.widget.extended.vm.ConsoleButtonCell;
import org.ovirt.engine.ui.userportal.widget.extended.vm.ConsoleEditButtonCell;
import org.ovirt.engine.ui.userportal.widget.extended.vm.ImageButtonCell;
import org.ovirt.engine.ui.userportal.widget.extended.vm.ImageMaskCell;
import org.ovirt.engine.ui.userportal.widget.extended.vm.ImageMaskCell.ShowMask;
import org.ovirt.engine.ui.userportal.widget.extended.vm.TooltipCell;
import org.ovirt.engine.ui.userportal.widget.extended.vm.TooltipCell.TooltipProvider;
import org.ovirt.engine.ui.userportal.widget.extended.vm.UserPortalItemSimpleColumn;
import org.ovirt.engine.ui.userportal.widget.refresh.UserPortalRefreshManager;
import org.ovirt.engine.ui.userportal.widget.table.UserPortalSimpleActionTable;
import org.ovirt.engine.ui.userportal.widget.table.column.VmImageColumn;
import org.ovirt.engine.ui.userportal.widget.table.column.VmImageColumn.OsTypeExtractor;
import org.ovirt.engine.ui.userportal.widget.table.column.VmStatusColumn;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.CompositeCell;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.CellTable.Resources;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.RowStyles;
import com.google.inject.Inject;

public class SideTabExtendedVirtualMachineView extends AbstractSideTabWithDetailsView<UserPortalItemModel, UserPortalListModel>
implements SideTabExtendedVirtualMachinePresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SideTabExtendedVirtualMachineView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final VmTableResources vmTableResources = GWT.create(VmTableResources.class);

    private final ApplicationResources applicationResources;
    private final MainTabBasicListItemMessagesTranslator statusTranslator;
    private final ApplicationConstants constants;
    private final ErrorPopupManager errorPopupManager;

    @Inject
    public SideTabExtendedVirtualMachineView(UserPortalListProvider modelProvider,
            ApplicationTemplates templates,
            ApplicationResources applicationResources,
            ErrorPopupManager errorPopupManager,
            MainTabBasicListItemMessagesTranslator translator,
            ApplicationConstants constants,
            ClientStorage clientStorage) {
        super(modelProvider, applicationResources, clientStorage);
        this.applicationResources = applicationResources;
        this.statusTranslator = translator;
        this.constants = constants;
        this.errorPopupManager = errorPopupManager;
        applicationResources.sideTabExtendedVmStyle().ensureInjected();
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable(templates);
    }

    @Override
    protected SimpleActionTable<UserPortalItemModel> createActionTable() {
        return new UserPortalSimpleActionTable<UserPortalItemModel>(modelProvider,
                getTableResources(),
                ClientGinjectorProvider.getEventBus(),
                ClientGinjectorProvider.getClientStorage(),
                new UserPortalRefreshManager(modelProvider,
                        ClientGinjectorProvider.getEventBus(),
                        ClientGinjectorProvider.getClientStorage()));
    }

    @Override
    protected Resources getTableResources() {
        return vmTableResources;
    }

    @Override
    protected Object getSubTabPanelContentSlot() {
        return SideTabExtendedVirtualMachinePresenter.TYPE_SetSubTabPanelContent;
    }

    void initTable(final ApplicationTemplates templates) {
        final String elementIdPrefix = getTable().getContentTableElementId();

        VmImageColumn<UserPortalItemModel> vmImageColumn =
                new VmImageColumn<UserPortalItemModel>(new OsTypeExtractor<UserPortalItemModel>() {
                    @Override
                    public int extractOsType(UserPortalItemModel item) {
                        return item.getOsId();
                    }
                });

        ImageMaskCell<UserPortalItemModel> vmImageColumnWithMask = new ImageMaskCell<UserPortalItemModel>(
                vmImageColumn,
                applicationResources.disabledSmallMask(),
                new ShowMask<UserPortalItemModel>() {
                    @Override
                    public boolean showMask(UserPortalItemModel value) {
                        return !value.isVmUp();
                    }
                });

        TooltipCell<UserPortalItemModel> vmImageColumnWithMaskAndTooltip = new TooltipCell<UserPortalItemModel>(
                new UserPortalItemSimpleColumn(vmImageColumnWithMask),
                new TooltipProvider<UserPortalItemModel>() {
                    @Override
                    public String getTooltip(UserPortalItemModel value) {
                        return AsyncDataProvider.getInstance().getOsName(value.getOsId());
                    }
                });
        getTable().addColumn(new UserPortalItemSimpleColumn(vmImageColumnWithMaskAndTooltip), constants.empty(), "77px"); //$NON-NLS-1$

        TooltipCell<UserPortalItemModel> statusColumn = new TooltipCell<UserPortalItemModel>(
                new VmStatusColumn(),
                new TooltipProvider<UserPortalItemModel>() {
                    @Override
                    public String getTooltip(UserPortalItemModel value) {
                        return statusTranslator.translate(value.getStatus().name());
                    }
                });
        statusColumn.setElementIdPrefix(elementIdPrefix);
        statusColumn.setColumnId("status"); //$NON-NLS-1$

        getTable().addColumn(new UserPortalItemSimpleColumn(statusColumn), constants.empty(), "55px"); //$NON-NLS-1$

        Cell<UserPortalItemModel> nameAndDescriptionCell = new AbstractCell<UserPortalItemModel>() {
            @Override
            public void render(Context context, UserPortalItemModel item, SafeHtmlBuilder sb) {
                sb.append(templates.vmNameCellItem(
                        ElementIdUtils.createTableCellElementId(elementIdPrefix, "name", context), //$NON-NLS-1$
                        item.getName()));

                String description = item.getDescription();
                if (description != null && !description.isEmpty()) {
                    sb.append(templates.vmDescriptionCellItem(description));
                }
            }
        };

        Column<UserPortalItemModel, UserPortalItemModel> nameAndDescriptionColumn =
                new Column<UserPortalItemModel, UserPortalItemModel>(nameAndDescriptionCell) {
            @Override
            public UserPortalItemModel getValue(UserPortalItemModel item) {
                return item;
            }
        };
        getTable().addColumn(nameAndDescriptionColumn, constants.empty(), "400px"); //$NON-NLS-1$

        getTable().addColumn(new Column<UserPortalItemModel, UserPortalItemModel>(createActionsCompositeCell(elementIdPrefix)) {
            @Override
            public UserPortalItemModel getValue(UserPortalItemModel object) {
                return object;
            }
        }, constants.empty(), "154px"); //$NON-NLS-1$

        ConsoleButtonCell openConsoleCell = new ConsoleButtonCell(
                applicationResources.sideTabExtendedVmStyle().enabledConsoleButton(),
                applicationResources.sideTabExtendedVmStyle().disabledConsoleButton(),
                constants.openConsoleLabel(),
                new AbstractConsoleButtonCell.ConsoleButtonCommand() {
                    @Override
                    public void execute(UserPortalItemModel model) {
                        try {
                            if (!model.isPool()) {
                            model.getVmConsoles().connect();
                            }
                        } catch (VmConsoles.ConsoleConnectException e) {
                            errorPopupManager.show(e.getLocalizedErrorMessage());
                        }
                    }
                });
        openConsoleCell.setElementIdPrefix(elementIdPrefix);
        openConsoleCell.setColumnId("openConsoleButton"); //$NON-NLS-1$

        getTable().addColumn(new UserPortalItemSimpleColumn(openConsoleCell), constants.empty(), "100px"); //$NON-NLS-1$

        ConsoleEditButtonCell consoleEditCell = new ConsoleEditButtonCell(
                applicationResources.sideTabExtendedVmStyle().enabledEditConsoleButton(),
                applicationResources.sideTabExtendedVmStyle().disabledEditConsoleButton(),
                constants.editConsoleLabel(),
                new AbstractConsoleButtonCell.ConsoleButtonCommand() {
                    @Override
                    public void execute(UserPortalItemModel model) {
                        getModel().getEditConsoleCommand().execute();
                    }
                });
        consoleEditCell.setElementIdPrefix(elementIdPrefix);
        consoleEditCell.setColumnId("editConsoleButton"); //$NON-NLS-1$

        getTable().addColumn(new UserPortalItemSimpleColumn(consoleEditCell), constants.empty());

        getTable().addActionButton(new UserPortalButtonDefinition<UserPortalItemModel>(constants.newVm()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getNewVmCommand();
            }
        });
        getTable().addActionButton(new UserPortalButtonDefinition<UserPortalItemModel>(constants.editVm()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getEditCommand();
            }
        });
        getTable().addActionButton(new UserPortalButtonDefinition<UserPortalItemModel>(constants.removeVm()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getRemoveCommand();
            }
        });
        getTable().addActionButton(new UserPortalButtonDefinition<UserPortalItemModel>(constants.cloneVm()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getCloneVmCommand();
            }
        });
        getTable().addActionButton(new UserPortalButtonDefinition<UserPortalItemModel>(constants.runOnceVm()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getRunOnceCommand();
            }
        });
        getTable().addActionButton(new UserPortalButtonDefinition<UserPortalItemModel>(constants.changeCdVm()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getChangeCdCommand();
            }
        });
        getTable().addActionButton(new UserPortalButtonDefinition<UserPortalItemModel>(constants.makeTemplateVm()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getNewTemplateCommand();
            }
        });

        getTable().setExtraRowStyles(new RowStyles<UserPortalItemModel>() {

            @Override
            public String getStyleNames(UserPortalItemModel row, int rowIndex) {
                if (row == null) {
                    return null;
                }

                if (isSelectedRow(row)) {
                    return null;
                }

                return row.isVmUp() ?
                        applicationResources.sideTabExtendedVmStyle().vmUpRow() :
                            applicationResources.sideTabExtendedVmStyle().vmDownRow();
            }

            protected boolean isSelectedRow(UserPortalItemModel row) {
                UserPortalItemModel selectedModel = (UserPortalItemModel) getModel().getSelectedItem();
                if (selectedModel != null) {
                    if (modelProvider.getKey(selectedModel).equals(modelProvider.getKey(row))) {
                        return true;
                    }
                }

                @SuppressWarnings("unchecked")
                List<UserPortalItemModel> selectedModels = getModel().getSelectedItems();

                if (selectedModels == null) {
                    return false;
                }

                for (UserPortalItemModel model : selectedModels) {
                    if (modelProvider.getKey(model).equals(modelProvider.getKey(row))) {
                        return true;
                    }
                }

                return false;
            }

        });
    }

    protected CompositeCell<UserPortalItemModel> createActionsCompositeCell(String elementIdPrefix) {
        ImageButtonCell<UserPortalItemModel> runCell = new VmButtonsImageButtonCell(
                applicationResources.playIcon(), applicationResources.playDisabledIcon()) {
            @Override
            protected String getTitle(UserPortalItemModel value) {
                return value.isPool() ? constants.takeVmLabel() : constants.runVmLabel();
            }

            @Override
            protected UICommand resolveCommand(UserPortalItemModel value) {
                return value.isPool() ? value.getTakeVmCommand() : value.getRunCommand();
            }
        };
        runCell.setElementIdPrefix(elementIdPrefix);
        runCell.setColumnId("runButton"); //$NON-NLS-1$

        ImageButtonCell<UserPortalItemModel> shutdownCell = new VmButtonsImageButtonCell(
                applicationResources.stopIcon(), applicationResources.stopDisabledIcon()) {
            @Override
            protected String getTitle(UserPortalItemModel value) {
                return value.isPool() ? constants.returnVmLabel() : constants.shutDownVm();
            }

            @Override
            protected UICommand resolveCommand(UserPortalItemModel value) {
                return value.isPool() ? value.getReturnVmCommand() : value.getShutdownCommand();
            }
        };
        shutdownCell.setElementIdPrefix(elementIdPrefix);
        shutdownCell.setColumnId("shutdownButton"); //$NON-NLS-1$

        ImageButtonCell<UserPortalItemModel> suspendCell = new VmButtonsImageButtonCell(
                applicationResources.suspendIcon(), applicationResources.suspendDisabledIcon()) {
            @Override
            protected String getTitle(UserPortalItemModel value) {
                return constants.suspendVmLabel();
            }

            @Override
            protected UICommand resolveCommand(UserPortalItemModel value) {
                return value.getPauseCommand();
            }
        };
        suspendCell.setElementIdPrefix(elementIdPrefix);
        suspendCell.setColumnId("suspendButton"); //$NON-NLS-1$

        ImageButtonCell<UserPortalItemModel> stopCell = new VmButtonsImageButtonCell(
                applicationResources.powerIcon(), applicationResources.powerDisabledIcon()) {
            @Override
            protected String getTitle(UserPortalItemModel value) {
                return constants.powerOffVm();
            }

            @Override
            protected UICommand resolveCommand(UserPortalItemModel value) {
                return value.getStopCommand();
            }
        };
        stopCell.setElementIdPrefix(elementIdPrefix);
        stopCell.setColumnId("stopButton"); //$NON-NLS-1$

        ImageButtonCell<UserPortalItemModel> rebootCell = new VmButtonsImageButtonCell(
                applicationResources.rebootIcon(), applicationResources.rebootDisabledIcon()) {

            @Override
            protected String getTitle(UserPortalItemModel value) {
                return constants.rebootVm();
            }

            @Override
            protected UICommand resolveCommand(UserPortalItemModel value) {
                return value.getRebootCommand();
            }
        };
        rebootCell.setElementIdPrefix(elementIdPrefix);
        rebootCell.setColumnId("rebootColumn"); //$NON-NLS-1$

        CompositeCell<UserPortalItemModel> compositeCell = new BorderedCompositeCell<UserPortalItemModel>(
                new ArrayList<HasCell<UserPortalItemModel, ?>>(Arrays.asList(
                        new UserPortalItemSimpleColumn(runCell),
                        new UserPortalItemSimpleColumn(shutdownCell),
                        new UserPortalItemSimpleColumn(suspendCell),
                        new UserPortalItemSimpleColumn(stopCell),
                        new UserPortalItemSimpleColumn(rebootCell))));

        return compositeCell;
    }

    public interface VmTableResources extends CellTable.Resources {
        interface TableStyle extends CellTable.Style {
        }

        @Override
        @Source({ CellTable.Style.DEFAULT_CSS, "org/ovirt/engine/ui/userportal/css/ExtendedVmListTable.css" })
        TableStyle cellTableStyle();
    }

    abstract class VmButtonsImageButtonCell extends ImageButtonCell<UserPortalItemModel> {

        public VmButtonsImageButtonCell(ImageResource enabledImage, ImageResource disabledImage) {
            super(enabledImage, applicationResources.sideTabExtendedVmStyle().vmButtonEnabled(),
                    disabledImage, applicationResources.sideTabExtendedVmStyle().vmButtonDisabled());
        }
    }

}
