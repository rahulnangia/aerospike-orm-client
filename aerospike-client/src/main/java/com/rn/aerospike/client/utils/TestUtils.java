package com.rn.aerospike.client.utils;

/**
 * Created by rahul.nangia
 */
public class TestUtils {

    private TestUtils testUtils;

    public TestUtils() {

    }

    public int getSum(Integer a, Integer b){
        return a+b;
    }

    public int getSumImproved(Integer a, Integer b, Integer c){
        return (a==null? 0: a)+(b==null? 0: b) + c;
    }

    public int getAppendedSum(Integer a, Integer b){
        int c=10;
        int temp = testUtils.getSum(a, b);
        return temp +  c;
    }

    public void setTestUtils(TestUtils testUtils) {
        this.testUtils = testUtils;
    }
}
