package io.github.funcfoo.id;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class JakeUtilsTest {
    @Test
    void testPrivateIpv4() {
        int[] ints = JakeUtils.privateIpv4();
        System.out.println(Arrays.toString(ints));
        assertEquals(4, ints.length);
    }
}