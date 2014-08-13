FROM ubuntu

ENV http_proxy http://proxy.ifmo.ru:3128
ENV https_proxy http://proxy.ifmo.ru:3128
ENV DEBIAN_FRONTEND noninteractive

WORKDIR /root

RUN apt-get update
RUN apt-get install -y wget binutils java-common unzip

RUN echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections

RUN wget https://db.tt/dFU3BqFP -O /root/oracle-java8-installer_8u5-1~webupd8~3_all.deb
RUN dpkg -i oracle-java8-installer_8u5-1~webupd8~3_all.deb

RUN apt-get update
RUN apt-get install -y maven git

RUN rm /usr/share/maven/conf/settings.xml
ADD settings.xml /usr/share/maven/conf/settings.xml

RUN git clone https://github.com/ailabitmo/DAAFSE.git
RUN cd DAAFSE/stream-publisher/ && mvn clean install -DskipTests=true

RUN wget https://dl.dropboxusercontent.com/u/1497018/daafse/server.log.zip -O /root/DAAFSE/stream-publisher/server.log.zip
RUN cd /root/DAAFSE/stream-publisher/ && unzip server.log.zip -d .

RUN ls -al /root && ls -al /root/DAAFSE && ls -al /root/DAAFSE/stream-publisher/
