package com.github.leifoolsen.jerseyguicepersist.rest.exception;

import com.google.common.base.MoreObjects;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.gson.GsonBuilder;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ErrorMessage {
    private String id = UUID.randomUUID().toString();
    private int responseStatusCode;
    private Integer errorCode;
    private String exceptionClass;
    private String message;
    private String messageTemplate;
    private String location;
    private List<ConstraintViolationMessage> constraintViolationMessages;

    @XmlTransient
    private String stackTrace;

    protected ErrorMessage() {}

    private ErrorMessage(Builder builder) {
        responseStatusCode = builder.responseStatusCode;
        errorCode = builder.errorCode;
        exceptionClass = builder.exceptionClass;
        message = builder.message;
        messageTemplate = builder.messageTemplate;
        location = builder.location;
        constraintViolationMessages = builder.constraintViolationMessages;
        stackTrace = builder.stackTrace;
    }

    /**
     *
     * @param  t the exceptionClass we're building an error message for
     * @param  uriInfo provides access to application and request
     *         URI information. Relative URIs are relative to the base URI of the
     *         application, see {@link UriInfo#getBaseUri}.
     * @return the error message
     */
    public static Builder with(Throwable t, UriInfo uriInfo) {
        return new Builder(t, uriInfo);
    }

    /**
     *
     * @param status The HTTP Status errorCode that should be returned by the server. <br />
     *         See {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10">HTTP/1.1 documentation</a>}
     *         for list of status codes. Additional status codes can be added by applications
     *         by creating an implementation of {@link Response.StatusType}.
     * @param message interpolated error message.
     * @return the error message
     */
    public static Builder with(Response.Status status, String message) {
        return new Builder(status, message);
    }


    /**
     * @return message id
     */
    public String getId() {
        return id;
    }

    /**
     * @return The HTTP Status errorCode that should be returned by the server. <br />
     *         See {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10">HTTP/1.1 documentation</a>}
     *         for list of status codes. Additional status codes can be added by applications
     *         by creating an implementation of {@link Response.StatusType}.
     */
    public int getResponseStatusCode() {
        return responseStatusCode;
    }

    /**
     * @return application specific error errorCode
     */
    public Integer getErrorCode() {
        return errorCode;
    }

    /**
     * @return the exceptionClass
     */
    public String getExceptionClass() {
        return exceptionClass;
    }
    /**
     * @return interpolated error message.
     */
    public String getMessage() {
        return message;
    }

    /**
     * @return non-interpolated error message.
     */
    public String getMessageTemplate() {
        return messageTemplate;
    }

    /**
     * @return path to resource method
     */
    public String getLocation() {
        return location;
    }

    /**
     * @return stack trace caused by the exceptionClass
     */
    public String getStackTrace() {
        return stackTrace;
    }

    /**
     * @return a list of {@code ConstraintViolationMessage} messages.
     */
    public List<ConstraintViolationMessage> getConstraintViolationMessages() {
        return constraintViolationMessages;
    }


    /**
     * @return a JSON formatted, pretty print, representation of the error message
     */
    @Override
    public String toString() {
        try {
            return new GsonBuilder().setPrettyPrinting().create().toJson(this);
        }
        catch (Exception e) {
            return "Marshalling failed with message: " + e.getMessage() +
                    "Fallback {" +
                    "id='" + id + '\'' +
                    ", responseStatusCode=" + responseStatusCode +
                    ", message='" + message + '\'' +
                    '}';
        }
    }


    // --------------------------
    //
    // --------------------------

    public static class Builder {
        private int responseStatusCode;
        private Integer errorCode;
        private String exceptionClass;
        private String message;
        private String messageTemplate;
        private String location;
        private String stackTrace;
        private List<ConstraintViolationMessage> constraintViolationMessages;

        private Builder(Throwable t, UriInfo uriInfo) {

            exceptionClass = t.getClass().getName();
            message = t.getMessage();
            stackTrace = Throwables.getStackTraceAsString(t);
            location = uriInfo.getAbsolutePath().toString();

            responseStatusCode = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();

            if(t instanceof ConstraintViolationException) {
                // Bean validation
                responseStatusCode = Response.Status.BAD_REQUEST.getStatusCode();
            }
            else if (t instanceof EntityNotFoundException) {
                // JPA
                responseStatusCode = Response.Status.NOT_FOUND.getStatusCode();
            }
            else if (t instanceof EntityExistsException) {
                // JPA
                responseStatusCode = Response.Status.CONFLICT.getStatusCode();
            }
            else if(t instanceof WebApplicationException) {
                // WebApplicationException, ... and sub classes
                responseStatusCode = ((WebApplicationException) t).getResponse().getStatus();
            }

            //-----------------------------------------------------
            // Add application spesific exceptions as needed, e.g:
            //else if (t instanceof SomeApplicationException) {
            //    responseStatusCode = ((ApplicationException) t).getResponseStatusCode();
            //    errorCode = ((ApplicationException) t).getErrorCode();
            //    messageTemplate = ((ApplicationException) t).getMessageTemplate();
            //}
            //-----------------------------------------------------


            // Look for constraint violations in this exceptionClass or in exceptionClass cause
            ConstraintViolationException cve = t instanceof ConstraintViolationException
                    ? (ConstraintViolationException)t
                    : t.getCause() instanceof ConstraintViolationException
                            ? (ConstraintViolationException)t.getCause()
                            : null;

            if(cve != null) {
                if(MoreObjects.firstNonNull(message, "").trim().length() < 1) {
                    message = "Bean Validation constraint(s) violated.";
                }
                constraintViolationMessages = createConstraintViolationMessages(cve.getConstraintViolations());
            }
        }

        private Builder(Response.Status status, String message) {
            this.responseStatusCode = status.getStatusCode();
            this.message = message;
        }


        public Builder errorCode(Integer errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        public Builder exceptionClass(String exceptionClass) {
            this.exceptionClass = exceptionClass;
            return this;
        }

        public Builder messageTemplate(String messageTemplate) {
            this.messageTemplate = messageTemplate;
            return this;
        }

        public Builder location(String location) {
            this.location = location;
            return this;
        }

        public Builder stackTrace(String stackTrace) {
            this.stackTrace = stackTrace;
            return this;
        }

        public Builder constraintViolations(Set<ConstraintViolation<?>> constraintViolations) {
            constraintViolationMessages = createConstraintViolationMessages(constraintViolations);
            return this;
        }

        private static List<ConstraintViolationMessage> createConstraintViolationMessages(
                Set<ConstraintViolation<?>> constraintViolations) {

            List<ConstraintViolationMessage> result = Lists.newArrayList();
            if(constraintViolations != null) {
                for (ConstraintViolation<?> constraintViolation : constraintViolations) {
                    result.add(new ConstraintViolationMessage(
                                    constraintViolation.getMessage(),
                                    constraintViolation.getMessageTemplate(),
                                    constraintViolation.getPropertyPath(),
                                    constraintViolation.getInvalidValue()));
                }
            }
            return result;
        }

        public ErrorMessage build() {
            return new ErrorMessage(this);
        }
    }
}
