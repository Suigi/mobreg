package com.jitterted.mobreg.adapter.in.web.admin;

import com.jitterted.mobreg.application.EnsembleTimerHolder;
import com.jitterted.mobreg.application.TestMemberBuilder;
import com.jitterted.mobreg.application.port.EnsembleRepository;
import com.jitterted.mobreg.application.port.InMemoryEnsembleRepository;
import com.jitterted.mobreg.domain.Ensemble;
import com.jitterted.mobreg.domain.EnsembleId;
import com.jitterted.mobreg.domain.EnsembleTimer;
import com.jitterted.mobreg.domain.MemberId;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

public class EnsembleTimerHolderTest {

    @Test
    void whenNoTimerExistsForEnsembleOneIsCreated() {
        Fixture fixture = createEnsembleRepositoryWithEnsembleHavingParticipants(EnsembleId.of(77));
        EnsembleTimerHolder ensembleTimerHolder = new EnsembleTimerHolder(fixture.ensembleRepository());

        EnsembleTimer ensembleTimer = ensembleTimerHolder.timerFor(EnsembleId.of(77));

        assertThat(ensembleTimer.ensembleId())
                .isEqualTo(EnsembleId.of(77));
        assertThat(ensembleTimer.participants())
                .containsExactlyElementsOf(fixture.participants());
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

}
