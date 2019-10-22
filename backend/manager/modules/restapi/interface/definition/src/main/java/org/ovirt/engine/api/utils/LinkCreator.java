/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.utils;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.model.DetailedLink;
import org.ovirt.engine.api.model.Link;
import org.ovirt.engine.api.model.LinkCapabilities;
import org.ovirt.engine.api.model.Parameter;
import org.ovirt.engine.api.model.ParametersSet;
import org.ovirt.engine.api.model.Request;
import org.ovirt.engine.api.model.Url;

public class LinkCreator {
    private static final String SEARCH_RELATION = "/search";
    private static final String SEARCH_TEMPLATE = "?search={query}";
    private static final String MATRIX_PARAMETER_TEMPLATE = ";%s={%s}";

    /**
     * Create a search link with the given parameters
     *
     * @param url the url
     * @param rel the link to add
     * @param flags flags for this link, e.g: 'searchable'
     * @return the link the was created
     */
    public static DetailedLink createLink(String url, String rel, LinkFlags flags) {
        return createLink(url, rel, flags, new ParametersSet());
    }

    /**
     * Create a search link with the given parameters
     *
     * @param url the url
     * @param rel the link to add
     * @param flags flags for this link, e.g: 'searchable'
     * @param params url parameters
     * @return the link the was created
     */
    public static DetailedLink createLink(String url, String rel, LinkFlags flags, ParametersSet params) {
        DetailedLink link = new DetailedLink();
        link.setRel(rel);
        link.setHref(combine(url, rel));
        if (flags == LinkFlags.SEARCHABLE) {
            LinkCapabilities capabilities = new LinkCapabilities();
            capabilities.setSearchable(true);
            link.setLinkCapabilities(capabilities);
        }
        link.setRequest(new Request());
        link.getRequest().setUrl(new Url());
        link.getRequest().getUrl().getParametersSets().add(params);
        return link;
    }

    /**
     * Create a search link with the given parameters
     *
     * @param url the url
     * @param rel the link to add
     * @param params url parameters
     * @return the link the was created
     */
    public static Link createLink(String url, String rel, List<ParametersSet> params) {
        Link link = new Link();
        link.setRel(rel + SEARCH_RELATION);
        link.setHref(combine(url, params) + SEARCH_TEMPLATE);
        return link;
    }

    public static Link createLink(String url, String rel) {
        Link link = new Link();
        link.setRel(rel);
        link.setHref(url);
        return link;
    }

    /**
     * Create a search link with the given parameters
     * @param url the url
     * @param rel the link to add
     * @return link with search
     */
    public static Link createSearchLink(String url, String rel) {
        Link link = new Link();
        link.setRel(rel + SEARCH_RELATION);
        link.setHref(combine(url, rel) + SEARCH_TEMPLATE);
        return link;
    }

    /**
     * Combine head and tail portions of a URI path.
     *
     * @param head the path head
     * @param tail the path tail
     * @return the combined head and tail
     */
    public static String combine(String head, String tail) {
        return StringUtils.removeEnd(head, "/") + "/" + StringUtils.removeStart(tail, "/");
    }

    /**
     * Combine URL params to URI path.
     *
     * @param head the path head
     * @param params the URL params to append
     * @return the combined head and params
     */
    public static String combine(String head, List<ParametersSet> params) {
        StringBuilder combined_params = new StringBuilder();
        if (params != null) {
           for (ParametersSet ps : params) {
               for (Parameter param : ps.getParameters()) {
                   combined_params.append(String.format(MATRIX_PARAMETER_TEMPLATE, param.getName(), param.getValue()));
              }
           }
        }
        combined_params.insert(0, head);
        return combined_params.toString();
    }

    /**
     * Used to specify link options
     */
    public enum LinkFlags { NONE, SEARCHABLE; }
}
