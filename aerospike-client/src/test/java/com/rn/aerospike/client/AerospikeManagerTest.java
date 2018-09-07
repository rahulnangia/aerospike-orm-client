package com.rn.aerospike.client;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.rn.aerospike.client.exceptions.InvalidDTOException;
import com.rn.aerospike.client.exceptions.OperationException;
import com.rn.aerospike.client.model.AerospikeLockValue;
import com.rn.aerospike.client.operations.Operation;
import com.rn.aerospike.client.test.AerospikeConvertibleEntityA;
import com.rn.aerospike.client.test.AerospikeConvertibleEntityB;
import com.rn.aerospike.client.test.AerospikeCustomEntityA;
import com.rn.aerospike.client.test.AerospikeCustomEntityB;
import com.rn.aerospike.client.test.AerospikeJsonEntity;
import com.rn.aerospike.client.test.AerospikeTestEntityA;
import com.rn.aerospike.client.test.AerospikeTestEntityB;
import com.rn.aerospike.client.test.AerospikeTestEnum;
import com.rn.aerospike.client.test.AerospikeTestErrorEntityA;
import com.rn.aerospike.client.test.AerospikeTestLock;
import com.rn.aerospike.client.test.TestCounter;
import com.rn.aerospike.common.annotations.StorableBin;
import com.rn.aerospike.common.records.AerospikeRecord;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by rahul
 */
public class AerospikeManagerTest {

    private AerospikeClient client;
    private String COUNTER_KEY;
    private AerospikeTestEntityA MAIN_ENTITY;
    private static Map<Class<? extends AerospikeRecord>, PropertyDescriptor[]> classFieldMap;

    @BeforeClass
    public void setup() {
        client = new AerospikeClient("172.28.128.3", 3000, true);
        COUNTER_KEY = "counter";
        MAIN_ENTITY = getDummyTestEntityA("key1");
        classFieldMap = new HashMap<>();
    }

    @AfterClass
    public void destroy() {
        client.close();
    }

    @Test(groups = "entityInitTest", expectedExceptions = InvalidDTOException.class)
    public void testEntityInit() throws OperationException {
        client.get("", AerospikeTestErrorEntityA.class);
    }
    
    @Test(groups = "put")
    public void testPut() throws OperationException {
        client.put(MAIN_ENTITY);
    }

    @Test(dependsOnGroups = "exists")
    public void testPutTTL() throws OperationException, InterruptedException {
        AerospikeTestEntityA entity = AerospikeTestEntityA.newBuilder().key("testPutIfNotExist").build();
        client.put(entity, 3);
        Assert.assertTrue(client.exists(entity));
        Thread.sleep(4000);
        Assert.assertFalse(client.exists(entity));
    }

    @Test(dependsOnGroups = "delete")
    public void testPutIfNotExist() throws Exception {
        AerospikeTestEntityA entity1 = AerospikeTestEntityA.newBuilder().key("testPutIfNotExist").build();
        AerospikeTestEntityA entity2 = AerospikeTestEntityA.newBuilder().key("testPutIfNotExist").build();
        client.put(entity1);
        Assert.assertEquals(false, client.putIfNotExists(entity2));

        // Delete all newly created objects
        client.delete(entity1);
    }

    @Test(dependsOnGroups = "delete")
    public void testPutIfNotExistWithTTL() throws Exception {
        AerospikeTestEntityA entity1 = AerospikeTestEntityA.newBuilder().key("testPutIfNotExistTTL").build();
        AerospikeTestEntityA entity2 = AerospikeTestEntityA.newBuilder().key("testPutIfNotExistTTL").build();
        client.putIfNotExists(entity1, 2);
        Assert.assertTrue(client.exists(entity1));
        Assert.assertEquals(false, client.putIfNotExists(entity2));
        Thread.sleep(3000);
        Assert.assertFalse(client.exists(entity1));
    }

    @Test(dependsOnGroups = "put")
    public void testReadRecord() throws OperationException {
        // Positive
        AerospikeTestEntityA actual = AerospikeTestEntityA.newBuilder().key("key1").build();
        boolean read = client.read(actual);
        Assert.assertTrue(read);
        Assert.assertEquals(actual, MAIN_ENTITY);

        // Negative
        actual = AerospikeTestEntityA.newBuilder().key("key-non-existent").build();
        read = client.read(actual);
        Assert.assertFalse(read);
        Assert.assertNotEquals(actual, MAIN_ENTITY);
    }

