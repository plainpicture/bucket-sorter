#!/bin/sh

/usr/bin/java -Xmx512m -jar target/bucket-sorter-1.0.0-jar-with-dependencies.jar 2>&1 | /usr/bin/tee -a log/bucket-sorter.log
