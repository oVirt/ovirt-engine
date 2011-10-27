package org.ovirt.engine.ui.uicommon.models.templates;
import java.util.Collections;
import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.ui.uicompat.*;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.common.vdscommands.*;
import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.common.action.*;
import org.ovirt.engine.ui.frontend.*;
import org.ovirt.engine.ui.uicommon.*;
import org.ovirt.engine.ui.uicommon.models.*;
import org.ovirt.engine.core.common.*;

import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.ui.uicommon.*;
import org.ovirt.engine.ui.uicommon.models.*;

@SuppressWarnings("unused")
public class TemplateGeneralModel extends EntityModel
{


	public VmTemplate getEntity()
	{
		if(super.getEntity() == null)
		{
			return null;
		}
		if (super.getEntity() instanceof VmTemplate)
		{
			return (VmTemplate)super.getEntity();
		}
		else
		{
			java.util.Map.Entry<VmTemplate, java.util.ArrayList<DiskImage>> pair = (java.util.Map.Entry<VmTemplate, java.util.ArrayList<DiskImage>>)super.getEntity();
			return pair.getKey();
		}
	}
	public void setEntity(VmTemplate value)
	{
		super.setEntity(value);
	}



	private String cpuInfo;
	public String getCpuInfo()
	{
		return cpuInfo;
	}
	public void setCpuInfo(String value)
	{
		if (!StringHelper.stringsEqual(cpuInfo, value))
		{
			cpuInfo = value;
			OnPropertyChanged(new PropertyChangedEventArgs("CpuInfo"));
		}
	}

	private boolean hasTimeZone;
	public boolean getHasTimeZone()
	{
		return hasTimeZone;
	}
	public void setHasTimeZone(boolean value)
	{
		if (hasTimeZone != value)
		{
			hasTimeZone = value;
			OnPropertyChanged(new PropertyChangedEventArgs("HasTimeZone"));
		}
	}

	private boolean hasDomain;
	public boolean getHasDomain()
	{
		return hasDomain;
	}
	public void setHasDomain(boolean value)
	{
		if (hasDomain != value)
		{
			hasDomain = value;
			OnPropertyChanged(new PropertyChangedEventArgs("HasDomain"));
		}
	}


	public TemplateGeneralModel()
	{
		setTitle("General");
	}

	@Override
	protected void OnEntityChanged()
	{
		super.OnEntityChanged();

		if (super.getEntity() != null)
		{
			UpdateProperties();
		}
	}

	@Override
	protected void EntityPropertyChanged(Object sender, PropertyChangedEventArgs e)
	{
		super.EntityPropertyChanged(sender, e);

		if (e.PropertyName.equals("num_of_cpus"))
		{
			UpdateProperties();
		}
	}

	private void UpdateProperties()
	{
		VmTemplate template = getEntity();

		setCpuInfo(StringFormat.format("%1$s (%2$s Socket(s), %3$s Core(s) per Socket)", template.getnum_of_cpus(), template.getnum_of_sockets(), template.getcpu_per_socket()));

		setHasTimeZone(DataProvider.IsWindowsOsType(template.getos()));
		setHasDomain(DataProvider.IsWindowsOsType(template.getos()));
	}
}