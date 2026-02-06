package com.edu.config;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageConfig {

    @Value("${storage.root}")
    private String rootDir;

    public Path getRoot() {
        return Paths.get(rootDir);
    }
}
