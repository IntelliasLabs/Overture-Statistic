/**
 Copyright ©2024 Intellias
 */
package com.intellias.mobility.statistic;

import org.springframework.boot.SpringApplication;

public class TestStatisticFrameworkApplication {

  public static void main(String[] args) {
    SpringApplication.from(StatisticFrameworkApplication::main)
        .with(TestcontainersConfiguration.class)
        .run(args);
  }
}
