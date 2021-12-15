package org.ovirt.engine.core.searchbackend;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class SyntaxContainer implements Iterable<SyntaxObject> {

    private final String origText;
    private final LinkedList<SyntaxObject> objList = new LinkedList<>();
    private final List<String> currentCompletions = new ArrayList<>();

    private static final String LINE_SEPARATOR = "\n";
    private boolean valid = false;
    private SyntaxError error = SyntaxError.NO_ERROR;
    private final int[] errorPos = new int[2];
    private int privateMaxCount;
    private long searchFrom = 0;
    private boolean caseSensitive=true;

    /**
     * Some of the searches needs to combine view table which holds tags.
     * For a search on users it is even more than tags, it is also VMs, because the view contains
     * also a foreign vm_guid that belongs to a user. This is to support searching for both tags and/or vms
     * on a certain user. Infact the name vdc_users_with_tags is a bit misleading.
     * @return
     */
    public boolean isSearchUsingTags() {
        return origText.contains("tag")
                || isUserSearchUsingTags()
                || getCrossRefObjList().contains(SearchObjects.VDC_STORAGE_POOL_OBJ_NAME)
                || getCrossRefObjList().contains(SearchObjects.VDC_STORAGE_DOMAIN_OBJ_NAME)
                // use tags in searches like : Vms: Cluster.<property>=<value> <and/or> Host.<property>=<value>
                // TODO : try to generalize those cases
                || (origText.toLowerCase().startsWith(SearchObjects.VM_OBJ_NAME.toLowerCase())
                && origText.toLowerCase().contains(SearchObjects.VDC_CLUSTER_OBJ_NAME.toLowerCase())
                && getCrossRefObjList().contains(SearchObjects.VDS_OBJ_NAME))
                || (origText.toLowerCase().startsWith(SearchObjects.AUDIT_OBJ_NAME.toLowerCase())
                && origText.toLowerCase().contains(SearchObjects.TEMPLATE_OBJ_NAME.toLowerCase()));
    }

    public int getMaxCount() {
        return privateMaxCount;
    }

    public void setMaxCount(int value) {
        privateMaxCount = value;
    }

    public long getSearchFrom() {
        return searchFrom;
    }

    public void setSearchFrom(long value) {
        searchFrom = value;
    }

    public boolean getvalid() {
        return valid;
    }

    public void setvalid(boolean value) {
        valid = value;
    }

    public SyntaxError getError() {
        return error;
    }

    public boolean getCaseSensitive() {
        return caseSensitive;
    }

    public void setCaseSensitive(boolean value) {
        caseSensitive = value;
    }
    public int getErrorStartPos() {
        return errorPos[0];
    }

    public int getErrorEndPos() {
        return errorPos[1];
    }

    public SyntaxObject getFirst() {
        return objList.getFirst();
    }

    public String getSearchObjectStr() {
        if (objList.getFirst() != null) {
            return getObjSingularName(objList.getFirst().getBody());
        }
        return null;
    }

    public SyntaxContainer(final String origText) {
        this.origText = origText;
        valid = false;
    }

    public void setErr(SyntaxError errCode, int startPos, int endPos) {
        errorPos[0] = startPos;
        errorPos[1] = endPos;
        error = errCode;
        valid = false;
    }

    public void addSyntaxObject(SyntaxObjectType type, String body, int startPos, int endPos) {
        SyntaxObject newObj = new SyntaxObject(type, body, startPos, endPos);
        objList.addLast(newObj);
    }

    public SyntaxObjectType getState() {
        SyntaxObjectType retval = SyntaxObjectType.BEGIN;
        if (objList.size() > 0) {
            retval = objList.getLast().getType();
        }
        return retval;
    }

    public int getLastHandledIndex() {
        int retval = 0;
        if (objList.size() > 0) {
            retval = objList.getLast().getPos()[1];
        }
        return retval;
    }

    public String getPreviousSyntaxObject(int steps, SyntaxObjectType type) {
        String retval = "";
        if (objList.size() > steps) {
            SyntaxObject obj = objList.get(objList.size() - 1 - steps);
            if (obj.getType() == type) {
                retval = obj.getBody();
            }
        }
        if ("".equals(retval)
                && ((type == SyntaxObjectType.CROSS_REF_OBJ) || (type == SyntaxObjectType.SEARCH_OBJECT))) {
            retval = objList.getFirst().getBody();
        }
        return retval;
    }

    public SyntaxObjectType getPreviousSyntaxObjectType(int steps) {
        SyntaxObjectType retval = SyntaxObjectType.END;
        if (objList.size() > steps) {
            SyntaxObject obj = objList.get(objList.size() - 1 - steps);
            retval = obj.getType();
        }
        return retval;
    }

    public void addToACList(String[] acArr) {
        for (int idx = 0; idx < acArr.length; idx++) {
            currentCompletions.add(acArr[idx]);
        }
    }

    public String[] getCompletionArray() {
        String[] retval = new String[currentCompletions.size()];
        for (int idx = 0; idx < currentCompletions.size(); idx++) {
            retval[idx] = currentCompletions.get(idx);
        }
        return retval;
    }

    public ArrayList<String> getCrossRefObjList() {
        ArrayList<String> retval = new ArrayList<>();
        String searchObj = getObjSingularName(getSearchObjectStr());
        for (SyntaxObject obj : objList) {
            if (obj.getType() == SyntaxObjectType.CROSS_REF_OBJ) {
                String objSingularName = getObjSingularName(obj.getBody());
                if (!retval.contains(objSingularName) &&
                        searchObj != null && !searchObj.equals(objSingularName)) {
                    retval.add(objSingularName);
                }
            }
        }
        return retval;
    }

    public String getObjSingularName(String obj) {
        String retval = obj;

        if (obj == null) {
            return null;
        }
        if (SearchObjects.AD_USER_OBJ_NAME.equals(obj) || SearchObjects.AD_USER_PLU_OBJ_NAME.equals(obj)) {
            retval = SearchObjects.AD_USER_OBJ_NAME;
        } else if (SearchObjects.AUDIT_OBJ_NAME.equals(obj) || SearchObjects.AUDIT_PLU_OBJ_NAME.equals(obj)) {
            retval = SearchObjects.AUDIT_OBJ_NAME;
        } else if (SearchObjects.TEMPLATE_OBJ_NAME.equals(obj) || SearchObjects.TEMPLATE_PLU_OBJ_NAME.equals(obj)) {
            retval = SearchObjects.TEMPLATE_OBJ_NAME;
        } else if (SearchObjects.VDC_USER_OBJ_NAME.equals(obj) || SearchObjects.VDC_USER_PLU_OBJ_NAME.equals(obj)) {
            retval = SearchObjects.VDC_USER_OBJ_NAME;
        } else if (SearchObjects.VDC_GROUP_OBJ_NAME.equals(obj) || SearchObjects.VDC_GROUP_PLU_OBJ_NAME.equals(obj)) {
            retval = SearchObjects.VDC_GROUP_OBJ_NAME;
        } else if (SearchObjects.VDS_OBJ_NAME.equals(obj) || SearchObjects.VDS_PLU_OBJ_NAME.equals(obj)) {
            retval = SearchObjects.VDS_OBJ_NAME;
        } else if (SearchObjects.VM_OBJ_NAME.equals(obj) || SearchObjects.VM_PLU_OBJ_NAME.equals(obj)) {
            retval = SearchObjects.VM_OBJ_NAME;
        } else if (SearchObjects.DISK_OBJ_NAME.equals(obj) || SearchObjects.DISK_PLU_OBJ_NAME.equals(obj)) {
            retval = SearchObjects.DISK_OBJ_NAME;
        } else if (SearchObjects.QUOTA_OBJ_NAME.equals(obj) || SearchObjects.QUOTA_PLU_OBJ_NAME.equals(obj)) {
            retval = SearchObjects.QUOTA_OBJ_NAME;
        } else if (SearchObjects.VDC_POOL_OBJ_NAME.equals(obj) || SearchObjects.VDC_POOL_PLU_OBJ_NAME.equals(obj)) {
            retval = SearchObjects.VDC_POOL_OBJ_NAME;
        } else if (SearchObjects.VDC_CLUSTER_OBJ_NAME.equals(obj) || SearchObjects.VDC_CLUSTER_PLU_OBJ_NAME.equals(obj)) {
            retval = SearchObjects.VDC_CLUSTER_OBJ_NAME;
        } else if (SearchObjects.VDC_STORAGE_DOMAIN_OBJ_NAME.equals(obj)
                || SearchObjects.VDC_STORAGE_DOMAIN_PLU_OBJ_NAME.equals(obj)) {
            retval = SearchObjects.VDC_STORAGE_DOMAIN_OBJ_NAME;
        } else if (SearchObjects.GLUSTER_VOLUME_OBJ_NAME.equals(obj)
                || SearchObjects.GLUSTER_VOLUME_PLU_OBJ_NAME.equals(obj)) {
            retval = SearchObjects.GLUSTER_VOLUME_OBJ_NAME;
        } else if (SearchObjects.NETWORK_OBJ_NAME.equals(obj) || SearchObjects.NETWORK_PLU_OBJ_NAME.equals(obj)) {
            retval = SearchObjects.NETWORK_OBJ_NAME;
        } else if (SearchObjects.PROVIDER_OBJ_NAME.equals(obj) || SearchObjects.PROVIDER_PLU_OBJ_NAME.equals(obj)) {
            retval = SearchObjects.PROVIDER_OBJ_NAME;
        } else if (SearchObjects.SESSION_OBJ_NAME.equals(obj) || SearchObjects.SESSION_PLU_OBJ_NAME.equals(obj)) {
            retval = SearchObjects.SESSION_OBJ_NAME;
        } else if (SearchObjects.JOB_OBJ_NAME.equals(obj) || SearchObjects.JOB_PLU_OBJ_NAME.equals(obj)) {
            retval = SearchObjects.JOB_OBJ_NAME;
        } else if (SearchObjects.VNIC_PROFILE_OBJ_NAME.equals(obj) || SearchObjects.VNIC_PROFILE_PLU_OBJ_NAME.equals(obj)) {
            retval = SearchObjects.VNIC_PROFILE_OBJ_NAME;
        } else {
            retval = obj;

        }
        return retval;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("---------------- SyntaxContainer ---------------------");
        sb.append(LINE_SEPARATOR);
        sb.append("origText       = ");
        sb.append(origText);
        sb.append(LINE_SEPARATOR);
        sb.append("Valid           = ");
        sb.append(Boolean.toString(valid));
        sb.append(LINE_SEPARATOR);
        sb.append("Error           = ");
        sb.append(error.toString());
        sb.append(LINE_SEPARATOR);
        sb.append("CrossRefObjlist = ");
        for (String cro : getCrossRefObjList()) {
            sb.append(cro).append(", ");
        }
        sb.append("Syntax object list:");

        for (SyntaxObject obj : objList) {
            sb.append("    ");
            sb.append(LINE_SEPARATOR);
            sb.append(obj.toString());
        }
        return sb.toString();
    }

    public boolean contains(SyntaxObjectType type, String val) {
        boolean retval = false;
        for (SyntaxObject obj : objList) {
            if ((obj.getType() == type) && val.equalsIgnoreCase(obj.getBody())) {
                retval = true;
                break;
            }
        }
        return retval;
    }

    public ListIterator<SyntaxObject> listIterator(int index) {
        return objList.listIterator(index);
    }

    @Override
    public Iterator<SyntaxObject> iterator() {
        return objList.iterator();
    }


    private boolean isUserSearchUsingTags() {
        return SearchObjects.VDC_USER_OBJ_NAME.equals(getSearchObjectStr())
                && origText.contains("vm");
    }
}
