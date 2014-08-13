# Use latest Fedora image as the base
FROM fedora

ENV http_proxy http://proxy.ifmo.ru:3128
ENV https_proxy http://proxy.ifmo.ru:3128

# xmlstarlet is useful when modifying attributes/elements
# saxon can be used to execute configuration transformation using XSLT
# augeas is a great tool to edit any configuration files (XML too)
RUN yum -y install java-1.8.0-openjdk-headless xmlstarlet saxon augeas git maven

# Clean the metadata
RUN yum clean all

# Set the WILDFLY_VERSION env variable
ENV WILDFLY_VERSION 8.1.0.Final

# Add the WildFly distribution to /opt
RUN cd /opt && curl http://download.jboss.org/wildfly/$WILDFLY_VERSION/wildfly-$WILDFLY_VERSION.tar.gz | tar zx

# Make sure the distribution is available from a well-known place
RUN ln -s /opt/wildfly-$WILDFLY_VERSION /opt/wildfly

# Set the JBOSS_HOME env variable
ENV JBOSS_HOME /opt/wildfly

# Create the wildfly user and group
RUN groupadd -r wildfly -g 433 && useradd -u 431 -r -g wildfly -d /opt/wildfly -s /sbin/nologin -c "WildFly user" -p $(openssl passwd -1 wildfly)  wildfly

# Change the owner of the /opt/wildfly directory
RUN chown -R wildfly:wildfly /opt/wildfly*

ADD settings.xml /usr/share/maven/conf/settings.xml
ADD deploy.sh	/root/deploy.sh
RUN chmod 777 deploy.sh

#################################################
# Build and deploy the application

RUN git clone https://github.com/ailabitmo/DAAFSE.git

#
#################################################

# Expose the ports we're interested in
EXPOSE 8080 9990

# Run everything below as the wildfly user
USER wildfly



