mvn compile exec:java \
      -Dexec.mainClass=com.google.ce.fukudak.AllRides -e \
      -Dexec.args="--project=$PROJECT \
      --sinkProject=$PROJECT \
      --stagingLocation=$STAGING \
      --runner=DataflowPipelineRunner \
      --streaming=true \
      --numWorkers=3 \
      --zone=asia-northeast1-a"

