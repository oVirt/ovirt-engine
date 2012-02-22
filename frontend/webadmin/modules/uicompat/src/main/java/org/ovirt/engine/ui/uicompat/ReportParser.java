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

    private final Map<String, Resource> resourceMap =
            new HashMap<String, Resource>();

    public static ReportParser getInstance() {
        return INSTANCE;
    }

    public Map<String, Resource> getResourceMap() {
        return resourceMap;
    }
    public void parseReport(String xmlPath) {
        // parse the XML document into a DOM
        Document messageDom = XMLParser.parse(xmlPath);

        Element reportsElement = (Element) messageDom.getElementsByTagName("reports").item(0);
        NodeList resourcesNodeList = reportsElement.getElementsByTagName("resource");

        initResources(resourcesNodeList);
    }

    private void initResources(NodeList nodeList) {
        Node resourceNode;
        int i = 0;

        // initialize the resources
        while ((resourceNode = nodeList.item(i)) != null) {
            Element resourceElement = ((Element) resourceNode);
            Resource resource =
                    new Resource(resourceElement.getAttribute("type"));

            // initialize resource categories
            addCategories2Resource(resourceElement, resource);

            resourceMap.put(resource.getType(), resource);

            i++;
        }
    }

    private void addCategories2Resource(Element resourceElement, Resource resource) {

        NodeList nodeList = resourceElement.getElementsByTagName("category");
        Node node;
        int i = 0;

        while ((node = nodeList.item(i)) != null) {
            Element categoryElement = ((Element) node);
            Category category = new Category(categoryElement.getAttribute("name"));

            // initialize categories uri's
            addUris2Category(categoryElement, category);

            resource.addCategory(category);
            i++;
        }
    }

    private void addUris2Category(Element categoryElement, Category category) {

        NodeList nodeList = categoryElement.getElementsByTagName("uri");
        Node node;
        int i = 0;

        while ((node = nodeList.item(i)) != null) {
            Element uriElement = ((Element) node);
            URI uri =
                    new URI(uriElement.getAttribute("name"),
                            uriElement.getAttributes()
                                    .getNamedItem("description")
                                    .getNodeValue(),
                            uriElement.getFirstChild().getNodeValue(),
                            uriElement.getAttribute("idParamName"),
                            Boolean.parseBoolean(uriElement.getAttribute("multiple")));

            category.addURI(uri);
            i++;
        }
    }


    public static class Resource {
        private final String type;

        private final List<Category> catergoriesList = new LinkedList<Category>();

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
        private final List<URI> uriList = new LinkedList<URI>();

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
}
