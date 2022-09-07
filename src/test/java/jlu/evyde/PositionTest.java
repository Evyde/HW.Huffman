package jlu.evyde;

import jlu.evyde.LZ77.Position;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class PositionTest {
    @Test
    public void addTest() {
        for (int i = 0; i < 10000; i++) {
            Position tempPosition = new Position();
            assertEquals((long) tempPosition.get(), Math.min(i, 4095));
            assertEquals((long) new Position(Math.min(i, 4095)).get(), Math.min(i, 4095));
        }
    }

    @Test
    public void removeLastTest() {
        Position.reset();
        for (int i = 0; i < 10000; i++) {
            Position tempPosition = new Position();
            assertEquals((long) tempPosition.get(), 0);
            assertEquals((long) new Position(Math.min(i, 4095)).get(), Math.min(i, 4095));
            assertEquals(0, (long) Position.removeLast().get());
        }
    }

    @Test
    public void removeFirstTest() {

    }

    @Test
    public void randomRemoveTest() {

    }

    @Test
    public void hashMapTest() {
        Map<Position, Integer> windowMap = new HashMap<>();

        for (int i = 0; i < 10000; i++) {
            Position tempPosition = new Position();
            windowMap.put(tempPosition, i);
            assertEquals((long) tempPosition.get(), 0);
            assertEquals((long) windowMap.get(new Position(0)), i);
            assertEquals((long) new Position(Math.min(i, 4095)).get(), Math.min(i, 4095));
            assertEquals(i, (int) windowMap.remove(new Position(0)));
            Position.removeFirst();
        }

        assertTrue(windowMap.isEmpty());
    }

}