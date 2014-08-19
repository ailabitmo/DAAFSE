# Use latest Fedora image as the base
FROM fedora

# xmlstarlet is useful when modifying attributes/elements
# saxon can be used to execute configuration transformation using XSLT
# augeas is a great tool to edit any configuration files (XML too)
RUN yum -y install java-1.8.0-openjdk-devel xmlstarlet saxon augeas git maven openssl unzip

# Clean the metadata
RUN yum clean all

RUN alternatives --install /usr/bin/java java /usr/lib/jvm/jre-1.8.0-openjdk.x86_64/bin/java 200000
RUN alternatives --install /usr/bin/javac javac /usr/lib/jvm/java-1.8.0-openjdk.x86_64/bin/javac 200000
RUN echo 1 | alternatives --config java
RUN echo 2 | alternatives --config javac

ENV JAVA_HOME /usr/lib/jvm/jre-1.8.0-openjdk.x86_64

# Set the WILDFLY_VERSION env variable
ENV WILDFLY_VERSION 8.1.0.Final

# Add the WildFly distribution to /opt
RUN cd /opt && curl http://download.jboss.org/wildfly/$WILDFLY_VERSION/wildfly-$WILDFLY_VERSION.tar.gz | tar zx

# Make sure the distribution is available from a well-known place
RUN ln -s /opt/wildfly-$WILDFLY_VERSION /opt/wildfly

# Set the JBOSS_HOME env variable
ENV JBOSS_HOME /opt/wildfly

# Create the wildfly user and group
RUN groupadd -r wildfly -g 433 && useradd -u 431 -r -g wildfly -d /home/wildfly -s /sbin/nologin -c "WildFly user" -p $(openssl passwd -1 wildfly)  wildfly

# Change the owner of the /opt/wildfly directory
RUN chown -R wildfly:wildfly /opt/wildfly*
RUN mkdir /home/wildfly
RUN chmod 777 -R /home/wildfly

WORKDIR /home/wildfly

ADD deploy.sh /home/wildfly/deploy.sh
RUN chmod 777 /home/wildfly/deploy.sh

#################################################
# Build and deploy the application

RUN git clone https://github.com/ailabitmo/DAAFSE.git /home/wildfly/DAAFSE

#
#################################################

# Expose the ports we're interested in
EXPOSE 8080 9990

# Run everything below as the wildfly user
#USER wildfly