    @Test(groups = "get", dependsOnGroups = "put")
    public void testGetKey() throws OperationException {
        // Positive
        AerospikeTestEntityA actual = client.get("key1", AerospikeTestEntityA.class);
        Assert.assertEquals(actual, MAIN_ENTITY);

        // Negative
        actual = client.get("key-non-existent", AerospikeTestEntityA.class);
        Assert.assertNull(actual);
    }

    @Test(groups = "get", dependsOnGroups = "put")
    public void testGetKeySpecificBins() throws OperationException, IllegalAccessException, IntrospectionException, InvocationTargetException {
        // Positive
        AerospikeTestEntityA actual = client.get("key1", AerospikeTestEntityA.class, "string", "set");
        assertAllBinsNull(actual, "string", "set");
        Assert.assertEquals(actual.getValue1(), MAIN_ENTITY.getValue1());
        Assert.assertEquals(actual.getValue2(), MAIN_ENTITY.getValue2());

        // Non-existent bin specified
        actual = client.get("key1", AerospikeTestEntityA.class, "string", "set", "nonExistBin");
        assertAllBinsNull(actual, "string", "set");
        Assert.assertEquals(actual.getValue1(), MAIN_ENTITY.getValue1());
        Assert.assertEquals(actual.getValue2(), MAIN_ENTITY.getValue2());
    }

    @Test(groups = "exists", dependsOnGroups = "put")
    public void testExistsRecord() throws Exception {
        Assert.assertTrue(client.exists(MAIN_ENTITY));
        Assert.assertFalse(client.exists(AerospikeTestEntityA.newBuilder().key("key2").build()));
    }

    @Test(groups = "exists", dependsOnGroups = "put")
    public void testExistsKey() throws Exception {
        Assert.assertTrue(client.exists(MAIN_ENTITY.getAerospikeKey(), AerospikeTestEntityA.class));
        Assert.assertFalse(client.exists("key2", AerospikeTestEntityA.class));
    }

    @Test(groups = "delete", dependsOnGroups = "exists")
    public void testDeleteRecord() throws Exception {
        AerospikeTestEntityA entity = AerospikeTestEntityA.newBuilder().key("del1").value1("abc").build();
        client.put(entity);
        Assert.assertTrue(client.exists(entity));
        client.delete(entity);
        Assert.assertFalse(client.exists(entity));
    }

    @Test(groups = "delete", dependsOnGroups = "exists")
    public void testDeleteKeys() throws Exception {
        AerospikeTestEntityA entity1 = AerospikeTestEntityA.newBuilder().key("del1").value1("abc").build();
        AerospikeTestEntityA entity2 = AerospikeTestEntityA.newBuilder().key("del1").value1("abc").build();
        client.put(entity1);
        client.put(entity2);
        Assert.assertTrue(client.exists(entity1));
        Assert.assertTrue(client.exists(entity2));
        client.delete(AerospikeTestEntityA.class, entity1.getAerospikeKey(), entity2.getAerospikeKey());
        Assert.assertFalse(client.exists(entity1));
        Assert.assertFalse(client.exists(entity2));
    }

    @Test(dependsOnGroups = {"put", "delete"})
    public void testGetBulkHomogeneousRequest() throws Exception {
        // Insert entity
        AerospikeTestEntityA entity1 = getDummyTestEntityA("entity1");
        AerospikeTestEntityA entity2 = getDummyTestEntityA("entity2");
        AerospikeTestEntityA entity3 = getDummyTestEntityA("entity3");
        client.put(entity1);
        client.put(entity2);

        Map<String, AerospikeTestEntityA> bulkResponse = client.getBulk(AerospikeTestEntityA.class, new String[]{"entity1",
                "entity3", "entity2"});
        Assert.assertNotNull(bulkResponse);
        Assert.assertEquals(bulkResponse.size(), 3);
        Assert.assertEquals(bulkResponse.get("entity1"), entity1);
        Assert.assertEquals(bulkResponse.get("entity2"), entity2);
        Assert.assertNull(bulkResponse.get("entity3"));

        client.delete(entity1, entity2);
    }

