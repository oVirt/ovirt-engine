package org.ovirt.engine.core.bll.gluster;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSession;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSessionDetails;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;

public class GetGlusterVolumeGeoRepSessionsQuery<P extends IdQueryParameters> extends GlusterQueriesCommandBase<P>{

    public GetGlusterVolumeGeoRepSessionsQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        List<GlusterGeoRepSession> geoRepSessions = glusterGeoRepDao.getGeoRepSessions(getParameters().getId());
        /*
         * If master volume has sessions, update the master server names in accordance with masterBrickId in sessionDetails.
         */
        if (geoRepSessions != null) {
            for (GlusterGeoRepSession currentSession : geoRepSessions) {
                // For each session get corresponding session details.
                List<GlusterGeoRepSessionDetails> geoRepSessionDetails = glusterGeoRepDao.getGeoRepSessionDetails(currentSession.getId());
                /*
                 * Session details could be null, if they are not yet synced. possible if session detail command failed for some unexpected reason
                 * such as network failure even though the sessions in the cluster are synced(sessionListCommand)
                 */
                if(geoRepSessionDetails == null) {
                    continue;
                }
                /*
                 * If non null session detail, set masterBrick servername in accordance with that in brick
                 * as obtained by using masterbrickId
                 */
                for (GlusterGeoRepSessionDetails currentDetail : geoRepSessionDetails) {
                    if(currentDetail == null) {
                        continue;
                    }
                    Guid currentMasterBrickId = currentDetail.getMasterBrickId();
                    if(currentMasterBrickId == null) {
                        continue;
                    }
                    GlusterBrickEntity currentBrick = glusterBrickDao.getById(currentMasterBrickId);
                    if (currentBrick != null) {
                        currentDetail.setMasterBrickHostName(currentBrick.getServerName());
                    }
                }
                /*
                 * Finally set session details to the current session
                 */
                currentSession.setSessionDetails((ArrayList<GlusterGeoRepSessionDetails>) geoRepSessionDetails);
            }
        }
        getQueryReturnValue().setReturnValue(geoRepSessions);
    }

}
