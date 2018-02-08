package org.ovirt.engine.core.bll.exportimport.vnics;

import java.util.Objects;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.bll.exportimport.vnics.MapVnicHandlers.ApplyProfileById;
import org.ovirt.engine.core.bll.exportimport.vnics.MapVnicHandlers.NetworkAttachedToCluster;
import org.ovirt.engine.core.bll.exportimport.vnics.MapVnicHandlers.SourceNameExistsOnEngine;
import org.ovirt.engine.core.bll.exportimport.vnics.MapVnicHandlers.TargetIdExistsOnEngine;
import org.ovirt.engine.core.bll.exportimport.vnics.MapVnicHandlers.TargetNameExistsOnEngine;
import org.ovirt.engine.core.bll.exportimport.vnics.MapVnicsHandlers.CreateContextPerMatchedMapping;
import org.ovirt.engine.core.bll.exportimport.vnics.MapVnicsHandlers.MatchUserMappingToOvfVnic;
import org.ovirt.engine.core.bll.exportimport.vnics.MapVnicsHandlers.ReportResults;
import org.ovirt.engine.core.bll.exportimport.vnics.MapVnicsHandlers.RunContextPerMatchedMapping;
import org.ovirt.engine.core.common.flow.Flow;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.dao.network.VnicProfileDao;
import org.ovirt.engine.core.dao.network.VnicProfileViewDao;

@Singleton
public class MapVnicsFlow implements Flow<MapVnicsContext> {

    private MatchUserMappingToOvfVnic matchMapping;

    @Inject
    private CreateContextPerMatchedMapping createContexts;

    @Inject
    public MapVnicsFlow() {
    }

    public MapVnicsFlow(CreateContextPerMatchedMapping createContexts) {
        this.createContexts = Objects.requireNonNull(createContexts);
        init();
    }

    @PostConstruct
    private void init() {
        matchMapping = new MatchUserMappingToOvfVnic();
        RunContextPerMatchedMapping runContexts = new RunContextPerMatchedMapping();
        ReportResults report = new ReportResults();

        matchMapping.setOnSuccess(createContexts);
        matchMapping.setOnNeutral(report);

        createContexts.setOnSuccess(runContexts);
        createContexts.setOnNeutral(report);

        runContexts.setOnSuccess(report);
        runContexts.setOnNeutral(report);
    }

    @Override
    public MatchUserMappingToOvfVnic getHead() {
        return matchMapping;
    }

    /**
     * Direct DI factory method for testing
     */
    public static MapVnicsFlow of(VnicProfileViewDao vnicProfileViewDao, VnicProfileDao vnicProfileDao, NetworkClusterDao networkClusterDao, NetworkDao networkDao) {
        SourceNameExistsOnEngine sourceNameExistsOnEngine = new SourceNameExistsOnEngine(vnicProfileViewDao);
        TargetIdExistsOnEngine targetIdExistsOnEngine = new TargetIdExistsOnEngine(vnicProfileDao);
        TargetNameExistsOnEngine targetNameExistsOnEngine = new TargetNameExistsOnEngine(vnicProfileViewDao);
        NetworkAttachedToCluster targetIdNetworkAttachedToCluster = new NetworkAttachedToCluster(networkClusterDao);
        NetworkAttachedToCluster targetNameNetworkAttachedToCluster = new NetworkAttachedToCluster(networkClusterDao);
        NetworkAttachedToCluster sourceNetworkAttachedToCluster = new NetworkAttachedToCluster(networkClusterDao);
        ApplyProfileById applyProfileById = new ApplyProfileById(networkDao);

        MapVnicFlow mapVnicFlow = new MapVnicFlow(sourceNameExistsOnEngine, targetIdExistsOnEngine,
                targetNameExistsOnEngine, targetIdNetworkAttachedToCluster, targetNameNetworkAttachedToCluster, sourceNetworkAttachedToCluster, applyProfileById);

        return new MapVnicsFlow(new CreateContextPerMatchedMapping(mapVnicFlow));
    }
}
