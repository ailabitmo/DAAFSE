FROM ubuntu

RUN echo "#!/bin/sh\nexit 0" > /usr/sbin/policy-rc.d && chmod 777 /usr/sbin/policy-rc.d
RUN apt-get update
RUN apt-get install -y rabbitmq-server

RUN /usr/sbin/rabbitmq-plugins enable rabbitmq_management
RUN /usr/sbin/rabbitmq-plugins enable rabbitmq_web_stomp
RUN echo "[{rabbit, [{loopback_users, []}]}]." > /etc/rabbitmq/rabbitmq.config

CMD /usr/sbin/rabbitmq-server

EXPOSE 5672 15674 15672 4369
