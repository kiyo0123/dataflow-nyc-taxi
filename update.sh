mvn compile exec:java \
      -Dexec.mainClass=com.google.ce.fukudak.AllRidesBQ -e \
      -Dexec.args="--project=$PROJECT \
      --update \
      --jobName=$JOBNAME \
      --sinkProject=$PROJECT \
      --stagingLocation=$STAGING \
      --runner=DataflowPipelineRunner \
      --streaming=true \
      --autoscalingAlgorithm=THROUGHPUT_BASED \
      --maxNumWorkers=10 \
      --zone=asia-east1-a \
      --outputTable=fukudak-dflab:nyc_taxi.output \
      --dropoffTable=fukudak-dflab:nyc_taxi.dropoff \
      --pickupTable=fukudak-dflab:nyc_taxi.pickup \
      --sinkTopic=visualizer"

