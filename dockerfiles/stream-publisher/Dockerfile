FROM fedora

WORKDIR /root

RUN yum -y upgrade
RUN yum install -y wget java-1.8.0-openjdk-devel unzip maven git
RUN yum clean all

RUN rm /usr/share/maven/conf/settings.xml
ADD settings.xml /usr/share/maven/conf/settings.xml

RUN git clone https://github.com/ailabitmo/DAAFSE.git
RUN cd DAAFSE/stream-publisher/ && mvn clean install -DskipTests=true

RUN wget https://dl.dropboxusercontent.com/u/1497018/daafse/server.log.zip -O /root/DAAFSE/stream-publisher/server.log.zip
RUN cd /root/DAAFSE/stream-publisher/ && unzip server.log.zip -d .
