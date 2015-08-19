package hanabi;

import java.io.PrintStream;

/**
 * Maintains the distribution of discrete buckets.
 */
public class Histogram {
    private int[] counts;
    private int n;

    public Histogram(int buckets) {
        counts = new int[buckets];
    }

    public void increment(int bucket) {
        counts[bucket]++;
        n++;
    }

    public double mean() {
        double sum = 0;
        for (int i = 0; i < counts.length; i++) {
            sum += i * counts[i];
        }
        return sum / n;
    }

    public double proportion(int bucket) {
        return (double) counts[bucket] / n;
    }

    public double proportionUncertainty(int bucket, double z) {
        double pObserved = (double) counts[bucket] / n;
        double stderr = Math.sqrt(pObserved * (1 - pObserved) / n);
        return stderr * z;
    }

    public void dump(double z, PrintStream out) {
        for (int i = 0; i < counts.length; i++) {
            if (counts[i] > 0) {
                double p = proportion(i);
                double margin = proportionUncertainty(i, z);
                out.printf("%d: %.3f +- %.3f%n", i, p * 100, margin * 100);
            }
        }
        out.printf("mean: %.3f%n", mean());
    }
}
