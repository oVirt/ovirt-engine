package org.ovirt.engine.ui.userportal.section.main.view.tab.extended;

import java.util.List;

import org.gwtbootstrap3.client.ui.constants.IconType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.widget.refresh.SimpleRefreshManager;
import org.ovirt.engine.ui.common.widget.table.SimpleActionTable;
import org.ovirt.engine.ui.common.widget.table.cell.AbstractCell;
import org.ovirt.engine.ui.common.widget.table.cell.AbstractIconButtonCell;
import org.ovirt.engine.ui.common.widget.table.column.AbstractColumn;
import org.ovirt.engine.ui.uicommonweb.ErrorPopupManager;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.VmConsoles;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalListModel;
import org.ovirt.engine.ui.userportal.ApplicationConstants;
import org.ovirt.engine.ui.userportal.ApplicationResources;
import org.ovirt.engine.ui.userportal.ApplicationTemplates;
import org.ovirt.engine.ui.userportal.gin.AssetProvider;
import org.ovirt.engine.ui.userportal.gin.ClientGinjectorProvider;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.SideTabExtendedVirtualMachinePresenter;
import org.ovirt.engine.ui.userportal.section.main.view.AbstractSideTabWithDetailsView;
import org.ovirt.engine.ui.userportal.uicommon.model.vm.UserPortalListProvider;
import org.ovirt.engine.ui.userportal.widget.action.UserPortalButtonDefinition;
import org.ovirt.engine.ui.userportal.widget.basic.MainTabBasicListItemMessagesTranslator;
import org.ovirt.engine.ui.userportal.widget.extended.vm.AbstractConsoleButtonCell;
import org.ovirt.engine.ui.userportal.widget.extended.vm.ConsoleButtonCell;
import org.ovirt.engine.ui.userportal.widget.extended.vm.ConsoleEditButtonCell;
import org.ovirt.engine.ui.userportal.widget.table.UserPortalSimpleActionTable;
import org.ovirt.engine.ui.userportal.widget.table.column.AbstractUserportalMaskedDataurlImageColumn;
import org.ovirt.engine.ui.userportal.widget.table.column.VmStatusColumn;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.CellTable.Resources;
import com.google.gwt.user.cellview.client.RowStyles;
import com.google.inject.Inject;

