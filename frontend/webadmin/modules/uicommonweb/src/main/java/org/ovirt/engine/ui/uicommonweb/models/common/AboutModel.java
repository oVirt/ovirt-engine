package org.ovirt.engine.ui.uicommonweb.models.common;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.StringFormat;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Extensions;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicompat.Clipboard;

@SuppressWarnings("unused")
public class AboutModel extends Model
{

    private java.util.List<HostInfo> hosts;

    public java.util.List<HostInfo> getHosts()
    {
        return hosts;
    }

    public void setHosts(java.util.List<HostInfo> value)
    {
        if (hosts != value)
        {
            hosts = value;
            OnPropertyChanged(new PropertyChangedEventArgs("Hosts"));
        }
    }

    private boolean showOnlyVersion;

    public boolean getShowOnlyVersion()
    {
        return showOnlyVersion;
    }

    public void setShowOnlyVersion(boolean value)
    {
        if (showOnlyVersion != value)
        {
            showOnlyVersion = value;
            ShowOnlyVersionChanged();
            OnPropertyChanged(new PropertyChangedEventArgs("ShowOnlyVersion"));
        }
    }

    private String productVersion;

    public String getProductVersion()
    {
        return productVersion;
    }

    public void setProductVersion(String value)
    {
        if (!StringHelper.stringsEqual(productVersion, value))
        {
            productVersion = value;
            OnPropertyChanged(new PropertyChangedEventArgs("ProductVersion"));
        }
    }

    // public string Enterprise { get; private set; }

    // public string Description { get; private set; }

    // public string CustomerId { get; private set; }

    // public string Limitations { get; private set; }

    // public string TimeLimit { get; private set; }

    // public string CPUSockets { get; private set; }

    private UICommand privateCopyToClipboardCommand;

    public UICommand getCopyToClipboardCommand()
    {
        return privateCopyToClipboardCommand;
    }

    public void setCopyToClipboardCommand(UICommand value)
    {
        privateCopyToClipboardCommand = value;
    }

