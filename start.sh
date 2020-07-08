#!/bin/bash
nohup redis-server &
java -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -XX:MaxRAMFraction=1 -XshowSettings:vm -jar peppol-processor.jar