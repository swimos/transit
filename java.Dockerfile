#Dockerfile

FROM openjdk:11-jre-stretch

WORKDIR /

EXPOSE 9002

COPY dist/swim-transit-3.11.0-SNAPSHOT /app/swim-transit-3.11.0-SNAPSHOT/
COPY dist/ui/ /app/swim-transit-3.11.0-SNAPSHOT/ui

WORKDIR /app/swim-transit-3.11.0-SNAPSHOT/bin
ENTRYPOINT ["./swim-transit"]
