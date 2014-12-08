FROM ubuntu

ENV DEBIAN_FRONTEND noninteractive

WORKDIR /root

RUN apt-get update
RUN apt-get install -y wget binutils java-common unzip curl

RUN echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections
RUN wget https://db.tt/dFU3BqFP -O /root/oracle-java8-installer_8u5-1~webupd8~3_all.deb
RUN dpkg -i oracle-java8-installer_8u5-1~webupd8~3_all.deb

RUN apt-get update
RUN apt-get install -y maven git

ENV JAVA_HOME /usr/lib/jvm/java-8-oracle/jre

# Set the WILDFLY_VERSION env variable
ENV WILDFLY_VERSION 8.1.0.Final

# Add the WildFly distribution to /opt
RUN cd /opt && curl http://download.jboss.org/wildfly/$WILDFLY_VERSION/wildfly-$WILDFLY_VERSION.tar.gz | tar zx

# Make sure the distribution is available from a well-known place
RUN mv /opt/wildfly-$WILDFLY_VERSION /opt/wildfly

# Set the JBOSS_HOME env variable
ENV JBOSS_HOME /opt/wildfly

ADD deploy.sh /root/deploy.sh
RUN chmod 777 /root/deploy.sh

#ADD settings.xml /usr/share/maven/conf/settings.xml

#################################################
# Build and deploy the application

RUN git clone https://github.com/ailabitmo/DAAFSE.git /root/DAAFSE

#
#################################################

# Expose the ports we're interested in
EXPOSE 8080 9990

CMD ./deploy.sh redeploy && ./deploy.sh run
