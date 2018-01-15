package org.ovirt.engine.core.bll.exportimport.vnics;

import static org.ovirt.engine.core.common.flow.HandlerOutcome.FAILURE;
import static org.ovirt.engine.core.common.flow.HandlerOutcome.SUCCESS;

import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.NetworkClusterId;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.core.common.flow.AbstractHandler;
import org.ovirt.engine.core.common.flow.Context;
import org.ovirt.engine.core.common.flow.HandlerOutcome;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.dao.network.VnicProfileDao;
import org.ovirt.engine.core.dao.network.VnicProfileViewDao;

public class MapVnicHandlers {

    public static class ApplyNoProfile extends AbstractHandler<MapVnicContext> {

        @Override
        public HandlerOutcome handle(MapVnicContext ctx) {
            ctx.getOvfVnic().setVnicProfileId(null);
            ctx.getOvfVnic().setVnicProfileName(null);
            ctx.getOvfVnic().setNetworkName(null);
            ctx.trace(SUCCESS, ApplyNoProfile.class);
            return SUCCESS;
        }
    }

    public static class ApplyProfileById extends AbstractHandler<MapVnicContext> {

        @Inject
        private NetworkDao networkDao;

        @Inject
        public ApplyProfileById() {
        }

        public ApplyProfileById(NetworkDao networkDao) {
            this.networkDao = Objects.requireNonNull(networkDao);
        }

        @Override
        public HandlerOutcome handle(MapVnicContext ctx) {
            VnicProfile vnicProfile = ctx.getVnicProfileFoundByDao();
            if (vnicProfile == null || vnicProfile.getNetworkId() == null) {
                ctx.trace(FAILURE, ApplyProfileById.class);
                return FAILURE;
            }
            ctx.getOvfVnic().setVnicProfileId(vnicProfile.getId());
            ctx.getOvfVnic().setVnicProfileName(vnicProfile.getName());
            ctx.getOvfVnic().setNetworkName(networkDao.get(vnicProfile.getNetworkId()).getName());
            ctx.trace(SUCCESS, ApplyProfileById.class);
            return SUCCESS;
        }
    }

    public static class InternalProfileUniqueNameExistsOnEngine {

        @Inject
        private VnicProfileViewDao vnicProfileViewDao;

        @Inject
        public InternalProfileUniqueNameExistsOnEngine() {
        }

        public InternalProfileUniqueNameExistsOnEngine(VnicProfileViewDao vnicProfileViewDao) {
            this.vnicProfileViewDao = Objects.requireNonNull(vnicProfileViewDao);
        }

        VnicProfileView handle(String vnicProfileName, String vnicProfileNetworkName, Guid clusterId) {
            List<VnicProfileView> vnicProfileViews = vnicProfileViewDao.getAllForCluster(clusterId);
            if (vnicProfileViews == null) {
                return null;
            }
            return vnicProfileViews
                    .stream()
                    .filter(vnicProfileView ->
                            Objects.equals(vnicProfileView.getName(), vnicProfileName) &&
                                    Objects.equals(vnicProfileView.getNetworkName(), vnicProfileNetworkName))
                    .findFirst().orElse(null);
        }
    }

    public static class NOP<C extends Context> extends AbstractHandler<C> {

        @Override
        public HandlerOutcome handle(C ctx) {
            ctx.trace(SUCCESS, NOP.class);
            return SUCCESS;
        }
    }

    public static class ProfileMappingSpecified extends AbstractHandler<MapVnicContext> {

        @Override
        public HandlerOutcome handle(MapVnicContext ctx) {
            if(ctx.getProfileMapping() != null) {
                ctx.trace(SUCCESS, ProfileMappingSpecified.class);
                return SUCCESS;
            }
            ctx.trace(FAILURE, ProfileMappingSpecified.class);
            return FAILURE;
        }
    }

    public static class SourceNameExistsOnEngine extends AbstractHandler<MapVnicContext> {

        @Inject
        private InternalProfileUniqueNameExistsOnEngine internalHandler;

        @Inject
        public SourceNameExistsOnEngine() {
        }

        public SourceNameExistsOnEngine(VnicProfileViewDao vnicProfileViewDao) {
            internalHandler = new InternalProfileUniqueNameExistsOnEngine(vnicProfileViewDao);
        }

        @Override
        public HandlerOutcome handle(MapVnicContext ctx) {
            VnicProfileView vnicProfile = internalHandler.handle(
                    ctx.getOvfVnic().getVnicProfileName(),
                    ctx.getOvfVnic().getNetworkName(),
                    ctx.getClusterId());
            if (vnicProfile != null) {
                ctx.setVnicProfileFoundByDao(vnicProfile);
                ctx.trace(SUCCESS, SourceNameExistsOnEngine.class);
                return SUCCESS;
            }
            ctx.trace(FAILURE, SourceNameExistsOnEngine.class);
            return FAILURE;

        }
    }

