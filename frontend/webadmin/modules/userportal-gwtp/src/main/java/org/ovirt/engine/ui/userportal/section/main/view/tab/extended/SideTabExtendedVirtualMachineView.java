package org.ovirt.engine.ui.userportal.section.main.view.tab.extended;

import java.util.ArrayList;
import java.util.Arrays;

import org.ovirt.engine.core.common.businessentities.VmOsType;
import org.ovirt.engine.ui.common.system.ErrorPopupManager;
import org.ovirt.engine.ui.common.widget.table.column.SafeHtmlColumn;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalListModel;
import org.ovirt.engine.ui.userportal.ApplicationResources;
import org.ovirt.engine.ui.userportal.ApplicationTemplates;
import org.ovirt.engine.ui.userportal.section.main.presenter.popup.console.ConsolePopupPresenterWidget;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.ConsoleManager;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.SideTabExtendedVirtualMachinePresenter;
import org.ovirt.engine.ui.userportal.section.main.view.AbstractSideTabWithDetailsView;
import org.ovirt.engine.ui.userportal.section.main.view.tab.basic.widget.ConsoleUtils;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalListProvider;
import org.ovirt.engine.ui.userportal.widget.action.UserPortalButtonDefinition;
import org.ovirt.engine.ui.userportal.widget.action.UserPortalImageButtonDefinition;
import org.ovirt.engine.ui.userportal.widget.extended.vm.BorderedCompositeCell;
import org.ovirt.engine.ui.userportal.widget.extended.vm.ConsoleButtonCell;
import org.ovirt.engine.ui.userportal.widget.extended.vm.ImageButtonCell;
import org.ovirt.engine.ui.userportal.widget.extended.vm.UserPortalItemImageButtonColumn;
import org.ovirt.engine.ui.userportal.widget.table.column.VmImageColumn;
import org.ovirt.engine.ui.userportal.widget.table.column.VmImageColumn.OsTypeExtractor;
import org.ovirt.engine.ui.userportal.widget.table.column.VmStatusColumn;
import org.ovirt.engine.ui.userportal.widget.extended.vm.ConsoleButtonCell.ConsoleButtonCommand;

import com.google.gwt.cell.client.CompositeCell;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.Column;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.proxy.RevealRootPopupContentEvent;

