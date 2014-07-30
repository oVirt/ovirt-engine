package org.ovirt.engine.core.searchbackend;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class SyntaxContainer implements Iterable<SyntaxObject> {

    private final String mOrigText;
    private final LinkedList<SyntaxObject> mObjList = new LinkedList<SyntaxObject>();
    private final List<String> mCurrentCompletions = new ArrayList<String>();

    private static final String LINE_SEPARATOR = "\n";
    private boolean mValid = false;
    private SyntaxError mError = SyntaxError.NO_ERROR;
    private final int[] mErrorPos = new int[2];
    private int privateMaxCount;
    private long searchFrom = 0;
    private boolean caseSensitive=true;

    public boolean isSearchUsingTags() {
        return mOrigText.contains("tag")
                || (getSearchObjectStr() != null && (getSearchObjectStr().equals(SearchObjects.VDC_USER_OBJ_NAME)))
                || (getCrossRefObjList().contains(SearchObjects.VDC_STORAGE_POOL_OBJ_NAME))
                || (getCrossRefObjList().contains(SearchObjects.VDC_STORAGE_DOMAIN_OBJ_NAME));
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
        return mValid;
    }

    public void setvalid(boolean value) {
        mValid = value;
    }

    public SyntaxError getError() {
        return mError;
    }

    public boolean getCaseSensitive() {
        return caseSensitive;
    }

    public void setCaseSensitive(boolean value) {
        caseSensitive = value;
    }
    public int getErrorStartPos() {
        return mErrorPos[0];
    }

    public int getErrorEndPos() {
        return mErrorPos[1];
    }

    public SyntaxObject getFirst() {
        return mObjList.getFirst();
    }

    public String getSearchObjectStr() {
        if (mObjList.getFirst() != null) {
            return getObjSingularName(mObjList.getFirst().getBody());
        }
        return null;
    }

    public SyntaxContainer(final String origText) {
        mOrigText = origText;
        mValid = false;
    }

    public void setErr(SyntaxError errCode, int startPos, int endPos) {
        mErrorPos[0] = startPos;
        mErrorPos[1] = endPos;
        mError = errCode;
        mValid = false;
    }

    public void addSyntaxObject(SyntaxObjectType type, String body, int startPos, int endPos) {
        SyntaxObject newObj = new SyntaxObject(type, body, startPos, endPos);
        mObjList.addLast(newObj);
    }

    public SyntaxObjectType getState() {
        SyntaxObjectType retval = SyntaxObjectType.BEGIN;
        if (mObjList.size() > 0) {
            retval = mObjList.getLast().getType();
        }
        return retval;
    }

    public int getLastHandledIndex() {
        int retval = 0;
        if (mObjList.size() > 0) {
            retval = mObjList.getLast().getPos()[1];
        }
        return retval;
    }

    public String getPreviousSyntaxObject(int steps, SyntaxObjectType type) {
        String retval = "";
        if (mObjList.size() > steps) {
            SyntaxObject obj = mObjList.get(mObjList.size() - 1 - steps);
            if (obj.getType() == type) {
                retval = obj.getBody();
            }
        }
        if ("".equals(retval)
                && ((type == SyntaxObjectType.CROSS_REF_OBJ) || (type == SyntaxObjectType.SEARCH_OBJECT))) {
            retval = mObjList.getFirst().getBody();
        }
        return retval;
    }

    public SyntaxObjectType getPreviousSyntaxObjectType(int steps) {
        SyntaxObjectType retval = SyntaxObjectType.END;
        if (mObjList.size() > steps) {
            SyntaxObject obj = mObjList.get(mObjList.size() - 1 - steps);
            retval = obj.getType();
        }
        return retval;
    }

    public void addToACList(String[] acArr) {
        for (int idx = 0; idx < acArr.length; idx++) {
            mCurrentCompletions.add(acArr[idx]);
        }
    }

    public String[] getCompletionArray() {
        String[] retval = new String[mCurrentCompletions.size()];
        for (int idx = 0; idx < mCurrentCompletions.size(); idx++) {
            retval[idx] = mCurrentCompletions.get(idx);
        }
        return retval;
    }

    public ArrayList<String> getCrossRefObjList() {
        ArrayList<String> retval = new ArrayList<String>();
        String searchObj = getObjSingularName(getSearchObjectStr());
        for (SyntaxObject obj : mObjList) {
            if (obj.getType() == SyntaxObjectType.CROSS_REF_OBJ) {
                String objSingularName = getObjSingularName(obj.getBody());
                if ((!retval.contains(objSingularName)) &&
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
        }
        else if (SearchObjects.AUDIT_OBJ_NAME.equals(obj) || SearchObjects.AUDIT_PLU_OBJ_NAME.equals(obj)) {
            retval = SearchObjects.AUDIT_OBJ_NAME;
        }
        else if (SearchObjects.TEMPLATE_OBJ_NAME.equals(obj) || SearchObjects.TEMPLATE_PLU_OBJ_NAME.equals(obj)) {
            retval = SearchObjects.TEMPLATE_OBJ_NAME;
        }
        else if (SearchObjects.VDC_USER_OBJ_NAME.equals(obj) || SearchObjects.VDC_USER_PLU_OBJ_NAME.equals(obj)) {
            retval = SearchObjects.VDC_USER_OBJ_NAME;
        }
        else if (SearchObjects.VDC_GROUP_OBJ_NAME.equals(obj) || SearchObjects.VDC_GROUP_PLU_OBJ_NAME.equals(obj)) {
            retval = SearchObjects.VDC_GROUP_OBJ_NAME;
        }
        else if (SearchObjects.VDS_OBJ_NAME.equals(obj) || SearchObjects.VDS_PLU_OBJ_NAME.equals(obj)) {
            retval = SearchObjects.VDS_OBJ_NAME;
        }
        else if (SearchObjects.VM_OBJ_NAME.equals(obj) || SearchObjects.VM_PLU_OBJ_NAME.equals(obj)) {
            retval = SearchObjects.VM_OBJ_NAME;
        }
        else if (SearchObjects.DISK_OBJ_NAME.equals(obj) || SearchObjects.DISK_PLU_OBJ_NAME.equals(obj)) {
            retval = SearchObjects.DISK_OBJ_NAME;
        }
        else if (SearchObjects.QUOTA_OBJ_NAME.equals(obj) || SearchObjects.QUOTA_PLU_OBJ_NAME.equals(obj)) {
            retval = SearchObjects.QUOTA_OBJ_NAME;
        }
        else if (SearchObjects.VDC_POOL_OBJ_NAME.equals(obj) || SearchObjects.VDC_POOL_PLU_OBJ_NAME.equals(obj)) {
            retval = SearchObjects.VDC_POOL_OBJ_NAME;
        }
        else if (SearchObjects.VDC_CLUSTER_OBJ_NAME.equals(obj) || SearchObjects.VDC_CLUSTER_PLU_OBJ_NAME.equals(obj)) {
            retval = SearchObjects.VDC_CLUSTER_OBJ_NAME;
        }
        else if (SearchObjects.VDC_STORAGE_DOMAIN_OBJ_NAME.equals(obj)
                || SearchObjects.VDC_STORAGE_DOMAIN_PLU_OBJ_NAME.equals(obj)) {
            retval = SearchObjects.VDC_STORAGE_DOMAIN_OBJ_NAME;
        }
        else if (SearchObjects.GLUSTER_VOLUME_OBJ_NAME.equals(obj)
                || SearchObjects.GLUSTER_VOLUME_PLU_OBJ_NAME.equals(obj)) {
            retval = SearchObjects.GLUSTER_VOLUME_OBJ_NAME;
        }
        else if (SearchObjects.NETWORK_OBJ_NAME.equals(obj) || SearchObjects.NETWORK_PLU_OBJ_NAME.equals(obj)) {
            retval = SearchObjects.NETWORK_OBJ_NAME;
        } else if (SearchObjects.PROVIDER_OBJ_NAME.equals(obj) || SearchObjects.PROVIDER_PLU_OBJ_NAME.equals(obj)) {
            retval = SearchObjects.PROVIDER_OBJ_NAME;
        } else {
            retval = obj;

        }
        return retval;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("---------------- SyntaxContainer ---------------------");
        sb.append(LINE_SEPARATOR);
        sb.append("mOrigText       = ");
        sb.append(mOrigText);
        sb.append(LINE_SEPARATOR);
        sb.append("Valid           = ");
        sb.append(Boolean.toString(mValid));
        sb.append(LINE_SEPARATOR);
        sb.append("Error           = ");
        sb.append(mError.toString());
        sb.append(LINE_SEPARATOR);
        sb.append("CrossRefObjlist = ");
        for (String cro : getCrossRefObjList()) {
            sb.append(cro).append(", ");
        }
        sb.append("Syntax object list:");

        for (SyntaxObject obj : mObjList) {
            sb.append("    ");
            sb.append(LINE_SEPARATOR);
            sb.append(obj.toString());
        }
        return sb.toString();
    }

    public boolean contains(SyntaxObjectType type, String val) {
        boolean retval = false;
        for (SyntaxObject obj : mObjList) {
            if ((obj.getType() == type) && val.equalsIgnoreCase(obj.getBody())) {
                retval = true;
                break;
            }
        }
        return retval;
    }

    public ListIterator<SyntaxObject> listIterator(int index) {
        return mObjList.listIterator(index);
    }

    @Override
    public Iterator<SyntaxObject> iterator() {
        return mObjList.iterator();
    }
}
