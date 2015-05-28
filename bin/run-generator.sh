#!/bin/bash

set -e

spark-submit --master yarn-client --num-executors 32 --executor-memory 8G --class name.szul.edu.kaggle.facebook.FeatureGenerator target/scala-2.10/kaggle-facebook-assembly-1.0.jar

