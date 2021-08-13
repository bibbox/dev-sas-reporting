FROM java:8 

RUN echo "deb [check-valid-until=no] http://archive.debian.org/debian jessie-backports main" > /etc/apt/sources.list.d/jessie-backports.list
RUN sed -i '/deb http:\/\/deb.debian.org\/debian jessie-updates main/d' /etc/apt/sources.list

# Install maven
#RUN apt-get update
RUN apt-get -o Acquire::Check-Valid-Until=false update
RUN apt-get install -y maven

WORKDIR /code

RUN wget "https://jaspersoft.jfrog.io/artifactory/third-party-ce-artifacts/org/olap4j/olap4j/0.9.7.309-JS-3/olap4j-0.9.7.309-JS-3.jar"
RUN ["mvn", "install:install-file", "-Dfile=olap4j-0.9.7.309-JS-3.jar", "-DgroupId=org.olap4j", "-DartifactId=olap4j", "-Dversion=0.9.7.309-JS-3", "-Dpackaging=jar"]


# Prepare by downloading dependencies
ADD pom.xml /code/pom.xml
RUN ["mvn", "dependency:resolve"]
RUN ["mvn", "verify"]

# Adding source, compile and package into a fat jar
ADD src /code/src
RUN ["mvn", "package"]

EXPOSE 4567
CMD ["/usr/lib/jvm/java-8-openjdk-amd64/bin/java", "-jar", "target/sasreporting-jar-with-dependencies.jar"]
