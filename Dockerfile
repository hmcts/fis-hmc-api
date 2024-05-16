ARG APP_INSIGHTS_AGENT_VERSION=3.2.6
FROM hmctspublic.azurecr.io/base/java:17-distroless

COPY lib/AI-Agent.xml /opt/app/
COPY lib/applicationinsights.json /opt/app/
COPY build/libs/fis-hmc-api.jar /opt/app/

EXPOSE 4550
CMD [ "fis-hmc-api.jar" ]
