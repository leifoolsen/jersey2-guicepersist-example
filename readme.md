#Jersey-2, Guice Persist, Embedded Jetty
A project demonstrating how to configure Google Guice with JPA in a Jersey2 (JAX-RS) container. This project does not 
use the Guice servlet module or the Guice persist filter - which anyway should be regarded as redundant components in a 
stateless JAX-RS container.

Typically, in a servlet environment, Guice is bootstrapped trough a ServletModule, and the HTTP request 
[Unit of Work](https://github.com/google/guice/wiki/Transactions) lifecycle is managed trough a PersistFilter.
Since a servlet filter is unnecessary in JAX-RS, one can use a combined JAX-RS ContainerRequest Response Filter to 
handle Unit of Work. Beyond that, to ensure a consistent thread safe entity manager in singletons, an 
```Provider<EntityManager>``` is injected rather than injecting the entity manager directly. 

## Prerequisites
Maven 3

##Steps to run this project
* Fork, Clone or Download ZIP
* Build project: *mvn clean install -U*
* Start Jetty from project folder: *mvn exec:java*
* Application.wadl: *http://localhost:8080/api/application.wadl*
* Example usage: *http://localhost:8080/api/users*
* Import project into your favourite IDE
* Open `UserResourceTest.java` to start exploring code

###Note
The project can be packaged with the [appassembler-maven-plugin](http://mojo.codehaus.org/appassembler/appassembler-maven-plugin/)

* Build the project with the *appassembler* profile: *mvn install -Pappassembler* 
* ... then run the app from the project folder with the following command:<br/>sh _target/appassembler/bin/startapp_
* Open a browser and hit *http://localhost:8087/api/users*
