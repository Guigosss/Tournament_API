package com.technofuturtic.tournament_api.bll.metrics;

import com.technofuturtic.tournament_api.dl.enums.TournamentFormat;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

/**
 * Métriques métier exposées sur /actuator/prometheus.
 */
@Component
public class TournamentMetrics {

    private static final String UNKNOWN_FORMAT = "UNKNOWN";

    private final MeterRegistry registry;
    private final Counter winnerEmailsSent;
    private final Counter winnerEmailsFailed;

    public TournamentMetrics(MeterRegistry registry) {
        this.registry = registry;
        this.winnerEmailsSent = winnerEmailCounter("sent");
        this.winnerEmailsFailed = winnerEmailCounter("failed");
    }

    /**
     * Exécute la génération d'un tournoi en mesurant sa durée,
     * puis compte la génération si elle a réussi. Les exceptions sont propagées telles quelles.
     */
    public void recordGeneration(TournamentFormat format, Runnable generation) {
        String formatTag = format != null ? format.name() : UNKNOWN_FORMAT;
        Timer.Sample sample = Timer.start(registry);
        String outcome = "failure";
        try {
            generation.run();
            outcome = "success";
            Counter.builder("tournament.generated")
                    .description("Nombre de tournois générés")
                    .tag("format", formatTag)
                    .register(registry)
                    .increment();
        } finally {
            sample.stop(Timer.builder("tournament.generation.duration")
                    .description("Durée de génération d'un tournoi")
                    .tag("format", formatTag)
                    .tag("outcome", outcome)
                    .register(registry));
        }
    }

    public void winnerEmailSent() {
        winnerEmailsSent.increment();
    }

    public void winnerEmailFailed() {
        winnerEmailsFailed.increment();
    }

    private Counter winnerEmailCounter(String result) {
        return Counter.builder("tournament.winner.emails")
                .description("Emails de victoire envoyés ou en échec")
                .tag("result", result)
                .register(registry);
    }
}
