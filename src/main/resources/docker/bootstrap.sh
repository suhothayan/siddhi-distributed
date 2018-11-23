#!/bin/sh


java -cp /opt/siddhi/*:. "$MAIN_CLASS" "$CONSUME" "$PUBLISH" "$DATA1" "$DATA2"