package org.ovirt.engine.core.bll.scheduling.queries;

import java.util.Collections;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.dao.scheduling.AffinityGroupDao;

public class GetAffinityGroupByIdQuery extends AffinityGroupsQueryBase<IdQueryParameters> {

    @Inject
    private AffinityGroupDao affinityGroupDao;

    public GetAffinityGroupByIdQuery(IdQueryParameters parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        AffinityGroup group = affinityGroupDao.get(getParameters().getId());
        checkBrokenGroups(Collections.singletonList(group));

        getQueryReturnValue().setReturnValue(group);
    }
}
