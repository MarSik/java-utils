package com.github.marsik.util.uuid;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.uuid.EthernetAddress;
import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.NoArgGenerator;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UuidGeneratorImpl implements UuidGenerator {
    private final NoArgGenerator generator;

    public UuidGeneratorImpl() {
        EthernetAddress ethernetAddress = EthernetAddress.fromInterface();
        if (ethernetAddress == null) {
            ethernetAddress = EthernetAddress.constructMulticastAddress();
        }
        generator = Generators.timeBasedGenerator(ethernetAddress);
    }

    @Override
    public UUID generate() {
        final UUID uuid = generator.generate();
        log.debug("Generating UUID {}", uuid.toString());
        return uuid;
    }
}