    @Test(dependsOnGroups = {"put", "delete"})
    public void testGetBulkHomogeneousRequestWithSpecificBins() throws Exception {
        // Insert entity
        AerospikeTestEntityA entity1 = getDummyTestEntityA("entity1");
        AerospikeTestEntityA entity2 = getDummyTestEntityA("entity2");
        AerospikeTestEntityA entity3 = getDummyTestEntityA("entity3");
        client.put(entity1);
        client.put(entity2);

        Map<String, AerospikeTestEntityA> bulkResponse = client.getBulk(AerospikeTestEntityA.class, new String[]{"entity1",
                "entity3", "entity2"}, "enum", "map");
        Assert.assertNotNull(bulkResponse);
        Assert.assertEquals(bulkResponse.size(), 3);
        assertAllBinsNull(bulkResponse.get("entity1"), "enum", "map");
        assertAllBinsNull(bulkResponse.get("entity2"), "enum", "map");
        Assert.assertEquals(bulkResponse.get("entity1").getValue8(), entity1.getValue8());
        Assert.assertEquals(bulkResponse.get("entity1").getValue12(), entity1.getValue12());
        Assert.assertEquals(bulkResponse.get("entity2").getValue8(), entity2.getValue8());
        Assert.assertEquals(bulkResponse.get("entity2").getValue12(), entity2.getValue12());
        Assert.assertNull(bulkResponse.get("entity3"));

        client.delete(entity1, entity2);
    }

    @Test(dependsOnGroups = {"put", "delete"})
    public void testReadBulkHeterogeneousRequest() throws Exception {
        // Insert entity
        AerospikeTestEntityA entity1 = getDummyTestEntityA("entity1");
        AerospikeTestEntityA entity2 = getDummyTestEntityA("entity2");
        AerospikeTestEntityB entityB = AerospikeTestEntityB.builder().value1("entityB").value2(1D).build();
        client.put(entity1);
        client.put(entity2);
        client.put(entityB);

        AerospikeTestEntityA searchEntity1 = AerospikeTestEntityA.newBuilder().key("entity1").build();
        AerospikeTestEntityA searchEntity2 = AerospikeTestEntityA.newBuilder().key("entity2").build();
        AerospikeTestEntityA searchEntity3 = AerospikeTestEntityA.newBuilder().key("entity3").build();
        AerospikeTestEntityB searchEntityB = AerospikeTestEntityB.builder().value1("entityB").build();

        Map<String, AerospikeRecord> bulkResponse = client.readBulk(new AerospikeRecord[]{searchEntity1, searchEntity2, searchEntity3, searchEntityB});
        Assert.assertNotNull(bulkResponse);
        Assert.assertEquals(bulkResponse.size(), 4);
        Assert.assertEquals(bulkResponse.get("entity1"), entity1);
        Assert.assertEquals(bulkResponse.get("entity2"), entity2);
        Assert.assertEquals(bulkResponse.get("entityB"), entityB);

        // Since Read call, same objects should have been populated
        Assert.assertTrue(bulkResponse.get("entity1") == searchEntity1);
        Assert.assertTrue(bulkResponse.get("entity2") == searchEntity2);
        Assert.assertTrue(bulkResponse.get("entityB") == searchEntityB);

        client.delete(entity1, entity2, entityB);
    }

