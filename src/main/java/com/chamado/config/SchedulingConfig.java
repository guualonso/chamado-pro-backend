package com.chamado.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Habilita a execução de tarefas agendadas (@Scheduled),
 * usadas pelo SlaSchedulerService para monitorar SLAs.
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
