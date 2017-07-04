mvn compile exec:java \
      -Dexec.mainClass=com.google.ce.fukudak.AllRides -e \
      -Dexec.args="--project=fukudak-dflab \
      --sinkProject=fukudak-dflab \
      --stagingLocation=gs://fukudak-dflab.appspot.com/staging/ \
      --runner=DataflowPipelineRunner \
      --streaming=true \
      --numWorkers=3 \
      --zone=asia-northeast1-a"

