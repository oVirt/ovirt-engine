package org.ovirt.engine.ui.uicommonweb;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.GetVdsGroupByIdParameters;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.common.queries.IsVmWithSameNameExistParameters;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
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

}
