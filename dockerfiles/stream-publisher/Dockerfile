FROM fedora:20

WORKDIR /root

ENV DAAFSE_SP_HOME /root/DAAFSE/stream-publisher

RUN yum install -y wget java-1.8.0-openjdk-devel unzip maven git
RUN yum clean all

RUN alternatives --install /usr/bin/java java /usr/lib/jvm/jre-1.8.0-openjdk.x86_64/bin/java 200000
RUN alternatives --install /usr/bin/javac javac /usr/lib/jvm/java-1.8.0-openjdk.x86_64/bin/javac 200000
RUN echo 2 | alternatives --config java
RUN echo 2 | alternatives --config javac

ENV JAVA_HOME /usr/lib/jvm/jre-1.8.0-openjdk.x86_64

RUN git clone https://github.com/ailabitmo/DAAFSE.git
RUN wget https://dl.dropboxusercontent.com/u/1497018/daafse/stream.json.zip -O /root/DAAFSE/stream-publisher/stream.json.zip

WORKDIR $DAAFSE_SP_HOME

RUN unzip stream.json.zip -d .
RUN mvn clean install -DskipTests=true

CMD java -jar target/stream-publisher-1.0-SNAPSHOT-jar-with-dependencies.jar stream.json "TTL"
