# Aerospike ORM Client

This is Object Relational Mapping based client for JAVA that can be used to interact with NoSQL database Aerospike.

When using this client we don't need to use any native queries to interact with the database. All READ and WRITE operations can be performed using simple JAVA entity object.

## Usage Instructions

### Defining your Entity

  1. Define a Key structure for your Entity by implementing AerospikeRecord. Base implementation for AerospikeRecordStringKey is provided which uses String as a key type.
  
  2. Define your Entity class by marking with @StorableRecord annotation, specifying the aerospike setname.
  
  3. Define your bins as fields of the Entity and mark them with StorableBin annotation, specifying the name of the bin and its data type.
  
  4. Apart from standard data types like int, float, long, string, map, json, etc there is supoort for a CUSTOM data type where you can specify your own read and write logic from the database which will be used to populate the field of entity.
  
  5. LIST, MAP & JSON types have been defined by default to cover most of the use cases.
  
  Sample code for creating an entity can be found below
  ```
  @NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@StorableRecord(setname = "test")
@ToString
public class AerospikeTestEntityA implements AerospikeRecordStringKey {

    @StorableBin(name = "string", type = StorableType.STRING)
    private String value1;

    @StorableBin(name = "set", type = StorableType.SET)
    private Set<String> value2;

    @StorableBin(name = "double", type = StorableType.DOUBLE)
    private Double value3;

    @StorableBin(name = "float", type = StorableType.FLOAT)
    private Float value4;

    @StorableBin(name = "int", type = StorableType.INT)
    private int value5;

    @StorableBin(name = "long", type = StorableType.LONG)
    private long value6;

    @StorableBin(name = "bool", type = StorableType.BOOLEAN)
    private boolean value7;

    @StorableBin(name = "enum", type = StorableType.ENUM)
    private AerospikeTestEnum value8;

    @StorableBin(name = "bytes", type = StorableType.BYTES)
    private byte[] value9;

    @StorableBin(name = "json", type = StorableType.JSON)
    private AerospikeJsonEntity value10;

    @StorableBin(name = "list", type = StorableType.LIST)
    private List<String> value11;

    @StorableBin(name = "map", type = StorableType.MAP)
    private Map<String, String> value12;

    @StorableBin(name = "custom", type = StorableType.CUSTOM, keyConverter = AerospikeCustomEntityConverterA.class)
    private AerospikeCustomEntityA value13;

    @StorableBin(name = "customList", type = StorableType.LIST, keyConverter = AerospikeCustomEntityConverterA.class)
    private List<AerospikeCustomEntityA> value14;
}
```

### Creating a Connection
 Create a connection using the following snippet. 
 ```
 AerospikeClient client = new AerospikeClient("<aerospike-ip>", 3000, true);
 ```
 Note that this is not a singleton instance and has been deliberately kept to allow for making more than one connection.
 
 ### Supported Operations
 
 The client currently supports all basic operations like get, read(which reads into an object), put, putIfNotExists, delete, etc which can be found well documented in the **IAerospikeClient** interface. 
 ```
 //Sample put & delete method
 AerospikeTestEntityA entity = AerospikeTestEntityA.newBuilder().key("key1").build();
 client.put(entity, 3);
 client.delete(entity);
 
 //Sample get method
 AerospikeTestEntityA actual = client.get("key1", AerospikeTestEntityA.class);
 ```
 
 Apart from the standard functions there are a few functions that I'd like to focus on:
 
 #### Realtime Counters
setCounter - This can be used for realtime counting(eg: counting number of requests). This is a thread safe function and offers concurrency-control for realtime transactions. Corresponding setCounterWithTTL, increment, decrement and delete counter functions are also provided. Note that the namespace should be configured as in-memory setting.
```
//Sample for using counters
client.setCounter(TestCounter.class, COUNTER_KEY, 5, 1000);
long incrValue = client.incrCounter(TestCounter.class, COUNTER_KEY, 2, 3);
long recdValue = client.getCounter(TestCounter.class, COUNTER_KEY);
```
 
 #### Distributed Locking
acquireLock - This function is provided for use cases where we might require distributed locking to prevent concurrency issues across distributed web servers in a realtime environment. Corresponding method for aquireLockWithTTL and releaseLock are also provided.
```
//Sample for using locking
boolean acquired = client.acquireLock(AerospikeTestLock.class, "lockKey", 4);
client.releaseLock(AerospikeTestLock.class, "lockKey2");
```

 #### Bulk Reads
 getBulk and readBulk methods provide the support to perform Bulk reads in order to save on the network latency when peroforming large number of reads.
 ```
 //Sample for bulk reads
 Map<String, AerospikeTestEntityA> bulkResponse = client.getBulk(AerospikeTestEntityA.class, new String[]{"entity1",
                "entity3", "entity2"});
 ```
 
 This utility is designed with keeping most of the production issues in mind and comes with minimum integration steps. Just clone , build using maven and include the dependency.
 
 Suggestions are welcome.
 
## Get Set Go! Happy Aerospiking !!!
 

