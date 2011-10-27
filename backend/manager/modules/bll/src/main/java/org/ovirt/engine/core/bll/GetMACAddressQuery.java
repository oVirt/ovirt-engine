package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.*;

/**
 * Gets the MAC address in which the SolideICE service is running
 */
public class GetMACAddressQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {
    public GetMACAddressQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        // ManagementClass mc = new
        // ManagementClass("Win32_NetworkAdapterConfiguration");
        // ManagementObjectCollection moc = mc.GetInstances();
        // java.util.ArrayList<String> MACAddressList = new
        // java.util.ArrayList<String>();
        // for (ManagementObject mo : moc)
        // {
        // if (mo != null)
        // {
        // // return all MAC Addresses of all cards
        // if ((Boolean)mo.get("IPEnabled"))
        // {
        // MACAddressList.add(mo.get("MacAddress").toString());
        // }
        //
        // mo.Dispose();
        // }
        // }
        // getQueryReturnValue().setReturnValue(MACAddressList);
    }
}
