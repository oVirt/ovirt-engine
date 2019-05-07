package org.ovirt.engine.core.bll.scheduling.queries;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.dao.scheduling.AffinityGroupDao;

public class GetAffinityGroupsByVmIdQuery extends AffinityGroupsQueryBase<IdQueryParameters> {

    @Inject
    private AffinityGroupDao affinityGroupDao;

    public GetAffinityGroupsByVmIdQuery(IdQueryParameters parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        List<AffinityGroup> groups = affinityGroupDao.getAllAffinityGroupsByVmId(getParameters().getId());
        checkBrokenGroups(groups);

        getQueryReturnValue().setReturnValue(groups);
    }

}
