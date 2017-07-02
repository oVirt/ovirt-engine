package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.common.util.MutabilityAssertor;
import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.restapi.logging.Messages;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public abstract class AbstractBackendSubResource<R extends BaseResource, Q /* extends Queryable */> extends
        AbstractBackendResource<R, Q> {

    private static final String[] STRICTLY_IMMUTABLE = { "id" };

    protected String id;
    protected Guid guid;

    protected AbstractBackendSubResource(String id, Class<R> modelType, Class<Q> entityType) {
        super(modelType, entityType);
        this.id = id;
        this.guid = asGuidOr404(id);
    }

    protected R performGet(QueryType query, QueryParametersBase params) {
        return performGet(query, params, null);
    }

    protected R performGet(QueryType query, QueryParametersBase params, Class<? extends BaseResource> suggestedParentType) {
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

    protected final <T> R performUpdate(R incoming,
                              Q entity,
                              R model,
                              EntityIdResolver<T> entityResolver,
                              ActionType update,
                              ParametersProvider<R, Q> updateProvider) {

        entity = doUpdate(incoming, entity, model, entityResolver, update, updateProvider);
        R model2 = map(entity);
        deprecatedPopulate(model2, entity);
        return addLinks(doPopulate(model2, entity));
    }

    protected <T> Q doUpdate(R incoming,
            Q entity,
            R model,
            EntityIdResolver<T> entityResolver,
            ActionType update,
            ParametersProvider<R, Q> updateProvider) {
        validateUpdate(incoming, model);

        performAction(update, updateProvider.getParameters(incoming, entity));

        entity = getEntity(entityResolver, false);
        return entity;
    }

    protected <T> R performUpdate(R incoming,
            EntityIdResolver<T> entityResolver,
            ActionType update,
            ParametersProvider<R, Q> updateProvider) {
        Q entity = getEntity(entityResolver, true);

        validateUpdate(incoming, map(entity));
        // REVISIT maintain isolation across retrievals and update
        entity = doUpdate(incoming, entity, entityResolver, update, updateProvider);
        R model = map(entity);
        deprecatedPopulate(model, entity);
        return addLinks(doPopulate(model, entity));
    }

    protected <T> Q doUpdate(R incoming,
            Q entity,
            EntityIdResolver<T> entityResolver,
            ActionType update,
            ParametersProvider<R, Q> updateProvider) {
        performAction(update, updateProvider.getParameters(incoming, entity));

        entity = getEntity(entityResolver, false);
        return entity;
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
        ActionParametersBase getParameters(R model, Q entity);
    }
}
