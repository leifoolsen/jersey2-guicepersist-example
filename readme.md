#Jersey-2, Guice Persist, Embedded Jetty
Description og this project; TBD.

##Steps to run this project
* Fork, Clone or Download ZIP
* Build project: *mvn clean install -U*
* Start Jetty from project folder: *mvn exec:java*
* Application.wadl: *http://localhost:8080/api/application.wadl*
* Example usage: *http://localhost:8080/api/users*
* Import project into your favourite IDE
* Open `UserResourceTest.java` to start exploring code

###Note
You can package the project with the [appassembler-maven-plugin](http://mojo.codehaus.org/appassembler/appassembler-maven-plugin/)

* Build the project with the *appassembler* profile: *mvn install -Pappassembler* 
* ... then run the app from the project folder with the following command: <br/>sh _target/appassembler/bin/startapp_
* Open a browser and hit *http://localhost:8087/api/say/hello*
