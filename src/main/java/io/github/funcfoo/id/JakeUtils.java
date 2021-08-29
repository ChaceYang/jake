package io.github.funcfoo.id;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public final class JakeUtils {
    private JakeUtils() {}

    private static final int[] EMPTY_INT_ARRAY = new int[0];

    public static int[] privateIpv4() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if(networkInterface.isLoopback() || !networkInterface.isUp()) continue;
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    if(inetAddress instanceof Inet6Address) continue;
                    byte[] address = inetAddress.getAddress();
                    if(isPrivateIPv4(address)) {
                        return new int[]{
                                address[0] & 0xFF,
                                address[1] & 0xFF,
                                address[2] & 0xFF,
                                address[3] & 0xFF
                        };
                    }
                }
            }
        } catch (SocketException e) {
            throw new IllegalStateException("dev socket error", e);
        }
        return EMPTY_INT_ARRAY;
    }

    private static boolean isPrivateIPv4(byte[] ip) {
        return ip[0] == 10
                || (ip[0] & 0xFF) == 172 && (ip[1] >= 16 && ip[1] < 32)
                || (ip[0] & 0xFF) == 192 && (ip[1] & 0xFF) == 168;
    }
}
