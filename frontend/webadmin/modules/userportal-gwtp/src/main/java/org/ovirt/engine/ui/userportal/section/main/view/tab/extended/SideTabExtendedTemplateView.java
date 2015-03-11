package org.ovirt.engine.ui.userportal.section.main.view.tab.extended;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.utils.ElementIdUtils;
import org.ovirt.engine.ui.common.widget.table.cell.TextCellWithTooltip;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalTemplateListModel;
import org.ovirt.engine.ui.userportal.ApplicationConstants;
import org.ovirt.engine.ui.userportal.ApplicationResources;
import org.ovirt.engine.ui.userportal.ApplicationTemplates;
import org.ovirt.engine.ui.userportal.SideTabWithDetailsViewStyle;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.SideTabExtendedTemplatePresenter;
import org.ovirt.engine.ui.userportal.section.main.view.AbstractSideTabWithDetailsView;
import org.ovirt.engine.ui.userportal.uicommon.model.template.UserPortalTemplateListProvider;
import org.ovirt.engine.ui.userportal.widget.action.UserPortalButtonDefinition;
import org.ovirt.engine.ui.userportal.widget.table.column.VmImageColumn;
import org.ovirt.engine.ui.userportal.widget.table.column.VmImageColumn.OsTypeExtractor;


import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
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

    private static final TemplateTableHeaderResources TEMPLATE_TABLE_HEADER_RESOURCES =
            GWT.create(TemplateTableHeaderResources.class);

    private static final TemplateSideTabWithDetailsViewStyle TEMPLATE_SIDE_TAB_WITH_DETAILS_VIEW_STYLE =
            GWT.create(TemplateSideTabWithDetailsViewStyle.class);
    static {
        TEMPLATE_SIDE_TAB_WITH_DETAILS_VIEW_STYLE.templateSideTab().ensureInjected();
    }

    @Inject
    public SideTabExtendedTemplateView(
            UserPortalTemplateListProvider provider,
            ApplicationTemplates templates,
            ApplicationConstants constants,
            CommonApplicationConstants commonConstants,
            ApplicationResources applicationResources,
            ClientStorage clientStorage) {
        super(provider, applicationResources, clientStorage);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable(templates, constants, commonConstants);
    }

    @Override
    protected Resources getTableResources() {
        return templateTableResources;
    }

    @Override
    protected Resources getTableHeaderResources() {
        return TEMPLATE_TABLE_HEADER_RESOURCES;
    }

    @Override
    protected String getTableContainerStyleName() {
        return TEMPLATE_SIDE_TAB_WITH_DETAILS_VIEW_STYLE.templateSideTab().mainContentPanel();
    }

    @Override
    protected Object getSubTabPanelContentSlot() {
        return SideTabExtendedTemplatePresenter.TYPE_SetSubTabPanelContent;
    }

    private void initTable(final ApplicationTemplates templates, ApplicationConstants constants,
                           CommonApplicationConstants commonConstants) {
        getTable().enableColumnResizing();
        final String elementIdPrefix = getTable().getContentTableElementId();

        getTable().addColumn(new VmImageColumn<VmTemplate>(new OsTypeExtractor<VmTemplate>() {
            @Override
            public int extractOsType(VmTemplate item) {
                return item.getOsId();
            }
        }), "", "77px"); //$NON-NLS-1$ //$NON-NLS-2$

        Cell<VmTemplate> nameCell = new AbstractCell<VmTemplate>() {
            @Override
            public void render(Context context, VmTemplate template, SafeHtmlBuilder sb) {
                sb.append(templates.vmNameCellItem(
                        ElementIdUtils.createTableCellElementId(elementIdPrefix, "name", context), //$NON-NLS-1$
                        template.getName()));
            }
        };

        Column<VmTemplate, VmTemplate> nameColumn = new Column<VmTemplate, VmTemplate>(nameCell) {
            @Override
            public VmTemplate getValue(VmTemplate template) {
                return template;
            }
        };
        getTable().addColumn(nameColumn, commonConstants.templateName(), "350px"); //$NON-NLS-1$

        final TextCellWithTooltip subversionNumberCell = new TextCellWithTooltip(TextCellWithTooltip.UNLIMITED_LENGTH);

        final AbstractTextColumn<VmTemplate> subversionNumberColumn = new AbstractTextColumn<VmTemplate>(subversionNumberCell) {

            @Override
            public String getValue(VmTemplate template) {
                return "(" + template.getTemplateVersionNumber() + ")"; //$NON-NLS-1$ //$NON-NLS-2$;
            }
        };
        table.addColumn(subversionNumberColumn, commonConstants.templateVersion(), "140px"); //$NON-NLS-1$

        final TextCellWithTooltip subversionNameCell = new TextCellWithTooltip(TextCellWithTooltip.UNLIMITED_LENGTH);

        final AbstractTextColumn<VmTemplate> subversionNameColumn = new AbstractTextColumn<VmTemplate>(subversionNameCell) {

            @Override
            public String getValue(VmTemplate template) {
                return template.getTemplateVersionName() != null && !template.getTemplateVersionName().isEmpty()
                        ? template.getTemplateVersionName()
                        : "";
            }
        };
        table.addColumn(subversionNameColumn, commonConstants.templateVersionName(), "350px"); //$NON-NLS-1$

        final TextCellWithTooltip descriptionCell = new TextCellWithTooltip(TextCellWithTooltip.UNLIMITED_LENGTH);

        final AbstractTextColumn<VmTemplate> descriptionColumn = new AbstractTextColumn<VmTemplate>(descriptionCell) {

            @Override
            public String getValue(VmTemplate template) {
                return template.getDescription() != null && !template.getDescription().isEmpty()
                        ? template.getDescription()
                        : "";
            }
        };
        table.addColumn(descriptionColumn, commonConstants.templateDescription());

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

    public interface TemplateTableHeaderResources extends CellTable.Resources {

        @Source({ CellTable.Style.DEFAULT_CSS,
                "org/ovirt/engine/ui/userportal/css/ExtendedTemplateListTable.css",
                "org/ovirt/engine/ui/userportal/css/ExtendedTemplateListTableHeader.css"})
        @Override
        CellTable.Style cellTableStyle();
    }

    public interface TemplateSideTabWithDetailsViewStyle extends ClientBundle {

        @Source({"org/ovirt/engine/ui/userportal/css/SideTabWithDetailsViewStyle.css",
                 "org/ovirt/engine/ui/userportal/css/SideTabExtendedTemplateViewStyle.css"})
        SideTabWithDetailsViewStyle templateSideTab();
    }

}
