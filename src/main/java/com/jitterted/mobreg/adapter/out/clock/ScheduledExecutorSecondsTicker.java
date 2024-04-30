package com.jitterted.mobreg.adapter.out.clock;

import com.jitterted.mobreg.application.EnsembleTimerTickHandler;
import com.jitterted.mobreg.application.port.SecondsTicker;
import com.jitterted.mobreg.domain.EnsembleId;

import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ScheduledExecutorSecondsTicker implements SecondsTicker {
    private static final int TIMER_INITIAL_DELAY = 0;
    private static final int TIMER_PERIOD_IN_SECONDS = 1;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final ScheduledCountdown scheduledCountdown = new ScheduledCountdown();

    public ScheduledExecutorSecondsTicker() {
    }

    @Override
    public void start(EnsembleId ensembleId, EnsembleTimerTickHandler ensembleTimerTickHandler) {
        scheduledCountdown.requireNotScheduled();

        Runnable tickHandlerTask = () ->
                ensembleTimerTickHandler.handleTickFor(ensembleId, Instant.now());

        ScheduledFuture<?> countdownHandle = scheduler.scheduleAtFixedRate(tickHandlerTask, TIMER_INITIAL_DELAY, TIMER_PERIOD_IN_SECONDS, TimeUnit.SECONDS);
        scheduledCountdown.scheduledWith(ensembleId, countdownHandle);
    }

    @Override
    public void stop() {
        scheduledCountdown.stop();
    }

    private static class ScheduledCountdown {
        private ScheduledFuture<?> countdownHandle;
        private EnsembleId ensembleId;

        public ScheduledCountdown() {
        }

        public void scheduledWith(EnsembleId ensembleId, ScheduledFuture<?> countdownHandle) {
            this.countdownHandle = countdownHandle;
            this.ensembleId = ensembleId;
        }

        public void stop() {
            if (countdownHandle == null) {
                return;
            }
            countdownHandle.cancel(false);
            countdownHandle = null;
            ensembleId = null;
        }

        public boolean isScheduled() {
            return countdownHandle != null;
        }

        public void requireNotScheduled() {
            if (isScheduled()) {
                throw new IllegalStateException("Countdown timer already scheduled for " + ensembleId);
            }
        }
    }
}
