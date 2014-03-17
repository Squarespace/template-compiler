#!/bin/bash

mvn clean release:clean release:prepare release:perform -B 2>&1 \
    | tee release.log

