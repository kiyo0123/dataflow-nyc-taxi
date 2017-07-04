package com.google.ce.fukudak;

import com.google.api.services.bigquery.model.TableFieldSchema;
import com.google.api.services.bigquery.model.TableRow;
import com.google.api.services.bigquery.model.TableSchema;
import com.google.ce.fukudak.common.CustomPipelineOptions;
import com.google.cloud.dataflow.sdk.Pipeline;
import com.google.cloud.dataflow.sdk.coders.TableRowJsonCoder;
import com.google.cloud.dataflow.sdk.io.BigQueryIO;
import com.google.cloud.dataflow.sdk.io.PubsubIO;
import com.google.cloud.dataflow.sdk.options.PipelineOptionsFactory;
import com.google.cloud.dataflow.sdk.transforms.DoFn;
import com.google.cloud.dataflow.sdk.transforms.MapElements;
import com.google.cloud.dataflow.sdk.transforms.ParDo;
import com.google.cloud.dataflow.sdk.values.PCollection;
import com.google.cloud.dataflow.sdk.values.TypeDescriptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Dataflow command-line options must be specified:
 * --project=<your project ID>
 * --sinkProject=<your project ID>
 * --stagingLocation=gs://<your staging bucket>
 * --runner=DataflowPipelineRunner
 * --streaming=true
 * --numWorkers=3
 * --zone=europe-west1-c
 * You can launch the pipeline from the command line using:
 * mvn exec:java -Dexec.mainClass="dataflowlab.AllRides" -e -Dexec.args="<your arguments>"
 */

// input data format
// {"ride_id":"736c52b7-675f-42c7-95b7-3486d29bef11",
//	"point_idx":389,
//  "latitude":40.75985000000024,
//  "longitude":-74.00301000000033,
//  "timestamp":"2017-02-06T04:30:27.10659-05:00",
//  "meter_reading":9.510703,
//  "meter_increment":0.024449108,
//  "ride_status":"enroute",
//  "passenger_count":6}


@SuppressWarnings("serial")
public class AllRidesBQ {
  private static final Logger LOG = LoggerFactory.getLogger(AllRidesBQ.class);

  /**
   * Helper method to build the table schema for the output table.
   */
  private static TableSchema buildTaxiSchema() {
    List<TableFieldSchema> fields = new ArrayList<>();
    fields.add(new TableFieldSchema().setName("ride_id").setType("STRING"));
    fields.add(new TableFieldSchema().setName("point_idx").setType("INTEGER"));
    fields.add(new TableFieldSchema().setName("latitude").setType("FLOAT"));
    fields.add(new TableFieldSchema().setName("longitude").setType("FLOAT"));
    fields.add(new TableFieldSchema().setName("timestamp").setType("TIMESTAMP"));
    fields.add(new TableFieldSchema().setName("meter_reading").setType("FLOAT"));
    fields.add(new TableFieldSchema().setName("meter_increment").setType("FLOAT"));
    fields.add(new TableFieldSchema().setName("ride_status").setType("STRING"));
    fields.add(new TableFieldSchema().setName("passenger_count").setType("INTEGER"));
    TableSchema schema = new TableSchema().setFields(fields);
    return schema;
  }

  private static class FilterAllRides extends DoFn<TableRow, TableRow> {
    FilterAllRides() {}

    @Override
    public void processElement(ProcessContext c) {
      TableRow ride = c.element();

      // Access to data fields:
      String ride_id = ride.get("ride_id").toString();
      String timestamp = ride.get("timestamp").toString();
      int passenger_count = Integer.parseInt(ride.get("passenger_count").toString());
      float lat = Float.parseFloat(ride.get("latitude").toString());
      float lon = Float.parseFloat(ride.get("longitude").toString());
      c.output(ride);
    }
  }

  private static class DropoffFilter extends DoFn<TableRow, TableRow> {
    DropoffFilter() {}

