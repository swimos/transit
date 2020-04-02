#Dockerfile

FROM openjdk:11-jre-stretch

WORKDIR /

EXPOSE 9002

COPY dist/swim-transit-3.10.2 /app/swim-transit-3.10.2/
COPY dist/ui/ /app/swim-transit-3.10.2/ui

WORKDIR /app/swim-transit-3.10.2/bin
ENTRYPOINT ["./swim-transit"]
