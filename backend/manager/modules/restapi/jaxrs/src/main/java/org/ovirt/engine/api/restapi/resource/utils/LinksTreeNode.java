package org.ovirt.engine.api.restapi.resource.utils;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;

public class LinksTreeNode {

    String element;
    LinksTreeNode parent;
    List<LinksTreeNode> children;
    boolean followed=false;

    public LinksTreeNode(String element) {
        super();
        this.element = element;
        children = new LinkedList<>();
    }

    public LinksTreeNode(String element, LinksTreeNode parent) {
        super();
        this.element = element;
        this.parent = parent;
        children = new LinkedList<>();
    }

    public Optional<LinksTreeNode> getChild(String childName) {
        return children.stream().filter(link -> link.element.equals(childName)).findFirst();
    }

    public void addChild(String childName) {
        children.add(new LinksTreeNode(childName, this));
    }

    public List<LinksTreeNode> getChildren() {
        return children;
    }

    public boolean isLeaf() {
        return this.children.isEmpty();
    }

    public String getElement() {
        return element;
    }

    public boolean isRoot() {
        return parent==null;
    }

    public boolean isFollowed() {
        return followed;
    }

    public void setFollowed(boolean followed) {
        this.followed = followed;
    }
    /**
     * Gets an iterator of elements leading up to this node, ROOT excluded.
     * e.g: for the node 'x' in the tree:
     *
     * ROOT
     *   disk_attachments
     *   nics
     *       x
     *       y
     *
     * the method will return an iterator with the values: nics, x
     */
    public Iterator<String> getPathIterator() {
        Stack<String> path = new Stack<>();
        LinksTreeNode node = this;
        do {
            if (!node.isRoot()) {
                path.push(this.element);
                node = node.parent;
            }
        } while (node!=null);
        return path.iterator();
    }

    /**
     * Receives a path for a single link, represented as 1 or more path elements
     * separated by dots, e.g: "disk_attachments.template". Returns true if this path exists
     * in the tree (meaning everything under this node), false otherwise. This method is
     * used by developers who add designated implementations for specific links. For example,
     * if a developer adds a command GetVmWithNics which optimizes fetching of Vms and their
     * Nics, he/she would write: "if (tree.pathExists("nics")) {...} where 'tree' is the root
     * of the link tree.
     */
    public boolean pathExists(String path) {
        Iterator<String> iterator = Arrays.asList(path.split("\\.")).iterator();
        return pathExists(this, iterator);
    }

    /**
     * This method marks the provided links as 'followed' in the links-tree.
     * Links are provided in the same format as the URL parameter, e.g:
     * ["nics", "disks.disk_attachments", "disks.template"].
     *
     * Developers who add implementation for following specific links are
     * responsible to mark these links as 'followed' when they are done.
     */
    public void markAsFollowed(String... links) {
        for (String link : links) {
            Optional<LinksTreeNode> node = Optional.of(this);
            for (String segment : link.split("\\.")) {
                if (node.isPresent()) {
                    node = node.get().getChild(segment);
                }
                if (node.isPresent()) {
                    node.get().setFollowed(true);
                }
            }
        }
    }

    private boolean pathExists(LinksTreeNode node, Iterator<String> iterator) {
        while(iterator.hasNext()) {
            Optional<LinksTreeNode> child = getChild(iterator.next());
            if (!child.isPresent()) {
                return false;
            } else {
                return pathExists(child.get(), iterator);
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return toString(1);
    }

    private String toString(int depth) {
        StringBuilder builder = new StringBuilder(element);
        for (LinksTreeNode node : children) {
            builder.append("\n").append(getTabs(depth)).append(node.toString(depth+1));
        }
        return builder.toString();
    }

    private String getTabs(int tabs) {
        StringBuilder builder = new StringBuilder();
        for (int i=0; i<tabs; i++) {
            builder.append("\t");
        }
        return builder.toString();
    }
}
