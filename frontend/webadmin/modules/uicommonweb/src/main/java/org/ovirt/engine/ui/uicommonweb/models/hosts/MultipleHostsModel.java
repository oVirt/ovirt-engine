package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.ArrayList;

import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;

public class MultipleHostsModel extends Model {

    ListModel hosts;

    EntityModel useCommonPassword;
    EntityModel commonPassword;

    ClusterModel clusterModel;

    private UICommand applyPasswordCommand;

    public MultipleHostsModel()
    {
        setHosts(new ListModel());
        setUseCommonPassword(new EntityModel());
        setCommonPassword(new EntityModel());
        setApplyPasswordCommand(new UICommand("ApplyPassword", this)); //$NON-NLS-1$

        getUseCommonPassword().getEntityChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                getCommonPassword().setIsChangable((Boolean) getUseCommonPassword().getEntity());
                getApplyPasswordCommand().setIsExecutionAllowed((Boolean) getUseCommonPassword().getEntity());
            }
        });
        getUseCommonPassword().setEntity(false);
    }

    public ListModel getHosts() {
        return hosts;
    }

    public void setHosts(ListModel hosts) {
        this.hosts = hosts;
    }

    public EntityModel getUseCommonPassword() {
        return useCommonPassword;
    }

    public void setUseCommonPassword(EntityModel useCommonPassword) {
        this.useCommonPassword = useCommonPassword;
    }

    public EntityModel getCommonPassword() {
        return commonPassword;
    }

    public void setCommonPassword(EntityModel commonPassword) {
        this.commonPassword = commonPassword;
    }

    public ClusterModel getClusterModel() {
        return clusterModel;
    }

    public void setClusterModel(ClusterModel clusterModel) {
        this.clusterModel = clusterModel;
    }

    public UICommand getApplyPasswordCommand() {
        return applyPasswordCommand;
    }

    public void setApplyPasswordCommand(UICommand applyPasswordCommand) {
        this.applyPasswordCommand = applyPasswordCommand;
    }

    @Override
    public void ExecuteCommand(UICommand command)
    {
        super.ExecuteCommand(command);

        if (command == getApplyPasswordCommand())
        {
            applyPassword();
        }
    }

    private void applyPassword() {
        String password = (String) getCommonPassword().getEntity();
        ArrayList<EntityModel> items = new ArrayList<EntityModel>();
        for (Object object : getHosts().getItems())
        {
            HostDetailModel host = (HostDetailModel) ((EntityModel) object).getEntity();
            host.setPassword(password);

            EntityModel entityModel = new EntityModel();
            entityModel.setEntity(host);
            items.add(entityModel);
        }
        getHosts().setItems(items);
    }

    public boolean validate() {
        boolean isValid = true;
        setMessage(null);
        Iterable<EntityModel> items = getHosts().getItems();
        for (EntityModel model : items)
        {
            HostDetailModel host = (HostDetailModel) model.getEntity();
            if (host.getName().trim().length() == 0)
            {
                setMessage(ConstantsManager.getInstance().getMessages().importClusterHostNameEmpty(host.getAddress()));
                isValid = false;
                break;
            }
            else if (host.getPassword().trim().length() == 0)
            {
                setMessage(ConstantsManager.getInstance()
                        .getMessages()
                        .importClusterHostPasswordEmpty(host.getAddress()));
                isValid = false;
                break;
            }
            else if (host.getFingerprint().trim().length() == 0)
            {
                setMessage(ConstantsManager.getInstance()
                        .getMessages()
                        .importClusterHostFingerprintEmpty(host.getAddress()));
                isValid = false;
                break;
            }
        }
        return isValid;
    }
}
