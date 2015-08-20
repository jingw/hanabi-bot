package hanabi;

import java.io.PrintStream;

/**
 * Maintains the distribution of discrete buckets.
 */
public class Histogram {
    private int[] counts;
    private int n;
    private String histName;

    public Histogram(int buckets, String name) {
        counts = new int[buckets];
        histName = name;
    }

    public void increment(int bucket) {
        counts[bucket]++;
        n++;
    }

    public double mean() {
        if (n < 1) {
            throw new IllegalStateException("Not enough samples");
        }
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
        out.printf("%s:\n", histName);
        for (int i = 0; i < counts.length; i++) {
            if (counts[i] > 0) {
                double p = proportion(i);
                double margin = proportionUncertainty(i, z);
                out.printf("  %d: %.3f +- %.3f%n", i, p * 100, margin * 100);
            }
        }
        out.printf("  mean: %.3f +- %.3f%n", mean(), standardError() * z);
        out.printf("  median: %d%n", percentile(0.5));
    }

    public double standardError() {
        return standardDeviation() / Math.sqrt(n);
    }

    public double standardDeviation() {
        if (n < 2) {
            throw new IllegalStateException("Not enough samples");
        }
        double mean = mean();
        double ssr = 0;
        for (int i = 0; i < counts.length; i++) {
            ssr += (i - mean) * (i - mean) * counts[i];
        }
        return Math.sqrt(ssr / (n - 1));
    }

    public int percentile(double p) {
        if (p < 0 || p > 1) {
            throw new IllegalArgumentException("p out of range");
        }
        int runningTotal = 0;
        for (int i = 0; i < counts.length; i++) {
            runningTotal += counts[i];
            if ((double) runningTotal / n >= p) {
                return i;
            }
        }
        throw new AssertionError(); // maybe this is reachable with floating point inaccuracies?
    }
}
