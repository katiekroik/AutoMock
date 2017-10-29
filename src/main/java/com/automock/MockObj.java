package com.automock;

import static com.automock.AutoMock.decapitalize;

/**
 * Created by Katie on 10/28/17.
 */
public class MockObj {

    private final Object obj;
    private final int num;
    private final Class clazz;

    public MockObj(Object obj, int num, Class clazz) {
        this.obj = obj;
        this.num = num;
        this.clazz = clazz;
    }

    public Object getObj() {
        return obj;
    }

    public String getName() {
        return decapitalize(AutoMock.getType(obj)) + num;
    }

    public String getType() {
        return AutoMock.getType(obj);
    }

    public Class getClazz() {
        return clazz;
    }
}
