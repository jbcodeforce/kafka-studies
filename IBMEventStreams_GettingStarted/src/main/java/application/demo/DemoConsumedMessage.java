/*
 *
 * Licensed Materials - Property of IBM
 *
 * 5737-H33
 *
 * (C) Copyright IBM Corp. 2019  All Rights Reserved.
 *
 * US Government Users Restricted Rights - Use, duplication or
 * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 *
 */

package application.demo;

import javax.json.Json;

public class DemoConsumedMessage {

    private String topic;
    private int partition;
    private long offset;
    private String value;
    private long timestamp;

    public DemoConsumedMessage(String topic, int partition, long offset, String value, long timestamp) {
        this.topic = topic;
        this.partition = partition;
        this.offset = offset;
        this.value = value;
        this.timestamp = timestamp;
    }

    public String getTopic() {
        return topic;
    }

    public int getPartition() {
        return partition;
    }

    public long getOffset() {
        return offset;
    }

    public String getValue() {
        return value;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String encode() {
        return Json.createObjectBuilder()
            .add("topic", topic)
            .add("partition", partition)
            .add("offset", offset)
            .add("value", value)
            .add("timestamp", timestamp)
            .build().toString();
    }
}