package com.google.ce.fukudak.common;

/*
 * Copyright (C) 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

import com.google.cloud.dataflow.sdk.options.DataflowPipelineOptions;
import com.google.cloud.dataflow.sdk.options.Default;
import com.google.cloud.dataflow.sdk.options.Description;
import com.google.cloud.dataflow.sdk.options.Validation;

public interface CustomPipelineOptions extends DataflowPipelineOptions {
  @Description("ProjectId where data source topic lives")
  @Default.String("pubsub-public-data")
  @Validation.Required
  String getSourceProject();

  void setSourceProject(String value);

  @Description("TopicId of source topic")
  @Default.String("taxirides-realtime")
  @Validation.Required
  String getSourceTopic();

  void setSourceTopic(String value);

  @Description("ProjectId where data sink topic lives")
  @Validation.Required
  String getSinkProject();

  void setSinkProject(String value);

  @Description("TopicId of sink topic")
  @Default.String("visualizer")
  @Validation.Required
  String getSinkTopic();

  void setSinkTopic(String value);

  @Description("Table to write to, specified as "
      + "<project_id>:<dataset_id>.<table_id>. "
      + "The dataset_id must already exist")
  @Default.String("fukudak-dflab:nyc_taxi.output")
  @Validation.Required
  String getOutputTable();
  void setOutputTable(String value);

  @Description("Filtered Table to write to, specified as "
      + "<project_id>:<dataset_id>.<table_id>. "
      + "The dataset_id must already exist")
  @Default.String("fukudak-dflab:nyc_taxi.dropoff")
  @Validation.Required
  String getDropoffTable();
  void setDropoffTable(String value);

  @Description("Filtered Table to write to, specified as "
      + "<project_id>:<dataset_id>.<table_id>. "
      + "The dataset_id must already exist")
  @Default.String("fukudak-dflab:nyc_taxi.pickup")
  @Validation.Required
  String getPickupTable();
  void setPickupTable(String value);

}