package co.infoclinic.term.hira.model;

import java.util.List;

public class HiraTreeNode {
    private String code;
    private String label;
    private String type;     // "group" | "leaf"
    private int childCount;
    private List<HiraTreeNode> children;

    public HiraTreeNode() {}

    public HiraTreeNode(String code, String label, String type, int childCount) {
        this.code = code;
        this.label = label;
        this.type = type;
        this.childCount = childCount;
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public int getChildCount() { return childCount; }
    public void setChildCount(int childCount) { this.childCount = childCount; }
    public List<HiraTreeNode> getChildren() { return children; }
    public void setChildren(List<HiraTreeNode> children) { this.children = children; }
}
