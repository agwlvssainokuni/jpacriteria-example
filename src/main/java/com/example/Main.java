package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main {

    public static void main(String[] args) {
        System.exit(doMain(args));
    }

    public static int doMain(String[] args) {
        try (var app = SpringApplication.run(Main.class, args)) {
            return SpringApplication.exit(app);
        }
    }
}
