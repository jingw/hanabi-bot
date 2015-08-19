package hanabi;

import org.junit.Assert;
import org.junit.Test;

public class BitVectorUtilTest {
    @Test
    public void testDeleteAndShift() {
        Assert.assertEquals(BitVectorUtil.deleteAndShift(0b1111, 0), 0b111);
        Assert.assertEquals(BitVectorUtil.deleteAndShift(0b1110, 0), 0b111);
        Assert.assertEquals(BitVectorUtil.deleteAndShift(0b1101, 1), 0b111);
        Assert.assertEquals(BitVectorUtil.deleteAndShift(0b1101, 2), 0b101);
    }
}
