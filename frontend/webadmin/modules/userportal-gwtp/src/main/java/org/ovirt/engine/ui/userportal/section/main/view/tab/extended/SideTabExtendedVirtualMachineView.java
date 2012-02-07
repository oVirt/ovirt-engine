package org.ovirt.engine.ui.userportal.section.main.view.tab.extended;

import org.ovirt.engine.ui.common.widget.table.column.SafeHtmlColumn;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalListModel;
import org.ovirt.engine.ui.userportal.ApplicationTemplates;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.SideTabExtendedVirtualMachinePresenter;
import org.ovirt.engine.ui.userportal.section.main.view.AbstractSideTabWithDetailsView;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalListProvider;
import org.ovirt.engine.ui.userportal.widget.action.UserPortalButtonDefinition;
import org.ovirt.engine.ui.userportal.widget.table.column.VmImageColumn;
import org.ovirt.engine.ui.userportal.widget.table.column.VmStatusColumn;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.inject.Inject;

public class SideTabExtendedVirtualMachineView extends AbstractSideTabWithDetailsView<UserPortalItemModel, UserPortalListModel>
        implements SideTabExtendedVirtualMachinePresenter.ViewDef {

    @Inject
    public SideTabExtendedVirtualMachineView(UserPortalListProvider modelProvider, ApplicationTemplates templates) {
        super(modelProvider);
        initTable(templates);
    }

    @Override
    protected Object getSubTabPanelContentSlot() {
        return SideTabExtendedVirtualMachinePresenter.TYPE_SetSubTabPanelContent;
    }

    void initTable(final ApplicationTemplates templates) {
        getTable().addColumn(new VmImageColumn(), "", "77px");
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
        getTable().addColumn(nameAndDescriptionColumn, "");

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

}
