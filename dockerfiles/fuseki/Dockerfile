FROM ubuntu

ENTRYPOINT ["/bin/bash"]

ENV http_proxy http://proxy.ifmo.ru:3128
ENV DEBIAN_FRONTEND noninteractive

WORKDIR /root

RUN echo "#!/bin/sh\nexit 0" > /usr/sbin/policy-rc.d && chmod 777 /usr/sbin/policy-rc.d
RUN apt-get update
RUN apt-get install -y wget openjdk-7-jre-headless unzip
RUN wget http://apache-mirror.rbc.ru/pub/apache/jena/binaries/jena-fuseki-1.1.0-distribution.zip
RUN unzip jena-fuseki-1.1.0-distribution.zip -d .

ADD config.ttl /root/jena-fuseki-1.1.0/config.ttl
ADD start.sh /root/start.sh

RUN chmod 777 /root/start.sh
