# AcmeAir MicroServices Springboot

### Usage Instructions

##### Clone Git Repos

    git clone https://github.com/BluePerf/acmeair-mainservice-springboot.git
    git clone https://github.com/BluePerf/acmeair-authservice-springboot.git
    git clone https://github.com/BluePerf/acmeair-bookingservice-springboot.git
    git clone https://github.com/BluePerf/acmeair-customerservice-springboot.git
    git clone https://github.com/BluePerf/acmeair-flightservice-springboot.git

##### Building the application

  
	  cd <acmeair-mainservice-GIT-PATH>  
	  mvn clean package
    
      cd <acmeair-authservice-GIT-PATH>  
      mvn clean package
    
      cd <acmeair-bookingservice-GIT-PATH>  
      mvn clean package
    
      cd <acmeair-customerservice-GIT-PATH>  
      mvn clean package
    
      cd <acmeair-flightservice-GIT-PATH>  
      mvn clean package
  
## Docker Instructions

Prereq: [Install Docker, docker-compose, and start Docker daemon on your local machine](https://docs.docker.com/installation/)

1. cd acmeair-mainservice-java
2. Create docker network
 * docker network create --driver bridge my-net
3. Build/Start Containers. This will build all the micro-services, mongo db instances, and an nginx proxy.
    * docker-compose build
    * NETWORK=my-net docker-compose up

4. Go to http://docker_machine_ip/acmeair
5. Go to the Configuration Page and Load the Database
