package exampleTests;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class HogeESTest {

    @Test
    public void test01() {
        int input = 3;
        int result = square(input);
        assertEquals(result, 9);
    }

    @Test
    public void test02() {
        int input = -5;
        int result = square(input);
        assertEquals(result, 25);
    }

    @Test
    public void test03() {
        int input = 0;
        int result = square(input);
        assertEquals(result, 0);
    }

}