package com.rn.aerospike.client.utils;

import com.aerospike.client.Key;
import com.rn.aerospike.client.exceptions.OperationException;
import com.rn.aerospike.client.IAerospikeClient;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by rahul
 */
public class CounterUpdater {

    private static volatile Map<String, Map<String, ConcurrentHashMap<String, Tuple<Long,Integer>>>> counterMap = new HashMap<>();

    private final IAerospikeClient iAerospikeClient;

    private static final Logger LOGGER = Logger.getLogger(CounterUpdater.class);

    public CounterUpdater(IAerospikeClient iAerospikeClient) {
        this.iAerospikeClient = iAerospikeClient;
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(
                new Runnable() {
                    @Override
                    public void run() {
                        update();
                    }
                }, 10, 10, TimeUnit.SECONDS
        );
    }

    public long incrCounter(Key key, long by, Integer ttl) {
        if (!counterMap.containsKey(key.namespace)) {
            synchronized (counterMap) {
                if (!counterMap.containsKey(key.namespace)) {
                    counterMap.put(key.namespace, new HashMap<String, ConcurrentHashMap<String, Tuple<Long, Integer>>>());
                }
            }
        }
        if (!counterMap.get(key.namespace).containsKey(key.setName)) {
            synchronized (counterMap.get(key.namespace)) {
                if (!counterMap.get(key.namespace).containsKey(key.setName)) {
                    counterMap.get(key.namespace).put(key.setName, new ConcurrentHashMap<String, Tuple<Long, Integer>>());
                }
            }
        }
        counterMap.get(key.namespace).get(key.setName).putIfAbsent(key.userKey.toString(), new Tuple<Long, Integer>(0L, null));
        try{
            Tuple<Long, Integer> counterWithTTL = counterMap.get(key.namespace).get(key.setName).get(key.userKey.toString());
            long newCount;
            synchronized (counterWithTTL) {
                counterWithTTL.setSecond(ttl);
                counterWithTTL.setFirst(counterWithTTL.getFirst() + by);
                newCount = counterWithTTL.getFirst();
            }
            return newCount;
        }catch (NullPointerException e){
            //Only possible if got deleted after adding, then not to be added again
            return -1;
        }
    }

    public void deleteCounterKey(Key key) {
        if (counterMap.containsKey(key.namespace) && counterMap.get(key.namespace).containsKey(key.setName)) {
            counterMap.get(key.namespace).get(key.setName).remove(key.userKey.toString());
        }
    }

    private void update() {
        for (Map.Entry<String, Map<String, ConcurrentHashMap<String, Tuple<Long, Integer>>>> namespaceEntry : counterMap.entrySet()) {
            String namespace = namespaceEntry.getKey();
            for (Map.Entry<String, ConcurrentHashMap<String, Tuple<Long, Integer>>> setEntry : counterMap.get(namespace).entrySet()) {
                String setName = setEntry.getKey();
                for (Map.Entry<String, Tuple<Long, Integer>> counterEntry : counterMap.get(namespace).get(setName).entrySet()) {
                    Key key = new Key(namespace, setName, counterEntry.getKey());
                    long incrValue;
                    Integer ttl;
                    synchronized (counterEntry.getValue()) {
                        incrValue = counterEntry.getValue().getFirst();
                        ttl = counterEntry.getValue().getSecond();
                        counterEntry.getValue().setFirst(0L);
                        counterEntry.getValue().setSecond(null);
                    }
                    try {
                        if(incrValue != 0L || ttl != null) {
                            if (ttl == null) {
                                iAerospikeClient.incrCounter(key, incrValue);
                            } else {
                                iAerospikeClient.incrCounter(key, incrValue, ttl);
                            }
                        }
                    } catch (OperationException e) {
                        LOGGER.error("Unable to perform periodic update of counter for key: " + counterEntry.getKey(), e);
                    }
                }
            }
        }
    }
}
