package com.automock;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Created by Katie on 10/28/17.
 */
public class AutoMock {

    private File fout;
    private FileOutputStream fos;
    private BufferedWriter bw;
    private List<MockObj> mockObjList;
    private Map<Class, Integer> numOfClasses;

    public AutoMock(Object... objs) throws IOException, InvocationTargetException, IllegalAccessException {
        String absPath = System.getProperty("user.dir");
        absPath = absPath.replace("main", "automock");
        mockObjList = new LinkedList<>();
        numOfClasses = new HashMap<>();
        String fileName = absPath + "/TestMock.java";
        System.out.println(absPath);
        int inc = 1;
        fout = new File(fileName);
        while (fout.exists() && !fout.isDirectory()) {
            fout = new File("TestMock" + inc++ + ".java");
        }
        fos = new FileOutputStream(fout);

        bw = new BufferedWriter(new OutputStreamWriter(fos));
        write("import org.junit.Before;");
        write("import org.junit.Test;");
        write("import static org.mockito.Mockito.mock;");
        write("import static org.mockito.Mockito.when;");
        write("import static org.junit.Assert.assertEquals;");
        write("import static org.junit.Assert.assertTrue;");
        write("import static org.junit.Assert.assertNotNull;");

        for (Object o: objs) {
            writeImport(o);
        }
        write("public class TestMock {");
        for (Object o: objs) {
            mockObjList.add(new MockObj(o, getNum(o), o.getClass()));
        }

        for (MockObj obj: mockObjList) {
            initialize(obj);
        }
        writeBefore();
        for (MockObj obj: mockObjList) {
            processObj(obj);
        }
        write("\t}");
        for (MockObj obj: mockObjList) {
            writeBasicTest(obj);
        }
        writeEnd();
        bw.close();
    }

    private int getNum(Object o) {
        int n = 0;
        if (numOfClasses.containsKey(o.getClass())) {
            n = numOfClasses.get(o.getClass());
            numOfClasses.put(o.getClass(), n + 1);
        } else {
            numOfClasses.put(o.getClass(), 1);
        }
        return n;
    }


    private void writeImport(Object o) throws IOException {
        write("import " + o.getClass().getTypeName() + ";");
    }

    private void writeBefore() throws IOException {
        write("\t@Before");
        write("\tpublic void init() {");
    }

    private void writeEnd() throws IOException {
        write("}");
    }

    private void initialize(MockObj o) throws IOException {
        write("\t" + o.getType() + " " + o.getName() + ";");
    }

    private void processObj(MockObj o) throws InvocationTargetException, IllegalAccessException, IOException {
        if (o == null) {
            return;
        }
        write("\t\t" + o.getName() + " = mock(" + o.getType() + ".class);");
        for (Method method: o.getObj().getClass().getMethods()) {
            if (isClassMethod(method)) {
                continue;
            }
            if (Modifier.isPublic(method.getModifiers()) && method.getParameters().length == 0) {
                Object res = null;
                try {
                    res = method.invoke(o.getObj());
                } catch (Throwable t) {

                }
                if (res != null) {
                    if (res instanceof String) {
                        res = "\"" + res + "\"";
                        write("\t\twhen(" + o.getName() + "." + method.getName() + "()).thenReturn(" + res + ");");
                    } else if (res instanceof Number) {
                        write("\t\twhen(" + o.getName() + "." + method.getName() + "()).thenReturn(" + res + ");");
                    } else if (res.getClass().isArray() || res instanceof Map || res instanceof List || res instanceof Set) {
                        String mockName = mock(res);
                        write("\t\twhen(" + o.getName() + "." + method.getName() + "()).thenReturn(" + mockName + ");");
                    } else { // some other mocked object
                        String mockName = decapitalize(getType(o.getName())) + getNum(res);
                        write("\t\t" + getType(res) + " " + mockName + "= mock(" + getType(res) + ".class);");
                        write("\t\twhen(" + o.getName() + "." + method.getName() + "()).thenReturn(" + mockName + ");");
                    }
                }
            }
        }
    }

    private String mock(Object o) throws IOException {
        if (o instanceof String) {
            return "\"" + o + "\"";
        } else if (o instanceof Number) {
            return o.toString();
        } else if (o.getClass().isArray()) {
            Object[] array = (Object[]) o;
            String type = getType(array[0]);
            String name = decapitalize(type) + getNum(array);
            write("\t\t" + type + "[] " + name  + " = new " + type + "[" + array.length + "];");
            for (int i = 0; i < array.length; i++) {
                Object entry = array[i];
                String mockName = mock(entry);
                write("\t\t" + name + "[" + i + "]=" + mockName + ";");
            }
            return name;
        } else if (o instanceof Map) {
            String name = "map" + getNum(o);
            write("\t\tMap " + name + " = new HashMap<>();");
            for (Object entry: ((Map) o).entrySet()) {
                Object key = mock(((Map.Entry) entry).getKey());
                Object value = mock(((Map.Entry) entry).getValue());
                write("\t\t" + name + ".put(" + key + ", " + value + ");");
            }
            return name;
        } else if (o instanceof Set || o instanceof List) {
            String name = "";
            if (o instanceof List) {
                name = "list" + getNum(o);
                write("\t\tList " + name + " = new LinkedList<>();");
            } else {
                name = "set" + getNum(o);
                write("\t\tSet " + name + " = new HashSet<>();");
            }
            for (Object obj: ((Collection) o)) {
                String mockName = mock(obj);
                write("\t\t" + name + ".add(" + mockName + ");");
            }
            return name;
        } else {
            String str = decapitalize(o.getClass().getSimpleName()) + getNum(o);
            write("\t\t" + o.getClass().getSimpleName() + " " + str + "= mock(" + o.getClass().getSimpleName() + ".class);");
            return str;
        }
    }

    private void writeBasicTest(MockObj o) throws InvocationTargetException, IllegalAccessException, IOException {
        write("\t@Test");
        write("\tpublic void basicTest" + o.getName() + "() {");
        for (Method method: o.getObj().getClass().getMethods()) {
            if (isClassMethod(method)) {
                continue;
            }
            if (Modifier.isPublic(method.getModifiers()) && method.getParameters().length == 0) {
                Object res = null;
                try {
                    res = method.invoke(o.getObj());
                } catch (Throwable t) {
                    // do nothing
                }
                if (res != null) {
                    if (res instanceof String) {
                        res = "\"" + res + "\"";
                        write("\t\tassertEquals(" + res + ", " + o.getName() + "." + method.getName() + "());");
                    } else if (res instanceof Number) {
                        write("\t\tassertTrue(" + res + " == " + o.getName() + "." + method.getName() + "());");
                    } else {
                        write("\t\tassertNotNull(" + o.getName() + "." + method.getName() + "());");
                    }
                }
            }
        }
        write("\t}");
    }

    private void write(String line) throws IOException {
        bw.write(line);
        bw.newLine();
    }

    private boolean isClassMethod(Method me) {
        boolean contains = false;
        Method[] methods = Class.class.getMethods();
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getName().equals(me.getName())) {
                contains = true;
                break;
            }
        }
        return contains;
    }

    public static String getType(Object o) {
        String[] type = o.getClass().getName().split("\\.");
        return type[type.length - 1];
    }

    public static String decapitalize(String str) {
        return str.substring(0, 1).toLowerCase() + str.substring(1);
    }
}