    @Test(dependsOnGroups = {"put", "delete"})
    public void testReadBulkHeterogeneousRequestWithSpecificBins() throws Exception {
        // Insert entity
        AerospikeTestEntityA entity1 = getDummyTestEntityA("entity1");
        AerospikeTestEntityA entity2 = getDummyTestEntityA("entity2");
        AerospikeTestEntityB entityB = AerospikeTestEntityB.builder().value1("entityB").value2(1D).build();
        client.put(entity1);
        client.put(entity2);
        client.put(entityB);
        AerospikeTestEntityA searchEntity1 = AerospikeTestEntityA.newBuilder().key("entity1").build();
        AerospikeTestEntityA searchEntity2 = AerospikeTestEntityA.newBuilder().key("entity2").build();
        AerospikeTestEntityA searchEntity3 = AerospikeTestEntityA.newBuilder().key("entity3").build();
        AerospikeTestEntityB searchEntityB = AerospikeTestEntityB.builder().value1("entityB").build();

        Map<String, AerospikeRecord> bulkResponse = client.readBulk(new AerospikeRecord[]{searchEntity1, searchEntity2, searchEntity3, searchEntityB}, "custom", "customList", "double", "non-existent");
        Assert.assertNotNull(bulkResponse);
        Assert.assertEquals(bulkResponse.size(), 4);
        assertAllBinsNull(bulkResponse.get("entity1"), "custom", "customList", "double");
        assertAllBinsNull(bulkResponse.get("entity2"), "custom", "customList", "double");
        assertAllBinsNull(bulkResponse.get("entityB"), "string", "double");
        Assert.assertEquals(((AerospikeTestEntityA) bulkResponse.get("entity1")).getValue3(), entity1.getValue3());
        Assert.assertEquals(((AerospikeTestEntityA) bulkResponse.get("entity1")).getValue13(), entity1.getValue13());
        Assert.assertEquals(((AerospikeTestEntityA) bulkResponse.get("entity1")).getValue14(), entity1.getValue14());
        Assert.assertEquals(((AerospikeTestEntityA) bulkResponse.get("entity2")).getValue3(), entity2.getValue3());
        Assert.assertEquals(((AerospikeTestEntityA) bulkResponse.get("entity2")).getValue13(), entity2.getValue13());
        Assert.assertEquals(((AerospikeTestEntityA) bulkResponse.get("entity2")).getValue14(), entity2.getValue14());
        Assert.assertEquals(((AerospikeTestEntityB) bulkResponse.get("entityB")).getValue2(), entityB.getValue2());

        // Since Read call, same objects should have been populated
        Assert.assertTrue(bulkResponse.get("entity1") == searchEntity1);
        Assert.assertTrue(bulkResponse.get("entity2") == searchEntity2);
        Assert.assertTrue(bulkResponse.get("entityB") == searchEntityB);

        client.delete(entity1, entity2, entityB);
    }

    @Test(groups = "setGetCounter")
    public void testCounterSetGetExpiry() throws Exception {

        long setValue = client.setCounter(TestCounter.class, COUNTER_KEY, 5, 5);
        long recdValue = client.getCounter(TestCounter.class, COUNTER_KEY);

        Assert.assertEquals(setValue, 5);
        Assert.assertEquals(recdValue, 5);

        Thread.sleep(6000);
        long recdAfterExpiry = client.getCounter(TestCounter.class, COUNTER_KEY);
        Assert.assertEquals(-1, recdAfterExpiry);

    }

    @Test(groups = "deleteCounter", dependsOnGroups = "setGetCounter")
    public void testDeleteCounter() throws Exception {
        client.setCounter(TestCounter.class, COUNTER_KEY, 5, 1000);
        long recdValue = client.getCounter(TestCounter.class, COUNTER_KEY);
        Assert.assertNotEquals(recdValue, -1);
        client.deleteCounter(TestCounter.class, COUNTER_KEY);
        Assert.assertEquals(client.getCounter(TestCounter.class, COUNTER_KEY), -1);
    }

    @Test(dependsOnGroups = "deleteCounter")
    public void testIncrCounter() throws Exception {
        client.setCounter(TestCounter.class, COUNTER_KEY, 5, 1000);
        long incrValue = client.incrCounter(TestCounter.class, COUNTER_KEY, 2);
        long recdValue = client.getCounter(TestCounter.class, COUNTER_KEY);

        Assert.assertEquals(incrValue, 7);
        Assert.assertEquals(recdValue, 7);

        // Delete all newly created objects
        client.deleteCounter(TestCounter.class, COUNTER_KEY);
    }

