/**
 * Autogenerated by Avro
 *
 * DO NOT EDIT DIRECTLY
 */
package acme.eda.demo.ordermgr.infra.events;

import org.apache.avro.generic.GenericArray;
import org.apache.avro.specific.SpecificData;
import org.apache.avro.util.Utf8;
import org.apache.avro.message.BinaryMessageEncoder;
import org.apache.avro.message.BinaryMessageDecoder;
import org.apache.avro.message.SchemaStore;

/** Order events to report change to the Order entity. It can take different type and may have different payload per type used. */
@org.apache.avro.specific.AvroGenerated
public class OrderEvent extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  private static final long serialVersionUID = 843744335611564155L;


  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"OrderEvent\",\"namespace\":\"acme.eda.demo.ordermgr.infra.events\",\"doc\":\"Order events to report change to the Order entity. It can take different type and may have different payload per type used.\",\"fields\":[{\"name\":\"orderID\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"},\"doc\":\"Unique order identifier\",\"default\":\"-1\"},{\"name\":\"timestampMillis\",\"type\":\"long\",\"doc\":\"time stamp of the order creation\"},{\"name\":\"type\",\"type\":{\"type\":\"enum\",\"name\":\"EventType\",\"doc\":\"The different types of events emitted\",\"symbols\":[\"OrderCreated\",\"OrderUpdated\",\"OrderInTransit\",\"OrderCompleted\",\"OrderRejected\",\"OrderCancelled\"],\"default\":\"OrderCreated\"},\"doc\":\"Type of event\"},{\"name\":\"payload\",\"type\":[{\"type\":\"record\",\"name\":\"OrderCreatedEvent\",\"fields\":[{\"name\":\"orderID\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"},\"doc\":\"Unique ID from source system\"},{\"name\":\"productID\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"},\"doc\":\"Unique ID for the product as defined in product catalog\"},{\"name\":\"customerID\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"},\"doc\":\"Unique ID for the customer organization\"},{\"name\":\"quantity\",\"type\":\"int\",\"doc\":\"Quantity ordered\",\"default\":-1},{\"name\":\"status\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"},\"doc\":\"Status of the order.\",\"default\":\"Pending\"},{\"name\":\"shippingAddress\",\"type\":{\"type\":\"record\",\"name\":\"Address\",\"fields\":[{\"name\":\"street\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"},\"doc\":\"Street name with number within the street\"},{\"name\":\"city\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"},\"doc\":\"city\"},{\"name\":\"state\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"},\"doc\":\"State code or name\"},{\"name\":\"country\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"},\"doc\":\"Country\"},{\"name\":\"zipcode\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"},\"doc\":\"Zipcode\"}]},\"doc\":\"Address to ship the ordered items\",\"namespace\":\"acme.eda.demo.ordermgr.infra.events\"}]}],\"doc\":\"Different payload structure depending of event type\"}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }

  private static final SpecificData MODEL$ = new SpecificData();

  private static final BinaryMessageEncoder<OrderEvent> ENCODER =
      new BinaryMessageEncoder<>(MODEL$, SCHEMA$);

  private static final BinaryMessageDecoder<OrderEvent> DECODER =
      new BinaryMessageDecoder<>(MODEL$, SCHEMA$);

  /**
   * Return the BinaryMessageEncoder instance used by this class.
   * @return the message encoder used by this class
   */
  public static BinaryMessageEncoder<OrderEvent> getEncoder() {
    return ENCODER;
  }

  /**
   * Return the BinaryMessageDecoder instance used by this class.
   * @return the message decoder used by this class
   */
  public static BinaryMessageDecoder<OrderEvent> getDecoder() {
    return DECODER;
  }

  /**
   * Create a new BinaryMessageDecoder instance for this class that uses the specified {@link SchemaStore}.
   * @param resolver a {@link SchemaStore} used to find schemas by fingerprint
   * @return a BinaryMessageDecoder instance for this class backed by the given SchemaStore
   */
  public static BinaryMessageDecoder<OrderEvent> createDecoder(SchemaStore resolver) {
    return new BinaryMessageDecoder<>(MODEL$, SCHEMA$, resolver);
  }

  /**
   * Serializes this OrderEvent to a ByteBuffer.
   * @return a buffer holding the serialized data for this instance
   * @throws java.io.IOException if this instance could not be serialized
   */
  public java.nio.ByteBuffer toByteBuffer() throws java.io.IOException {
    return ENCODER.encode(this);
  }

  /**
   * Deserializes a OrderEvent from a ByteBuffer.
   * @param b a byte buffer holding serialized data for an instance of this class
   * @return a OrderEvent instance decoded from the given buffer
   * @throws java.io.IOException if the given bytes could not be deserialized into an instance of this class
   */
  public static OrderEvent fromByteBuffer(
      java.nio.ByteBuffer b) throws java.io.IOException {
    return DECODER.decode(b);
  }

  /** Unique order identifier */
  private java.lang.String orderID;
  /** time stamp of the order creation */
  private long timestampMillis;
  /** Type of event */
  private acme.eda.demo.ordermgr.infra.events.EventType type;
  /** Different payload structure depending of event type */
  private java.lang.Object payload;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use <code>newBuilder()</code>.
   */
  public OrderEvent() {}

  /**
   * All-args constructor.
   * @param orderID Unique order identifier
   * @param timestampMillis time stamp of the order creation
   * @param type Type of event
   * @param payload Different payload structure depending of event type
   */
  public OrderEvent(java.lang.String orderID, java.lang.Long timestampMillis, acme.eda.demo.ordermgr.infra.events.EventType type, java.lang.Object payload) {
    this.orderID = orderID;
    this.timestampMillis = timestampMillis;
    this.type = type;
    this.payload = payload;
  }

  @Override
  public org.apache.avro.specific.SpecificData getSpecificData() { return MODEL$; }

  @Override
  public org.apache.avro.Schema getSchema() { return SCHEMA$; }

  // Used by DatumWriter.  Applications should not call.
  @Override
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return orderID;
    case 1: return timestampMillis;
    case 2: return type;
    case 3: return payload;
    default: throw new IndexOutOfBoundsException("Invalid index: " + field$);
    }
  }

  // Used by DatumReader.  Applications should not call.
  @Override
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: orderID = value$ != null ? value$.toString() : null; break;
    case 1: timestampMillis = (java.lang.Long)value$; break;
    case 2: type = (acme.eda.demo.ordermgr.infra.events.EventType)value$; break;
    case 3: payload = value$; break;
    default: throw new IndexOutOfBoundsException("Invalid index: " + field$);
    }
  }

  /**
   * Gets the value of the 'orderID' field.
   * @return Unique order identifier
   */
  public java.lang.String getOrderID() {
    return orderID;
  }


  /**
   * Sets the value of the 'orderID' field.
   * Unique order identifier
   * @param value the value to set.
   */
  public void setOrderID(java.lang.String value) {
    this.orderID = value;
  }

  /**
   * Gets the value of the 'timestampMillis' field.
   * @return time stamp of the order creation
   */
  public long getTimestampMillis() {
    return timestampMillis;
  }


  /**
   * Sets the value of the 'timestampMillis' field.
   * time stamp of the order creation
   * @param value the value to set.
   */
  public void setTimestampMillis(long value) {
    this.timestampMillis = value;
  }

  /**
   * Gets the value of the 'type' field.
   * @return Type of event
   */
  public acme.eda.demo.ordermgr.infra.events.EventType getType() {
    return type;
  }


  /**
   * Sets the value of the 'type' field.
   * Type of event
   * @param value the value to set.
   */
  public void setType(acme.eda.demo.ordermgr.infra.events.EventType value) {
    this.type = value;
  }

  /**
   * Gets the value of the 'payload' field.
   * @return Different payload structure depending of event type
   */
  public java.lang.Object getPayload() {
    return payload;
  }


  /**
   * Sets the value of the 'payload' field.
   * Different payload structure depending of event type
   * @param value the value to set.
   */
  public void setPayload(java.lang.Object value) {
    this.payload = value;
  }

  /**
   * Creates a new OrderEvent RecordBuilder.
   * @return A new OrderEvent RecordBuilder
   */
  public static acme.eda.demo.ordermgr.infra.events.OrderEvent.Builder newBuilder() {
    return new acme.eda.demo.ordermgr.infra.events.OrderEvent.Builder();
  }

  /**
   * Creates a new OrderEvent RecordBuilder by copying an existing Builder.
   * @param other The existing builder to copy.
   * @return A new OrderEvent RecordBuilder
   */
  public static acme.eda.demo.ordermgr.infra.events.OrderEvent.Builder newBuilder(acme.eda.demo.ordermgr.infra.events.OrderEvent.Builder other) {
    if (other == null) {
      return new acme.eda.demo.ordermgr.infra.events.OrderEvent.Builder();
    } else {
      return new acme.eda.demo.ordermgr.infra.events.OrderEvent.Builder(other);
    }
  }

  /**
   * Creates a new OrderEvent RecordBuilder by copying an existing OrderEvent instance.
   * @param other The existing instance to copy.
   * @return A new OrderEvent RecordBuilder
   */
  public static acme.eda.demo.ordermgr.infra.events.OrderEvent.Builder newBuilder(acme.eda.demo.ordermgr.infra.events.OrderEvent other) {
    if (other == null) {
      return new acme.eda.demo.ordermgr.infra.events.OrderEvent.Builder();
    } else {
      return new acme.eda.demo.ordermgr.infra.events.OrderEvent.Builder(other);
    }
  }

  /**
   * RecordBuilder for OrderEvent instances.
   */
  @org.apache.avro.specific.AvroGenerated
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<OrderEvent>
    implements org.apache.avro.data.RecordBuilder<OrderEvent> {

    /** Unique order identifier */
    private java.lang.String orderID;
    /** time stamp of the order creation */
    private long timestampMillis;
    /** Type of event */
    private acme.eda.demo.ordermgr.infra.events.EventType type;
    /** Different payload structure depending of event type */
    private java.lang.Object payload;

    /** Creates a new Builder */
    private Builder() {
      super(SCHEMA$, MODEL$);
    }

    /**
     * Creates a Builder by copying an existing Builder.
     * @param other The existing Builder to copy.
     */
    private Builder(acme.eda.demo.ordermgr.infra.events.OrderEvent.Builder other) {
      super(other);
      if (isValidValue(fields()[0], other.orderID)) {
        this.orderID = data().deepCopy(fields()[0].schema(), other.orderID);
        fieldSetFlags()[0] = other.fieldSetFlags()[0];
      }
      if (isValidValue(fields()[1], other.timestampMillis)) {
        this.timestampMillis = data().deepCopy(fields()[1].schema(), other.timestampMillis);
        fieldSetFlags()[1] = other.fieldSetFlags()[1];
      }
      if (isValidValue(fields()[2], other.type)) {
        this.type = data().deepCopy(fields()[2].schema(), other.type);
        fieldSetFlags()[2] = other.fieldSetFlags()[2];
      }
      if (isValidValue(fields()[3], other.payload)) {
        this.payload = data().deepCopy(fields()[3].schema(), other.payload);
        fieldSetFlags()[3] = other.fieldSetFlags()[3];
      }
    }

    /**
     * Creates a Builder by copying an existing OrderEvent instance
     * @param other The existing instance to copy.
     */
    private Builder(acme.eda.demo.ordermgr.infra.events.OrderEvent other) {
      super(SCHEMA$, MODEL$);
      if (isValidValue(fields()[0], other.orderID)) {
        this.orderID = data().deepCopy(fields()[0].schema(), other.orderID);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.timestampMillis)) {
        this.timestampMillis = data().deepCopy(fields()[1].schema(), other.timestampMillis);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.type)) {
        this.type = data().deepCopy(fields()[2].schema(), other.type);
        fieldSetFlags()[2] = true;
      }
      if (isValidValue(fields()[3], other.payload)) {
        this.payload = data().deepCopy(fields()[3].schema(), other.payload);
        fieldSetFlags()[3] = true;
      }
    }

    /**
      * Gets the value of the 'orderID' field.
      * Unique order identifier
      * @return The value.
      */
    public java.lang.String getOrderID() {
      return orderID;
    }


    /**
      * Sets the value of the 'orderID' field.
      * Unique order identifier
      * @param value The value of 'orderID'.
      * @return This builder.
      */
    public acme.eda.demo.ordermgr.infra.events.OrderEvent.Builder setOrderID(java.lang.String value) {
      validate(fields()[0], value);
      this.orderID = value;
      fieldSetFlags()[0] = true;
      return this;
    }

    /**
      * Checks whether the 'orderID' field has been set.
      * Unique order identifier
      * @return True if the 'orderID' field has been set, false otherwise.
      */
    public boolean hasOrderID() {
      return fieldSetFlags()[0];
    }


    /**
      * Clears the value of the 'orderID' field.
      * Unique order identifier
      * @return This builder.
      */
    public acme.eda.demo.ordermgr.infra.events.OrderEvent.Builder clearOrderID() {
      orderID = null;
      fieldSetFlags()[0] = false;
      return this;
    }

    /**
      * Gets the value of the 'timestampMillis' field.
      * time stamp of the order creation
      * @return The value.
      */
    public long getTimestampMillis() {
      return timestampMillis;
    }


    /**
      * Sets the value of the 'timestampMillis' field.
      * time stamp of the order creation
      * @param value The value of 'timestampMillis'.
      * @return This builder.
      */
    public acme.eda.demo.ordermgr.infra.events.OrderEvent.Builder setTimestampMillis(long value) {
      validate(fields()[1], value);
      this.timestampMillis = value;
      fieldSetFlags()[1] = true;
      return this;
    }

    /**
      * Checks whether the 'timestampMillis' field has been set.
      * time stamp of the order creation
      * @return True if the 'timestampMillis' field has been set, false otherwise.
      */
    public boolean hasTimestampMillis() {
      return fieldSetFlags()[1];
    }


    /**
      * Clears the value of the 'timestampMillis' field.
      * time stamp of the order creation
      * @return This builder.
      */
    public acme.eda.demo.ordermgr.infra.events.OrderEvent.Builder clearTimestampMillis() {
      fieldSetFlags()[1] = false;
      return this;
    }

    /**
      * Gets the value of the 'type' field.
      * Type of event
      * @return The value.
      */
    public acme.eda.demo.ordermgr.infra.events.EventType getType() {
      return type;
    }


    /**
      * Sets the value of the 'type' field.
      * Type of event
      * @param value The value of 'type'.
      * @return This builder.
      */
    public acme.eda.demo.ordermgr.infra.events.OrderEvent.Builder setType(acme.eda.demo.ordermgr.infra.events.EventType value) {
      validate(fields()[2], value);
      this.type = value;
      fieldSetFlags()[2] = true;
      return this;
    }

    /**
      * Checks whether the 'type' field has been set.
      * Type of event
      * @return True if the 'type' field has been set, false otherwise.
      */
    public boolean hasType() {
      return fieldSetFlags()[2];
    }


    /**
      * Clears the value of the 'type' field.
      * Type of event
      * @return This builder.
      */
    public acme.eda.demo.ordermgr.infra.events.OrderEvent.Builder clearType() {
      type = null;
      fieldSetFlags()[2] = false;
      return this;
    }

    /**
      * Gets the value of the 'payload' field.
      * Different payload structure depending of event type
      * @return The value.
      */
    public java.lang.Object getPayload() {
      return payload;
    }


    /**
      * Sets the value of the 'payload' field.
      * Different payload structure depending of event type
      * @param value The value of 'payload'.
      * @return This builder.
      */
    public acme.eda.demo.ordermgr.infra.events.OrderEvent.Builder setPayload(java.lang.Object value) {
      validate(fields()[3], value);
      this.payload = value;
      fieldSetFlags()[3] = true;
      return this;
    }

    /**
      * Checks whether the 'payload' field has been set.
      * Different payload structure depending of event type
      * @return True if the 'payload' field has been set, false otherwise.
      */
    public boolean hasPayload() {
      return fieldSetFlags()[3];
    }


    /**
      * Clears the value of the 'payload' field.
      * Different payload structure depending of event type
      * @return This builder.
      */
    public acme.eda.demo.ordermgr.infra.events.OrderEvent.Builder clearPayload() {
      payload = null;
      fieldSetFlags()[3] = false;
      return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public OrderEvent build() {
      try {
        OrderEvent record = new OrderEvent();
        record.orderID = fieldSetFlags()[0] ? this.orderID : (java.lang.String) defaultValue(fields()[0]);
        record.timestampMillis = fieldSetFlags()[1] ? this.timestampMillis : (java.lang.Long) defaultValue(fields()[1]);
        record.type = fieldSetFlags()[2] ? this.type : (acme.eda.demo.ordermgr.infra.events.EventType) defaultValue(fields()[2]);
        record.payload = fieldSetFlags()[3] ? this.payload :  defaultValue(fields()[3]);
        return record;
      } catch (org.apache.avro.AvroMissingFieldException e) {
        throw e;
      } catch (java.lang.Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private static final org.apache.avro.io.DatumWriter<OrderEvent>
    WRITER$ = (org.apache.avro.io.DatumWriter<OrderEvent>)MODEL$.createDatumWriter(SCHEMA$);

  @Override public void writeExternal(java.io.ObjectOutput out)
    throws java.io.IOException {
    WRITER$.write(this, SpecificData.getEncoder(out));
  }

  @SuppressWarnings("unchecked")
  private static final org.apache.avro.io.DatumReader<OrderEvent>
    READER$ = (org.apache.avro.io.DatumReader<OrderEvent>)MODEL$.createDatumReader(SCHEMA$);

  @Override public void readExternal(java.io.ObjectInput in)
    throws java.io.IOException {
    READER$.read(this, SpecificData.getDecoder(in));
  }

}










