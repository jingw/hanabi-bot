package hanabi;

public class BitVectorUtil {
    /**
     * Remove the ith bit by shifting down all higher order bits
     */
    public static int deleteAndShift(int vector, int i) {
        int lowerMask = (1 << i) - 1;
        int upperMask = ~((1 << (i + 1)) - 1);
        return ((vector & upperMask) >>> 1) | (vector & lowerMask);
    }
}
