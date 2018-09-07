package com.rn.aerospike.client;

import com.aerospike.client.Key;
import com.rn.aerospike.client.exceptions.CASException;
import com.rn.aerospike.client.exceptions.OperationException;
import com.rn.aerospike.client.model.AerospikeLock;
import com.rn.aerospike.client.model.Counter;
import com.rn.aerospike.client.operations.Operation;
import com.rn.aerospike.common.records.AerospikeRecord;

import java.util.Collection;
import java.util.Map;

/**
 * @author rahul
 */
public interface IAerospikeClient {

    /**
     * Put a record
     * @param record
     * @param <U>
     * @throws OperationException
     */
    <U extends AerospikeRecord> void put(U record) throws OperationException;

    /**
     * Put a record with given TTL
     * @param record
     * @param ttl - ttl in seconds
     * @param <U>
     * @throws OperationException
     */
    <U extends AerospikeRecord> void put(U record, int ttl) throws OperationException;

    /**
     * Put a record only if no record with same key exists before
     * @param record
     * @param <U>
     * @return
     * @throws OperationException
     */
    <U extends AerospikeRecord> boolean putIfNotExists(U record) throws OperationException;

    /**
     * Put a record with TTL only if no record with same key exists before
     * @param record
     * @param ttl - ttl in seconds
     * @param <U>
     * @return
     * @throws OperationException
     */
    <U extends AerospikeRecord> boolean putIfNotExists(U record, int ttl) throws OperationException;

    /**
     * Put a record after performing an operation on it and the previously stored record
     * and return the updated one
     * @param record - record to put
     * @param operation - operation defined on newRecord and foundRecord
     * @param retries - number of retries required
     * @param <U>
     * @return The updated record
     * @throws OperationException
     * @throws CASException
     */
    <U extends AerospikeRecord> U add(U record, Operation<U> operation, int retries) throws OperationException, CASException;

    /**
     * Put a record after performing an operation on it and the previously stored record
     * and return the updated one. Also put a TTL on it
     * @param record - record to put
     * @param operation - operation defined on newRecord and foundRecord
     * @param ttl - TTL in seconds
     * @param retries - number of retries required
     * @param <U>
     * @return The updated record
     * @throws OperationException
     * @throws CASException
     */
    <U extends AerospikeRecord> U add(U record, Operation<U> operation, int ttl, int retries) throws OperationException, CASException;

    /**
     * Find the corresponding record and populate information in same object
     * @param record
     * @param <U>
     * @return
     * @throws OperationException
     */
    <U extends AerospikeRecord> boolean read(U record) throws OperationException;

    /**
     * Find the record given key and its Class
     * @param key
     * @param clazz
     * @param <U>
     * @return
     * @throws OperationException
     */
    <V,U extends AerospikeRecord<V>> U get(V key, Class<U> clazz, String... bins) throws OperationException;

    <L extends AerospikeLock> boolean acquireLock(Class<L> lockClazz, String key) throws OperationException;

    <L extends AerospikeLock> boolean acquireLock(Class<L> lockClazz, final String key, final int ttl) throws OperationException;

    <L extends AerospikeLock> void releaseLock(Class<L> lockClazz, String key) throws OperationException;

    /**
     * Find all objects for given class and all keys
     * @param clazz
     * @param keys
     * @param <U>
     * @return - a map of key to record
     * @throws OperationException
     */
    <V,U extends AerospikeRecord<V>> Map<V, U> getBulk(Class<U> clazz, V[] keys, String... bins) throws OperationException;

    <V> Map<V, AerospikeRecord> readBulk(AerospikeRecord<V>[] records, String... bins) throws OperationException;

    <V, U extends AerospikeRecord<V>> void delete(Class<U> clazz, V... keys) throws OperationException;

    void delete(AerospikeRecord... records) throws OperationException;

    boolean exists(AerospikeRecord record) throws OperationException;

    <V,U extends AerospikeRecord<V>> boolean exists(V key, Class<U> clazz) throws OperationException;

    <C extends Counter> long setCounter(Class<C> counterClass, String key, long value, int time) throws OperationException;

    <C extends Counter> long incrCounter(Class<C> counterClass, String key, long by) throws OperationException;

    <T extends Counter> long incrCounter(Key key, long by) throws OperationException;

    <T extends Counter> long incrCounter(Key key, long by, int time) throws OperationException;

    <C extends Counter> long incrCounterDelayed(Class<C> counterClass, String key, long by) throws OperationException;

    <C extends Counter> long incrCounterDelayed(Class<C> counterClass, String key, long by, Integer time) throws OperationException;

    <C extends Counter> long incrCounter(Class<C> counterClass, String key, long by, int time) throws OperationException;

    <C extends Counter> long decrCounter(Class<C> counterClass, String key, long by) throws OperationException;

    <C extends Counter> long decrCounterDelayed(Class<C> counterClass, String key, long by) throws OperationException;

    <C extends Counter> long decrCounterDelayed(Class<C> counterClass, String key, long by, Integer time) throws OperationException;

    <C extends Counter> long decrCounter(Class<C> counterClass, String key, long by, int time) throws OperationException;

    <C extends Counter> boolean deleteCounter(Class<C> counterClass, String key) throws OperationException;

    <C extends Counter> long getCounter(Class<C> counterClass, String key) throws OperationException;

    <C extends Counter> Map<String, Long> getBulkCounters(Class<C> counterClass, String... keys) throws OperationException;

    <C extends Counter> Map<String, Long> getBulkCounters(Class<C> counterClass, Collection<String> keys) throws OperationException;

}