    @Override
    public void processElement(ProcessContext c) {
      TableRow ride = c.element();
      String status = ride.get("ride_status").toString();
      if (status.equals("dropoff")) {
        c.output(ride);
      } else {
        LOG.info("Status is not dropoff. filtered out");
      }

    }
  }

  private static class PickupFilter extends DoFn<TableRow, TableRow> {
    PickupFilter() {}

    @Override
    public void processElement(ProcessContext c) {
      TableRow ride = c.element();
      String status = ride.get("ride_status").toString();
      if (status.equals("pickup")) {
        c.output(ride);
      } else {
        LOG.info("Status is not pickup. filtered out");
      }

    }
  }

/*
  private static String getTableName(String baseTableName) {
    final DateTimeFormatter FORMATTER = DateTimeFormat.forPattern("yyyyMMdd").withZone(DateTimeZone.UTC);

    // This code generates a valid BigQuery partition name:
    Instant instant = Instant.now(); // any Joda instant in a reasonable time range
    //String baseTableName = options.getDropoffTable(); // a valid BigQuery table name
    //String partitionName = String.format("%s$%s", baseTableName, FORMATTER.print(instant));
    String partitionName = String.format("%s_%s", baseTableName, FORMATTER.print(instant));
    return partitionName;
  }
*/

  public static void main(String[] args) {
    CustomPipelineOptions options = PipelineOptionsFactory.fromArgs(args).withValidation().as(CustomPipelineOptions.class);
    TableSchema schema = buildTaxiSchema();
    Pipeline p = Pipeline.create(options);

    // reading from inputs
    PCollection<TableRow> inputs;
    inputs = p.apply(PubsubIO.Read.named("read from PubSub")
        .topic("projects/"+options.getSourceProject()+"/topics/"+options.getSourceTopic())
        .timestampLabel("ts")
        .withCoder(TableRowJsonCoder.of()));

    // apply filter
    PCollection<TableRow> filtered1;
    filtered1 = inputs.apply("Filter1", ParDo.of(new FilterAllRides()));

    // apply filter
    PCollection<TableRow> filtered3;
    filtered3 = inputs.apply("Pass Through Filter (do nothing)",
        MapElements.via((TableRow e) -> e).withOutputType(TypeDescriptor.of(TableRow.class)));

    // apply drop off filter
    PCollection<TableRow> dropoffRide = filtered1.apply("Dropoff Ride", ParDo.of(new DropoffFilter()));

    // apply drop off filter
    PCollection<TableRow> pickupRide = filtered1.apply("Pickup Ride", ParDo.of(new PickupFilter()));

    // write down to bigquery
    filtered1.apply(BigQueryIO.Write.named("BigQuery Write (all)")
        .to(options.getOutputTable())
        .withSchema(schema)
        .withWriteDisposition(BigQueryIO.Write.WriteDisposition.WRITE_APPEND)
        .withCreateDisposition(BigQueryIO.Write.CreateDisposition.CREATE_IF_NEEDED));


    // write down to filtered bigquery table
    dropoffRide.apply(BigQueryIO.Write.named("BigQuery Write (dropoff)")
        .to(options.getDropoffTable())
        .withSchema(schema)
        .withWriteDisposition(BigQueryIO.Write.WriteDisposition.WRITE_APPEND)
        .withCreateDisposition(BigQueryIO.Write.CreateDisposition.CREATE_IF_NEEDED));

    // write down to filtered bigquery table
    pickupRide.apply(BigQueryIO.Write.named("BigQuery Write (pickup)")
        .to(options.getPickupTable())
        .withSchema(schema)
        .withWriteDisposition(BigQueryIO.Write.WriteDisposition.WRITE_APPEND)
        .withCreateDisposition(BigQueryIO.Write.CreateDisposition.CREATE_IF_NEEDED));


    //write down to pubsub
    filtered3.apply(PubsubIO.Write.named("Write to PubSub for Visualizer")
        .topic("projects/"+options.getSinkProject()+"/topics/"+options.getSinkTopic())
        .withCoder(TableRowJsonCoder.of()));

    p.run();
  }
}

