package org.ovirt.engine.core.bll.exportimport.vnics;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.network.ExternalVnicProfileMapping;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.core.compat.Guid;

class MapVnicFlowTestUtils {

    private static final boolean GENERATE_DEBUG_INFO = false;

    static void printDataPointDetails(List<VnicProfileView> views, ExternalVnicProfileMapping mapping, int count, String description) {
        if (!GENERATE_DEBUG_INFO) {
            return;
        }
        System.out.println(String.format("[%s - %d] profile view from dao [%s], input mapping {%s}", description, count,
                printProfileViews(views), printMapping(mapping)));
    }

    private static String printMapping(ExternalVnicProfileMapping mapping) {
        if (mapping == null) {
            return "null";
        }
        if (new ExternalVnicProfileMapping(null, null, null).equalsEntire(mapping)) {
            return "empty";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(mapping.getSourceNetworkName()).append(", ")
                .append(mapping.getSourceProfileName()).append(", ")
                .append(mapping.getTargetProfileId() != null ? mapping.getTargetProfileId().toString(): "null").append(", ")
                .append(mapping.getTargetNetworkName()).append(", ")
                .append(mapping.getTargetProfileName());
        return sb.toString();
    }

    private static String printProfileViews(List<VnicProfileView> views) {
        if (views == null) {
            return "null";
        }
        if (views.isEmpty()) {
            return "empty";
        }
        StringBuilder sb = new StringBuilder();
        for (VnicProfileView vpv : views) {
            sb.append('{')
                    .append(vpv.getNetworkName())
                    .append(',')
                    .append(vpv.getName())
                    .append('}');
        }
        return sb.toString();
    }

    static void printContexts(MapVnicContext expected, MapVnicContext actual) {
        if (!GENERATE_DEBUG_INFO) {
            return;
        }
        printContext(expected, "expected");
        printContext(actual, "actual");
        System.out.println("------------ Actual Trace Start --------------");
        System.out.println(actual.getTrace());
        System.out.println("------------ Actual Trace End --------------");
    }

    private static void printContext(MapVnicContext ctx, String ctxName) {
        System.out.println(ctxName + " " + ctx.print());
        if (ctx.getException() != null) {
            System.out.println(ctx.getException());
        }
    }

    static MapVnicContext makeCtx(Guid clusterId, ExternalVnicProfileMapping mapping, VmNetworkInterface vnic){
        return new MapVnicContext().setClusterId(clusterId).setProfileMapping(mapping).setOvfVnic(vnic);
    }

    static MapVnicContext makeCtx(Guid clusterId, VmNetworkInterface vnic){
        return new MapVnicContext().setClusterId(clusterId).setOvfVnic(vnic);
    }

    static ExternalVnicProfileMapping mappingOf(String sourceProfileName, String sourceNetworkName, Guid targetProfileId) {
        return new ExternalVnicProfileMapping(sourceNetworkName, sourceProfileName, targetProfileId);
    }

    static ExternalVnicProfileMapping mappingOf(String sourceProfileName, String sourceNetworkName, String targetProfileName, String targetNetworkName) {
        return new ExternalVnicProfileMapping(sourceNetworkName, sourceProfileName, targetNetworkName, targetProfileName);
    }

    static VmNetworkInterface vnicOf(Guid profileId, String profileName, String networkName) {
        VmNetworkInterface vnic = vnicOf(profileName, networkName);
        vnic.setVnicProfileId(profileId);
        return vnic;
    }

    static VmNetworkInterface vnicOf(String profileName, String networkName) {
        VmNetworkInterface vnic = new VmNetworkInterface();
        vnic.setVnicProfileName(profileName);
        vnic.setNetworkName(networkName);
        return vnic;
    }

    static VnicProfileView profileViewOf(String networkName, String profileName) {
        VnicProfileView vpv = new VnicProfileView();
        vpv.setNetworkName(networkName);
        vpv.setName(profileName);
        return vpv;
    }

    static VnicProfile profileOf(Guid profileId, String profileName, Guid networkId) {
        VnicProfile vp = profileOf(profileName, networkId);
        vp.setId(profileId);
        return vp;
    }

    static VnicProfile profileOf(String profileName, Guid networkId) {
        VnicProfile vp = new VnicProfile();
        vp.setName(profileName);
        vp.setNetworkId(networkId);
        return vp;
    }

    static VnicProfileView profileViewOf(Guid profileId, String profileName, String networkName, Guid networkId) {
        VnicProfileView vpv = new VnicProfileView();
        vpv.setName(profileName);
        vpv.setNetworkName(networkName);
        vpv.setNetworkId(networkId);
        vpv.setId(profileId);
        return vpv;
    }

    static VnicProfileView profileViewOf(Guid id) {
        VnicProfileView vpv = new VnicProfileView();
        vpv.setId(id);
        return vpv;
    }

    static NetworkCluster networkClusterOf(Guid networkId) {
        NetworkCluster nc = new NetworkCluster();
        nc.setNetworkId(networkId);
        return nc;
    }

    static Network networkOf(Guid networkId, String networkName) {
        Network n = new Network();
        n.setId(networkId);
        n.setName(networkName);
        return n;
    }
}
