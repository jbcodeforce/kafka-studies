import org.junit.Assert;
import org.junit.Test;

public class TestSomeThing {
    
    @Test
    public void testProper(){
        String p = "True";

        Assert.assertTrue(Boolean.parseBoolean(p));
    }
}