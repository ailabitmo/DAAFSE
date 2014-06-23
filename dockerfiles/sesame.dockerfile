#
# Docker image with OpenRDF Sesame 2.7.11 and Tomcat 7.
#
# Listening ports: 8080
#
# To run in detached mode:
# $ sudo docker run -d -p 8080:8080 -t -i kolchinmax:sesame
# $ /etc/init.d/tomtact7 restart
# $ Press Ctrl+P and Ctrl+Q
#

FROM ubuntu

ENTRYPOINT ["/bin/bash"]

# ENV http_proxy http://proxy.ifmo.ru:3128
ENV DEBIAN_FRONTEND noninteractive

WORKDIR /root

RUN echo "#!/bin/sh\nexit 0" > /usr/sbin/policy-rc.d && chmod 777 /usr/sbin/policy-rc.d
RUN apt-get update
RUN apt-get install -y wget tomcat7 unzip
RUN wget http://downloads.sourceforge.net/project/sesame/Sesame%202/2.7.11/openrdf-sesame-2.7.11-sdk.zip && unzip openrdf-sesame-2.7.11-sdk.zip -d .
RUN cp openrdf-sesame-2.7.11/war/* /var/lib/tomcat7/webapps/
RUN rm -r openrdf-sesame-2.7.11-sdk.zip openrdf-sesame-2.7.11
RUN mkdir /usr/share/tomcat7/.aduna && chmod -R 777 /usr/share/tomcat7/.aduna
