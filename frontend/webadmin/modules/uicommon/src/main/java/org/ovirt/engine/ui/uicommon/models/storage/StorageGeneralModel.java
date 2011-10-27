package org.ovirt.engine.ui.uicommon.models.storage;
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
public class StorageGeneralModel extends EntityModel
{

	private boolean isNfs;
	public boolean getIsNfs()
	{
		return isNfs;
	}
	public void setIsNfs(boolean value)
	{
		if (isNfs != value)
		{
			isNfs = value;
			OnPropertyChanged(new PropertyChangedEventArgs("IsNfs"));
		}
	}

	private boolean isLocalS;
	public boolean getIsLocalS()
	{
		return isLocalS;
	}
	public void setIsLocalS(boolean value)
	{
		if (isLocalS != value)
		{
			isLocalS = value;
			OnPropertyChanged(new PropertyChangedEventArgs("IsLocalS"));
		}
	}

	private String nfsPath;
	public String getNfsPath()
	{
		return nfsPath;
	}
	public void setNfsPath(String value)
	{
		if (!StringHelper.stringsEqual(nfsPath, value))
		{
			nfsPath = value;
			OnPropertyChanged(new PropertyChangedEventArgs("NfsPath"));
		}
	}

	private String localPath;
	public String getLocalPath()
	{
		return localPath;
	}
	public void setLocalPath(String value)
	{
		if (!StringHelper.stringsEqual(localPath, value))
		{
			localPath = value;
			OnPropertyChanged(new PropertyChangedEventArgs("LocalPath"));
		}
	}



	public StorageGeneralModel()
	{
		setTitle("General");
	}

	@Override
	protected void OnEntityChanged()
	{
		super.OnEntityChanged();

		if (getEntity() != null)
		{
			storage_domains storageDomain = (storage_domains)getEntity();

			setIsNfs(storageDomain.getstorage_type() == StorageType.NFS);
			setIsLocalS(storageDomain.getstorage_type() == StorageType.LOCALFS);
			if (getIsNfs() || getIsLocalS())
			{
				storage_server_connections connection = DataProvider.GetStorageConnectionById(storageDomain.getstorage());
				String path = null;
				if(connection != null)
				{
					path = connection.getconnection();
				}
				else
				{
					nfsPath = null;
					setLocalPath(null);
				}
				if(getIsNfs())
				{
					setNfsPath(path);
				}
				else
				{
					setLocalPath(path);
				}
			}
			else
			{
				setNfsPath(null);
				setLocalPath(null);
			}

		}
	}
}