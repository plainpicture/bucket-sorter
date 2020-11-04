FROM alpine:3.12.1

RUN apk --no-cache add git=2.26.2-r0 maven=3.6.3-r0 openjdk8=8.252.09-r0
COPY . /usr/local/src/bucket-sorter
WORKDIR /usr/local/src/bucket-sorter
RUN mvn clean package -DskipTests
CMD ["sh", "start.sh"]
EXPOSE 19400
