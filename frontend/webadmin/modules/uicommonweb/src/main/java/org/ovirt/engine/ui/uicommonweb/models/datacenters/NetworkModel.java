package org.ovirt.engine.ui.uicommonweb.models.datacenters;

import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IntegerValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IpAddressValidation;
import org.ovirt.engine.ui.uicommonweb.validation.LengthValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicommonweb.validation.RegexValidation;

@SuppressWarnings("unused")
public class NetworkModel extends Model
{

    private boolean privateIsNew;

    public boolean getIsNew()
    {
        return privateIsNew;
    }

    public void setIsNew(boolean value)
    {
        privateIsNew = value;
    }

    private EntityModel privateName;

    public EntityModel getName()
    {
        return privateName;
    }

    private void setName(EntityModel value)
    {
        privateName = value;
    }

    private EntityModel privateAddress;

    public EntityModel getAddress()
    {
        return privateAddress;
    }

    private void setAddress(EntityModel value)
    {
        privateAddress = value;
    }

    private EntityModel privateSubnet;

    public EntityModel getSubnet()
    {
        return privateSubnet;
    }

    private void setSubnet(EntityModel value)
    {
        privateSubnet = value;
    }

    private EntityModel privateGateway;

    public EntityModel getGateway()
    {
        return privateGateway;
    }

    private void setGateway(EntityModel value)
    {
        privateGateway = value;
    }

    private EntityModel privateDescription;

    public EntityModel getDescription()
    {
        return privateDescription;
    }

    private void setDescription(EntityModel value)
    {
        privateDescription = value;
    }

    private EntityModel privateVLanTag;

    public EntityModel getVLanTag()
    {
        return privateVLanTag;
    }

    private void setVLanTag(EntityModel value)
    {
        privateVLanTag = value;
    }

    private EntityModel privateIsStpEnabled;

    public EntityModel getIsStpEnabled()
    {
        return privateIsStpEnabled;
    }

    private void setIsStpEnabled(EntityModel value)
    {
        privateIsStpEnabled = value;
    }

    private EntityModel privateHasVLanTag;

    public EntityModel getHasVLanTag()
    {
        return privateHasVLanTag;
    }

    private void setHasVLanTag(EntityModel value)
    {
        privateHasVLanTag = value;
    }

    public NetworkModel()
    {
        setName(new EntityModel());
        setAddress(new EntityModel());
        setSubnet(new EntityModel());
        setGateway(new EntityModel());
        setDescription(new EntityModel());
        setVLanTag(new EntityModel());
        EntityModel tempVar = new EntityModel();
        tempVar.setEntity(false);
        setIsStpEnabled(tempVar);
        EntityModel tempVar2 = new EntityModel();
        tempVar2.setEntity(false);
        setHasVLanTag(tempVar2);
    }

    private void HasVLanTagChanged()
    {
        if (!(Boolean) getHasVLanTag().getEntity())
        {
            getVLanTag().setIsValid(true);
        }
    }

    public boolean Validate()
    {
        RegexValidation tempVar = new RegexValidation();
        tempVar.setExpression("^[A-Za-z0-9_]{1,15}$");
        tempVar.setMessage("Name must contain alphanumeric characters or '_' (maximum length 15 characters).");
        RegexValidation tempVar2 = new RegexValidation();
        tempVar2.setIsNegate(true);
        tempVar2.setExpression("^(bond)");
        tempVar2.setMessage("Network name shouldn't start with 'bond'.");
        getName().ValidateEntity(new IValidation[] { new NotEmptyValidation(), tempVar, tempVar2 });

        getAddress().ValidateEntity(new IValidation[] { new IpAddressValidation() });

        getSubnet().ValidateEntity(new IValidation[] { new IpAddressValidation() });

        getGateway().ValidateEntity(new IValidation[] { new IpAddressValidation() });

        LengthValidation tempVar3 = new LengthValidation();
        tempVar3.setMaxLength(40);
        getDescription().ValidateEntity(new IValidation[] { tempVar3 });

        getVLanTag().setIsValid(true);
        if ((Boolean) getHasVLanTag().getEntity())
        {
            IntegerValidation tempVar4 = new IntegerValidation();
            tempVar4.setMinimum(0);
            tempVar4.setMaximum(4095);
            getVLanTag().ValidateEntity(new IValidation[] { new NotEmptyValidation(), tempVar4 });
        }

        return getName().getIsValid() && getAddress().getIsValid() && getSubnet().getIsValid()
                && getGateway().getIsValid() && getVLanTag().getIsValid() && getDescription().getIsValid();
    }
}
