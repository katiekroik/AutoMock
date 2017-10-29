package com.automock;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Katie on 10/28/17.
 */
public class TestObj {

    private String one;
    private Integer two;
    private TestSubObj testSubObj;
    private Integer[] arr;
    private Map map;


    public TestObj() {
    }

    public void setOne(String one) {
        this.one = one;
    }

    public void setTwo(Integer two) {
        this.two = two;
    }

    public void setTestSubObj(TestSubObj testSubObj) {
        this.testSubObj = testSubObj;
    }

    public String getOne() {
        return one;
    }

    public Integer getTwo() {
        return two;
    }

    public String getBoth() {
        return one + two;
    }

    public TestSubObj getTest() {
        return testSubObj;
    }

    public TestSubObj getTestSubObj() {
        return testSubObj;
    }

    public Integer[] getArr() {
        return arr;
    }

    public void setArr(Integer[] arr) {
        this.arr = arr;
    }

    public Map getMap() {
        return map;
    }

    public void setMap(Map map) {
        this.map = map;
    }
}
