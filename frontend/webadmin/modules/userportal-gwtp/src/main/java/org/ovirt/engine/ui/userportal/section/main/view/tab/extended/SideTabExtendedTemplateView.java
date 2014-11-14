package org.ovirt.engine.ui.userportal.section.main.view.tab.extended;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.utils.ElementIdUtils;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalTemplateListModel;
import org.ovirt.engine.ui.userportal.ApplicationConstants;
import org.ovirt.engine.ui.userportal.ApplicationResources;
import org.ovirt.engine.ui.userportal.ApplicationTemplates;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.SideTabExtendedTemplatePresenter;
import org.ovirt.engine.ui.userportal.section.main.view.AbstractSideTabWithDetailsView;
import org.ovirt.engine.ui.userportal.uicommon.model.template.UserPortalTemplateListProvider;
import org.ovirt.engine.ui.userportal.widget.action.UserPortalButtonDefinition;
import org.ovirt.engine.ui.userportal.widget.table.column.VmImageColumn;
import org.ovirt.engine.ui.userportal.widget.table.column.VmImageColumn.OsTypeExtractor;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.CellTable.Resources;
import com.google.gwt.user.cellview.client.Column;
import com.google.inject.Inject;

public class SideTabExtendedTemplateView extends AbstractSideTabWithDetailsView<VmTemplate, UserPortalTemplateListModel>
        implements SideTabExtendedTemplatePresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SideTabExtendedTemplateView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final TemplateTableResources templateTableResources = GWT.create(TemplateTableResources.class);

    @Inject
    public SideTabExtendedTemplateView(
            UserPortalTemplateListProvider provider,
            ApplicationTemplates templates,
            ApplicationConstants constants,
            ApplicationResources applicationResources,
            ClientStorage clientStorage) {
        super(provider, applicationResources, clientStorage);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable(templates, constants);
    }

    @Override
    protected Resources getTableResources() {
        return templateTableResources;
    }

    @Override
    protected Object getSubTabPanelContentSlot() {
        return SideTabExtendedTemplatePresenter.TYPE_SetSubTabPanelContent;
    }

    private void initTable(final ApplicationTemplates templates, ApplicationConstants constants) {
        final String elementIdPrefix = getTable().getContentTableElementId();

        getTable().addColumn(new VmImageColumn<VmTemplate>(new OsTypeExtractor<VmTemplate>() {
            @Override
            public int extractOsType(VmTemplate item) {
                return item.getOsId();
            }
        }), "", "77px"); //$NON-NLS-1$ //$NON-NLS-2$

        Cell<VmTemplate> nameAndDescriptionCell = new AbstractCell<VmTemplate>() {
            @Override
            public void render(Context context, VmTemplate template, SafeHtmlBuilder sb) {
                sb.append(templates.vmNameCellItem(
                        ElementIdUtils.createTableCellElementId(elementIdPrefix, "name", context), //$NON-NLS-1$
                        template.getName()));

                String description = template.getDescription();
                if (description != null && !description.isEmpty()) {
                    sb.append(templates.vmDescriptionCellItem(description));
                }
            }
        };

        Column<VmTemplate, VmTemplate> nameAndDescriptionColumn = new Column<VmTemplate, VmTemplate>(nameAndDescriptionCell) {
            @Override
            public VmTemplate getValue(VmTemplate template) {
                return template;
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
