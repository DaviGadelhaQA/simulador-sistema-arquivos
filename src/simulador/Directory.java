package simulador;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

public class Directory implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private Directory parent;

    private Map<String, File> files;
    private Map<String, Directory> directories;

    public Directory(String name, Directory parent) {
        this.name = name;
        this.parent = parent;

        this.files = new TreeMap<>();
        this.directories = new TreeMap<>();
    }

    public String getName() {
        return name;
    }

    public Directory getParent() {
        return parent;
    }

    public Map<String, File> getFiles() {
        return files;
    }

    public Map<String, Directory> getDirectories() {
        return directories;
    }

    public void setName(String name) {
        this.name = name;
    }
}