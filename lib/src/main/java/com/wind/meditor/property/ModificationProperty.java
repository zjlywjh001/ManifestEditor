package com.wind.meditor.property;

import java.util.ArrayList;
import java.util.List;

import com.wind.meditor.utils.NodeValue;
import org.apache.commons.text.RandomStringGenerator;

/**
 * 修改的参数
 *
 * @author windysha
 */
public class ModificationProperty {
    
    private List<String> usesPermissionList = new ArrayList<>();
    private List<MetaData> metaDataList = new ArrayList<>();
    private List<MetaData> deleteMetaDataList = new ArrayList<>();

    private List<AttributeItem> applicationAttributeList = new ArrayList<>();
    private List<AttributeItem> manifestAttributeList = new ArrayList<>();

    private boolean coexist_on = false;

    private String oldPackageName;

    private String newPackageName;

    public List<String> getUsesPermissionList() {
        return usesPermissionList;
    }

    public ModificationProperty addUsesPermission(String permissionName) {
        usesPermissionList.add(permissionName);
        return this;
    }

    public List<AttributeItem> getApplicationAttributeList() {
        return applicationAttributeList;
    }

    public ModificationProperty addApplicationAttribute(AttributeItem item) {
        applicationAttributeList.add(item);
        return this;
    }

    public List<MetaData> getMetaDataList() {
        return metaDataList;
    }

    public ModificationProperty addMetaData(MetaData data) {
        metaDataList.add(data);
        return this;
    }

    public List<AttributeItem> getManifestAttributeList() {
        return manifestAttributeList;
    }

    public ModificationProperty addManifestAttribute(AttributeItem item) {
        manifestAttributeList.add(item);
        return this;
    }

    public List<MetaData> getDeleteMetaDataList() {
        return deleteMetaDataList;
    }

    public ModificationProperty addDeleteMetaData(String name) {
        this.deleteMetaDataList.add(new MetaData(name, ""));
        return this;
    }

    public static class MetaData {
        private String name;
        private String value;

        public MetaData(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

    }

    public void setCoexistInfo(boolean on, String oldN,String newN) {
        this.coexist_on = on;
        this.oldPackageName = oldN;
        this.newPackageName = newN;
        if (this.newPackageName == null) {
            char [][] pairs = {{'a','z'},{'A','Z'}};
            String pkgsuffix = new RandomStringGenerator.Builder().withinRange(pairs).build().generate(3);
            this.newPackageName = this.oldPackageName + "." +pkgsuffix;
            System.out.println("New package name not set, auto generated: "+this.newPackageName);
            this.addManifestAttribute(new AttributeItem(NodeValue.Manifest.PACKAGE, this.newPackageName).setNamespace(null));
        } else {
            System.out.println("New package name: "+this.newPackageName);
        }
    }

    public String getOldPackageName() {
        return this.oldPackageName;
    }

    public String getNewPackageName() {
        return this.newPackageName;
    }

    public boolean isCoexist_on() {
        return this.coexist_on;
    }
}
