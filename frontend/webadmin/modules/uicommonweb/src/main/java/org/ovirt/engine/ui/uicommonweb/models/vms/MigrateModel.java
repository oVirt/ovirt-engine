package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

@SuppressWarnings("unused")
public class MigrateModel extends Model
{

    private ListModel privateHosts;

    public ListModel getHosts()
    {
        return privateHosts;
    }

    private void setHosts(ListModel value)
    {
        privateHosts = value;
    }

    private ArrayList<VM> privateVmList;

    public ArrayList<VM> getVmList()
    {
        return privateVmList;
    }

    public void setVmList(ArrayList<VM> value)
    {
        privateVmList = value;
    }

    private boolean privateVmsOnSameCluster;

    public boolean getVmsOnSameCluster()
    {
        return privateVmsOnSameCluster;
    }

    public void setVmsOnSameCluster(boolean value)
    {
        privateVmsOnSameCluster = value;
    }

    private boolean isAutoSelect;

    public boolean getIsAutoSelect()
    {
        return isAutoSelect;
    }

    public void setIsAutoSelect(boolean value)
    {
        if (isAutoSelect != value)
        {
            isAutoSelect = value;
            getHosts().setIsChangable(!isAutoSelect);
            onPropertyChanged(new PropertyChangedEventArgs("IsAutoSelect")); //$NON-NLS-1$
            setIsSameVdsMessageVisible(!value);
            privateSelectHostAutomatically_IsSelected.setEntity(value);
            privateSelectDestinationHost_IsSelected.setEntity(!value);
            privateHosts.setIsChangable(!value);
        }
    }

    private boolean isHostSelAvailable;

    public boolean getIsHostSelAvailable()
    {
        return isHostSelAvailable;
    }

    public void setIsHostSelAvailable(boolean value)
    {
        if (isHostSelAvailable != value)
        {
            isHostSelAvailable = value;
            onPropertyChanged(new PropertyChangedEventArgs("IsHostSelAvailable")); //$NON-NLS-1$
        }
    }

    private boolean noSelAvailable;

    public boolean getNoSelAvailable()
    {
        return noSelAvailable;
    }

    public void setNoSelAvailable(boolean value)
    {
        if (noSelAvailable != value)
        {
            noSelAvailable = value;
            onPropertyChanged(new PropertyChangedEventArgs("NoSelAvailable")); //$NON-NLS-1$
        }
    }

    private boolean isSameVdsMessageVisible;

    public boolean getIsSameVdsMessageVisible()
    {
        return isSameVdsMessageVisible;
    }

    public void setIsSameVdsMessageVisible(boolean value)
    {
        isSameVdsMessageVisible = value && gethasSameVdsMessage() && !getIsAutoSelect();
        onPropertyChanged(new PropertyChangedEventArgs("IsSameVdsMessageVisible")); //$NON-NLS-1$
    }

    // onPropertyChanged(new PropertyChangedEventArgs("IsSameVdsMessageVisible"));
    private boolean privatehasSameVdsMessage;

    public boolean gethasSameVdsMessage()
    {
        return privatehasSameVdsMessage;
    }

    public void sethasSameVdsMessage(boolean value)
    {
        privatehasSameVdsMessage = value;
    }

    private EntityModel privateSelectHostAutomatically_IsSelected;

    public EntityModel getSelectHostAutomatically_IsSelected()
    {
        return privateSelectHostAutomatically_IsSelected;
    }

    public void setSelectHostAutomatically_IsSelected(EntityModel value)
    {
        privateSelectHostAutomatically_IsSelected = value;
    }

    private EntityModel privateSelectDestinationHost_IsSelected;

    public EntityModel getSelectDestinationHost_IsSelected()
    {
        return privateSelectDestinationHost_IsSelected;
    }

    public void setSelectDestinationHost_IsSelected(EntityModel value)
    {
        privateSelectDestinationHost_IsSelected = value;
    }

    public MigrateModel()
    {
        setHosts(new ListModel());
        getHosts().getSelectedItemChangedEvent().addListener(this);

        setSelectHostAutomatically_IsSelected(new EntityModel());
        getSelectHostAutomatically_IsSelected().getEntityChangedEvent().addListener(this);

        setSelectDestinationHost_IsSelected(new EntityModel());
        getSelectDestinationHost_IsSelected().getEntityChangedEvent().addListener(this);
    }

    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args)
    {
        super.eventRaised(ev, sender, args);
        if (sender == getHosts() && getVmsOnSameCluster())
        {
            VDS selectedHost = (VDS) getHosts().getSelectedItem();
            sethasSameVdsMessage(false);
            for (VM vm : getVmList())
            {
                if (selectedHost.getId().equals(vm.getRunOnVds()))
                {
                    sethasSameVdsMessage(true);
                    break;
                }
            }
            setIsSameVdsMessageVisible(gethasSameVdsMessage());
        }
        else if (ev.matchesDefinition(EntityModel.EntityChangedEventDefinition))
        {
            if (sender == getSelectHostAutomatically_IsSelected())
            {
                setIsAutoSelect((Boolean) getSelectHostAutomatically_IsSelected().getEntity());
            }
            else if (sender == getSelectDestinationHost_IsSelected())
            {
                setIsAutoSelect(!(Boolean) getSelectDestinationHost_IsSelected().getEntity());
            }
        }
    }
}
