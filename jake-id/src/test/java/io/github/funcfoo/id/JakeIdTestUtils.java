package io.github.funcfoo.id;

public class JakeIdTestUtils {
    public static int[] splitIpv4(String ipv4) {
        String[] ipv4Address = ipv4.split("\\.");
        return new int[] {
                Integer.parseInt(ipv4Address[0]),
                Integer.parseInt(ipv4Address[1]),
                Integer.parseInt(ipv4Address[2]),
                Integer.parseInt(ipv4Address[3]),
        };
    }

    public static void logId(String method, long id) {
        String binaryString = Long.toBinaryString(id);
        int splitMachineIdIndex = binaryString.length() - 9 - 13;
        int splitSequenceIndex = binaryString.length() - 9;
        log(method,id + ":"
                + binaryString.substring(0,splitMachineIdIndex)
                + "-"
                + binaryString.substring(splitMachineIdIndex, splitSequenceIndex)
                + "-"
                + binaryString.substring(splitSequenceIndex));
    }

    public static void log(String method, Object o) {
        System.out.println("method: " + method + " message: " + o);
    }

}
