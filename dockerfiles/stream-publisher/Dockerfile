FROM fedora

WORKDIR /root

RUN yum install -y wget java-1.8.0-openjdk-devel unzip maven git
RUN yum clean all

RUN alternatives --install /usr/bin/java java /usr/lib/jvm/jre-1.8.0-openjdk.x86_64/bin/java 200000
RUN alternatives --install /usr/bin/javac javac /usr/lib/jvm/java-1.8.0-openjdk.x86_64/bin/javac 200000
RUN alternatives --display java
RUN alternatives --display javac
RUN java -version
RUN javac -version

RUN git clone https://github.com/ailabitmo/DAAFSE.git
RUN cd DAAFSE/stream-publisher/ && mvn clean install -DskipTests=true

RUN wget https://dl.dropboxusercontent.com/u/1497018/daafse/server.log.zip -O /root/DAAFSE/stream-publisher/server.log.zip
RUN cd /root/DAAFSE/stream-publisher/ && unzip server.log.zip -d .
