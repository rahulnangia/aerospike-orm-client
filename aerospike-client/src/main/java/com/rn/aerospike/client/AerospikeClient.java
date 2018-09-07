package com.rn.aerospike.client;

import com.aerospike.client.AerospikeException;
import com.aerospike.client.Bin;
import com.aerospike.client.Host;
import com.aerospike.client.Key;
import com.aerospike.client.Value;
import com.aerospike.client.Value.StringValue;
import com.aerospike.client.policy.BatchPolicy;
import com.aerospike.client.policy.GenerationPolicy;
import com.aerospike.client.policy.Policy;
import com.aerospike.client.policy.RecordExistsAction;
import com.aerospike.client.policy.WritePolicy;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.rn.aerospike.client.exceptions.CASException;
import com.rn.aerospike.client.exceptions.KeyInitializationException;
import com.rn.aerospike.client.exceptions.OperationException;
import com.rn.aerospike.client.mapping.RecordBinMapper;
import com.rn.aerospike.client.mapping.RecordBin;
import com.rn.aerospike.client.model.AerospikeLock;
import com.rn.aerospike.client.model.AerospikeLockValue;
import com.rn.aerospike.client.model.Counter;
import com.rn.aerospike.client.operations.Operation;
import com.rn.aerospike.client.utils.PolicyHelper;
import com.rn.aerospike.client.utils.Triple;
import com.rn.aerospike.client.utils.Tuple;
import com.rn.aerospike.client.mapping.Record;
import com.rn.aerospike.client.model.AerospikeObject;
import com.rn.aerospike.client.utils.CounterUpdater;
import com.rn.aerospike.common.annotations.StorableRecord;
import com.rn.aerospike.common.exceptions.ConversionException;
import com.rn.aerospike.common.exceptions.JsonUtilityException;
import com.rn.aerospike.common.records.AerospikeRecord;
import org.apache.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Created by rahul
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class AerospikeClient implements IAerospikeClient {

    private final Policy DEFAULT_READ_POLICY;
    private final BatchPolicy DEFAULT_BATCH_POLICY;
    private final WritePolicy DEFAULT_WRITE_POLICY;
    private final WritePolicy CREATE_NOT_EXIST_POLICY;
    private final CounterUpdater counterUpdater;

    private static final Logger LOGGER = Logger.getLogger(AerospikeClient.class);

    private Map<Class, AerospikeRecord> dummyInstances;
    private com.aerospike.client.AerospikeClient client;
    private RecordBinMapper mapper;

    public AerospikeClient(Policy policy, BatchPolicy batchPolicy, WritePolicy writePolicy, boolean lazyLoad) {
        DEFAULT_READ_POLICY = policy;
        DEFAULT_READ_POLICY.sendKey = true;
        DEFAULT_BATCH_POLICY = batchPolicy;
        DEFAULT_BATCH_POLICY.sendKey = true;
        DEFAULT_WRITE_POLICY = writePolicy;
        DEFAULT_WRITE_POLICY.sendKey = true;
        CREATE_NOT_EXIST_POLICY = new WritePolicy(writePolicy);
        CREATE_NOT_EXIST_POLICY.sendKey = true;
        CREATE_NOT_EXIST_POLICY.recordExistsAction = RecordExistsAction.CREATE_ONLY;
        counterUpdater = new CounterUpdater(this);
        mapper = RecordBinMapper.getMapper(lazyLoad);
        dummyInstances = new HashMap<>();
    }

    public AerospikeClient(String host, int port, boolean lazyLoad) {
        this(new Policy(), new BatchPolicy(), new WritePolicy(), lazyLoad);
        client = new com.aerospike.client.AerospikeClient(host, port);
    }

    public AerospikeClient(String host, int port, Policy readPolicy, boolean lazyLoad) {
        this(readPolicy, new BatchPolicy(), new WritePolicy(), lazyLoad);
        client = new com.aerospike.client.AerospikeClient(host, port);
    }

    public AerospikeClient(List<String> hosts, int port, Policy readPolicy, BatchPolicy batchPolicy, boolean lazyLoad) {
        this(readPolicy, batchPolicy, new WritePolicy(), lazyLoad);
        List<Host> hostList = Lists.newLinkedList();
        for (String host : hosts) {
            host = host.trim();
            if (!Strings.isNullOrEmpty(host)) {
                hostList.add(new Host(host, port));
            }
        }
        client = new com.aerospike.client.AerospikeClient(null, hostList.toArray(new Host[hostList.size()]));
    }

    public AerospikeClient(List<String> hosts, int port, boolean lazyLoad) {
        this(new Policy(), new BatchPolicy(), new WritePolicy(), lazyLoad);
        List<Host> hostList = Lists.newLinkedList();
        for (String host : hosts) {
            host = host.trim();
            if (!Strings.isNullOrEmpty(host)) {
                hostList.add(new Host(host, port));
            }
        }
        client = new com.aerospike.client.AerospikeClient(null, hostList.toArray(new Host[hostList.size()]));
    }

    public AerospikeClient(List<String> hosts, int port, Policy readPolicy, BatchPolicy batchPolicy,
                           WritePolicy writePolicy, boolean lazyLoad) {
        this(readPolicy, batchPolicy, writePolicy, lazyLoad);

        List<Host> hostList = Lists.newLinkedList();
        for (String host : hosts) {
            host = host.trim();
            if (!Strings.isNullOrEmpty(host)) {
                hostList.add(new Host(host, port));
            }
        }
        client = new com.aerospike.client.AerospikeClient(null, hostList.toArray(new Host[hostList.size()]));
    }

    public AerospikeClient(String host, int port) {
        this(host, port, false);
    }

    public AerospikeClient(String host, int port, Policy readPolicy) {
        this(host, port, readPolicy, false);
    }

    public AerospikeClient(List<String> hosts, int port, Policy readPolicy, BatchPolicy batchPolicy) {
        this(hosts, port, readPolicy, batchPolicy, false);
    }

    public AerospikeClient(List<String> hosts, int port) {
        this(hosts, port, false);
    }

    public AerospikeClient(List<String> hosts, int port, Policy readPolicy, BatchPolicy batchPolicy,
                           WritePolicy writePolicy) {
        this(hosts, port, readPolicy, batchPolicy, writePolicy, false);
    }

    @Override
    public <T extends AerospikeRecord> void put(T record) throws OperationException {
        put(record, DEFAULT_WRITE_POLICY);
    }

    @Override
    public <T extends AerospikeRecord> void put(T record, int ttl) throws OperationException {
        put(record, PolicyHelper.getDefaultTTLWritePolicy(ttl, DEFAULT_WRITE_POLICY));
    }

    @Override
    public <T extends AerospikeRecord> boolean putIfNotExists(T record) throws OperationException {
        return putIfNotExists(record, CREATE_NOT_EXIST_POLICY);
    }

    @Override
    public <T extends AerospikeRecord> boolean putIfNotExists(T record, int ttl) throws OperationException {
        WritePolicy ttlPolicy = new WritePolicy(CREATE_NOT_EXIST_POLICY);
        ttlPolicy.expiration = ttl;
        return putIfNotExists(record, ttlPolicy);
    }

    public <T extends AerospikeRecord> boolean putIfNotExists(T record, WritePolicy writePolicy)
            throws OperationException {
        try {
            put(record, writePolicy);
            return true;
        } catch (OperationException e) {
            if (e.getErrorCode() == 5) {
                return false;
            }
            throw new OperationException(e);
        }
    }

    @Override
    public <T extends AerospikeRecord> T add(T record, Operation<T> operation, int retries)
            throws OperationException, CASException {
        return add(record, operation, DEFAULT_WRITE_POLICY, retries);
    }

    @Override
    public <T extends AerospikeRecord> T add(T record, Operation<T> operation, int ttl, int retries)
            throws OperationException, CASException {
        return add(record, operation, PolicyHelper.getDefaultTTLWritePolicy(ttl, DEFAULT_WRITE_POLICY), retries);
    }

    private <T extends AerospikeRecord> T add(T record, Operation<T> operation, WritePolicy writePolicy, int retries)
            throws OperationException, CASException {
        if (retries < 0) {
            throw new CASException("Unable to add for: " + record.getAerospikeKey());
        }

        Tuple<T, Integer> foundRecordWithGen = null;

        try {
            foundRecordWithGen = getWithGeneration(record.getAerospikeKey(), (Class<T>) record.getClass());
        } catch (Exception e) {
            throw new OperationException(e);
        }

        T foundRecord = foundRecordWithGen == null ? null : foundRecordWithGen.getFirst();
        T updatedRecord = operation.operate(record, foundRecord);

        if (updatedRecord != null) {
            WritePolicy addPolicy = new WritePolicy(writePolicy);
            addPolicy.generationPolicy = GenerationPolicy.EXPECT_GEN_EQUAL;
            addPolicy.generation = foundRecordWithGen == null ? 0 : foundRecordWithGen.getSecond();

            try {
                put(updatedRecord, addPolicy);
            } catch (OperationException e) {
                return add(record, operation, writePolicy, --retries);
            }
        }
        return updatedRecord;
    }

    @Override
    public <T extends AerospikeRecord> boolean read(T record) throws OperationException {
        return get(record.getAerospikeKey(), record) == null ? false : true;
    }

    @Override
    public <V, T extends AerospikeRecord<V>> T get(V key, Class<T> clazz, String... bins) throws OperationException {
        try {
            return get(key, clazz.newInstance(), bins);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new OperationException(e);
        }
    }

    private <V, T extends AerospikeRecord<V>> T get(V key, T record, String... bins) throws OperationException {
        Tuple<T, Integer> value = getWithGeneration(key, record, bins);
        return value == null ? null : value.getFirst();
    }

    private <V, T extends AerospikeRecord<V>> Tuple<T, Integer> getWithGeneration(V key, Class<T> clazz, String... bins)
            throws OperationException {
        try {
            return getWithGeneration(key, clazz.newInstance(), bins);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new OperationException(e);
        }
    }

    @Override
    public <L extends AerospikeLock> boolean acquireLock(Class<L> lockClazz, final String key, final int ttl)
            throws OperationException {
        try {
            L lockObject = lockClazz.newInstance();
            lockObject.setKey(key);
            lockObject.setValue(new AerospikeLockValue(AerospikeLockValue.AerospikeLockValueEnum.PROCESSING));
            return putIfNotExists(lockObject, ttl);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new KeyInitializationException("", e);
        } catch (Exception e) {
            delete(lockClazz, key);
            throw e;
        }
    }

    @Override
    public <L extends AerospikeLock> boolean acquireLock(Class<L> lockClazz, final String key)
            throws OperationException {
        return acquireLock(lockClazz, key, -1);
    }

    @Override
    public <L extends AerospikeLock> void releaseLock(Class<L> lockClazz, final String key) throws OperationException {
        try {
            delete(lockClazz, key);
        } catch (Exception e) {
            LOGGER.error(String.format("Unable to release lock on %s, with key %s",
                    lockClazz == null ? null : lockClazz.getClass().getName(), key));
            throw e;
        }
    }

    private <V, T extends AerospikeRecord<V>> Tuple<T, Integer> getWithGeneration(V key, T record, String... bins)
            throws OperationException {
        com.aerospike.client.Record aerospikeRecord = null;
        boolean success = false;
        Record set = mapper.getMapping(record.getClass());
        if (set != null) {
            if (bins == null || bins.length == 0) {
                aerospikeRecord = client.get(DEFAULT_READ_POLICY, new Key(set.getNamespace(), set.getSetName(),
                        AerospikeRecordHelper.getAerospikeKeyValue(record, key)));
            } else {
                aerospikeRecord = client.get(DEFAULT_READ_POLICY, new Key(set.getNamespace(), set.getSetName(),
                        AerospikeRecordHelper.getAerospikeKeyValue(record, key)), bins);
            }
            if (aerospikeRecord == null) {
                return null;
            }
            success = populateEmptyObject(record, aerospikeRecord, set, key, bins);
        }
        if (success) {
            return new Tuple<>(record, aerospikeRecord.generation);
        } else {
            return null;
        }
    }

    @Override
    public <V, T extends AerospikeRecord<V>> Map<V, T> getBulk(Class<T> clazz, V[] keys, String... bins) throws OperationException {
        if (keys == null) {
            return null;
        }
        Map<V, T> responses = Maps.newHashMap();
        List<Key> keyList = Lists.newLinkedList();
        Record set = mapper.getMapping(clazz);
        if (set != null) {
            for (V key : keys) {
                keyList.add(new Key(set.getNamespace(), set.getSetName(), getKeyForClass(clazz, key)));
            }
            com.aerospike.client.Record[] records;
            if (bins == null || bins.length == 0) {
                records = client.get(DEFAULT_BATCH_POLICY, keyList.toArray(new Key[keyList.size()]));
            } else {
                records = client.get(DEFAULT_BATCH_POLICY, keyList.toArray(new Key[keyList.size()]), bins);
            }

            if (records != null) {
                for (int i = 0; i < records.length; i++) {
                    if (records[i] == null) {
                        responses.put(keys[i], null);
                        continue;
                    }
                    try {
                        T response = clazz.newInstance();
                        boolean success = populateEmptyObject(response, records[i], set, keys[i], bins);
                        if (success) {
                            responses.put(keys[i], response);
                        } else {
                            responses.put(keys[i], null);
                        }
                    } catch (Exception e) {
                        LOGGER.error(String.format("Failed to convert to object for key %s", keys[i]), e);
                    }
                }
            }
        }

        return responses.isEmpty() ? null : responses;
    }

    @Override
    public <V> Map<V, AerospikeRecord> readBulk(AerospikeRecord<V>[] records, String... bins) throws OperationException {
        return processBulkReadGet(false, records, bins);
    }

    // TODO: remove the createNewInstances support in future, as not to be used
    private <V> Map<V, AerospikeRecord> processBulkReadGet(boolean createNewInstances, AerospikeRecord<V>[] records, String... bins)
            throws OperationException {
        if (records == null) {
            return null;
        }
        Map<V, AerospikeRecord> responses = Maps.newHashMap();
        List<Key> keys = Lists.newArrayList();
        List<Triple<V, AerospikeRecord, Record>> properties = Lists.newArrayList();
        for (AerospikeRecord<V> record : records) {
            if (record != null) {
                Record set = mapper.getMapping(record.getClass());
                if (set != null) {
                    properties.add(new Triple<V, AerospikeRecord, Record>(record.getAerospikeKey(), record, set));
                    keys.add(new Key(set.getNamespace(), set.getSetName(),
                            getKeyForClass(record.getClass(), record.getAerospikeKey())));
                }
            }
        }

        com.aerospike.client.Record[] results;
        if (bins == null || bins.length == 0) {
            results = client.get(DEFAULT_BATCH_POLICY, keys.toArray(new Key[keys.size()]));
        } else {
            results = client.get(DEFAULT_BATCH_POLICY, keys.toArray(new Key[keys.size()]), bins);
        }
        if (results != null) {
            for (int i = 0; i < results.length; i++) {
                if (results[i] == null) {
                    responses.put(properties.get(i).getFirst(), null);
                    continue;
                }
                try {
                    AerospikeRecord response;
                    if (createNewInstances) {
                        response = properties.get(i).getSecond().getClass().newInstance();
                    } else {
                        response = properties.get(i).getSecond();
                    }
                    boolean success = populateEmptyObject(response, results[i], properties.get(i).getThird(),
                            properties.get(i).getFirst(), bins);
                    if (success) {
                        responses.put(properties.get(i).getFirst(), response);
                    } else {
                        responses.put(properties.get(i).getFirst(), null);
                    }
                } catch (Exception e) {
                    LOGGER.error(String.format("Failed to convert to object for key %s", properties.get(i).getFirst()),
                            e);
                }
            }
        }

        return responses.isEmpty() ? null : responses;
    }

    @Override
    public void delete(AerospikeRecord... records) throws OperationException {
        if (records == null) {
            return;
        }
        for (AerospikeRecord record : records) {
            delete(getAerospikeKeyObject(record));
        }

    }

    @Override
    public <V, T extends AerospikeRecord<V>> void delete(Class<T> clazz, V... keys) throws OperationException {
        for (V key : keys) {
            client.delete(DEFAULT_WRITE_POLICY, getAerospikeKeyObject(clazz, getKeyForClass(clazz, key)));
        }
    }

    @Override
    public boolean exists(AerospikeRecord record) throws OperationException {
        return client.exists(DEFAULT_READ_POLICY, getAerospikeKeyObject(record));
    }

    @Override
    public <V, T extends AerospikeRecord<V>> boolean exists(V key, Class<T> clazz) throws OperationException {
        return client.exists(DEFAULT_READ_POLICY, getAerospikeKeyObject(clazz, getKeyForClass(clazz, key)));
    }

    @Override
    public <T extends Counter> long setCounter(Class<T> clazz, String key, long value, int time)
            throws OperationException {
        T counter = getCounterObject(clazz, key, value);
        put(counter, PolicyHelper.getDefaultTTLWritePolicy(time, DEFAULT_WRITE_POLICY));
        return value;
    }

    @Override
    public <T extends Counter> long incrCounter(Class<T> counterClass, String key, long by) throws OperationException {
        return incrCounter(counterClass, key, by, DEFAULT_WRITE_POLICY);
    }

    @Override
    public <T extends Counter> long incrCounter(Key key, long by) throws OperationException {
        return incrCounter(key, by, DEFAULT_WRITE_POLICY);
    }

    @Override
    public <T extends Counter> long incrCounter(Key key, long by, int time) throws OperationException {
        return incrCounter(key, by, PolicyHelper.getDefaultTTLWritePolicy(time, DEFAULT_WRITE_POLICY));
    }

    @Override
    public <C extends Counter> long incrCounterDelayed(Class<C> counterClass, String key, long by)
            throws OperationException {
        return incrCounterDelayed(counterClass, key, by, null);
    }

    @Override
    public <C extends Counter> long incrCounterDelayed(Class<C> counterClass, String key, long by, Integer time)
            throws OperationException {
        return counterUpdater.incrCounter(getAerospikeKeyObject(getCounterObject(counterClass, key, by)), by, time);
    }

    @Override
    public <T extends Counter> long incrCounter(Class<T> counterClass, String key, long by, int time)
            throws OperationException {
        return incrCounter(counterClass, key, by, PolicyHelper.getDefaultTTLWritePolicy(time, DEFAULT_WRITE_POLICY));
    }

    @Override
    public <T extends Counter> long decrCounter(Class<T> counterClass, String key, long by) throws OperationException {
        return incrCounter(counterClass, key, -1 * by);
    }

    @Override
    public <C extends Counter> long decrCounterDelayed(Class<C> counterClass, String key, long by)
            throws OperationException {
        return decrCounterDelayed(counterClass, key, by, null);
    }

    @Override
    public <C extends Counter> long decrCounterDelayed(Class<C> counterClass, String key, long by, Integer time)
            throws OperationException {
        return incrCounterDelayed(counterClass, key, -1 * by, time);
    }

    @Override
    public <T extends Counter> long decrCounter(Class<T> counterClass, String key, long by, int time)
            throws OperationException {
        return incrCounter(counterClass, key, -1 * by, time);
    }

    @Override
    public <T extends Counter> boolean deleteCounter(Class<T> counterClass, String key) throws OperationException {
        verifyCounterClass(counterClass);
        Key aerospikeKeyObject = getAerospikeKeyObject(counterClass, new StringValue(key));
        counterUpdater.deleteCounterKey(aerospikeKeyObject);
        return delete(aerospikeKeyObject);
    }

    @Override
    public <T extends Counter> long getCounter(Class<T> counterClass, String key) throws OperationException {
        verifyCounterClass(counterClass);
        Counter counter = get(key, counterClass);
        return counter == null ? -1 : counter.getCount();
    }

    @Override
    public <T extends Counter> Map<String, Long> getBulkCounters(Class<T> counterClass, String... keys)
            throws OperationException {
        verifyCounterClass(counterClass);
        Map<String, T> bulkCounters = getBulk(counterClass, keys);
        if (bulkCounters != null) {
            Map<String, Long> counterMap = Maps.newHashMap();
            for (Map.Entry<String, T> counterEntry : bulkCounters.entrySet()) {
                if (counterEntry.getValue() != null) {
                    counterMap.put(counterEntry.getKey(), counterEntry.getValue().getCount());
                } else {
                    counterMap.put(counterEntry.getKey(), -1L);
                }
            }
            return counterMap;
        }
        return null;
    }

    @Override
    public <T extends Counter> Map<String, Long> getBulkCounters(Class<T> counterClass, Collection<String> keys)
            throws OperationException {
        return keys == null ? null : getBulkCounters(counterClass, keys.toArray(new String[keys.size()]));
    }

    public void close() {
        try {
            this.client.close();
        } catch (Exception e) {
            // Do nothing
        }
    }

    private <T extends AerospikeRecord> void put(T record, WritePolicy writePolicy) throws OperationException {
        AerospikeObject object = getAerospikeWriteObject(record);
        try {
            if (object != null) {
                if (object.getBins().length > 0) {
                    client.put(writePolicy, object.getKey(), object.getBins());
                }
            } else {
                throw new OperationException("Cannot store null object ");
            }
        } catch (AerospikeException e) {
            throw new OperationException(e.getResultCode(), e.getMessage(), e);
        }
    }

    private boolean delete(Key key) {
        return client.delete(DEFAULT_WRITE_POLICY, key);
    }

    private <T extends Counter> long incrCounter(Class<T> counterClass, String key, long by, WritePolicy policy)
            throws OperationException {
        T counter = getCounterObject(counterClass, key, by);
        return incrCounter(getAerospikeKeyObject(counter), by, policy);
    }

    private long incrCounter(Key key, long by, WritePolicy policy) throws OperationException {
        try {
            com.aerospike.client.Record record = client.operate(policy, key, com.aerospike.client.Operation.add(new Bin(Counter.binName, by)),
                    com.aerospike.client.Operation.get(Counter.binName));
            return record.getLong(Counter.binName);
        } catch (AerospikeException e) {
            throw new OperationException(e.getResultCode(), "Unable to increment counter for Key: " + key.toString(),
                    e);
        }
    }

    private <T extends Counter> T getCounterObject(Class<T> counterClass, String key, long by)
            throws OperationException {
        verifyCounterClass(counterClass);
        T counter = null;
        try {
            counter = counterClass.newInstance();
            counter.setAerospikeKey(key);
            counter.setCount(by);
        } catch (Exception e) {
            throw new OperationException("Unable to instantiate object for counter class " + counterClass == null ? null
                    : counterClass.getName(), e);
        }
        return counter;
    }

    private <T extends Counter> void verifyCounterClass(Class<T> clazz) throws OperationException {
        if (!clazz.isAnnotationPresent(StorableRecord.class)) {
            throw new OperationException(
                    "StorableRecord annotation should be present on counter class " + clazz.getName());
        }
    }

    private <V, T extends AerospikeRecord<V>> boolean populateEmptyObject(T emptyObject, com.aerospike.client.Record record, Record set,
                                                                          V key, String... bins) throws OperationException {
        emptyObject.setAerospikeKey(key);
        Set<String> validBins = bins == null || bins.length == 0 ? null : Sets.newHashSet(bins);
        for (Entry<String, List<RecordBin>> recordBin : set.getBins().entrySet()) {
            String binName = recordBin.getKey();
            if (validBins == null || validBins.contains(binName)) {
                boolean successFulRead = false;
                for (RecordBin bin : recordBin.getValue()) {
                    try {
                        bin.getAerospikeReader().invoke(emptyObject, record.getValue(binName));
                        successFulRead = true;
                        break;
                    } catch (InvocationTargetException | IllegalAccessException | JsonUtilityException
                            | InstantiationException e) {
                        throw new OperationException(e);
                    } catch (ConversionException e) {
                        // ignore
                    }
                }
                if (!successFulRead) {
                    LOGGER.error("UNABLE TO READ BIN: " + binName + " IN " + emptyObject.getClass() + " FOR " + emptyObject.getAerospikeKey());
                }
            }
        }
        return true;
    }

    private Key getAerospikeKeyObject(AerospikeRecord record) throws OperationException {
        Class clazz = record.getClass();
        return getAerospikeKeyObject(clazz, AerospikeRecordHelper.getAerospikeKeyValue(record));
    }

    private <T extends AerospikeRecord> Key getAerospikeKeyObject(Class<T> clazz, Value key) throws OperationException {
        Record set = mapper.getMapping(clazz);
        if (set != null) {
            return new Key(set.getNamespace(), set.getSetName(), key);
        }
        throw new OperationException("Key not found for record object of " + key);
    }

    private AerospikeObject getAerospikeWriteObject(AerospikeRecord record) throws OperationException {
        Class clazz = record.getClass();
        Record set = mapper.getMapping(record.getClass());
        if (set != null) {
            Key key = new Key(set.getNamespace(), set.getSetName(), AerospikeRecordHelper.getAerospikeKeyValue(record));
            Bin[] bins = new Bin[set.getBins().size()];
            int i = 0;

            try {
                for (Entry<String, List<RecordBin>> recordBin : set.getBins().entrySet()) {
                    RecordBin writer = recordBin.getValue().get(0);
                    bins[i++] = new Bin(recordBin.getKey(), writer.getAerospikeWriter().invoke(record));
                }
            } catch (InvocationTargetException | IllegalAccessException | ConversionException
                    | InstantiationException e) {
                throw new OperationException(e);
            }
            return new AerospikeObject(key, bins);
        } else {
            throw new OperationException(
                    String.format("Cannot get unknown object of class %s. Must be @StorableRecord", clazz.getName()));
        }
    }

    private <V, T extends AerospikeRecord<V>> Value getKeyForClass(Class<T> clazz, V key)
            throws KeyInitializationException {
        try {
            if (dummyInstances.get(clazz) == null) {
                dummyInstances.put(clazz, clazz.newInstance());
            }
            T dummyInstance = (T) dummyInstances.get(clazz);
            return AerospikeRecordHelper.getAerospikeKeyValue(dummyInstance, key);
        } catch (Exception e) {
            throw new KeyInitializationException("Unable to intialize key for class: " + clazz.getName(), e);
        }
    }

}
