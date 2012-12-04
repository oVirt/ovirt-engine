package org.ovirt.engine.ui.uicommonweb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.common.queries.GetConfigurationValueParameters;
import org.ovirt.engine.core.common.queries.GetVdsGroupByIdParameters;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.common.queries.IsVmWithSameNameExistParameters;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.KeyValuePairCompat;
import org.ovirt.engine.ui.frontend.Frontend;

/**
 * Contains method for retrieving common data (mostly via frontend).
 *
 *
 * All method returning list of objects must avoid returning a null value, but an empty list.
 */
@SuppressWarnings("unused")
public final class DataProvider
{
    public static VmTemplate GetTemplateByID(Guid templateGUID)
    {
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.GetVmTemplate, new GetVmTemplateParameters(templateGUID));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (VmTemplate) returnValue.getReturnValue();
        }

        return null;
    }

    public static String GetAuthenticationMethod()
    {
        VdcQueryReturnValue returnValue =
                GetConfigFromCache(new GetConfigurationValueParameters(ConfigurationValues.AuthenticationMethod, Config.DefaultConfigurationVersion));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (String) returnValue.getReturnValue();
        }

        return ""; //$NON-NLS-1$
    }

    public static boolean IsLicenseHasDesktops()
    {
        // var dict = GetLicenseProperties();
        // return dict.ContainsKey("ProductTypeProperty") && dict["ProductTypeProperty"].Contains("Desktop");
        return true;
    }

    public static boolean IsVmNameUnique(String name)
    {
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.IsVmWithSameNameExist, new IsVmWithSameNameExistParameters(name));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return !(Boolean) returnValue.getReturnValue();
        }

        return true;
    }

    public static ArrayList<DbUser> GetUserList()
    {
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.Search, new SearchParameters("User:", SearchType.DBUser)); //$NON-NLS-1$

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return Linq.<DbUser> Cast((ArrayList<IVdcQueryable>) returnValue.getReturnValue());
        }

        return new ArrayList<DbUser>();
    }

    public static VDSGroup GetClusterById(Guid id)
    {
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.GetVdsGroupById, new GetVdsGroupByIdParameters(id));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (VDSGroup) returnValue.getReturnValue();
        }

        return null;
    }

    public static int GetMaxVmPriority()
    {
        VdcQueryReturnValue returnValue =
                GetConfigFromCache(new GetConfigurationValueParameters(ConfigurationValues.VmPriorityMaxValue, Config.DefaultConfigurationVersion));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (Integer) returnValue.getReturnValue();
        }

        return 100;
    }

    public static int RoundPriority(int priority)
    {
        int max = GetMaxVmPriority();
        int medium = max / 2;

        int[] levels = new int[] { 1, medium, max };

        for (int i = 0; i < levels.length; i++)
        {
            int lengthToLess = levels[i] - priority;
            int lengthToMore = levels[i + 1] - priority;

            if (lengthToMore < 0)
            {
                continue;
            }

            return Math.abs(lengthToLess) < lengthToMore ? levels[i] : levels[i + 1];
        }

        return 0;
    }

    // dictionary to hold cache of all config values (per version) queried by client, if the request for them succeeded.
    private static HashMap<Map.Entry<ConfigurationValues, String>, VdcQueryReturnValue> CachedConfigValues =
            new HashMap<Map.Entry<ConfigurationValues, String>, VdcQueryReturnValue>();

    // helper method to clear the config cache (currently used on each login)
    public static void ClearConfigCache()
    {
        if (CachedConfigValues != null)
        {
            CachedConfigValues.clear();
        }

    }

    // method to get an item from config while caching it (config is not supposed to change during a session)
    public static VdcQueryReturnValue GetConfigFromCache(GetConfigurationValueParameters parameters)
    {
        Map.Entry<ConfigurationValues, String> config_key =
                new KeyValuePairCompat<ConfigurationValues, String>(parameters.getConfigValue(),
                        parameters.getVersion());

        // populate cache if not in cache already
        if (!CachedConfigValues.containsKey(config_key))
        {

            VdcQueryReturnValue returnValue = Frontend.RunQuery(VdcQueryType.GetConfigurationValue, parameters);

            // only put result in cache if query succeeded
            if (returnValue != null && returnValue.getSucceeded())
            {
                CachedConfigValues.put(config_key, returnValue);
            }
            // return actual return value on error
            else
            {
                return returnValue;
            }
        }
        // return value from cache (either it was in, or the query succeeded, and it is now in the cache
        return CachedConfigValues.get(config_key);
    }
}
