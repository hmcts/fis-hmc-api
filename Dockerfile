ARG APP_INSIGHTS_AGENT_VERSION=3.7.1

# Application image
FROM hmctsprod.azurecr.io/base/java:21-distroless

# Change to non-root privilege
USER hmcts

COPY lib/AI-Agent.xml /opt/app/
COPY lib/applicationinsights.json /opt/app/
COPY build/libs/fis-hmc-api.jar /opt/app/

EXPOSE 4550
CMD [ "fis-hmc-api.jar" ]

CMD [ \
"--add-opens", "java.base/java.lang=ALL-UNNAMED", \
"fis-hmc-api.jar" \
]

HEALTHCHECK --interval=30s --timeout=15s --start-period=60s --retries=3 \
    CMD wget -q --spider localhost:4550/health || exit 1
