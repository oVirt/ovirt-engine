package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class MultipleHostsModel extends Model {

    ListModel<EntityModel<HostDetailModel>> hosts;

    EntityModel<Boolean> useCommonPassword;
    EntityModel<String> commonPassword;

    boolean configureFirewall;
    ClusterModel clusterModel;

    private UICommand applyPasswordCommand;

    public MultipleHostsModel() {
        setHosts(new ListModel<>());
        setUseCommonPassword(new EntityModel<>());
        setCommonPassword(new EntityModel<>());
        setApplyPasswordCommand(new UICommand("ApplyPassword", this)); //$NON-NLS-1$
        setConfigureFirewall(true);

        getUseCommonPassword().getEntityChangedEvent().addListener((ev, sender, args) -> {
            getCommonPassword().setIsChangeable(getUseCommonPassword().getEntity());
            getApplyPasswordCommand().setIsExecutionAllowed(getUseCommonPassword().getEntity());
        });
        getUseCommonPassword().setEntity(false);
    }

    public ListModel<EntityModel<HostDetailModel>> getHosts() {
        return hosts;
    }

    public void setHosts(ListModel<EntityModel<HostDetailModel>> hosts) {
        this.hosts = hosts;
    }

    public EntityModel<Boolean> getUseCommonPassword() {
        return useCommonPassword;
    }

    public void setUseCommonPassword(EntityModel<Boolean> useCommonPassword) {
        this.useCommonPassword = useCommonPassword;
    }

    public EntityModel<String> getCommonPassword() {
        return commonPassword;
    }

    public void setCommonPassword(EntityModel<String> commonPassword) {
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
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getApplyPasswordCommand()) {
            applyPassword();
        }
    }

    private void applyPassword() {
        String password = getCommonPassword().getEntity();
        ArrayList<EntityModel<HostDetailModel>> items = new ArrayList<>();
        for (Object object : getHosts().getItems()) {
            HostDetailModel host = (HostDetailModel) ((EntityModel) object).getEntity();
            host.setPassword(password);

            EntityModel<HostDetailModel> entityModel = new EntityModel<>();
            entityModel.setEntity(host);
            items.add(entityModel);
        }
        getHosts().setItems(items);
    }

    public boolean validate() {
        boolean isValid = true;
        setMessage(null);
        Iterable<EntityModel<HostDetailModel>> items = getHosts().getItems();
        for (EntityModel<HostDetailModel> model : items) {
            HostDetailModel host = model.getEntity();
            if (host.getName().trim().length() == 0) {
                setMessage(ConstantsManager.getInstance().getMessages().importClusterHostNameEmpty(host.getAddress()));
                isValid = false;
                break;
            } else if (host.getPassword().trim().length() == 0) {
                setMessage(ConstantsManager.getInstance()
                        .getMessages()
                        .importClusterHostPasswordEmpty(host.getAddress()));
                isValid = false;
                break;
            } else if (host.getSshPublicKey().trim().length() == 0) {
                setMessage(ConstantsManager.getInstance()
                        .getMessages()
                        .importClusterHostSshPublicKeyEmpty(host.getAddress()));
                isValid = false;
                break;
            } else if (!host.getAddress().equals(host.getGlusterPeerAddress())) {
                AsyncQuery<String> pkQuery = new AsyncQuery<>(host::setGlusterPeerAddressSSHPublicKey);
                AsyncDataProvider.getInstance()
                        .getHostSshPublicKey(pkQuery, host.getAddress(), VdsStatic.DEFAULT_SSH_PORT);

                if (!host.getSshPublicKey().equals(host.getGlusterPeerAddressSSHPublicKey())) {
                    setMessage(ConstantsManager.getInstance()
                            .getMessages()
                            .glusterPeerNotMatchingHostSshPublicKey(host.getAddress(),
                                    host.getGlusterPeerAddress()));
                    isValid = false;
                    break;
                }
            }
        }
        return isValid;
    }
    public boolean isConfigureFirewall() {
        return configureFirewall;
    }

    public void setConfigureFirewall(boolean enableFirewall) {
        this.configureFirewall = enableFirewall;
    }
}
