ARG APP_INSIGHTS_AGENT_VERSION=3.2.10
FROM hmctspublic.azurecr.io/base/java:17-distroless

COPY build/libs/fis-hmc-api.jar /opt/app/

EXPOSE 4045
CMD [ "fis-hmc-api.jar" ]
