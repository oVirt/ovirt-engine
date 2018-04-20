package org.ovirt.engine.core.bll.exportimport.vnics;

import static org.ovirt.engine.core.common.flow.HandlerOutcome.NEUTRAL;
import static org.ovirt.engine.core.common.flow.HandlerOutcome.SUCCESS;

import java.util.Objects;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.ovirt.engine.core.common.businessentities.network.ExternalVnicProfileMapping;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.flow.AbstractHandler;
import org.ovirt.engine.core.common.flow.Handler;
import org.ovirt.engine.core.common.flow.HandlerOutcome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapVnicsHandlers {

    public static class CreateContextPerMatchedMapping extends AbstractHandler<MapVnicsContext> {

        @Inject
        MapVnicFlow mapVnicFlow;

        @Inject
        public CreateContextPerMatchedMapping() {
        }

        public CreateContextPerMatchedMapping(MapVnicFlow mapVnicFlow) {
            this.mapVnicFlow = Objects.requireNonNull(mapVnicFlow);
        }

        @Override
        public HandlerOutcome handle(MapVnicsContext vnicsContext) {
            if (MapUtils.isEmpty(vnicsContext.getMatched())) {
                // if there are no matching vnics and user mappings, and not
                // even 'source' vnic profiles from the VM OVF, then nothing to do
                vnicsContext.trace(NEUTRAL, CreateContextPerMatchedMapping.class);
                return NEUTRAL;
            }
            vnicsContext.getMatched().forEach((vnic, mapping) -> {
                MapVnicContext vnicContext = new MapVnicContext(getClass().getSimpleName()).setOvfVnic(vnic)
                        .setClusterId(vnicsContext.getClusterId()).setProfileMapping(mapping).setFlow(mapVnicFlow);
                vnicsContext.getContexts().add(vnicContext);
            });
            vnicsContext.trace(SUCCESS, CreateContextPerMatchedMapping.class);
            return SUCCESS;
        }
    }


    public static class MatchUserMappingToOvfVnic extends AbstractHandler<MapVnicsContext> {

        @Override
        public HandlerOutcome handle(MapVnicsContext ctx) {
            if (CollectionUtils.isEmpty(ctx.getOvfVnics())) {
                // no vnics in VM OVF - nothing to do
                ctx.trace(NEUTRAL, MatchUserMappingToOvfVnic.class);
                return NEUTRAL;
            }

            if (CollectionUtils.isEmpty(ctx.getUserMappings())) {
                // no user mappings so subsequent handlers should use the
                // vnic profile from the OVF as the 'source' profile
                ctx.getOvfVnics().forEach(vnic -> ctx.getMatched().put(vnic, null));
            } else {
                ctx.getOvfVnics().forEach(vnic -> {
                    ExternalVnicProfileMapping matchingMapping =
                        ctx.getUserMappings()
                            .stream()
                            .filter(Objects::nonNull)
                            .filter(mapping -> mapping.isSameSourceProfile(vnic))
                            .findFirst()
                            .orElse(null);
                    ctx.getMatched().put(vnic, matchingMapping);
                });
            }
            ctx.trace(SUCCESS, MatchUserMappingToOvfVnic.class);
            return SUCCESS;
        }
    }

    public static class ReportResults extends AbstractHandler<MapVnicsContext> {

        private static final Logger logger = LoggerFactory.getLogger(ReportResults.class);
        private static final String NAME_FORMAT = "MissingEntities - VM %s";
        private static final String TRACE_FORMAT = "vNic ['%s','%s']: mapping terminated with '%s'";

        @Override
        public HandlerOutcome handle(MapVnicsContext vnicsContext) {
            if (!vnicsContext.hasContexts() && vnicsContext.hasTrace()) {
                logger.debug(auditName(vnicsContext), "No vnics found on the VM");
            } else {
                vnicsContext.getContexts().forEach(ctx -> {
                    if (ctx.isSuccessful()) {
                        logger.info(formatTrace(ctx));
                    } else if (ctx.getException() != null) {
                        vnicsContext.addNonAssociableVnic(formatVnic(ctx.getOvfVnic()));
                        logger.error(formatTrace(ctx), ctx.getException());
                    } else {
                        vnicsContext.addNonAssociableVnic(formatVnic(ctx.getOvfVnic()));
                        logger.warn(formatTrace(ctx));
                    }
                });
            }
            vnicsContext.trace(SUCCESS, ReportResults.class);
            return SUCCESS;
        }

        private String auditName(MapVnicsContext ctx) {
            return String.format(NAME_FORMAT, ctx.getVmName());
        }

        private String formatVnic(VmNetworkInterface vnic) {
            return String.format("['%s', '%s']", vnic.getName(), vnic.getNetworkName());
        }

        private String formatTrace(MapVnicContext ctx) {
            StringBuilder result = new StringBuilder();
            if (ctx.hasException()) {
                result.append(ctx.getException().getMessage());
            }
            if (ctx.hasTrace()) {
                HandlerOutcome outcome = ctx.getLastOutcome();
                if (outcome != null) {
                    result.append(result.length() > 0 ? ", " : "");
                    result.append(outcome.name());
                }
                Class<Handler> handler = ctx.getLastHandler();
                if (handler != null) {
                    result.append(result.length() > 0 ? ", " : "");
                    result.append(handler.getSimpleName());
                }
            }
            return String.format(TRACE_FORMAT, ctx.getOvfVnic().getName(), ctx.getOvfVnic().getNetworkName(), result);
        }
    }

    public static class RunContextPerMatchedMapping extends AbstractHandler<MapVnicsContext> {

        @Override
        public HandlerOutcome handle(MapVnicsContext vnicsContext) {
            if (CollectionUtils.isEmpty(vnicsContext.getContexts())) {
                // no contexts to run
                vnicsContext.trace(NEUTRAL, RunContextPerMatchedMapping.class);
                return NEUTRAL;
            }
            vnicsContext.getContexts().forEach(ctx ->  {
                ctx.getFlow().getHead().process(ctx);
                vnicsContext.trace(SUCCESS, RunContextPerMatchedMapping.class, ctx);
            });
            return SUCCESS;
        }
    }
}
