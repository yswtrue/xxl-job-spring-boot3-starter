package com.github.justfunxin.spring.boot.starter.xxl.job.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;

/**
 * @author Spencer Gibb
 */
@Slf4j
public class InetUtils {

    public static InetAddress findFirstNonLoopbackAddress(String... preferredNetworks) {
        InetAddress result = null;
        try {
            int lowest = Integer.MAX_VALUE;
            Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces();
            while (nics.hasMoreElements()) {
                NetworkInterface ifc = nics.nextElement();
                if (ifc.isUp()) {
                    log.trace("Testing interface: {}", ifc.getDisplayName());
                    if (ifc.getIndex() < lowest || result == null) {
                        lowest = ifc.getIndex();
                    } else {
                        continue;
                    }
                    Enumeration<InetAddress> addrs = ifc.getInetAddresses();
                    while (addrs.hasMoreElements()) {
                        InetAddress address = addrs.nextElement();
                        if (address instanceof Inet4Address
                                && !address.isLoopbackAddress()
                                && isPreferredAddress(address, preferredNetworks)) {
                            log.trace("Found non-loopback interface: {}", ifc.getDisplayName());
                            result = address;
                        }
                    }
                }
            }
        } catch (IOException ex) {
            log.error("Cannot get first non-loopback address", ex);
        }
        if (result != null) {
            return result;
        }
        try {
            return InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            log.warn("Unable to retrieve localhost");
        }
        return null;
    }

    // For testing.
    private static boolean isPreferredAddress(InetAddress address, String... preferredNetworks) {
        if (preferredNetworks.length == 0) {
            return true;
        }
        for (String regex : preferredNetworks) {
            final String hostAddress = address.getHostAddress();
            if (hostAddress.matches(regex) || hostAddress.startsWith(regex)) {
                return true;
            }
        }
        log.trace("Ignoring address: {}", address.getHostAddress());
        return false;
    }

}
