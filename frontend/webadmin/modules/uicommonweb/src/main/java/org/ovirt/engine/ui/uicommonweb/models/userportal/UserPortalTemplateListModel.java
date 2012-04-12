package org.ovirt.engine.ui.uicommonweb.models.userportal;

import org.ovirt.engine.core.common.VdcActionUtils;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmTemplateStatus;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.compat.ObservableCollection;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateListModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.UserPortalTemplateEventListModel;

public class UserPortalTemplateListModel extends TemplateListModel
{
    @Override
    protected void SyncSearch()
    {
        AsyncDataProvider.GetVmTemplatesWithPermittedAction(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object returnValue) {
                ((UserPortalTemplateListModel) model).setItems((Iterable) returnValue);
            }
        }), ActionGroup.EDIT_TEMPLATE_PROPERTIES);
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

    @Override
    protected void addCustomModelsDetailModelList(ObservableCollection<EntityModel> list) {
        list.add(new UserPortalTemplateEventListModel());
    }
}
