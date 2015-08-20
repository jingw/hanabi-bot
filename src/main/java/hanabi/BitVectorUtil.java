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

    /**
     * Extract the lowest n set bits. Returns the original vector if there are less than n set bits.
     */
    public static int lowestSetBits(int vector, int n) {
        int result = 0;
        int count = 0;
        for (int i = 0; i < Integer.SIZE && count < n; i++) {
            if ((vector & (1 << i)) != 0) {
                result |= 1 << i;
                count++;
            }
        }
        return result;
    }
}
