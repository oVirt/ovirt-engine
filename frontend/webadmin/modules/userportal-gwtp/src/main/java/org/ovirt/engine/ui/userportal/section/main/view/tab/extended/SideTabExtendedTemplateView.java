package org.ovirt.engine.ui.userportal.section.main.view.tab.extended;

import org.ovirt.engine.core.common.businessentities.VmOsType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.ui.common.widget.table.SimpleActionTable;
import org.ovirt.engine.ui.common.widget.table.column.SafeHtmlColumn;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalTemplateListModel;
import org.ovirt.engine.ui.userportal.ApplicationConstants;
import org.ovirt.engine.ui.userportal.ApplicationTemplates;
import org.ovirt.engine.ui.userportal.gin.ClientGinjectorProvider;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.SideTabExtendedTemplatePresenter;
import org.ovirt.engine.ui.userportal.section.main.view.AbstractSideTabWithDetailsView;
import org.ovirt.engine.ui.userportal.uicommon.model.template.UserPortalTemplateListProvider;
import org.ovirt.engine.ui.userportal.widget.action.UserPortalButtonDefinition;
import org.ovirt.engine.ui.userportal.widget.table.column.VmImageColumn;
import org.ovirt.engine.ui.userportal.widget.table.column.VmImageColumn.OsTypeExtractor;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.inject.Inject;

public class SideTabExtendedTemplateView extends AbstractSideTabWithDetailsView<VmTemplate, UserPortalTemplateListModel>
        implements SideTabExtendedTemplatePresenter.ViewDef {

    private static final TemplateTableResources templateTableResources = GWT.create(TemplateTableResources.class);

    @Inject
    public SideTabExtendedTemplateView(UserPortalTemplateListProvider provider, ApplicationTemplates templates, ApplicationConstants constants) {
        super(provider);
        initTable(templates, constants);
    }

    @Override
    protected SimpleActionTable<VmTemplate> createActionTable() {
        return new SimpleActionTable<VmTemplate>(modelProvider,
                templateTableResources,
                ClientGinjectorProvider.instance().getEventBus(),
                ClientGinjectorProvider.instance().getClientStorage());
    }

    @Override
    protected Object getSubTabPanelContentSlot() {
        return SideTabExtendedTemplatePresenter.TYPE_SetSubTabPanelContent;
    }

    private void initTable(final ApplicationTemplates templates, ApplicationConstants constants) {
        getTable().addColumn(new VmImageColumn<VmTemplate>(new OsTypeExtractor<VmTemplate>() {

            @Override
            public VmOsType extractOsType(VmTemplate item) {
                return item.getos();
            }
        }), "", "77px"); //$NON-NLS-1$ //$NON-NLS-2$

        SafeHtmlColumn<VmTemplate> nameAndDescriptionColumn = new SafeHtmlColumn<VmTemplate>() {
            @Override
            public SafeHtml getValue(VmTemplate template) {
                SafeHtmlBuilder builder = new SafeHtmlBuilder();
                builder.append(templates.vmNameCellItem(template.getname()));

                if (template.getdescription() != null) {
                    builder.append(templates.vmDescriptionCellItem(template.getdescription()));
                }

                return builder.toSafeHtml();
            }
        };
        getTable().addColumn(nameAndDescriptionColumn, constants.empty());

        getTable().addActionButton(new UserPortalButtonDefinition<VmTemplate>(constants.editTemplate()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getEditCommand();
            }
        });

        getTable().addActionButton(new UserPortalButtonDefinition<VmTemplate>(constants.removeTemplate()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getRemoveCommand();
            }
        });
    }

    public interface TemplateTableResources extends CellTable.Resources {
        interface TableStyle extends CellTable.Style {
        }

        @Override
        @Source({ CellTable.Style.DEFAULT_CSS, "org/ovirt/engine/ui/userportal/css/ExtendedTemplateListTable.css" })
        TableStyle cellTableStyle();
    }

}
