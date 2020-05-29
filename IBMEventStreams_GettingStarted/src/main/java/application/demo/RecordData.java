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

public class RecordData {

    private String topic;
    private int partition;
    private long offset;
    private long timestamp;
    
    public enum Status {
        DELIVERED, ERROR
    }

    private Status status;

    public RecordData(Status status, String topic, int partition, long offset, long timestamp) {
        this.status = status;
        this.topic = topic;
        this.partition = partition;
        this.offset = offset;
        this.timestamp = timestamp;
    }

    public RecordData(Status status, String topic) {
        this.status = status;
        this.topic = topic;
    }

    public Status getStatus() {
        return status;
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

    public long getTimestamp() {
        return timestamp;
    }

    public String encode() {
        return Json.createObjectBuilder()
            .add("status", status.toString())
            .add("topic", topic)
            .add("partition", partition)
            .add("offset", offset)
            .add("timestamp", timestamp)
            .build().toString();
	}
}