package ru.tokarev.cloudstorage.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class FolderTreeNode {

    Long id;
    String name;
    List<FolderTreeNode> children = new ArrayList<>();

    public FolderTreeNode(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public void addChild(FolderTreeNode child) {
        children.add(child);
    }

}
