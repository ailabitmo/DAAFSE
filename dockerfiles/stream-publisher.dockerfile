FROM ubuntu

ENV http_proxy http://proxy.ifmo.ru:3128
ENV https_proxy http://proxy.ifmo.ru:3128
ENV DEBIAN_FRONTEND noninteractive

WORKDIR /root

RUN apt-get update
RUN apt-get install -y wget binutils java-common

RUN echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections

ADD oracle-java8-installer_8u5-1~webupd8~3_all.deb /root/oracle-java8-installer_8u5-1~webupd8~3_all.deb
RUN dpkg -i oracle-java8-installer_8u5-1~webupd8~3_all.deb

RUN apt-get update
RUN apt-get install -y maven git

RUN rm /usr/share/maven/conf/settings.xml
ADD settings.xml /usr/share/maven/conf/settings.xml

RUN git clone https://github.com/ailabitmo/DAAFSE.git
RUN cd DAAFSE/stream-publisher/ && mvn clean install -DskipTests=true
ADD server.log /root/DAAFSE/stream-publisher/server.log
