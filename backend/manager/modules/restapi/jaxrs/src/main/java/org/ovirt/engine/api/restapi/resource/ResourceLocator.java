package org.ovirt.engine.api.restapi.resource;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ovirt.engine.api.rsdl.ServiceTreeCrawler;
import org.ovirt.engine.api.rsdl.ServiceTreeNode;

/**
 * This class provides the service of retrieving 'Resource' classes
 * according to hrefs. Resource classes are found by crawling along
 * the API ServiceTree according to the provided hrefs.
 */
public class ResourceLocator {

    private static final Pattern ULR_VERSION_PART_PATTERN = Pattern.compile("/api/(v\\d+/)?");

    private static ResourceLocator instance;

    private ResourceLocator() {
        //singleton
    }

    public static ResourceLocator getInstance() {
        if (instance==null) {
            instance = new ResourceLocator();
        }
        return instance;
    }

    /**
     * Get the Resource class corresponding to the provided href
     * Assumption: href is of a location, not an action
     * (e.g: .../vms/{id} - good, .../vms/{id}/start - bad).
     * Another assumption is that href is not empty.
     */
    public BaseBackendResource locateResource(String href) throws Exception {
        href = removePrefix(href);
        ServiceTreeCrawler crawler = new ServiceTreeCrawler(Arrays.asList(href.split("/")));
        BaseBackendResource resource = BackendApiResource.getInstance();
        ServiceTreeNode node = null;
        while (crawler.hasNext()) {
            node = crawler.next();
            if (node.isCollection()) { //collection context
                Method method = resource.getClass().getMethod(node.getGetter()); //getSubResource()
                resource = (BaseBackendResource) method.invoke(resource);
            } else { //single entity context
                Method method = resource.getClass().getMethod(node.getGetter(), String.class);  //getSubResource(String id)
                resource = (BaseBackendResource) method.invoke(resource, crawler.getCurrentPathSegment());
            }
        }
        return resource;
    }

    /**
     * If  a full URL is passed, truncate the prefix:
     * For:
     *   http://localhost:8080/ovirt-engine/api/datacenters/1034e9ba-c1a4-442c-8bc9-f7c1c997652b
     * Remove:
     *   http://localhost:8080/ovirt-engine/api/
     * Remain with:
     *   datacenters/1034e9ba-c1a4-442c-8bc9-f7c1c997652b
     *
     * Api definition with version also can be truncated (e.g. /api/v3/ or /api/v4/)
     */
    static String removePrefix(String href) {
        Matcher matcher = ULR_VERSION_PART_PATTERN.matcher(href);
        if (matcher.find()) {
            href = href.substring(matcher.end());
        }

        return href;
    }
}
