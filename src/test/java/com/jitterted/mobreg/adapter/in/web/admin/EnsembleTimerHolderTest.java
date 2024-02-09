package com.jitterted.mobreg.adapter.in.web.admin;

import com.jitterted.mobreg.application.EnsembleTimerHolder;
import com.jitterted.mobreg.application.TestMemberBuilder;
import com.jitterted.mobreg.application.port.Broadcaster;
import com.jitterted.mobreg.application.port.EnsembleRepository;
import com.jitterted.mobreg.application.port.InMemoryEnsembleRepository;
import com.jitterted.mobreg.domain.Ensemble;
import com.jitterted.mobreg.domain.EnsembleFactory;
import com.jitterted.mobreg.domain.EnsembleId;
import com.jitterted.mobreg.domain.EnsembleTimer;
import com.jitterted.mobreg.domain.MemberId;
import com.jitterted.mobreg.domain.TimeRemaining;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

public class EnsembleTimerHolderTest {

    @Test
    void newTimerHolderHasNoTimerForId() {
        EnsembleRepository ensembleRepository = new InMemoryEnsembleRepository();

        EnsembleTimerHolder ensembleTimerHolder = new EnsembleTimerHolder(ensembleRepository);

        assertThat(ensembleTimerHolder.hasTimerFor(EnsembleId.of(62)))
                .isFalse();
    }

    @Test
    void whenNoTimerExistsForEnsembleOneIsCreated() {
        Fixture fixture = createEnsembleRepositoryWithEnsembleHavingParticipants(EnsembleId.of(77));
        EnsembleTimerHolder ensembleTimerHolder = new EnsembleTimerHolder(fixture.ensembleRepository());

        EnsembleTimer ensembleTimer = ensembleTimerHolder.timerFor(EnsembleId.of(77));

        assertThat(ensembleTimer.ensembleId())
                .isEqualTo(EnsembleId.of(77));
        assertThat(ensembleTimer.participants())
                .containsExactlyElementsOf(fixture.participants());
        assertThat(ensembleTimerHolder.hasTimerFor(EnsembleId.of(77)))
                .isTrue();
    }

    @Test
    void existingTimerIsReturnedWhenHolderHasTimerForSpecificEnsemble() {
        Fixture fixture = createEnsembleRepositoryWithEnsembleHavingParticipants(EnsembleId.of(63));
        EnsembleTimerHolder ensembleTimerHolder = new EnsembleTimerHolder(fixture.ensembleRepository());
        EnsembleTimer createdEnsemblerTimer = ensembleTimerHolder.timerFor(EnsembleId.of(63));

        EnsembleTimer foundEnsembleTimer = ensembleTimerHolder.timerFor(EnsembleId.of(63));

        assertThat(foundEnsembleTimer)
                .isSameAs(createdEnsemblerTimer);
    }

    @Test
    void askingTimerStartedThrowsExceptionIfTimerDoesNotExistForEnsemble() {
        EnsembleTimerHolder ensembleTimerHolder = new EnsembleTimerHolder(new InMemoryEnsembleRepository());

        assertThatIllegalArgumentException()
                .isThrownBy(() -> ensembleTimerHolder.isTimerRunningFor(EnsembleId.of(444)))
                .withMessage("No timer for Ensemble ID 444 exists.");
    }

    @Test
    void startTimerThrowsExceptionIfTimerDoesNotExistForEnsemble() {
        EnsembleTimerHolder ensembleTimerHolder = new EnsembleTimerHolder(new InMemoryEnsembleRepository());

        assertThatIllegalArgumentException()
                .isThrownBy(() -> ensembleTimerHolder.startTimerFor(EnsembleId.of(333), Instant.now()))
                .withMessage("No timer for Ensemble ID 333 exists.");
    }

    @Test
    void onTickWhileRunningBroadcastsCurrentTimerState() {
        MockBroadcaster mockBroadcaster = new MockBroadcaster();
        Ensemble ensemble = EnsembleFactory.withStartTimeNowAndIdOf(515);
        EnsembleRepository ensembleRepository = new InMemoryEnsembleRepository();
        ensembleRepository.save(ensemble);
        EnsembleTimerHolder ensembleTimerHolder = new EnsembleTimerHolder(ensembleRepository, mockBroadcaster);
        ensembleTimerHolder.timerFor(EnsembleId.of(515));
        Instant timerStartedAt = Instant.now();
        ensembleTimerHolder.startTimerFor(EnsembleId.of(515), timerStartedAt);

        ensembleTimerHolder.handleTickFor(EnsembleId.of(515), timerStartedAt.plusSeconds(1));

        mockBroadcaster.verify();
    }

    void onTickWhenFinishedBroadcastsTimerFinished() {

    }

    // ---- ENCAPSULATED SETUP
    
    private static Fixture createEnsembleRepositoryWithEnsembleHavingParticipants(EnsembleId ensembleId) {
        EnsembleRepository ensembleRepository = new InMemoryEnsembleRepository();
        Ensemble ensemble = new Ensemble("Current", ZonedDateTime.now());
        ensemble.setId(ensembleId);
        List<MemberId> participants = createMembersAndJoinAsParticipant(ensemble);
        ensembleRepository.save(ensemble);
        return new Fixture(ensembleRepository, participants);
    }

    private static List<MemberId> createMembersAndJoinAsParticipant(Ensemble ensemble) {
        TestMemberBuilder testMemberBuilder = new TestMemberBuilder();
        List<MemberId> participants = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            MemberId firstMemberId = testMemberBuilder.buildAndSave().getId();
            ensemble.joinAsParticipant(firstMemberId);
            participants.add(firstMemberId);
        }
        return participants;
    }

    private record Fixture(EnsembleRepository ensembleRepository, List<MemberId> participants) {
    }

    private static class MockBroadcaster implements Broadcaster {
        private boolean wasCalled;

        @Override
        public void sendCurrentTimer(EnsembleTimer ensembleTimer) {
            assertThat(ensembleTimer.state())
                    .isEqualByComparingTo(EnsembleTimer.TimerState.RUNNING);
            assertThat(ensembleTimer.ensembleId())
                    .isEqualTo(EnsembleId.of(515));
            assertThat(ensembleTimer.timeRemaining())
                    /* minutesRemaining, secondsRemaining, percentRemaining  */
                    .isEqualTo(new TimeRemaining(3, 59, 99));
            wasCalled = true;
        }

        private void verify() {
            assertThat(wasCalled)
                    .as("Expected sendCurrentTimer() to have been called on the Broadcaster")
                    .isTrue();
        }
    }
}