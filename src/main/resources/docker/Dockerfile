FROM openjdk:8-jre-alpine
# COPY target/helloworld-1.0-SNAPSHOT.jar /opt/helloworld/helloworld-1.0-SNAPSHOT.jar
# Set up env variables
# CMD java -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap \
#  -cp /opt/helloworld/helloworld-1.0-SNAPSHOT.jar org.examples.java.App
# CMD java -version

# COPY ../../../target/siddhi-distributed-1.0-SNAPSHOT.jar /opt/siddhi/siddhi-distributed-1.0-SNAPSHOT.jar
ADD lib/* /opt/siddhi/

ADD bootstrap.sh /etc/bootstrap.sh
RUN chown root:root /etc/bootstrap.sh
RUN chmod 700 /etc/bootstrap.sh

# CMD java -version
#CMD java -cp opt/siddhi/* org.suho.siddhi.window.length.EventConsumer

ENV MAIN_CLASS ""
ENV CONSUME "-"
ENV PUBLISH "-"
ENV DATA1 "-"
ENV DATA2 "-"
ENV JAVA_OPPS ""

ENTRYPOINT ["/etc/bootstrap.sh"]