    @Test(dependsOnGroups = "setGetCounter")
    public void testIncrCounterWithTTL() throws Exception {
        client.setCounter(TestCounter.class, COUNTER_KEY, 5, 1000);
        long incrValue = client.incrCounter(TestCounter.class, COUNTER_KEY, 2, 3);
        long recdValue = client.getCounter(TestCounter.class, COUNTER_KEY);

        Assert.assertEquals(incrValue, 7);
        Assert.assertEquals(recdValue, 7);
        Thread.sleep(4000);
        Assert.assertEquals(client.getCounter(TestCounter.class, COUNTER_KEY), -1);
    }

    @Test(dependsOnGroups = "deleteCounter")
    public void testDecrCounter() throws Exception {
        client.setCounter(TestCounter.class, COUNTER_KEY, 5, 1000);
        long decrValue = client.decrCounter(TestCounter.class, COUNTER_KEY, 2);
        long recdValue = client.getCounter(TestCounter.class, COUNTER_KEY);

        Assert.assertEquals(decrValue, 3);
        Assert.assertEquals(recdValue, 3);
        // Delete all newly created objects
        client.deleteCounter(TestCounter.class, COUNTER_KEY);
    }

    @Test(dependsOnGroups = "setGetCounter")
    public void testDecrCounterWithTTL() throws Exception {
        client.setCounter(TestCounter.class, COUNTER_KEY, 5, 1000);
        long decrValue = client.decrCounter(TestCounter.class, COUNTER_KEY, 2, 3);
        long recdValue = client.getCounter(TestCounter.class, COUNTER_KEY);

        Assert.assertEquals(decrValue, 3);
        Assert.assertEquals(recdValue, 3);
        Thread.sleep(4000);
        Assert.assertEquals(client.getCounter(TestCounter.class, COUNTER_KEY), -1);
    }

    @Test(dependsOnGroups = "deleteCounter")
    public void testGetBulkCounter() throws OperationException {
        String newCounter = "newCounter";
        String nonExistentCounter = "nonExistentCounter";
        client.setCounter(TestCounter.class, COUNTER_KEY, 10, 1000);
        client.setCounter(TestCounter.class, newCounter, 15, 1000);
        Map<String, Long> bulkCountersResponse1 = client.getBulkCounters(TestCounter.class, COUNTER_KEY, newCounter,
                nonExistentCounter);
        Map<String, Long> bulkCountersResponse2 = client.getBulkCounters(TestCounter.class,
                Lists.newArrayList(COUNTER_KEY, newCounter, nonExistentCounter));

        Assert.assertNotNull(bulkCountersResponse1);
        Assert.assertNotNull(bulkCountersResponse2);
        Assert.assertEquals(bulkCountersResponse1.size(), 3);
        Assert.assertEquals(bulkCountersResponse2.size(), 3);
        Assert.assertTrue(bulkCountersResponse1.get(COUNTER_KEY) == 10);
        Assert.assertTrue(bulkCountersResponse2.get(COUNTER_KEY) == 10);
        Assert.assertTrue(bulkCountersResponse1.get(newCounter) == 15);
        Assert.assertTrue(bulkCountersResponse2.get(newCounter) == 15);
        Assert.assertTrue(bulkCountersResponse1.get(nonExistentCounter) == -1);
        Assert.assertTrue(bulkCountersResponse2.get(nonExistentCounter) == -1);
        client.deleteCounter(TestCounter.class, COUNTER_KEY);
        client.deleteCounter(TestCounter.class, newCounter);
    }

    @Test(dependsOnGroups = "delete")
    public void testStoreWithOperation() throws Exception {
        AerospikeTestEntityA testEntity1 = getDummyTestEntityA("entity1");
        client.put(testEntity1);

        client.add(testEntity1, new Operation<AerospikeTestEntityA>() {
            @Override
            public AerospikeTestEntityA operate(AerospikeTestEntityA newRecord, AerospikeTestEntityA foundRecord)
                    throws OperationException {
                if (foundRecord != null) {
                    foundRecord.setValue1(newRecord.getValue1().concat(foundRecord.getValue1()));
                }
                return foundRecord;
            }
        }, 1);

        AerospikeTestEntityA fetchedContent = client.get(testEntity1.getAerospikeKey(), AerospikeTestEntityA.class);
        Assert.assertEquals(testEntity1.getValue1().concat(testEntity1.getValue1()), fetchedContent.getValue1());

        // Delete all newly created objects
        client.delete(testEntity1);
    }

