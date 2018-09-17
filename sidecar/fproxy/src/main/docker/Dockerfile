FROM ubuntu:14.04

ARG MICRO_HOME=/opt/app/fproxy
ARG BIN_HOME=$MICRO_HOME/bin
ARG JAR_FILE=fproxy-exec.jar

RUN apt-get update

# Install and setup java8
RUN apt-get update && apt-get install -y software-properties-common
## sudo -E is required to preserve the environment. If you remove that line, it will most like freeze at this step
RUN sudo -E add-apt-repository ppa:openjdk-r/ppa && apt-get update && apt-get install -y openjdk-8-jdk

RUN sudo dpkg --purge --force-depends ca-certificates-java
RUN sudo apt-get install ca-certificates-java

## Setup JAVA_HOME, this is useful for docker commandline
ENV JAVA_HOME usr/lib/jvm/java-8-openjdk-$(dpkg --print-architecture)
RUN export JAVA_HOME

# Build up the deployment folder structure
RUN mkdir -p $MICRO_HOME
COPY ${JAR_FILE} $MICRO_HOME
RUN mkdir -p $BIN_HOME
COPY *.sh $BIN_HOME
RUN chmod 755 $BIN_HOME/*
RUN ln -s /logs $MICRO_HOME/logs
RUN mkdir /logs
# Create the appuser
RUN groupadd -r appgroup && \
    useradd -r -u 1001 -g appgroup appuser && \
    chown -R appuser:appgroup $MICRO_HOME && \
    chmod 777 /logs
USER appuser
CMD ["/opt/app/fproxy/bin/start.sh"]
