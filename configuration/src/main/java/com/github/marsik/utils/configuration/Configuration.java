package com.github.marsik.utils.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.cfg4j.provider.ConfigurationProvider;
import org.cfg4j.provider.ConfigurationProviderBuilder;
import org.cfg4j.source.ConfigurationSource;
import org.cfg4j.source.compose.MergeConfigurationSource;
import org.cfg4j.source.files.FilesConfigurationSource;
import org.cfg4j.source.inmemory.InMemoryConfigurationSource;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class Configuration {
    @Getter(AccessLevel.PROTECTED)
    protected final ConfigurationProvider provider;

    public Configuration() {
        Path configurationDir = Paths.get(System.getProperty("config.dir", "."));

        Properties defaults = new Properties();
        try {
            InputStream defaultStream = getClass().getClassLoader().getResourceAsStream("application.properties");
            if (defaultStream != null) {
                defaults.load(defaultStream);
            }
        } catch (IOException e) {
            log.error("Could not read the default config file", e);
        }

        final List<Path> configFiles = new ArrayList<>();

        try {
            Files.list(configurationDir)
                    .filter(f -> f.toString().endsWith(".properties")
                            || f.toString().endsWith(".yml")
                            || f.toString().endsWith(".yaml"))
                    .filter(Files::isReadable)
                    .sorted()
                    .forEach(configFiles::add);
        } catch (IOException e) {
            log.error("Could not retrieve list of config files", e);
        }

        ConfigurationSource source = new MergeConfigurationSource(
                new InMemoryConfigurationSource(defaults),
                new InMemoryConfigurationSource(System.getProperties()),
                new FilesConfigurationSource(() -> configFiles)
        );

        provider = new ConfigurationProviderBuilder()
                .withConfigurationSource(source)
                .withEnvironment(() -> System.getProperty("user.dir"))
                .build();
    }
}
