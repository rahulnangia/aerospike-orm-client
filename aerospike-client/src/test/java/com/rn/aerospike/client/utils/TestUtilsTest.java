package com.rn.aerospike.client.utils;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Created by rahul.nangia
 */
public class TestUtilsTest {

    @Test
    public void testGetSumIntgers(){
        TestUtils utils = new TestUtils();

        TestUtils mockObject = Mockito.mock(TestUtils.class);
        utils.setTestUtils(mockObject);

        //pass both integers
        Assert.assertEquals(utils.getSum(5,6), 11);

        Mockito.when(mockObject.getSum(Mockito.anyInt(), Mockito.anyInt())).thenReturn(17);
        Assert.assertEquals(utils.getAppendedSum(8,9), 27);
        Assert.assertEquals(utils.getAppendedSum(8,9), 27);
        Mockito.verify(mockObject, Mockito.times(2));

    }
    @Test
    public void testGetSumImproved(){
        TestUtils utils = new TestUtils();

        Assert.assertEquals(utils.getSumImproved(5,6, 8), 11);
        Assert.assertEquals(utils.getSumImproved(null,6, 8), 6);
        Assert.assertEquals(utils.getSumImproved(5,null,8), 5);
        Assert.assertEquals(utils.getSumImproved(null,null,8), 0);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testGetSumNullObject(){
        TestUtils utils = new TestUtils();

        //pass both integers
        utils.getSum(null,6);
    }
}
