mvn compile exec:java \
      -Dexec.mainClass=com.google.ce.fukudak.PickupRides -e \
      -Dexec.args="--project=$PRPOJECT \
      --sinkProject=$PROJECT \
      --stagingLocation=$STAGING \
      --runner=DataflowPipelineRunner \
      --streaming=true \
      --numWorkers=3 \
      --zone=asia-east1-a \
      --sinkTopic=pickup-visualizer"

