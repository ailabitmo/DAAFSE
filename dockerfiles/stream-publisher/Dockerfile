FROM fedora

WORKDIR /root

RUN yum install -y wget java-1.8.0-openjdk-devel unzip maven git
RUN yum clean all

RUN alternatives --install /usr/bin/java java /usr/lib/jvm/jre-1.8.0-openjdk.x86_64/bin/java 200000
RUN alternatives --install /usr/bin/javac javac /usr/lib/jvm/java-1.8.0-openjdk.x86_64/bin/javac 200000
RUN echo 1 | alternatives --config java
RUN echo 2 | alternatives --config javac

ENV JAVA_HOME /usr/lib/jvm/jre-1.8.0-openjdk.x86_64

RUN git clone https://github.com/ailabitmo/DAAFSE.git

RUN wget https://dl.dropboxusercontent.com/u/1497018/daafse/server.log.zip -O /root/DAAFSE/stream-publisher/server.log.zip
RUN cd /root/DAAFSE/stream-publisher/ && unzip server.log.zip -d .

RUN cd DAAFSE/stream-publisher/ && mvn clean install -DskipTests=true

CMD cd DAAFSE/stream-publisher/ && java -jar target/stream-publisher-1.0-SNAPSHOT-jar-with-dependencies.jar server.log "TTL"
