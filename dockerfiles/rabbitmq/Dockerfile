FROM ubuntu

RUN echo "#!/bin/sh\nexit 0" > /usr/sbin/policy-rc.d && chmod 777 /usr/sbin/policy-rc.d
RUN apt-get update
RUN apt-get install -y rabbitmq-server

EXPOSE 5672 15674
