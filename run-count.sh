mvn compile exec:java \
      -Dexec.mainClass=com.google.ce.fukudak.CountRides -e \
      -Dexec.args="--project=$PROJECT \
      --sinkProject=$PROJECT \
      --stagingLocation=$STAGING \
      --runner=DataflowPipelineRunner \
      --streaming=true \
      --numWorkers=3 \
      --zone=asia-east1-a \
      --sinkTopic=count-visualizer"

