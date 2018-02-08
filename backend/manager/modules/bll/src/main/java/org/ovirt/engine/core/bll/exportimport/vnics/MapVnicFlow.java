package org.ovirt.engine.core.bll.exportimport.vnics;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.exportimport.vnics.MapVnicHandlers.ApplyNoProfile;
import org.ovirt.engine.core.bll.exportimport.vnics.MapVnicHandlers.ApplyProfileById;
import org.ovirt.engine.core.bll.exportimport.vnics.MapVnicHandlers.NetworkAttachedToCluster;
import org.ovirt.engine.core.bll.exportimport.vnics.MapVnicHandlers.ProfileMappingSpecified;
import org.ovirt.engine.core.bll.exportimport.vnics.MapVnicHandlers.SourceNameExistsOnEngine;
import org.ovirt.engine.core.bll.exportimport.vnics.MapVnicHandlers.TargetIdExistsOnEngine;
import org.ovirt.engine.core.bll.exportimport.vnics.MapVnicHandlers.TargetIdSpecified;
import org.ovirt.engine.core.bll.exportimport.vnics.MapVnicHandlers.TargetNameExistsOnEngine;
import org.ovirt.engine.core.bll.exportimport.vnics.MapVnicHandlers.TargetNamesAreEmptyString;
import org.ovirt.engine.core.bll.exportimport.vnics.MapVnicHandlers.TargetNamesSpecified;
import org.ovirt.engine.core.bll.exportimport.vnics.MapVnicHandlers.TargetProfileSpecified;
import org.ovirt.engine.core.common.flow.Flow;
import org.ovirt.engine.core.common.flow.Handler;

public class MapVnicFlow implements Flow<MapVnicContext> {

    // head
    private Handler<MapVnicContext> profileMappingSpecified;
    // below fields are injected because they need to be injected with DAO's
    @Inject
    private SourceNameExistsOnEngine sourceNameExistsOnEngine;
    @Inject
    private TargetIdExistsOnEngine targetIdExistsOnEngine;
    @Inject
    private TargetNameExistsOnEngine targetNameExistsOnEngine;
    @Inject
    private NetworkAttachedToCluster targetIdNetworkAttachedToCluster;
    @Inject
    private NetworkAttachedToCluster targetNameNetworkAttachedToCluster;
    @Inject
    private NetworkAttachedToCluster sourceNetworkAttachedToCluster;
    @Inject
    private ApplyProfileById applyProfileById;

    @Inject
    public MapVnicFlow(SourceNameExistsOnEngine sourceNameExistsOnEngine, TargetIdExistsOnEngine targetIdExistsOnEngine,
            TargetNameExistsOnEngine targetNameExistsOnEngine, NetworkAttachedToCluster targetIdNetworkAttachedToCluster,
            NetworkAttachedToCluster targetNameNetworkAttachedToCluster, NetworkAttachedToCluster sourceNetworkAttachedToCluster, ApplyProfileById applyProfileById) {

        this.sourceNameExistsOnEngine = sourceNameExistsOnEngine;
        this.targetIdExistsOnEngine = targetIdExistsOnEngine;
        this.targetNameExistsOnEngine = targetNameExistsOnEngine;
        this.targetIdNetworkAttachedToCluster = targetIdNetworkAttachedToCluster;
        this.targetNameNetworkAttachedToCluster = targetNameNetworkAttachedToCluster;
        this.sourceNetworkAttachedToCluster = sourceNetworkAttachedToCluster;
        this.applyProfileById = applyProfileById;
        init();
    }

    public void init() {
        profileMappingSpecified = new ProfileMappingSpecified();

        Handler<MapVnicContext> targetProfileSpecified = new TargetProfileSpecified();
        Handler<MapVnicContext> targetIdSpecified = new TargetIdSpecified();
        Handler<MapVnicContext> targetNamesSpecified = new TargetNamesSpecified();
        Handler<MapVnicContext> targetNamesAreEmptyString = new TargetNamesAreEmptyString();
        Handler<MapVnicContext> applyNoProfile = new ApplyNoProfile();
        Handler<MapVnicContext> fallBack = new ApplyNoProfile();

        profileMappingSpecified.setOnSuccess(targetProfileSpecified);
        profileMappingSpecified.setOnFailure(sourceNameExistsOnEngine);

        targetProfileSpecified.setOnSuccess(targetIdSpecified);
        targetProfileSpecified.setOnFailure(sourceNameExistsOnEngine);

        targetIdSpecified.setOnSuccess(targetIdExistsOnEngine);
        targetIdSpecified.setOnFailure(targetNamesSpecified);

        targetIdExistsOnEngine.setOnSuccess(targetIdNetworkAttachedToCluster);
        targetIdExistsOnEngine.setOnFailure(targetNamesSpecified);

        targetNamesSpecified.setOnSuccess(targetNamesAreEmptyString);
        targetNamesSpecified.setOnFailure(sourceNameExistsOnEngine);

        targetIdNetworkAttachedToCluster.setName("Target_IdNetworkAttachedToCluster");
        targetIdNetworkAttachedToCluster.setOnSuccess(applyProfileById);
        targetIdNetworkAttachedToCluster.setOnFailure(targetNamesSpecified);

        targetNamesAreEmptyString.setOnSuccess(applyNoProfile);
        targetNamesAreEmptyString.setOnFailure(targetNameExistsOnEngine);

        targetNameExistsOnEngine.setOnSuccess(targetNameNetworkAttachedToCluster);
        targetNameExistsOnEngine.setOnFailure(sourceNameExistsOnEngine);

        sourceNameExistsOnEngine.setOnSuccess(sourceNetworkAttachedToCluster);
        sourceNameExistsOnEngine.setOnFailure(fallBack);

        targetNameNetworkAttachedToCluster.setName("Target_NameNetworkAttachedToCluster");
        targetNameNetworkAttachedToCluster.setOnSuccess(applyProfileById);
        targetNameNetworkAttachedToCluster.setOnFailure(sourceNameExistsOnEngine);

        sourceNetworkAttachedToCluster.setName("Source_NameNetworkAttachedToCluster");
        sourceNetworkAttachedToCluster.setOnSuccess(applyProfileById);
        sourceNetworkAttachedToCluster.setOnFailure(fallBack);
    }

    @Override
    public Handler<MapVnicContext> getHead() {
        return profileMappingSpecified;
    }
}
