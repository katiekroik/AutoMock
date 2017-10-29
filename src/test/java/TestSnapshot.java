import com.automock.AutoMock;
import com.automock.TestObj;
import com.automock.TestSubObj;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Katie on 10/28/17.
 */
public class TestSnapshot {

    @Test
    public void test() throws IllegalAccessException, IOException, InvocationTargetException {
        TestObj t = new TestObj();
        t.setOne("one");
        t.setTwo(2);
        t.setTestSubObj(new TestSubObj());
        t.setArr(new Integer[]{1, 2, 3});
        Map m = new HashMap<>();
        m.put("1", 1);
        m.put("2", 2);
        t.setMap(m);

        TestObj t2 = new TestObj();
        t2.setOne("two");
        t2.setTwo(3);
        t2.setTestSubObj(new TestSubObj());
        t2.setArr(new Integer[]{100, 200, 300});
        t2.setMap(new HashMap());
    }
}
