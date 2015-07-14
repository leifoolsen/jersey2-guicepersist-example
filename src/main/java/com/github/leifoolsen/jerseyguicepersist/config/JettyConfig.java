package com.github.leifoolsen.jerseyguicepersist.config;

import com.github.leifoolsen.jerseyguicepersist.constraint.AssertMethodAsTrue;
import com.github.leifoolsen.jerseyguicepersist.util.StringUtil;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class JettyConfig {
    @NotNull
    @Valid
    private ServerConfig serverConfig;

    @NotNull
    @Valid
    private ThreadPoolConfig threadPoolConfig;

    @NotNull
    @Valid
    private ServerConnectorConfig serverConnectorConfig;

    @NotNull
    @Valid
    private WebAppContextConfig webAppContextConfig;


    public JettyConfig() {}

    public JettyConfig serverConfig(final ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
        return this;
    }

    public ServerConfig serverConfig() { return serverConfig; }

    public JettyConfig threadPoolConfig(final ThreadPoolConfig threadPoolConfig) {
        this.threadPoolConfig = threadPoolConfig;
        return this;
    }

    public ThreadPoolConfig threadPoolConfig() { return threadPoolConfig; }

    public JettyConfig serverConnectorConfig (final ServerConnectorConfig serverConnectorConfig) {
        this.serverConnectorConfig = serverConnectorConfig;
        return this;
    }

    public ServerConnectorConfig serverConnectorConfig() { return serverConnectorConfig; }

    public JettyConfig webAppContextConfig (final WebAppContextConfig webAppContextConfig) {
        this.webAppContextConfig = webAppContextConfig;
        return this;
    }

    public WebAppContextConfig webAppContextConfig() { return webAppContextConfig; }


    // -------------------------------
    public static class ServerConfig {
        public static final String ACCESS_LOG_FILE = "access-yyyy_mm_dd.log";

        private String accessLogPath;
        private String shutdownToken;

        public ServerConfig() {}

        public ServerConfig accessLogPath(final String accessLogPath) {
            this.accessLogPath = StringUtil.blankToNull(accessLogPath);
            return this;
        }

        public String accessLogPath() { return accessLogPath; }

        public ServerConfig shutdownToken(final String shutdownToken) {
            this.shutdownToken = StringUtil.blankToNull(shutdownToken);
            return this;
        }

        public String shutdownToken() { return shutdownToken; }
    }


    // -------------------------------
    @AssertMethodAsTrue(value="isValid", message="maxThreads value must be greater than minThreads value")
    public static class ThreadPoolConfig {
        @Min(8)
        private int minThreads = 8;

        @Max(65535)
        private int maxThreads = 200;

        private boolean daemon;

        private String name;

        public ThreadPoolConfig() {}

        public boolean isValid() {
            return maxThreads > minThreads;
        }

        public ThreadPoolConfig minThreads(final int minThreads) {
            this.minThreads = minThreads;
            return this;
        }

        public int minThreads() { return minThreads; }

        public ThreadPoolConfig maxThreads(final int maxThreads) {
            this.maxThreads = maxThreads;
            return this;
        }

        public int maxThreads() { return maxThreads; }

        public ThreadPoolConfig daemon(final boolean daemon) {
            this.daemon = daemon;
            return this;
        }

        public boolean daemon() { return daemon; }

        public ThreadPoolConfig name(final String name) {
            this.name = StringUtil.blankToNull(name);
            return this;
        }

        public String name() { return name; }
    }

    // -------------------------------
    public static class ServerConnectorConfig {
        @NotBlank
        private String scheme = "http";

        @NotBlank
        private String host = "localhost";

        @Min(0)
        @Max(65535)
        private int port = 8080;

        @Min(0)
        private int idleTimeout = 30000;

        public ServerConnectorConfig() {}

        public ServerConnectorConfig scheme(final String scheme) {
            this.scheme = StringUtil.blankToNull(scheme);
            return this;
        }

        public String scheme() { return scheme; }

        public ServerConnectorConfig host(final String host) {
            this.host = StringUtil.blankToNull(host);
            return this;
        }

        public String host() { return host; }

        public ServerConnectorConfig port(final int port) {
            this.port = port;
            return this;
        }

        public int port() { return port; }

        public ServerConnectorConfig idleTimeout(final int idleTimeout) {
            this.idleTimeout = idleTimeout;
            return this;
        }

        public int idleTimeout() { return idleTimeout; }
    }


    // -------------------------------
    public static class WebAppContextConfig {
        @NotBlank
        private String contextPath = "/";

        @NotBlank
        private String resourceBase = "/webapp";

        private boolean enableDirectoryListing = false;

        public WebAppContextConfig() {}

        public WebAppContextConfig contextPath(final String contextPath) {
            this.contextPath = StringUtil.blankToNull(contextPath);
            return this;
        }

        public String contextPath() { return contextPath; }

        public WebAppContextConfig resourceBase(final String resourceBase) {
            this.resourceBase = StringUtil.blankToNull(resourceBase);
            return this;
        }

        public String resourceBase() { return resourceBase; }

        public WebAppContextConfig enableDirectoryListing(final boolean enableDirectoryListing) {
            this.enableDirectoryListing = enableDirectoryListing;
            return this;
        }

        public boolean enableDirectoryListing() { return enableDirectoryListing; }
    }
}
