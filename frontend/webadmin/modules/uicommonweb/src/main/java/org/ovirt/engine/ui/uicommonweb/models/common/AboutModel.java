package org.ovirt.engine.ui.uicommonweb.models.common;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicompat.Clipboard;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

@SuppressWarnings("unused")
public class AboutModel extends Model
{

    private List<HostInfo> hosts;

    public List<HostInfo> getHosts()
    {
        return hosts;
    }

    public void setHosts(List<HostInfo> value)
    {
        if (hosts != value)
        {
            hosts = value;
            OnPropertyChanged(new PropertyChangedEventArgs("Hosts")); //$NON-NLS-1$
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
            OnPropertyChanged(new PropertyChangedEventArgs("ShowOnlyVersion")); //$NON-NLS-1$
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
            OnPropertyChanged(new PropertyChangedEventArgs("ProductVersion")); //$NON-NLS-1$
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
        // QLogger.getInstance().ErrorFormat("AboutModel(AboutView view): cannot convert {0} cause bugous license expire date received",
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

        UICommand command = new UICommand("CopyToClipboard", this); //$NON-NLS-1$
        command.setTitle(ConstantsManager.getInstance().getConstants().copytoClipboardTitle());
        command.setIsAvailable(true);
        setCopyToClipboardCommand(command);
        getCommands().add(getCopyToClipboardCommand());

        setShowOnlyVersion(true);

        AsyncDataProvider.GetRpmVersionViaPublic(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object returnValue) {

                AboutModel aboutModel = (AboutModel) model;
                aboutModel.setProductVersion((String) returnValue);
            }
        }));
    }

    private void ShowOnlyVersionChanged() {

        if (!getShowOnlyVersion()) {

            AsyncDataProvider.GetHostList(new AsyncQuery(this, new INewAsyncCallback() {
                @Override
                public void OnSuccess(Object model, Object returnValue) {

                    AboutModel aboutModel = (AboutModel) model;
                    ArrayList<HostInfo> list = new ArrayList<HostInfo>();

                    for (VDS a : (List<VDS>) returnValue) {

                        HostInfo item = new HostInfo();
                        item.setHostName(a.getVdsName() + ":"); //$NON-NLS-1$

                        if (!StringHelper.isNullOrEmpty(a.getHostOs())) {
                            item.setOSVersion(ConstantsManager.getInstance().getConstants().osVersionAbout()
                                + " " + a.getHostOs()); //$NON-NLS-1$
                        }

                        if (a.getVersion() != null) {
                            item.setVDSMVersion(ConstantsManager.getInstance().getConstants().VDSMVersionAbout() + " " //$NON-NLS-1$
                                + a.getVersion().getRpmName() + " " //$NON-NLS-1$
                                + a.getVersion());
                        }

                        list.add(item);
                    }
                    aboutModel.setHosts(list);
                }
            }));
        }
    }

    public void CopyToClipboard() {
        String data = BuildClipboardData();
        CopyToClipboard(data);
    }

    private String BuildClipboardData()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(getRpmVersion());
        if (getShowOnlyVersion() == false)
        {
            sb.append("\n"); //$NON-NLS-1$
            sb.append(ConstantsManager.getInstance()
                    .getConstants()
                    .oVirtEnterpriseVirtualizationEngineHypervisorHostsAbout());

            if (getHosts() != null && getHosts().size() > 0)
            {
                for (HostInfo item : getHosts())
                {
                    sb.append("\t" + item.getHostName() //$NON-NLS-1$
                            + "\t" + item.getOSVersion() //$NON-NLS-1$
                            + "\t" + item.getVDSMVersion()); //$NON-NLS-1$
                    sb.append("\n"); //$NON-NLS-1$
                }
            }
            else
            {
                sb.append(ConstantsManager.getInstance().getConstants().noHostsAbout());
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
        return ConstantsManager.getInstance().getConstants().oVirtEngineForServersAndDesktopsAbout()
                + " " + getProductVersion(); //$NON-NLS-1$
    }
}
