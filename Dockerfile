FROM openjdk:8-jre-alpine

ENV APPLICATION_USER ktor
ENV BASIC_AUTH_EVENT_PLANNER_COGNITO ${BASIC_AUTH_EVENT_PLANNER_COGNITO}

RUN adduser -D -g '' $APPLICATION_USER

RUN mkdir /app
RUN chown -R $APPLICATION_USER /app

COPY ./certificates/event-planner-cognito.cer /app/certificates/event-planner-cognito.cer
COPY ./certificates/cognito-idp.eu-central-1.amazonaws.com.cer /app/certificates/cognito-idp.eu-central-1.amazonaws.com.cer

RUN $JAVA_HOME/bin/keytool -import -alias event-planner-cognito -file  /app/certificates/event-planner-cognito.cer -keystore $JAVA_HOME/lib/security/cacerts -storepass changeit -noprompt && \
    $JAVA_HOME/bin/keytool -import -alias cognito -file  /app/certificates/cognito-idp.eu-central-1.amazonaws.com.cer -keystore $JAVA_HOME/lib/security/cacerts -storepass changeit -noprompt

USER $APPLICATION_USER

COPY ./build/libs/event-planner-service.jar /app/event-planner-service.jar
WORKDIR /app


CMD ["java", "-server", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseCGroupMemoryLimitForHeap", "-XX:InitialRAMFraction=2", "-XX:MinRAMFraction=2", "-XX:MaxRAMFraction=2", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=100", "-XX:+UseStringDeduplication", "-jar", "event-planner-service.jar"]