package com.github.leifoolsen.jerseyguicepersist.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.UUID;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@Entity
public class User implements Serializable {

    private static final long serialVersionUID = 3665349089500867570L;


    @Id
    @Column(length=36)
    private String id  = UUID.randomUUID().toString();

    @Version
    private Long version;

    @Column(unique = true)
    private String username;

    private String password;
    private boolean active;

    protected User() {}

    public User(final String username, final String password, final boolean active) {
        this.username = username;
        this.password = password;
        this.active = active;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean isActive() {
        return active;
    }

}
