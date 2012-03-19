package org.ovirt.engine.ui.webadmin.uicommon;

import java.util.LinkedList;
import java.util.List;

import org.ovirt.engine.ui.common.widget.action.ActionButtonDefinition;
import org.ovirt.engine.ui.uicommonweb.ReportInit;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ReportParser.Category;
import org.ovirt.engine.ui.uicompat.ReportParser.Resource;
import org.ovirt.engine.ui.uicompat.ReportParser.URI;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminMenuBarButtonDefinition;

public class ReportActionsHelper {

    private static final ReportActionsHelper INSTANCE = new ReportActionsHelper();

    public static ReportActionsHelper getInstance() {
        return INSTANCE;
    }


    private ReportActionsHelper() {
    }

    public <T> List<ActionButtonDefinition<T>> getResourceSubActions(String resourceType,
            final SearchableListModel model) {
        List<ActionButtonDefinition<T>> subActions = new LinkedList<ActionButtonDefinition<T>>();

        Resource resource = ReportInit.getInstance().getResource(resourceType);
        if (resource != null) {
            for (Category category : resource.getCatergoriesList()) {
                List<ActionButtonDefinition<T>> categerySubActions =
                        getCategorySubActions(category, model);
                subActions.add(new WebAdminMenuBarButtonDefinition<T>(category.getName(), categerySubActions, true) {
                    @Override
                    public boolean isVisible(List<T> selectedItems) {
                        boolean isVisible = false;

                        for (ActionButtonDefinition<T> subAction : getSubActions()) {
                            if (subAction.isVisible(selectedItems)) {
                                return true;
                            }
                        }
                        return isVisible;
                    }
                });
            }
        }

        return subActions;
    }

    public <T> List<ActionButtonDefinition<T>> getCategorySubActions(final Category category,
            final SearchableListModel model) {
        List<ActionButtonDefinition<T>> subActions = new LinkedList<ActionButtonDefinition<T>>();

        for (final URI uri : category.getUriList()) {
            subActions.add(new WebAdminButtonDefinition<T>(uri.getName(), true, false, null, true, uri.getDescription()) {

                @Override
                public boolean isVisible(List<T> selectedItems) {
                    return isEnabled(selectedItems);
                }

                @Override
                protected UICommand resolveCommand() {

                    return model.addOpenReportCommand(uri.getId(), uri.isMultiple(), uri.getValue());
                }
            });
        }

        return subActions;
    }
}
