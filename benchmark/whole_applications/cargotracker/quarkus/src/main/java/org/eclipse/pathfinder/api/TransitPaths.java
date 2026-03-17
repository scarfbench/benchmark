package org.eclipse.pathfinder.api;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TransitPaths implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<TransitPath> transitPaths;

    public TransitPaths() {
        this.transitPaths = new ArrayList<>();
    }

    public TransitPaths(List<TransitPath> transitPaths) {
        this.transitPaths = transitPaths;
    }

    public List<TransitPath> getTransitPaths() {
        return transitPaths;
    }

    public void setTransitPaths(List<TransitPath> transitPaths) {
        this.transitPaths = transitPaths;
    }
}
