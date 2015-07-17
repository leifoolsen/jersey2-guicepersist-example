package com.github.leifoolsen.jerseyguicepersist.config;

import com.github.leifoolsen.jerseyguicepersist.constraint.AssertMethodAsTrue;
import com.github.leifoolsen.jerseyguicepersist.util.StringUtil;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class JettyConfig {
    @NotNull
    @Valid
    private ServerConfig serverConfig = null;

    @NotNull
    @Valid
    private ThreadPoolConfig threadPoolConfig = null;

    @NotNull
    @Valid
    private ServerConnectorConfig serverConnectorConfig = null;

    @NotNull
    @Valid
    private WebAppContextConfig webAppContextConfig = null;


    JettyConfig() {}

    public ServerConfig serverConfig() { return serverConfig; }

    public ThreadPoolConfig threadPoolConfig() { return threadPoolConfig; }

    public ServerConnectorConfig serverConnectorConfig() { return serverConnectorConfig; }

    public WebAppContextConfig webAppContextConfig() { return webAppContextConfig; }


    // -------------------------------
    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    @AssertMethodAsTrue(value="isValid", message="accessLogPath mey not be NULL")
    public static class ServerConfig {
        public static final String ACCESS_LOG_FILE = "access-yyyy_mm_dd.log";

        private boolean useAccessLog = false;
        private String accessLogPath = null;

        ServerConfig() {}

        public Boolean useAccessLog() { return useAccessLog; }

        public ServerConfig accessLogPath(final String accessLogPath) {
            this.accessLogPath = StringUtil.blankToNull(accessLogPath);
            return this;
        }

        public String accessLogPath() { return accessLogPath; }

        public boolean isValid() {
            return !useAccessLog || StringUtil.blankToNull(accessLogPath) != null;
        }
    }


    // -------------------------------
    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    @AssertMethodAsTrue(value="isValid", message="maxThreads value must be greater than minThreads value")
    public static class ThreadPoolConfig {
        @Min(8)
        private int minThreads = 8;

        @Max(65535)
        private int maxThreads = 200;

        private boolean daemon = false;

        private String name = null;

        ThreadPoolConfig() {}

        public boolean isValid() {
            return maxThreads > minThreads;
        }

        public int minThreads() { return minThreads; }

        public int maxThreads() { return maxThreads; }

        public boolean daemon() { return daemon; }

        public String name() { return name; }
    }

    // -------------------------------
    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
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

        private String shutdownToken = null;

        ServerConnectorConfig() {}

        public String scheme() { return scheme; }

        public String host() { return host; }

        public int port() { return port; }

        public int idleTimeout() { return idleTimeout; }

        public String shutdownToken() { return shutdownToken; }
    }


    // -------------------------------
    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class WebAppContextConfig {
        @NotBlank
        private String contextPath = "/";

        @NotBlank
        private String resourceBase = "/webapp";

        private boolean enableDirectoryListing = false;

        WebAppContextConfig() {}

        public String contextPath() { return contextPath; }

        public String resourceBase() { return resourceBase; }

        public boolean enableDirectoryListing() { return enableDirectoryListing; }
    }
}
