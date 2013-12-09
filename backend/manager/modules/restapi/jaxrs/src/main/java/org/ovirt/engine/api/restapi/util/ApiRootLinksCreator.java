package org.ovirt.engine.api.restapi.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.core.UriInfo;

import org.ovirt.engine.api.common.util.LinkHelper;
import org.ovirt.engine.api.common.util.LinkHelper.LinkFlags;
import org.ovirt.engine.api.model.DetailedLink;
import org.ovirt.engine.api.model.Link;
import org.ovirt.engine.api.model.Parameter;
import org.ovirt.engine.api.model.ParametersSet;

public class ApiRootLinksCreator {

    public static Collection<DetailedLink> getLinks(UriInfo uriInfo) {
        Collection<DetailedLink> links = new LinkedList<DetailedLink>();
        links.add(createLink("capabilities", uriInfo));
        links.add(createLink("clusters", LinkFlags.SEARCHABLE, uriInfo));
        links.add(createLink("datacenters", LinkFlags.SEARCHABLE, uriInfo));
        links.add(createLink("events", LinkFlags.SEARCHABLE, getEventParams(), uriInfo));
        links.add(createLink("hosts", LinkFlags.SEARCHABLE, uriInfo));
        links.add(createLink("networks", LinkFlags.SEARCHABLE, uriInfo));
        links.add(createLink("roles", uriInfo));
        links.add(createLink("storagedomains", LinkFlags.SEARCHABLE, uriInfo));
        links.add(createLink("tags", uriInfo));
        links.add(createLink("templates", LinkFlags.SEARCHABLE, uriInfo));
        links.add(createLink("users", LinkFlags.SEARCHABLE, uriInfo));
        links.add(createLink("groups", LinkFlags.SEARCHABLE, uriInfo));
        links.add(createLink("domains", uriInfo));
        links.add(createLink("vmpools", LinkFlags.SEARCHABLE, uriInfo));
        links.add(createLink("vms", LinkFlags.SEARCHABLE, uriInfo));
        links.add(createLink("disks", LinkFlags.SEARCHABLE, uriInfo));
        links.add(createLink("jobs", uriInfo));
        links.add(createLink("storageconnections", uriInfo));
        links.add(createLink("vnicprofiles", uriInfo));
        links.add(createLink("permissions", uriInfo));
        return links;
    }

    public static Collection<DetailedLink> getGlusterLinks(UriInfo uriInfo) {
        Collection<DetailedLink> links = new LinkedList<DetailedLink>();
        links.add(createLink("capabilities", uriInfo));
        links.add(createLink("clusters", LinkFlags.SEARCHABLE, uriInfo));
        links.add(createLink("events", LinkFlags.SEARCHABLE, getEventParams(), uriInfo));
        links.add(createLink("hosts", LinkFlags.SEARCHABLE, uriInfo));
        links.add(createLink("networks", LinkFlags.SEARCHABLE, uriInfo));
        links.add(createLink("roles", uriInfo));
        links.add(createLink("tags", uriInfo));
        links.add(createLink("users", LinkFlags.SEARCHABLE, uriInfo));
        links.add(createLink("groups", LinkFlags.SEARCHABLE, uriInfo));
        links.add(createLink("domains", uriInfo));
        return links;
    }

    public static List<String> getAllRels(UriInfo uriInfo) {
        List<String> rels = new ArrayList<String>();
        for (Link link : getLinks(uriInfo)) {
            rels.add(link.getRel());
        }
        return rels;
    }

    public static List<String> getGlusterRels(UriInfo uriInfo) {
        List<String> rels = new ArrayList<String>();
        for (Link link : getGlusterLinks(uriInfo)) {
            rels.add(link.getRel());
        }
        return rels;
    }

    private static DetailedLink createLink(String rel, LinkFlags flags, UriInfo uriInfo) {
        return LinkHelper.createLink(uriInfo.getBaseUri().getPath(), rel, flags);
    }

    private static DetailedLink createLink(String rel, LinkFlags flags, ParametersSet params, UriInfo uriInfo) {
        return LinkHelper.createLink(uriInfo.getBaseUri().getPath(), rel, flags, params);
    }

    private static DetailedLink createLink(String rel, UriInfo uriInfo) {
        return LinkHelper.createLink(uriInfo.getBaseUri().getPath(), rel, LinkFlags.NONE);
    }

    private static ParametersSet getEventParams() {
        ParametersSet ps = new ParametersSet();
        Parameter param = new Parameter();
        param.setName("from");
        param.setValue("event_id");
        ps.getParameters().add(param);
        return ps;
    }

}
