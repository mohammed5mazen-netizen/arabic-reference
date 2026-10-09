package com.mrsoft.arabicreference.shared.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.jupiter.api.Test;

class ClientAddressesTest {

    @Test
    void aDirectClientCannotSpoofTheForwardedHeader() {
        assertThat(ClientAddresses.choose("203.0.113.8", "198.51.100.9", Set.of("10.0.0.8"))).isEqualTo("203.0.113.8");
    }

    @Test
    void aTrustedProxyMaySupplyTheClientAddress() {
        assertThat(ClientAddresses.choose("10.0.0.8", "203.0.113.8, 10.0.0.8", Set.of("10.0.0.8"))).isEqualTo("203.0.113.8");
    }

    @Test
    void controlCharactersAreRemovedFromAddresses() {
        assertThat(ClientAddresses.sanitize("203.0.113.8\r\nINFO forged")).isEqualTo("203.0.113.8INFO forged");
    }
}
