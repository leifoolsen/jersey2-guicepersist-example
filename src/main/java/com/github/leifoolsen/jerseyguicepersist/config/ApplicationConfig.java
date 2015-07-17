package com.github.leifoolsen.jerseyguicepersist.config;

import com.github.leifoolsen.jerseyguicepersist.constraint.AssertMethodAsTrue;
import com.github.leifoolsen.jerseyguicepersist.util.StringUtil;
import com.google.common.base.Preconditions;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@AssertMethodAsTrue(value="isValidStage", message="Stage is not valid")
public class ApplicationConfig {

    public enum Stage {
        DEV("dev"),
        PROD("prod"),
        TEST("test");

        private static final Map<String, Stage> LOOKUP = new HashMap<>();
        static {
            for (Stage s : Stage.values()) {
                LOOKUP.put(s.stage, s);
            }
        }
        private final String stage;

        Stage(String stage) { this.stage = stage; }

        public String stage() { return stage; }

        public static Stage get(final String stage) {
            Preconditions.checkNotNull(stage, "Stage to get may not be null");
            return Preconditions.checkNotNull(LOOKUP.get(stage), "Stage '%s' not found", stage);
        }
    }

    @NotBlank
    private String stage = null;

    private String appHome;
    private String workPath;
    private String logPath;
    private boolean jerseyTraceLogging = false;

    @NotNull
    @Valid
    private JettyConfig jettyConfig;

    @NotNull
    @Valid
    private PersistenceUnitConfig persistenceUnitConfig;

    ApplicationConfig() {}

    public boolean isValidStage() {
        return Stage.LOOKUP.get(stage) != null;
    }

    public Stage stage() {
        return Stage.get(stage);
    }

    public ApplicationConfig appHome(final String homePath) {
        Preconditions.checkArgument(StringUtil.blankToNull(homePath) != null, "appHome path may not be null or empty");
        this.appHome = homePath;
        calculateWorkPath();
        calculateLogPath();
        jettyConfig().serverConfig().accessLogPath(logPath());
        return this;
    }

    public String appHome() { return appHome; }

    public String workPath() { return workPath; }

    public String logPath() { return logPath; }

    public boolean jerseyTraceLogging() { return jerseyTraceLogging; }

    public JettyConfig jettyConfig() { return jettyConfig; }

    public PersistenceUnitConfig persistenceUnitConfig() { return persistenceUnitConfig; }

    private void calculateWorkPath() {
        // Paths.get(appHome).resolve(workPath) will generate an absolute path from 'workPath' if workPath starts with '/'
        // else, workPath will be joined with appHome
        workPath = StringUtil.blankToNull(workPath) == null
                ? appHome
                : Paths.get(appHome).resolve(workPath).normalize().toAbsolutePath().toString();
    }

    private void calculateLogPath() {
        // Paths.get(appHome).resolve(logPath) will generate an absolute path from 'logPath' if logPath starts with '/'
        // else, logPath will be joined with appHome
        logPath = StringUtil.blankToNull(logPath) == null
                ? workPath
                : Paths.get(appHome).resolve(logPath).normalize().toAbsolutePath().toString();
    }
}
