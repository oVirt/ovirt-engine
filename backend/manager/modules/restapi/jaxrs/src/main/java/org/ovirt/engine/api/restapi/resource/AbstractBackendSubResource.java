package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.common.util.MutabilityAssertor;
import org.ovirt.engine.api.model.BaseResource;

import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.api.restapi.logging.Messages;

public abstract class AbstractBackendSubResource<R extends BaseResource, Q /* extends IVdcQueryable */> extends
        AbstractBackendResource<R, Q> {

    protected static final String[] STRICTLY_IMMUTABLE = { "id" };

    protected String id;
    protected Guid guid;

    protected AbstractBackendSubResource(String id, Class<R> modelType, Class<Q> entityType, String... subCollections) {
        super(modelType, entityType, subCollections);
        this.id = id;
        this.guid = asGuidOr404(id);
    }

    protected R performGet(VdcQueryType query, VdcQueryParametersBase params) {
        return performGet(query, params, null);
    }

    protected R performGet(VdcQueryType query, VdcQueryParametersBase params, Class<? extends BaseResource> suggestedParentType) {
        Q entity = getEntity(entityType, query, params, id, true);
        return addLinks(populate(map(entity, null), entity), suggestedParentType);
    }

    protected <T> Q getEntity(EntityIdResolver<T> entityResolver, boolean notFoundAs404) {
        try {
            return entityResolver.resolve((T) guid);
        } catch (Exception e) {
            return handleError(entityType, e, notFoundAs404);
        }
    }

    protected R performUpdate(R incoming,
                              Q entity,
                              R model,
                              EntityIdResolver<Guid> entityResolver,
                              VdcActionType update,
                              ParametersProvider<R, Q> updateProvider) {

        validateUpdate(incoming, model);

        performAction(update, updateProvider.getParameters(incoming, entity));

        return addLinks(populate(map(getEntity(entityResolver, false)), entity));
    }

    protected R performUpdate(R incoming,
            EntityIdResolver<Guid> entityResolver,
            VdcActionType update,
            ParametersProvider<R, Q> updateProvider) {
        // REVISIT maintain isolation across retrievals and update
        Q entity = getEntity(entityResolver, true);

        validateUpdate(incoming, map(entity));

        performAction(update, updateProvider.getParameters(incoming, entity));

        return addLinks(populate(map(getEntity(entityResolver, false)), entity));
    }
    /**
     * Validate update from an immutability point of view.
     *
     * @param incoming
     *            the incoming resource representation
     * @param existing
     *            the existing resource representation
     * @throws WebApplicationException
     *             wrapping an appropriate response iff an immutability
     *             constraint has been broken
     */
    protected void validateUpdate(R incoming, R existing) {
        String reason = localize(Messages.BROKEN_CONSTRAINT_REASON);
        String detail = localize(Messages.BROKEN_CONSTRAINT_DETAIL_TEMPLATE);
        Response error = MutabilityAssertor.imposeConstraints(getStrictlyImmutable(), incoming, existing, reason, detail);
        if (error != null) {
            throw new WebApplicationException(error);
        }
    }

    /**
     * Override this method if any additional resource-specific fields are
     * strictly immutable
     *
     * @return array of strict immutable field names
     */
    protected String[] getStrictlyImmutable() {
        return STRICTLY_IMMUTABLE;
    }

    protected interface ParametersProvider<R, Q> {
        VdcActionParametersBase getParameters(R model, Q entity);
    }
}
