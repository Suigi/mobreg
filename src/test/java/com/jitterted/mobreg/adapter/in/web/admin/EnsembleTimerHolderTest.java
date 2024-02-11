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
import org.assertj.core.api.SoftAssertions;
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
    void whenNoTimerExistsForEnsembleExceptionIsThrown() {
        Fixture fixture = createEnsembleRepositoryWithEnsembleHavingParticipants(EnsembleId.of(77));
        EnsembleTimerHolder ensembleTimerHolder = new EnsembleTimerHolder(fixture.ensembleRepository());

        assertThatIllegalStateException()
                .isThrownBy(() -> ensembleTimerHolder.timerFor(EnsembleId.of(77)))
                .withMessage("No Ensemble Timer exists for Ensemble 77.");
    }

    @Test
    void existingTimerIsReturnedWhenHolderHasTimerForSpecificEnsemble() {
        Fixture fixture = createEnsembleRepositoryWithEnsembleHavingParticipants(EnsembleId.of(63));
        EnsembleTimerHolder ensembleTimerHolder = new EnsembleTimerHolder(fixture.ensembleRepository());
        EnsembleTimer createdEnsemblerTimer = ensembleTimerHolder.createTimerFor(EnsembleId.of(63));

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
        BroadcastFixture fixture = createBroadcastFixture(515,
                                                          EnsembleTimer.TimerState.RUNNING,
                                                          new TimeRemaining(3, 59, 99));

        fixture.ensembleTimerHolder()
               .handleTickFor(EnsembleId.of(515), fixture.timerStartedAt().plusSeconds(1));

        fixture.mockBroadcaster().verifyTimerStateSent();
    }

    @Test
    void onTickWhenFinishedBroadcastsTimerFinished() {
        BroadcastFixture fixture = createBroadcastFixture(737,
                                                          EnsembleTimer.TimerState.FINISHED,
                                                          new TimeRemaining(0, 0, 0));

        fixture.ensembleTimerHolder()
               .handleTickFor(EnsembleId.of(737),
                              fixture.timerStartedAt()
                                     .plus(EnsembleTimer.DEFAULT_TIMER_DURATION));

        fixture.mockBroadcaster().verifyTimerStateSent();
    }

    @Test
    void onTimerCreationBroadcastsTimerWaitingToStart() {
        TimeRemaining expectedTimeRemaining = new TimeRemaining(4, 0, 100);
        Ensemble ensemble = EnsembleFactory.withStartTimeNowAndIdOf(475);
        MockBroadcaster mockBroadcaster = new MockBroadcaster(475, EnsembleTimer.TimerState.WAITING_TO_START, expectedTimeRemaining);
        EnsembleRepository ensembleRepository = new InMemoryEnsembleRepository();
        ensembleRepository.save(ensemble);
        EnsembleTimerHolder ensembleTimerHolder = new EnsembleTimerHolder(ensembleRepository, mockBroadcaster);

        ensembleTimerHolder.createTimerFor(EnsembleId.of(475));

        mockBroadcaster.verifyTimerStateSent();
    }

    void onEnsembleEndedRemoveAssociatedTimer() {

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


    private BroadcastFixture createBroadcastFixture(int ensembleId, EnsembleTimer.TimerState expectedTimerState, TimeRemaining expectedTimeRemaining) {
        Ensemble ensemble = EnsembleFactory.withStartTimeNowAndIdOf(ensembleId);
        MockBroadcaster mockBroadcaster = new MockBroadcaster(ensembleId, expectedTimerState, expectedTimeRemaining);
        EnsembleRepository ensembleRepository = new InMemoryEnsembleRepository();
        ensembleRepository.save(ensemble);
        EnsembleTimerHolder ensembleTimerHolder = new EnsembleTimerHolder(ensembleRepository, mockBroadcaster);
        ensembleTimerHolder.createTimerFor(EnsembleId.of(ensembleId));
        Instant timerStartedAt = Instant.now();
        ensembleTimerHolder.startTimerFor(EnsembleId.of(ensembleId), timerStartedAt);
        return new BroadcastFixture(mockBroadcaster, ensembleTimerHolder, timerStartedAt);
    }

    private record BroadcastFixture(MockBroadcaster mockBroadcaster,
                                    EnsembleTimerHolder ensembleTimerHolder, Instant timerStartedAt) {
    }


    private static class MockBroadcaster implements Broadcaster {
        private boolean wasCalled;
        private final int expectedEnsembleId;
        private final EnsembleTimer.TimerState expectedTimerState;
        private final TimeRemaining expectedTimeRemaining;
        private EnsembleTimer.TimerState lastState;
        private EnsembleId lastEnsembleId;
        private TimeRemaining lastTimeRemaining;

        public MockBroadcaster(int expectedEnsembleId, EnsembleTimer.TimerState expectedTimerState, TimeRemaining expectedTimeRemaining) {
            this.expectedEnsembleId = expectedEnsembleId;
            this.expectedTimerState = expectedTimerState;
            this.expectedTimeRemaining = expectedTimeRemaining;
        }

        @Override
        public void sendCurrentTimer(EnsembleTimer ensembleTimer) {
            wasCalled = true;
            lastState = ensembleTimer.state();
            lastEnsembleId = ensembleTimer.ensembleId();
            lastTimeRemaining = ensembleTimer.timeRemaining();
        }

        private void verifyTimerStateSent() {
            assertThat(wasCalled)
                    .as("Expected sendCurrentTimer() to have been called on the Broadcaster")
                    .isTrue();
            SoftAssertions softly = new SoftAssertions();
            softly.assertThat(lastState)
                  .isEqualByComparingTo(expectedTimerState);
            softly.assertThat(lastEnsembleId)
                  .isEqualTo(EnsembleId.of(expectedEnsembleId));
            softly.assertThat(lastTimeRemaining)
                  .isEqualTo(expectedTimeRemaining);
            softly.assertAll();

        }
    }
}
