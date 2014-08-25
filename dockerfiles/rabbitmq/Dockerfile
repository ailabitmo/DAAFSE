FROM ubuntu

ENV DEBIAN_FRONTEND noninteractive

RUN echo "#!/bin/sh\nexit 0" > /usr/sbin/policy-rc.d && chmod 777 /usr/sbin/policy-rc.d
RUN apt-get update
RUN apt-get install -y rabbitmq-server

RUN /usr/sbin/rabbitmq-plugins enable rabbitmq_management
RUN /usr/sbin/rabbitmq-plugins enable rabbitmq_web_stomp
RUN echo "[{rabbit, [{loopback_users, []}]}]." > /etc/rabbitmq/rabbitmq.config

EXPOSE 5672 15674 15672 4369

CMD ["/usr/sbin/rabbitmq-server"]
