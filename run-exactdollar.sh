mvn compile exec:java \
      -Dexec.mainClass=com.google.ce.fukudak.ExactDollarRides -e \
      -Dexec.args="--project=$PROJECT \
      --sinkProject=$PROJECT \
      --stagingLocation=$SRAGING \
      --runner=DataflowPipelineRunner \
      --streaming=true \
      --numWorkers=3 \
      --zone=asia-east1-a \
      --sinkTopic=exactdollar-visualizer"

