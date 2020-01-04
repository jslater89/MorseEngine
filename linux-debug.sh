#!/bin/bash

CLASSPATH=out/production/MorseEngine:out/production/Tester:Tester/libs/tablelayout.jar

#java -server -XX:+UseCompressedOops -XX:+UseConcMarkSweepGC -XX:+TieredCompilation -agentlib:hprof=cpu=samples,depth=100,interval=1,lineno=y,thread=y,file=hprof.txt -Xms2048M -Xmx4096M -cp $CLASSPATH com/manywords/softworks/tafl/OpenTafl --debug $*

java -cp $CLASSPATH com/manywords/softworks/morse/test/Tester

#jdb -server -XX:+UseCompressedOops -XX:+UseConcMarkSweepGC -XX:+TieredCompilation -Xms2048m -Xmx4096m -sourcepath src -classpath $CLASSPATH com/manywords/softworks/tafl/OpenTafl --debug $*

#java -server -XX:+UseCompressedOops -XX:+TieredCompilation -XX:+UseConcMarkSweepGC -Xms2048m -Xmx4096m -cp $CLASSPATH com/manywords/softworks/tafl/OpenTafl --window $*