public class SideTabExtendedVirtualMachineView extends AbstractSideTabWithDetailsView<UserPortalItemModel, UserPortalListModel>
        implements SideTabExtendedVirtualMachinePresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SideTabExtendedVirtualMachineView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    public interface VmTableResources extends CellTable.Resources {
        interface TableStyle extends CellTable.Style {
        }

        @Override
        @Source({ CellTable.Style.DEFAULT_CSS, "org/ovirt/engine/ui/userportal/css/ExtendedVmListTable.css" })
        TableStyle cellTableStyle();
    }

    private static final VmTableResources vmTableResources = GWT.create(VmTableResources.class);

    private final ErrorPopupManager errorPopupManager;

    private static final ApplicationTemplates templates = AssetProvider.getTemplates();
    private static final ApplicationResources resources = AssetProvider.getResources();
    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public SideTabExtendedVirtualMachineView(UserPortalListProvider modelProvider,
            ErrorPopupManager errorPopupManager,
            MainTabBasicListItemMessagesTranslator translator,
            ClientStorage clientStorage) {
        super(modelProvider, clientStorage);
        this.errorPopupManager = errorPopupManager;
        resources.sideTabExtendedVmStyle().ensureInjected();
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable();
    }

    @Override
    protected SimpleActionTable<UserPortalItemModel> createActionTable() {
        return new UserPortalSimpleActionTable<>(modelProvider,
                getTableResources(),
                ClientGinjectorProvider.getEventBus(),
                ClientGinjectorProvider.getClientStorage(),
                new SimpleRefreshManager(modelProvider,
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

    @SuppressWarnings({ "rawtypes", "unchecked" })
    void initTable() {
        final String elementIdPrefix = getTable().getContentTableElementId();

        ImageResource mask = resources.disabledSmallMask();

        AbstractUserportalMaskedDataurlImageColumn maskedVmImageColumn =
                new AbstractUserportalMaskedDataurlImageColumn(mask) {

                    @Override
                    public Guid getIconId(UserPortalItemModel itemModel) {
                        return itemModel.getSmallIconId();
                    }
                };

        getTable().addColumn(maskedVmImageColumn, constants.empty(), "77px"); //$NON-NLS-1$

        getTable().addColumn(new VmStatusColumn(), constants.empty(), "55px"); //$NON-NLS-1$

        AbstractCell<UserPortalItemModel> nameAndDescriptionCell = new AbstractCell<UserPortalItemModel>() {
            @Override
            public void render(Context context, UserPortalItemModel item, SafeHtmlBuilder sb, String id) {
                sb.append(templates.vmNameCellItem(
                        id,
                        item.getName()));

                String description = item.getDescription();
                if (description != null && !description.isEmpty()) {
                    sb.append(templates.vmDescriptionCellItem(description));
                }
            }
        };

        AbstractColumn<UserPortalItemModel, UserPortalItemModel> nameAndDescriptionColumn =
                new AbstractColumn<UserPortalItemModel, UserPortalItemModel>(nameAndDescriptionCell) {
            @Override
            public UserPortalItemModel getValue(UserPortalItemModel item) {
                return item;
            }
        };
        getTable().addColumn(nameAndDescriptionColumn, constants.empty(), "400px"); //$NON-NLS-1$

        ConsoleButtonCell openConsoleCell = new ConsoleButtonCell(
                resources.sideTabExtendedVmStyle().enabledConsoleButton(),
                resources.sideTabExtendedVmStyle().disabledConsoleButton(),
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
                }) {
            @Override
            public SafeHtml getTooltip(UserPortalItemModel value) {
                return SafeHtmlUtils.fromSafeConstant(constants.openConsoleLabel());
            }
        };
        openConsoleCell.setElementIdPrefix(elementIdPrefix);
        openConsoleCell.setColumnId("openConsoleButton"); //$NON-NLS-1$

        getTable().addColumn(new AbstractColumn(openConsoleCell) {
            @Override
            public Object getValue(Object object) {
                return object;
            }
        }, constants.empty(), "100px"); //$NON-NLS-1$

        ConsoleEditButtonCell consoleEditCell = new ConsoleEditButtonCell(
                resources.sideTabExtendedVmStyle().enabledEditConsoleButton(),
                resources.sideTabExtendedVmStyle().disabledEditConsoleButton(),
                new AbstractConsoleButtonCell.ConsoleButtonCommand() {
                    @Override
                    public void execute(UserPortalItemModel model) {
                        getModel().getEditConsoleCommand().execute();
                    }
                }) {
            @Override
            public SafeHtml getTooltip(UserPortalItemModel value) {
                return SafeHtmlUtils.fromSafeConstant(constants.editConsoleLabel());
            }
        };
        consoleEditCell.setElementIdPrefix(elementIdPrefix);
        consoleEditCell.setColumnId("editConsoleButton"); //$NON-NLS-1$

        getTable().addColumn(new AbstractColumn(consoleEditCell) {
            @Override
            public Object getValue(Object object) {
                return object;
            }

        }, constants.empty(), "30px"); //$NON-NLS-1$

        // start
        getTable().addColumn(new AbstractColumn<UserPortalItemModel, UserPortalItemModel>(
                new AbstractIconButtonCell<UserPortalItemModel>(IconType.PLAY) {
                    @Override
                    protected UICommand resolveCommand(UserPortalItemModel value) {
                        return value.isPool() ? value.getTakeVmCommand() : value.getRunCommand();
                    }

                }) {
            @Override
            public SafeHtml getTooltip(UserPortalItemModel value) {
                return SafeHtmlUtils.fromSafeConstant(value.isPool() ? constants.takeVmLabel() : constants.runVmLabel());
            }

            @Override
            public UserPortalItemModel getValue(UserPortalItemModel object) {
                return object;
            }
        }, constants.empty(), "15px"); //$NON-NLS-1$

        // shutdown
        getTable().addColumn(new AbstractColumn<UserPortalItemModel, UserPortalItemModel>(
                new AbstractIconButtonCell<UserPortalItemModel>(IconType.STOP) {
                    @Override
                    protected UICommand resolveCommand(UserPortalItemModel value) {
                        return value.isPool() ? value.getReturnVmCommand() : value.getShutdownCommand();
                    }

                }) {
            @Override
            public SafeHtml getTooltip(UserPortalItemModel value) {
                return SafeHtmlUtils.fromSafeConstant(value.isPool() ? constants.returnVmLabel() : constants.shutDownVm());
            }

            @Override
            public UserPortalItemModel getValue(UserPortalItemModel object) {
                return object;
            }
        }, constants.empty(), "15px"); //$NON-NLS-1$

        // suspend
        getTable().addColumn(new AbstractColumn<UserPortalItemModel, UserPortalItemModel>(
                new AbstractIconButtonCell<UserPortalItemModel>(IconType.PAUSE) {
                    @Override
                    protected UICommand resolveCommand(UserPortalItemModel value) {
                        return value.getPauseCommand();
                    }

                }) {
            @Override
            public SafeHtml getTooltip(UserPortalItemModel value) {
                return SafeHtmlUtils.fromSafeConstant(constants.suspendVmLabel());
            }

            @Override
            public UserPortalItemModel getValue(UserPortalItemModel object) {
                return object;
            }
        }, constants.empty(), "15px"); //$NON-NLS-1$

        // poweroff
        getTable().addColumn(new AbstractColumn<UserPortalItemModel, UserPortalItemModel>(
                new AbstractIconButtonCell<UserPortalItemModel>(IconType.POWER_OFF) {
                    @Override
                    protected UICommand resolveCommand(UserPortalItemModel value) {
                        return value.getStopCommand();
                    }

                }) {
            @Override
            public SafeHtml getTooltip(UserPortalItemModel value) {
                return SafeHtmlUtils.fromSafeConstant(constants.powerOffVm());
            }

            @Override
            public UserPortalItemModel getValue(UserPortalItemModel object) {
                return object;
            }
        }, constants.empty(), "15px"); //$NON-NLS-1$

        // reboot
        getTable().addColumn(new AbstractColumn<UserPortalItemModel, UserPortalItemModel>(
                // TODO: switch IconType.REFRESH to "pficon-spinner2" when ovirt-js-dependencies is
                // updated with the latest version of patternfly
                new AbstractIconButtonCell<UserPortalItemModel>(IconType.REFRESH) {
                    @Override
                    protected UICommand resolveCommand(UserPortalItemModel value) {
                        return value.getRebootCommand();
                    }

                }) {
            @Override
            public SafeHtml getTooltip(UserPortalItemModel value) {
                return SafeHtmlUtils.fromSafeConstant(constants.rebootVm());
            }

            @Override
            public UserPortalItemModel getValue(UserPortalItemModel object) {
                return object;
            }
        }, constants.empty(), "20px"); //$NON-NLS-1$


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
                        resources.sideTabExtendedVmStyle().vmUpRow() :
                            resources.sideTabExtendedVmStyle().vmDownRow();
            }

            protected boolean isSelectedRow(UserPortalItemModel row) {
                UserPortalItemModel selectedModel = getModel().getSelectedItem();
                if (selectedModel != null) {
                    if (modelProvider.getKey(selectedModel).equals(modelProvider.getKey(row))) {
                        return true;
                    }
                }

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
}
