package hanabi;

import org.junit.Assert;
import org.junit.Test;

public class TableauTest {
    @Test
    public void testIncrementCount() {
        int tableau = Tableau.EMPTY;
        for (int c = 0; c < 6; c++) {
            for (int i = 0; i < c; i++) {
                tableau = Tableau.increment(tableau, c);
            }
        }
        for (int c = 0; c < 6; c++) {
            Assert.assertEquals(c, Tableau.getCount(tableau, c));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIncrementTooMany() {
        int tableau = Tableau.EMPTY;
        for (int i = 0; i < 6; i++) {
            tableau = Tableau.increment(tableau, 0);
        }
    }
}
