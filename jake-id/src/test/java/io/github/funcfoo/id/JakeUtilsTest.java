package io.github.funcfoo.id;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class JakeUtilsTest {
    @Test
    void machineID() {
        int machineID1 = JakeUtils.machineId(JakeIdTestUtils.splitIpv4("192.168.33.0"));
        assertEquals(256, machineID1);
        int machineID2 = JakeUtils.machineId(JakeIdTestUtils.splitIpv4("192.168.33.1"));
        assertEquals(257, machineID2);
        int machineID3 = JakeUtils.machineId(JakeIdTestUtils.splitIpv4("192.168.0.1"));
        assertEquals(1, machineID3);
        int machineID5 = JakeUtils.machineId(JakeIdTestUtils.splitIpv4("192.168.31.255"));
        assertEquals(8191, machineID5);
    }

    @Test
    void testPrivateIpv4() {
        int[] ints = JakeUtils.privateIpv4();
        System.out.println(Arrays.toString(ints));
        assertEquals(4, ints.length);
    }
}