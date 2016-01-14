package org.ovirt.engine.ui.uicompat;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;

public class ReportParser {

    private static ReportParser INSTANCE = new ReportParser();

    private final Map<String, Resource> resourceMap = new HashMap<>();

    private final Map<String, Dashboard> dashboardMap = new HashMap<>();

    private boolean isCommunityEdition = false;

    public static ReportParser getInstance() {
        return INSTANCE;
    }

    public boolean isCommunityEdition() {
        return isCommunityEdition;
    }

    public Map<String, Resource> getResourceMap() {
        return resourceMap;
    }

    public Map<String, Dashboard> getDashboardMap() {
        return dashboardMap;
    }

    public boolean parseReport(String xmlPath) {
        try {
            // parse the XML document into a DOM
            Document messageDom = XMLParser.parse(xmlPath);

            Element reportsElement = (Element) messageDom.getElementsByTagName("reports").item(0); //$NON-NLS-1$
            NodeList dashboradsNodeList = reportsElement.getElementsByTagName("dashboard"); //$NON-NLS-1$
            initDashboards(dashboradsNodeList);
            NodeList resourcesNodeList = reportsElement.getElementsByTagName("resource"); //$NON-NLS-1$

            initResources(resourcesNodeList);
        } catch (Throwable e) {
            return false;
        }
        return true;
    }

    private void initDashboards(NodeList dashboardNodeList) {
        Node dashboardNode = dashboardNodeList.item(0);
        Element dashboardElement = (Element) dashboardNode;
        isCommunityEdition = Boolean.valueOf(dashboardElement.getAttribute("is_ce")); //$NON-NLS-1$

        NodeList resourcesNodeList = dashboardElement.getElementsByTagName("resource"); //$NON-NLS-1$
        Node resourceNode;
        int i = 0;

        while ((resourceNode = resourcesNodeList.item(i)) != null) {
            Element resourceElement = (Element) resourceNode;
            dashboardMap.put(resourceElement.getAttribute("type"), new Dashboard(resourceElement.getFirstChild() //$NON-NLS-1$
                    .getNodeValue()));
            i++;
        }
    }

    private void initResources(NodeList nodeList) {
        Node resourceNode;
        int i = 0;

        // initialize the resources
        while ((resourceNode = nodeList.item(i)) != null) {
            Element resourceElement = (Element) resourceNode;
            Resource resource =
                    new Resource(resourceElement.getAttribute("type")); //$NON-NLS-1$

            // initialize resource categories
            addCategories2Resource(resourceElement, resource);

            resourceMap.put(resource.getType(), resource);

            i++;
        }
    }

    private void addCategories2Resource(Element resourceElement, Resource resource) {

        NodeList nodeList = resourceElement.getElementsByTagName("category"); //$NON-NLS-1$
        Node node;
        int i = 0;

        while ((node = nodeList.item(i)) != null) {
            Element categoryElement = (Element) node;
            Category category = new Category(categoryElement.getAttribute("name")); //$NON-NLS-1$

            // initialize categories uri's
            addUris2Category(categoryElement, category);

            resource.addCategory(category);
            i++;
        }
    }

    private void addUris2Category(Element categoryElement, Category category) {

        NodeList nodeList = categoryElement.getElementsByTagName("uri"); //$NON-NLS-1$
        Node node;
        int i = 0;

        while ((node = nodeList.item(i)) != null) {
            Element uriElement = (Element) node;
            URI uri =
                    new URI(uriElement.getAttribute("name"), //$NON-NLS-1$
                            uriElement.getAttributes()
                                    .getNamedItem("description") //$NON-NLS-1$
                                    .getNodeValue(),
                            uriElement.getFirstChild().getNodeValue(),
                            uriElement.getAttribute("idParamName"), //$NON-NLS-1$
                            Boolean.parseBoolean(uriElement.getAttribute("multiple"))); //$NON-NLS-1$

            category.addURI(uri);
            i++;
        }
    }

    public static class Resource {
        private final String type;

        private final List<Category> catergoriesList = new LinkedList<>();

        public Resource(String type) {
            super();
            this.type = type;
        }

        public String getType() {
            return type;
        }

        public List<Category> getCatergoriesList() {
            return catergoriesList;
        }

        void addCategory(Category category) {
            catergoriesList.add(category);
        }
    }

    public static class Category {
        private final String name;
        private final List<URI> uriList = new LinkedList<>();

        public Category(String name) {
            super();
            this.name = name;
        }

        public String getName() {
            return name;
        }

        void addURI(URI uri) {
            uriList.add(uri);
        }

        public List<URI> getUriList() {
            return uriList;
        }
    }

    public static class URI {
        private final String name;
        private final String description;
        private final String value;
        private final String id;
        private boolean multiple = false;

        public URI(String name, String description, String value, String id, boolean multiple) {
            super();
            this.name = name;
            this.description = description;
            this.value = value;
            this.id = id;
            this.multiple = multiple;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }

        public String getDescription() {
            return description;
        }

        public String getId() {
            return id;
        }

        public boolean isMultiple() {
            return multiple;
        }
    }

    public static class Dashboard {
        String uri;

        private Dashboard(String uri) {
            this.uri = uri;
        }

        public String getUri() {
            return uri;
        }
    }
}
