package com.wind.meditor.visitor;


import com.wind.meditor.core.ManifestEditor;
import pxb.android.axml.NodeVisitor;


public class PackageNameReaderVisitor extends NodeVisitor {

    private String currentNode;

    private Object parent;
    public PackageNameReaderVisitor(String nodeName, Object parentClassObj) {
        super();
        this.currentNode = nodeName;
        this.parent = parentClassObj;
    }

    @Override
    public void attr(String ns, String name, int resourceId, int type, Object obj) {
        if (currentNode!=null && currentNode.equals("manifest")) {
            if (name.equals("package")) {
                ((ManifestEditor)parent).oldPackageName = (String)obj;
            }
        }
        super.attr(ns, name, resourceId, type, obj);
    }

    @Override
    public NodeVisitor child(String ns, String name) {
        if (name.equals("manifest")) {
            return new PackageNameReaderVisitor(name,parent);
        }
        return super.child(ns, name);
    }

}