public class SideTabExtendedVirtualMachineView extends AbstractSideTabWithDetailsView<UserPortalItemModel, UserPortalListModel>
        implements SideTabExtendedVirtualMachinePresenter.ViewDef {

    private final ApplicationResources applicationResources;
    private final ConsoleManager consoleManager;
    private final ErrorPopupManager errorPopupManager;
    private final ConsolePopupPresenterWidget consolePopup;
    private final EventBus eventBus;

    @Inject
    public SideTabExtendedVirtualMachineView(UserPortalListProvider modelProvider,
            ApplicationTemplates templates,
            ApplicationResources applicationResources,
            ConsoleUtils consoleUtils,
            ConsoleManager consoleManager,
            ErrorPopupManager errorPopupManager,
            ConsolePopupPresenterWidget consolePopup,
            EventBus eventBus
            ) {
        super(modelProvider);
        this.applicationResources = applicationResources;
        this.consoleManager = consoleManager;
        this.errorPopupManager = errorPopupManager;
        this.consolePopup = consolePopup;
        this.eventBus = eventBus;
        applicationResources.consoleButtonResource().ensureInjected();
        initTable(templates, consoleUtils);
    }

    @Override
    protected Object getSubTabPanelContentSlot() {
        return SideTabExtendedVirtualMachinePresenter.TYPE_SetSubTabPanelContent;
    }

    void initTable(final ApplicationTemplates templates,
            final ConsoleUtils consoleUtils) {
        getTable().addColumn(new VmImageColumn<UserPortalItemModel>(new OsTypeExtractor<UserPortalItemModel>() {

            @Override
            public VmOsType extractOsType(UserPortalItemModel item) {
                return item.getOsType();
            }
        }), "", "77px");

        getTable().addColumn(new VmStatusColumn(), "", "55px");

        SafeHtmlColumn<UserPortalItemModel> nameAndDescriptionColumn = new SafeHtmlColumn<UserPortalItemModel>() {
            @Override
            public SafeHtml getValue(UserPortalItemModel item) {
                SafeHtmlBuilder builder = new SafeHtmlBuilder();
                builder.append(templates.vmNameCellItem(item.getName()));

                if (item.getDescription() != null) {
                    builder.append(templates.vmDescriptionCellItem(item.getDescription()));
                }

                return builder.toSafeHtml();
            }
        };
        getTable().addColumn(nameAndDescriptionColumn, "", "400px");

        getTable().addColumn(new Column<UserPortalItemModel, UserPortalItemModel>(createActionsCompositeCell()) {

            @Override
            public UserPortalItemModel getValue(UserPortalItemModel object) {
                return object;
            }
        }, "", "130px");

        ConsoleButtonCell openConsoleCell = new ConsoleButtonCell(
                consoleUtils,
                "enabledConsoleButton",
                "disabledConsoleButton",
                "Open Console",
                new ConsoleButtonCommand() {

                    @Override
                    public void execute(UserPortalItemModel model) {
                        String message =
                                consoleManager.connectToConsole(consoleUtils.determineDefaultProtocol(model), model);
                        if (message != null) {
                            errorPopupManager.show(message);
                        }
                    }
                });

        getTable().addColumn(new UserPortalItemImageButtonColumn(openConsoleCell), "", "100px");

        ConsoleButtonCell consoleEditCell = new ConsoleButtonCell(
                consoleUtils,
                "enabledEditConsoleButton",
                "disabledEditConsoleButton",
                "Edit Console Options",
                new ConsoleButtonCommand() {

                    @Override
                    public void execute(UserPortalItemModel model) {
                        consolePopup.init(getModel());
                        RevealRootPopupContentEvent.fire(eventBus, consolePopup);
                    }
                });

        getTable().addColumn(new UserPortalItemImageButtonColumn(consoleEditCell), "");

        getTable().addActionButton(new UserPortalButtonDefinition<UserPortalItemModel>("New Server") {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getNewServerCommand();
            }
        });
        getTable().addActionButton(new UserPortalButtonDefinition<UserPortalItemModel>("New Desktop") {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getNewDesktopCommand();
            }
        });
        getTable().addActionButton(new UserPortalButtonDefinition<UserPortalItemModel>("Edit") {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getEditCommand();
            }
        });
        getTable().addActionButton(new UserPortalButtonDefinition<UserPortalItemModel>("Remove") {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getRemoveCommand();
            }
        });
        getTable().addActionButton(new UserPortalButtonDefinition<UserPortalItemModel>("Run Once") {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getRunOnceCommand();
            }
        });
        getTable().addActionButton(new UserPortalButtonDefinition<UserPortalItemModel>("Change CD") {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getChangeCdCommand();
            }
        });
        getTable().addActionButton(new UserPortalButtonDefinition<UserPortalItemModel>("Make Template") {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getNewTemplateCommand();
            }
        });
    }

    protected CompositeCell<UserPortalItemModel> createActionsCompositeCell() {
        ImageButtonCell<UserPortalItemModel> runCell = new ImageButtonCell<UserPortalItemModel>() {

            @Override
            protected UserPortalImageButtonDefinition<UserPortalItemModel> createButtonDefinition(final UserPortalItemModel data) {
                return new UserPortalImageButtonDefinition<UserPortalItemModel>(
                        data.getIsPool() ? "Take VM" : "Run",
                        applicationResources.playIcon(),
                        applicationResources.playDisabledIcon()
                ) {

                    @Override
                    protected UICommand resolveCommand() {
                        return data.getIsPool() ? data.getTakeVmCommand() : data.getRunCommand();
                    }

                };
            }
        };

        ImageButtonCell<UserPortalItemModel> shutdownCell = new ImageButtonCell<UserPortalItemModel>() {

            @Override
            protected UserPortalImageButtonDefinition<UserPortalItemModel> createButtonDefinition(final UserPortalItemModel data) {
                return new UserPortalImageButtonDefinition<UserPortalItemModel>(
                        data.getIsPool() ? "Return VM" : "Shutdown",
                        applicationResources.stopIcon(),
                        applicationResources.stopDisabledIcon()
                ) {

                    @Override
                    protected UICommand resolveCommand() {
                        return data.getIsPool() ? data.getReturnVmCommand() : data.getShutdownCommand();
                    }

                };
            }
        };

        ImageButtonCell<UserPortalItemModel> pauseCell = new ImageButtonCell<UserPortalItemModel>() {

            @Override
            protected UserPortalImageButtonDefinition<UserPortalItemModel> createButtonDefinition(final UserPortalItemModel data) {
                return new UserPortalImageButtonDefinition<UserPortalItemModel>(
                        "Pause",
                        applicationResources.pauseIcon(),
                        applicationResources.pauseDisabledIcon()
                ) {

                    @Override
                    protected UICommand resolveCommand() {
                        return data.getPauseCommand();
                    }

                };
            }
        };

        ImageButtonCell<UserPortalItemModel> stopCell = new ImageButtonCell<UserPortalItemModel>() {

            @Override
            protected UserPortalImageButtonDefinition<UserPortalItemModel> createButtonDefinition(final UserPortalItemModel data) {
                return new UserPortalImageButtonDefinition<UserPortalItemModel>(
                        "Stop",
                        applicationResources.powerIcon(),
                        applicationResources.powerDisabledIcon()
                ) {

                    @Override
                    protected UICommand resolveCommand() {
                        return data.getStopCommand();
                    }

                };
            }
        };

        CompositeCell<UserPortalItemModel> compositeCell =
                new BorderedCompositeCell<UserPortalItemModel>(
                        new ArrayList<HasCell<UserPortalItemModel, ?>>(
                                Arrays.asList(
                                        new UserPortalItemImageButtonColumn(runCell),
                                        new UserPortalItemImageButtonColumn(shutdownCell),
                                        new UserPortalItemImageButtonColumn(pauseCell),
                                        new UserPortalItemImageButtonColumn(stopCell)
                                        )
                        )
                );
        return compositeCell;
    }

}
