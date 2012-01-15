package org.ovirt.engine.ui.uicommonweb.models.userportal;

import java.util.Collections;

import org.ovirt.engine.core.common.VdcActionUtils;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmTemplateStatus;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.common.queries.MultilevelAdministrationByAdElementIdParameters;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateListModel;
import org.ovirt.engine.ui.uicompat.FrontendMultipleQueryAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleQueryAsyncCallback;

@SuppressWarnings("unused")
public class UserPortalTemplateListModel extends TemplateListModel implements IFrontendMultipleQueryAsyncCallback
{
    @Override
    protected void SyncSearch()
    {
        MultilevelAdministrationByAdElementIdParameters parameters =
                new MultilevelAdministrationByAdElementIdParameters(Frontend.getLoggedInUser().getUserId());

        // Get user permissions and send them to PostGetUserPermissions:
        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object ReturnValue)
            {
                UserPortalTemplateListModel userPortalTemplateListModel = (UserPortalTemplateListModel) model;
                java.util.ArrayList<permissions> userPermissions =
                        ReturnValue != null ? (java.util.ArrayList<permissions>) ((VdcQueryReturnValue) ReturnValue).getReturnValue()
                                : new java.util.ArrayList<permissions>();

                userPortalTemplateListModel.PostGetUserPermissions(userPermissions);
            }
        };

        Frontend.RunQuery(VdcQueryType.GetPermissionsByAdElementId, parameters, _asyncQuery);
    }

    public void PostGetUserPermissions(java.util.ArrayList<permissions> userPermissions)
    {
        java.util.ArrayList<VdcQueryType> listQueryType = new java.util.ArrayList<VdcQueryType>();
        java.util.ArrayList<VdcQueryParametersBase> listQueryParameters =
                new java.util.ArrayList<VdcQueryParametersBase>();

        for (permissions userPermission : userPermissions)
        {
            if (userPermission.getObjectType() == VdcObjectType.System)
            {
                // User has a permission on System -> Get all templates in the system:
                listQueryType.add(VdcQueryType.Search);
                SearchParameters searchParams = new SearchParameters("Template:", SearchType.VmTemplate);
                searchParams.setMaxCount(9999);
                listQueryParameters.add(searchParams);
                break;
            }
            else
            {
                // if user has a permission on a Template, add a query-request for that template:
                if (userPermission.getObjectType() == VdcObjectType.VmTemplate)
                {
                    listQueryType.add(VdcQueryType.GetVmTemplate);
                    listQueryParameters.add(new GetVmTemplateParameters(userPermission.getObjectId()));
                }
                // if user has a permission on a DataCenter, add a query-request for all the templates in that
                // DataCenter:
                else if (userPermission.getObjectType() == VdcObjectType.StoragePool)
                {
                    listQueryType.add(VdcQueryType.Search);
                    SearchParameters searchParams =
                            new SearchParameters("Template: datacenter = " + userPermission.getObjectName(),
                                    SearchType.VmTemplate);
                    searchParams.setMaxCount(9999);
                    listQueryParameters.add(searchParams);
                }
            }
        }

        GetUserTemplates(listQueryType, listQueryParameters);
    }

    private void GetUserTemplates(java.util.ArrayList<VdcQueryType> listQueryType,
            java.util.ArrayList<VdcQueryParametersBase> listQueryParameters)
    {
        if (listQueryType.isEmpty())
        {
            setItems(new java.util.ArrayList<VmTemplate>());
        }
        else
        {
            Frontend.RunMultipleQueries(listQueryType, listQueryParameters, this);
        }
    }

    @Override
    public void Executed(FrontendMultipleQueryAsyncResult result)
    {
        java.util.ArrayList<VmTemplate> items = new java.util.ArrayList<VmTemplate>();

        if (result != null)
        {
            java.util.List<VdcQueryType> listQueryType = result.getQueryTypes();
            java.util.List<VdcQueryReturnValue> listReturnValue = result.getReturnValues();
            for (int i = 0; i < listQueryType.size(); i++)
            {
                switch (listQueryType.get(i))
                {
                case GetVmTemplate:
                    if (listReturnValue.get(i) != null && listReturnValue.get(i).getSucceeded()
                            && listReturnValue.get(i).getReturnValue() != null)
                    {
                        VmTemplate template = (VmTemplate) listReturnValue.get(i).getReturnValue();
                        items.add(template);
                    }
                    break;

                case Search:
                    if (listReturnValue.get(i) != null && listReturnValue.get(i).getSucceeded()
                            && listReturnValue.get(i).getReturnValue() != null)
                    {
                        java.util.ArrayList<VmTemplate> templateList =
                                (java.util.ArrayList<VmTemplate>) listReturnValue.get(i).getReturnValue();
                        items.addAll(templateList);
                    }
                    break;
                }
            }
        }

        // Sort templates list
        java.util.ArrayList<VmTemplate> list = new java.util.ArrayList<VmTemplate>();
        VmTemplate blankTemplate = new VmTemplate();
        for (VmTemplate template : items)
        {
            if (template.getId().equals(NGuid.Empty))
            {
                blankTemplate = template;
                continue;
            }
            list.add(template);
        }
        Collections.sort(list, new Linq.VmTemplateByNameComparer());
        if (items.contains(blankTemplate))
        {
            list.add(0, blankTemplate);
        }

        setItems(list);
    }

    @Override
    protected void UpdateActionAvailability()
    {
        VmTemplate item = (VmTemplate) getSelectedItem();
        if (item != null)
        {
            java.util.ArrayList items = new java.util.ArrayList();
            items.add(item);
            getEditCommand().setIsExecutionAllowed(item.getstatus() != VmTemplateStatus.Locked
                    && !item.getId().equals(NGuid.Empty));
            getRemoveCommand().setIsExecutionAllowed(VdcActionUtils.CanExecute(items,
                    VmTemplate.class,
                    VdcActionType.RemoveVmTemplate));
        }
        else
        {
            getEditCommand().setIsExecutionAllowed(false);
            getRemoveCommand().setIsExecutionAllowed(false);
        }
    }

}