    public AboutModel()
    {

        // var licenseProperties = DataProvider.GetLicenseProperties();
        // Enterprise = licenseProperties.ContainsKey("EnterpriseProperty") ? licenseProperties["EnterpriseProperty"] :
        // string.Empty;
        // Description = licenseProperties.ContainsKey("DescriptionProperty") ? licenseProperties["DescriptionProperty"]
        // : string.Empty;
        // CustomerId = licenseProperties.ContainsKey("CustomerIdProperty") ? licenseProperties["CustomerIdProperty"] :
        // string.Empty;

        // //Build limitations text.
        // int allowedConcurrentDesktops =
        // licenseProperties.ContainsKey("MaxConcurrentlyRunningDesktopsProperty") &&
        // !string.IsNullOrEmpty(licenseProperties["MaxConcurrentlyRunningDesktopsProperty"]) ?
        // Convert.ToInt32(licenseProperties["MaxConcurrentlyRunningDesktopsProperty"]) :
        // 0;

        // var returnValue = Frontend.RunQuery(VdcQueryType.GetResourceUsage,
        // new GetResourceUsageParameters("MaxConcurrentlyRunningDesktops"));

        // int currentConcurrentDesktops = (returnValue != null && returnValue.Succeeded)
        // ? (Integer)returnValue.ReturnValue
        // : 0;

        // if (allowedConcurrentDesktops > 0)
        // {
        // Limitations = StringFormat.format("There are currently {0} running Virtual Machines out of possible {1}.",
        // currentConcurrentDesktops,
        // allowedConcurrentDesktops
        // );
        // }

        // //Build time limit text.
        // bool isProduct = licenseProperties.ContainsKey("IsProductProperty") &&
        // !string.IsNullOrEmpty(licenseProperties["IsProductProperty"])
        // ? Convert.ToBoolean(licenseProperties["IsProductProperty"])
        // : false;

        // if (licenseProperties.ContainsKey("TimeLimitProperty") &&
        // !string.IsNullOrEmpty(licenseProperties["TimeLimitProperty"]))
        // {

        // DateTime timeLimit = DateTime.Now;
        // if (licenseProperties["TimeLimitProperty"].Equals("0"))
        // {
        // TimeLimit = "Product Support period is unlimited.";
        // }
        // else
        // {
        // try
        // {
        // timeLimit = Convert.ToDateTime(licenseProperties["TimeLimitProperty"]);
        // }
        // catch {
        // QLogger.getInstance().ErrorFormat("AboutModel(AboutView view): cannot convert {0} cause bugous license expire date recieved",
        // licenseProperties["TimeLimitProperty"]); }
        // if (isProduct)
        // {
        // bool isSupported = licenseProperties.ContainsKey("IsSupportedProperty")
        // ? Convert.ToBoolean(licenseProperties["IsSupportedProperty"])
        // : false;

        // TimeLimit = isSupported
        // ? StringFormat.format("Product Support period will expire at {0}.", timeLimit)
        // : StringFormat.format("Product Support period has expired at {0}.", timeLimit);
        // }
        // else
        // {
        // TimeLimit = StringFormat.format("This evaluation version will expire at {0}.", timeLimit);
        // }
        // }
        // }

        // if (licenseProperties.ContainsKey("MaxHostSocketsProperty") &&
        // !string.IsNullOrEmpty(licenseProperties["MaxHostSocketsProperty"]))
        // {
        // VdcQueryReturnValue ret = Frontend.RunQuery(VdcQueryType.GetResourceUsage, new
        // GetResourceUsageParameters("MaxHostSockets"));
        // if (ret != null && ret.Succeeded)
        // {
        // if (string.IsNullOrEmpty(licenseProperties["MaxHostSocketsProperty"]) ||
        // int.Parse(licenseProperties["MaxHostSocketsProperty"]) == 0)
        // {
        // CPUSockets = StringFormat.format("{0}(unlimited)", (int)ret.ReturnValue);
        // }
        // else
        // {
        // CPUSockets = StringFormat.format("{0}(out of {1} supported by license)", (int)ret.ReturnValue,
        // licenseProperties["MaxHostSocketsProperty"]);
        // }
        // }
        // }

        UICommand tempVar = new UICommand("CopyToClipboard", this);
        tempVar.setTitle("Copy to Clipboard");
        tempVar.setIsAvailable(true);
        setCopyToClipboardCommand(tempVar);
        this.getCommands().add(getCopyToClipboardCommand());

        setShowOnlyVersion(true);
        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object result)
            {
                AboutModel aboutModel = (AboutModel) model;
                aboutModel.setProductVersion((String) result);
            }
        };
        AsyncDataProvider.GetRpmVersionViaPublic(_asyncQuery);
    }

    private void ShowOnlyVersionChanged()
    {
        if (!getShowOnlyVersion())
        {
            AsyncQuery _asyncQuery = new AsyncQuery();
            _asyncQuery.setModel(this);
            _asyncQuery.asyncCallback = new INewAsyncCallback() {
                @Override
                public void OnSuccess(Object model, Object result)
                {
                    AboutModel aboutModel = (AboutModel) model;
                    java.util.ArrayList<HostInfo> list = new java.util.ArrayList<HostInfo>();
                    for (VDS a : (java.util.List<VDS>) result)
                    {
                        HostInfo tempVar = new HostInfo();
                        tempVar.setHostName(a.getvds_name() + ":");
                        HostInfo hi = tempVar;
                        if (!StringHelper.isNullOrEmpty(a.gethost_os()))
                        {
                            hi.setOSVersion("OS Version - " + a.gethost_os());
                        }

                        if (a.getVersion().getFullVersion() != null)
                        {
                            hi.setVDSMVersion("VDSM Version - "
                                    + Extensions.GetFriendlyVersion(a.getVersion().getFullVersion()) + " "
                                    + a.getVersion().getBuildName());
                        }

                        list.add(hi);
                    }
                    aboutModel.setHosts(list);
                }
            };
            AsyncDataProvider.GetHostList(_asyncQuery);
        }
    }

    public void CopyToClipboard()
    {
        String data = BuildClipboardData();
        CopyToClipboard(data);
    }

    private String BuildClipboardData()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(getRpmVersion());
        if (getShowOnlyVersion() == false)
        {
            sb.append("\n");
            sb.append("oVirt Enterprise Virtualization Engine Hypervisor Hosts:");

            if (getHosts() != null && getHosts().size() > 0)
            {
                for (HostInfo item : getHosts())
                {
                    sb.append(StringFormat.format("\t%1$s\t%2$s\t%3$s",
                            item.getHostName(),
                            item.getOSVersion(),
                            item.getVDSMVersion()));
                    sb.append("\n");
                }
            }
            else
            {
                sb.append("[No Hosts]");
            }

            // sb.append();
            // sb.append("License Information:");
            // sb.Append("\tEnterprise:\t").append(Enterprise);
            // sb.Append("\tDescription:\t").append(Description);
            // sb.Append("\tLicense ID:\t").append(CustomerId);
            // sb.Append("\tUsed CPU Sockets:\t").append(CPUSockets);
            // if (Limitations != null)
            // {
            // sb.Append("\tLimitations:\t").append(Limitations);
            // }
            // sb.append();
            // sb.append(TimeLimit);

        }
        return sb.toString();
    }

    protected void CopyToClipboard(String data)
    {
        Clipboard.SetDataObject(data);
    }

    @Override
    public void ExecuteCommand(UICommand command)
    {
        super.ExecuteCommand(command);

        if (command == getCopyToClipboardCommand())
        {
            CopyToClipboard();
        }
    }

    private String getRpmVersion()
    {
        return "oVirt Engine for Servers and Desktops: " + getProductVersion();
    }
}
