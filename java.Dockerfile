#Dockerfile

FROM openjdk:11-jdk-stretch

WORKDIR /

EXPOSE 9002

COPY dist/swim-transit-3.10.0 /app/swim-transit-3.10.0/
COPY dist/ui/ /app/swim-transit-3.10.0/ui

WORKDIR /app/swim-transit-3.10.0/bin
ENTRYPOINT ["./swim-transit"]
