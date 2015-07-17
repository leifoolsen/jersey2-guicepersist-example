package com.github.leifoolsen.jerseyguicepersist.config;

import com.google.common.base.Splitter;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.Properties;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class PersistenceUnitConfig {
    @NotBlank
    private String name = null;

    @NotNull
    @NotEmpty
    private List<String> properties = null;

    public PersistenceUnitConfig() {}

    public String name() {
        return name;
    }

    public Properties properties() {
        Properties p = new Properties();
        for (String s : properties) {
            List<String> nameValue = Splitter.on('=').trimResults().splitToList(s);
            if(nameValue.size() > 0) {
                p.put(nameValue.get(0), nameValue.size() > 1 ? nameValue.get(1) : "");
            }
        }
        return  p;
    }
}