    @Test(groups = "lockTTL", dependsOnGroups = "get")
    public void testAcquireLockWithTTl() throws OperationException, InterruptedException {
        boolean acquired = client.acquireLock(AerospikeTestLock.class, "lockKey", 4);
        boolean acquired2 = client.acquireLock(AerospikeTestLock.class, "lockKey", 4);
        Thread.sleep(5000);
        boolean acquired3 = client.acquireLock(AerospikeTestLock.class, "lockKey", 4);
        AerospikeTestLock lockObject = client.get("lockKey", AerospikeTestLock.class);
        Assert.assertTrue(acquired);
        Assert.assertFalse(acquired2);
        Assert.assertTrue(acquired3);
        Assert.assertNotNull(lockObject);
        Assert.assertEquals(AerospikeLockValue.AerospikeLockValueEnum.PROCESSING, lockObject.getValue().getValue());
    }

    @Test(dependsOnGroups = {"delete", "exists", "lockTTL"})
    public void testAcquireAndReleaseLock() throws OperationException, InterruptedException {
        boolean acquired = client.acquireLock(AerospikeTestLock.class, "lockKey2");
        Assert.assertTrue(acquired);
        Thread.sleep(5000);
        Assert.assertTrue(client.exists("lockKey2", AerospikeTestLock.class));
        client.releaseLock(AerospikeTestLock.class, "lockKey2");
        Assert.assertFalse(client.exists("lockKey2", AerospikeTestLock.class));
    }

    @Test(expectedExceptions = OperationException.class)
    public void testStoreWithUnsuccessfulOperation() throws Exception {
        AerospikeTestEntityA testEntity1 = getDummyTestEntityA("entity1");
        client.put(testEntity1);

        client.add(testEntity1, new Operation<AerospikeTestEntityA>() {
            @Override
            public AerospikeTestEntityA operate(AerospikeTestEntityA newRecord, AerospikeTestEntityA foundRecord)
                    throws OperationException {
                if (foundRecord != null) {
                    foundRecord.setValue1(null);
                }
                throw new OperationException();
            }
        }, 1);
    }

    private void assertAllBinsNull(AerospikeRecord record, String... nonNullFields) throws IntrospectionException, InvocationTargetException, IllegalAccessException {
        Set<String> nonNullFieldSet = nonNullFields == null || nonNullFields.length == 0 ? null : Sets.newHashSet(nonNullFields);
        Class<? extends AerospikeRecord> recordClass = record.getClass();
        if (classFieldMap.get(recordClass) == null) {
            BeanInfo beanInfo = Introspector.getBeanInfo(recordClass);
            classFieldMap.put(recordClass, beanInfo.getPropertyDescriptors());
        }
        for (PropertyDescriptor descriptor : classFieldMap.get(recordClass)) {
            StorableBin storableBinAnnotation = null;
            try {
                storableBinAnnotation = recordClass.getDeclaredField(descriptor.getName()).getAnnotation(StorableBin.class);
            } catch (NoSuchFieldException e) {
                continue;
            }
            if (storableBinAnnotation != null) {
                if (nonNullFieldSet == null || !nonNullFieldSet.contains(storableBinAnnotation.name())) {
                    Object value = descriptor.getReadMethod().invoke(record);
                    if (descriptor.getPropertyType().isPrimitive()) {
                        if (descriptor.getPropertyType().equals(int.class)) {
                            Assert.assertEquals(value, (int) 0);
                        } else if (descriptor.getPropertyType().equals(short.class)) {
                            Assert.assertEquals(value, (short) 0);
                        } else if (descriptor.getPropertyType().equals(float.class)) {
                            Assert.assertEquals(value, 0F);
                        } else if (descriptor.getPropertyType().equals(long.class)) {
                            Assert.assertEquals(value, 0L);
                        } else if (descriptor.getPropertyType().equals(double.class)) {
                            Assert.assertEquals(value, 0.0D);
                        } else if (descriptor.getPropertyType().equals(byte.class)) {
                            Assert.assertEquals(value, (byte) 0);
                        } else if (descriptor.getPropertyType().equals(char.class)) {
                            Assert.assertEquals(value, '\u0000');
                        } else if (descriptor.getPropertyType().equals(boolean.class)) {
                            Assert.assertEquals(value, false);
                        }else {
                            Assert.fail("Primitive Type Check Failed");
                        }
                    } else {
                        Assert.assertNull(value);
                    }
                }
            }
        }
    }

