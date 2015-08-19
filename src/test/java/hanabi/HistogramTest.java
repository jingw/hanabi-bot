package hanabi;

import org.junit.Assert;
import org.junit.Test;

public class HistogramTest {
    @Test
    public void test() {
        Histogram hist = new Histogram(10);
        hist.increment(0);
        hist.increment(1);
        hist.increment(2);
        hist.increment(2);
        Assert.assertEquals(1.25, hist.mean(), 0);
        Assert.assertEquals(0.5, hist.proportion(2), 0);
    }

    @Test
    public void testStandardDeviation() {
        Histogram hist = new Histogram(10);
        hist.increment(0);
        hist.increment(1);
        hist.increment(3);
        // numpy.std([0,1,3],ddof=1)
        Assert.assertEquals(1.5275252316519468, hist.standardDeviation(), 1e-5);
    }
}
