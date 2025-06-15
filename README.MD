# Device maintenance

## Beggining

This is a springboot containerized application, which will run in docker and use rest apis to receive data and kafka to process and storage for the application.

### Pre requisites

You must have installed and configured in your machine:
	Java 21+
	Maven
	Docker
	git-bash terminal 


### Installing


Using git bash, clone the repository to your local machine.
On a bash terminal, run the docker login command with your credentials.
on a bash terminal move to the root path of the repository and run the script build-images.sh
	$ sh build-images.sh

If everything is correct, the script will find every subfolder which has a Dockerfile. It will move to this folder, run a mvn clean install (pom.xml should be at the same folder level as the DockerFile), then it will run the docker build and create the images in docker used in the project.

If everything is correct, run in the same directory the command.
	$ docker-compose  --profile infra up -d
	
This will start the infrastructure components of the application

 ✔ Container kafka               Running        
 ✔ Container eurekaserver        Running        
 ✔ Container configserver        Running        
 ✔ Container kafka-ui            Running        

After everything has been initialized, run the command

	$ docker-compose  --profile infra --profile app up -d
	
This will start the application images of the application.

 ✔ Container eurekaserver        Running                                 
 ✔ Container kafka               Running                                 
 ✔ Container configserver        Running                                 
 ✔ Container kafka-ui            Running                                 
 ✔ Container user-admin          Started                                 
 ✔ Container device-admin        Started                                 
 ✔ Container topicconfiguration  Started                                 
 ✔ Container webserver           Started                                 


## Executing

To access the api, after everything is running you can execute the tests, you can go to http://localhost:8084/swagger-ui/index.html
This is the device-admin application and it is the main focus of this example.

You can also access the application through the webserver application at http://localhost:8081/swagger-ui/index.html, but at this point, it is functional, but not completed.
The webserver also serves GraphQL Apis, that is exposed in the path http://localhost:8081/graphiql?path=/graphql

one example query for graphQL is:

query {
getDevices {
id
name
brand
state
creationTime
}
}


## System description


The application is composed by:
An Eureka server for service discovery
A kafka broker.
A springboot config server.
A kafka-ui to monitor kafka.
A springboot topicconfiguration application (will create the kafka topics to be used by the system).
A springboot user-admin application (not part of the scope, but mantained to show how the application would work adding more functionalities).
A springboot device-admin application (the core of what was proposed to do in the scope).
A springboot webserver application (not part of the scope, but mantained to show how the application would work adding more functionalities).

The application uses openapi specifications for creating the models and interfaces used in the system.
Those apis do not to do all the work of storing the data. They just validate and then push a message to a kafka topic.
The kafka streams application then take this message, process it and store them in state-stores.
When it is requested by a get or find api, it will get it from the state store instead of a traditional database.

No security has been implemented in the application (it is one of the TODOS, more on that in the section), but the idea is that just the webserver application would be opened to users. It's role is to control RBAC and permissions and only through it it would be possible to access the other components APIs that it calls through the eureka service discovery.
This component still need more work, specially in the handling of status codes and responses from the admin services (another TODO).


## Pending items and improvements

Apply spring security to the system.
Apply security to kafka broker
More unit tests (always more)
Create integration tests
Improve dealing with the responses in the webserver
Create a new component that listens to the kafka topics and stores data in a different relational database (used for more complex queries)