    public static class TargetIdExistsOnEngine extends AbstractHandler<MapVnicContext> {

        @Inject
        private VnicProfileDao vnicProfileDao;

        @Inject
        public TargetIdExistsOnEngine() {
        }

        public TargetIdExistsOnEngine(VnicProfileDao vnicProfileDao) {
            this.vnicProfileDao = Objects.requireNonNull(vnicProfileDao);
        }

        @Override
        public HandlerOutcome handle(MapVnicContext ctx) {
            VnicProfile vnicProfile = vnicProfileDao.get(ctx.getProfileMapping().getTargetProfileId());
            if (vnicProfile != null) {
                ctx.setVnicProfileFoundByDao(vnicProfile);
                ctx.trace(SUCCESS, TargetIdExistsOnEngine.class);
                return SUCCESS;
            }
            ctx.trace(FAILURE, TargetIdExistsOnEngine.class);
            return FAILURE;
        }
    }

    public static class TargetIdSpecified extends AbstractHandler<MapVnicContext> {

        @Override
        public HandlerOutcome handle(MapVnicContext ctx) {
            if(ctx.getProfileMapping().hasTargetId()) {
                ctx.trace(SUCCESS, TargetIdSpecified.class);
                return SUCCESS;
            }
            ctx.trace(FAILURE, TargetIdSpecified.class);
            return FAILURE;
        }
    }

    public static class TargetNameExistsOnEngine extends AbstractHandler<MapVnicContext> {

        @Inject
        private InternalProfileUniqueNameExistsOnEngine internalHandler;

        @Inject
        public TargetNameExistsOnEngine() {
        }

        public TargetNameExistsOnEngine(VnicProfileViewDao vnicProfileViewDao) {
            internalHandler = new InternalProfileUniqueNameExistsOnEngine(vnicProfileViewDao);
        }

        @Override
        public HandlerOutcome handle(MapVnicContext ctx) {
            VnicProfileView vnicProfile = internalHandler.handle(
                    ctx.getProfileMapping().getTargetProfileName(),
                    ctx.getProfileMapping().getTargetNetworkName(),
                    ctx.getClusterId());
            if (vnicProfile != null) {
                ctx.setVnicProfileFoundByDao(vnicProfile);
                ctx.trace(SUCCESS, TargetNameExistsOnEngine.class);
                return SUCCESS;
            }
            ctx.trace(FAILURE, TargetNameExistsOnEngine.class);
            return FAILURE;

        }
    }

    public static class TargetNamesAreEmptyString extends AbstractHandler<MapVnicContext> {

        @Override
        public HandlerOutcome handle(MapVnicContext ctx) {
            if(ctx.getProfileMapping().targetNamesAreEmptyString()) {
                ctx.trace(SUCCESS, TargetNamesAreEmptyString.class);
                return SUCCESS;
            }
            ctx.trace(FAILURE, TargetNamesAreEmptyString.class);
            return FAILURE;
        }
    }

    public static class TargetNamesSpecified extends AbstractHandler<MapVnicContext> {

        @Override
        public HandlerOutcome handle(MapVnicContext ctx) {
            if(ctx.getProfileMapping().hasTargetNames()) {
                ctx.trace(SUCCESS, TargetNamesSpecified.class);
                return SUCCESS;
            }
            ctx.trace(FAILURE, TargetNamesSpecified.class);
            return FAILURE;
        }
    }

    public static class NetworkAttachedToCluster extends AbstractHandler<MapVnicContext> {

        @Inject
        private NetworkClusterDao networkClusterDao;

        @Inject
        public NetworkAttachedToCluster() {
        }

        public NetworkAttachedToCluster(NetworkClusterDao networkClusterDao) {
            this.networkClusterDao = Objects.requireNonNull(networkClusterDao);
        }

        @Override
        public HandlerOutcome handle(MapVnicContext ctx) {
            NetworkCluster networkCluster = networkClusterDao.get(new NetworkClusterId(ctx.getClusterId(), ctx.getVnicProfileFoundByDao().getNetworkId()));
            HandlerOutcome outcome = networkCluster != null ? SUCCESS : FAILURE;
            ctx.trace(outcome, NetworkAttachedToCluster.class);
            return outcome;
        }
    }

    public static class TargetProfileSpecified extends AbstractHandler<MapVnicContext> {

        @Override
        public HandlerOutcome handle(MapVnicContext ctx) {
            if(ctx.getProfileMapping().hasTarget()) {
                ctx.trace(SUCCESS, TargetProfileSpecified.class);
                return SUCCESS;
            }
            ctx.trace(FAILURE, TargetProfileSpecified.class);
            return FAILURE;
        }
    }
}
