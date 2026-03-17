package org.example.realworldapi.util;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;

public class LibertyContainer extends GenericContainer<LibertyContainer> {

    public LibertyContainer(ImageFromDockerfile image, int httpPort, int httpsPort) {

        super(image);
        addExposedPorts(httpPort, httpsPort);

        waitingFor(Wait.forLogMessage("^.*CWWKF0011I.*$", 1));

    }

    public String getBaseURL() throws IllegalStateException {
        return "http://" + getHost() + ":" + getFirstMappedPort();
    }

}