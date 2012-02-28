package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.queries.VdcUserQueryParametersBase;
import org.ovirt.engine.core.compat.Guid;

/**
 * A base class for user queries that receive a User ID as part of their parameters.
 *
 * This class handles the filtering logic (see {@link #shouldReturnValue()}), and leaves the actual querying and data access
 * to its specific subtypes.
 */
public abstract class GetDataByUserIDQueriesBase<P extends VdcUserQueryParametersBase> extends QueriesCommandBase<P> {

    public GetDataByUserIDQueriesBase(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        List<?> result;
        if (!shouldReturnValue()) {
            result = getUnprivilegedQueryReturnValue();
        } else {
            result = getPrivilegedQueryReturnValue();
        }
        getQueryReturnValue().setReturnValue(result);
    }

    /**
     * @return The result of executing the query with the proper privileges, as defined by {@link #shouldReturnValue()}.
     */
    protected abstract List<?> getPrivilegedQueryReturnValue();

    /**
     * @return The result of executing the query without the proper privileges, as defined by {@link #shouldReturnValue()}.
     */
    @SuppressWarnings("rawtypes")
    protected List<?> getUnprivilegedQueryReturnValue() {
        return new ArrayList();
    }

    /**
     * Validates if the query should return anything or not, according to the user's permissions:
     * <ul>
     * <li>If the query is run as an administrator (note that since we've reached the {@link #executeQueryCommand()} method,
     * we've already validated that the use is indeed an administrator), the results from the database queries should be returned.</li>
     * <li>If the query is run as a user, it may return results <b>ONLY</b> if the user is querying about himself.</li>
     * </ul>
     */
    private boolean shouldReturnValue() {
        if (!getParameters().isFiltered()) {
            return true;
        }

        Guid executingUserID = getUserID();
        Guid requestedUserID = getParameters().getUserId();

        return executingUserID != null && requestedUserID != null && executingUserID.equals(requestedUserID);
    }
}

