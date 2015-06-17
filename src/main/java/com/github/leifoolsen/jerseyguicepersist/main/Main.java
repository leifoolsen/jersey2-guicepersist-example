package com.github.leifoolsen.jerseyguicepersist.main;

import com.google.common.base.MoreObjects;
import com.google.common.base.Splitter;
import com.google.common.primitives.Ints;
import eu.nets.oss.jetty.ContextPathConfig;
import eu.nets.oss.jetty.EmbeddedJettyBuilder;
import eu.nets.oss.jetty.PropertiesFileConfig;
import eu.nets.oss.jetty.StaticConfig;
import org.eclipse.jetty.server.Server;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Main {
    private static final int DEFAULT_PORT = 8080;
    private static final String DEFAULT_CONTEXT_PATH = "/";

    private Main() {}

    public static void main(String[] args) throws Exception {

        final Map<String, String> argsMap = Main.argsToMap(args);
        int port = MoreObjects.firstNonNull(Ints.tryParse(argsMap.get("port")), DEFAULT_PORT);

        if(argsMap.containsKey("shutdown")) {
            Main.attemptShutdown(port, argsMap.get("token"));
        }
        else {
            ContextPathConfig config = EmbeddedJettyBuilder.isStartedWithAppassembler()
                    ? new HerokuConfig(new PropertiesFileConfig())
                    : new StaticConfig(argsMap.get("context-path"), port);

            Main.attemptStartup(config);
        }
    }

    private static void attemptStartup(final ContextPathConfig config) throws IOException {

        Server server = JettyBootstrap.start(config);

        // Ctrl+C does not work inside IntelliJ
        if(!EmbeddedJettyBuilder.isStartedWithAppassembler()) {
            System.out.println(">>> Hit ENTER to stop");
            System.in.read();
            JettyBootstrap.stop(server);
        }
    }

    private static void attemptShutdown(final int port, final String shutdownToken) {
        try {
            URL url = new URL("http://localhost:" + port + "/shutdown?token=" + shutdownToken);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.getResponseCode();
            System.out.println(">>> Shutting down server @ " + url + ": " + connection.getResponseMessage());
        }
        catch (SocketException e) {
            System.out.println(">>> Server not running @ http://localhost:" + port);
            // Okay - the server is not running
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Map<String, String> argsToMap(String[] args) {
        // Convert args, e.g: port = 8087 context-path /myapp shutdown token=secret
        //                 -> port=8007", "context-path=/myapp", "shutdown= ", "token=secret"

        final Map<String, String> argsMap = new HashMap<>();

        if(args != null) {
            int i = 0;
            int n = args.length;
            while (i < n) {
                if (args[i].startsWith("port") ||
                    args[i].startsWith("token") ||
                    args[i].startsWith("context-path") ||
                    args[i].startsWith("contextPath")) {

                    List<String> p = Splitter.on('=').trimResults().splitToList(args[i]);
                    String name = p.get(0).equals("contextPath") ? "context-path" : p.get(0);
                    String value = p.size() > 1 ? p.get(1) : i < n-1 ? args[++i] : "";
                    if(args[i].equals("=")) value = i < n-1 ? args[++i] : "";

                    argsMap.put(name, value);
                }
                else if (args[i].equals("shutdown")) {
                    argsMap.put(args[i], "");
                }
                i++;
            }
        }
        argsMap.putIfAbsent("port", Integer.toString(DEFAULT_PORT));
        argsMap.putIfAbsent("context-path", DEFAULT_CONTEXT_PATH);
        return argsMap;
    }


    private static class HerokuConfig implements ContextPathConfig {

        private final PropertiesFileConfig delegate;

        private HerokuConfig(PropertiesFileConfig delegate) {
            this.delegate = delegate;
        }

        @Override
        public String getContextPath() {
            return delegate.getContextPath();
        }

        @Override
        public int getPort() {
            String port = System.getenv("PORT");
            return port == null ? delegate.getPort() : Integer.parseInt(port);
        }
    }
}
