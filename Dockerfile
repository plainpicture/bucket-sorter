FROM alpine

RUN apk update && apk add git maven openjdk8
RUN git clone https://github.com/plainpicture/bucket-sorter.git && cd bucket-sorter && mvn clean package -DskipTests
CMD cd bucket-sorter && sh start.sh
EXPOSE 19400
