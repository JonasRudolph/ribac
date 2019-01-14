FROM openjdk:10
WORKDIR /usr/ribac
EXPOSE 8080
EXPOSE 5005

# Add Maven dependencies (not shaded into the artifact; Docker-cached)
ADD target/lib lib/

# Add the service itself
ADD target/right-based-access-control.jar ribac.jar

ENTRYPOINT [                                                                \
    "java",                                                                 \
    "-Djava.net.preferIPv4Stack=true",                                      \
    "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", \
    "-jar", "ribac.jar"                         \
]