    public static AerospikeTestEntityA getDummyTestEntityA(String key) {
        return AerospikeTestEntityA.newBuilder().key(key).value1("value1").value2(new HashSet<String>() {
            {
                add("set-1");
                add("set-2");
            }
        }).value3(1.0d).value4(1.0f).value5(1).value6(1L).value7(true).value8(AerospikeTestEnum.TEST1)
                .value9("value9".getBytes()).value10(new AerospikeJsonEntity("json-1", 1))
                .value11(new ArrayList<String>() {
                    {
                        add("list-1");
                        add("list-1");
                        add("list-2");
                    }
                }).value12(new HashMap<String, String>() {
                    {
                        put("key1", "value1");
                        put("key2", "value2");
                    }
                }).value13(new AerospikeCustomEntityA("custom-1", 1)).value14(new ArrayList<AerospikeCustomEntityA>() {
                    {
                        add(new AerospikeCustomEntityA("custom-1", 1));
                        add(new AerospikeCustomEntityA("custom-2", 2));
                    }
                }).value15(new AerospikeConvertibleEntityA("convertible-1", 1))
                .value16(new ArrayList<AerospikeConvertibleEntityA>() {
                    {
                        add(new AerospikeConvertibleEntityA("convertibleList-1", 1));
                        add(new AerospikeConvertibleEntityA("convertibleList-2", 2));
                    }
                }).value17(new HashMap<AerospikeCustomEntityA, AerospikeCustomEntityB>() {
                    {
                        put(new AerospikeCustomEntityA("custom-map-key-1", 1),
                                new AerospikeCustomEntityB("custom-map-value-1", 1, true));
                        put(new AerospikeCustomEntityA("custom-map-key-2", 1),
                                new AerospikeCustomEntityB("custom-map-value-2", 1, false));
                    }
                }).value18(new HashMap<String, AerospikeCustomEntityB>() {
                    {
                        put("customMap2-key-1", new AerospikeCustomEntityB("custom-map-value-1", 1, true));
                        put("customMap2-key-2", new AerospikeCustomEntityB("custom-map-value-2", 1, false));
                    }
                }).value19(new HashMap<AerospikeConvertibleEntityA, AerospikeConvertibleEntityB>() {
                    {
                        put(new AerospikeConvertibleEntityA("convertibleMap-key-1", 1),
                                new AerospikeConvertibleEntityB("convertibleMap-value-1", 1, true));
                        put(new AerospikeConvertibleEntityA("convertibleMap-key-2", 1),
                                new AerospikeConvertibleEntityB("convertibleMap-value-2", 1, false));
                    }
                }).value20(new HashMap<String, AerospikeConvertibleEntityB>() {
                    {
                        put("convertiblMap2-key-1", new AerospikeConvertibleEntityB("convertibleMap-value-1", 1, true));
                        put("convertiblMap2-key-2",
                                new AerospikeConvertibleEntityB("convertibleMap-value-2", 1, false));
                    }
                }).value21(new HashMap<String, List<AerospikeConvertibleEntityA>>() {
                    {
                        put("customMap3key1",
                                Lists.newArrayList(new AerospikeConvertibleEntityA("customMap3value1", 100),
                                        new AerospikeConvertibleEntityA("customMap3value2", 200)));
                        put("customMap3key2",
                                Lists.newArrayList(new AerospikeConvertibleEntityA("customMap3value3", 300),
                                        new AerospikeConvertibleEntityA("customMap3value4", 400)));
                    }
                }).value22(new HashMap<String, Set<String>>() {
                    {
                        put("customMap4key1", Sets.newHashSet("setV1", "setV2"));
                        put("customMap4key2", Sets.newHashSet("setV3", "setV4"));
                    }
                }).value23(new HashSet<Integer>() {
                    {
                        add(1);
                        add(2);
                    }
                }).build();
    }


}
