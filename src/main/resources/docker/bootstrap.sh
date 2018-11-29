#!/bin/sh


java $JAVA_OPPS  -cp /opt/siddhi/*:. "$MAIN_CLASS" "$CONSUME" "$PUBLISH" "$DATA1" "$DATA2"