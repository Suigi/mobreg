package com.jitterted.mobreg.domain;

import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.*;

class HuddleServiceParticipantTest {

    @Test
    public void givenParticipantRegistersForHuddleThenWillBeFoundInHuddle() throws Exception {
        InMemoryHuddleRepository huddleRepository = new InMemoryHuddleRepository();
        HuddleService huddleService = new HuddleService(huddleRepository);
        Huddle huddle = new Huddle("test", ZonedDateTime.now());
        HuddleId huddleId = huddleRepository.save(huddle).getId();

        huddleService.registerParticipant(huddleId, "Participant J. Name", "pjname", "partiname");

        assertThat(huddle.numberRegistered())
                .isEqualTo(1);

        assertThat(huddle.participants())
                .extracting(Participant::githubUsername)
                .containsOnly("pjname");
    }
}