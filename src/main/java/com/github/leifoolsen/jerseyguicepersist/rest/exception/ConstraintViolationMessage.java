package com.github.leifoolsen.jerseyguicepersist.rest.exception;

import javax.validation.Path;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ConstraintViolationMessage {

    private String message;
    private String messageTemplate;
    private String propertyPath;
    private String invalidValue;

    /**
     * Create a {@code PropertyErrorMessage} instance. Constructor for JAXB providers.
     */
    protected ConstraintViolationMessage() {}

    /**
     * Create a {@code ConstraintViolationMessage} instance.
     *
     * @param message interpolated error message.
     * @param messageTemplate non-interpolated error message.
     * @param propertyPath  propertyPath.
     * @param invalidValue value that failed to pass constraints.
     */
    public ConstraintViolationMessage(String message, String messageTemplate, Path propertyPath, Object invalidValue) {
        this.message = message;
        this.messageTemplate = messageTemplate;
        this.propertyPath = propertyPath != null ? propertyPath.toString() : null;
        this.invalidValue = invalidValue != null ? invalidValue.toString() : null;
    }

    public String getMessage() {
        return message;
    }

    public String getMessageTemplate() {
        return messageTemplate;
    }

    public String getPropertyPath() {
        return propertyPath;
    }

    public String getInvalidValue() {
        return invalidValue;
    }
}
