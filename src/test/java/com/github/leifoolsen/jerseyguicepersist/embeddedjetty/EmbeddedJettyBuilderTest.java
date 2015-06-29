package com.github.leifoolsen.jerseyguicepersist.embeddedjetty;

import org.eclipse.jetty.server.Server;
import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class EmbeddedJettyBuilderTest {

    @Test
    @Ignore  // Tested in UserResourceTest
    public void startJetty() throws Exception {
        Server server = new EmbeddedJettyBuilder().defaultServer().build();
        EmbeddedJettyBuilder.start(server);
        assertThat(server.isRunning(), is(true));
        EmbeddedJettyBuilder.stop(server);
    }
}
