package com.jitterted.mobreg.adapter.in.web;

import com.jitterted.mobreg.domain.Huddle;
import com.jitterted.mobreg.domain.HuddleId;
import com.jitterted.mobreg.domain.Participant;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.*;

class HuddleSummaryViewTest {

    @Test
    public void memberRegisteredIsFalseWhenHuddleIsEmpty() throws Exception {
        Huddle huddle = createTestHuddle();

        HuddleSummaryView huddleSummaryView = HuddleSummaryView.toView(huddle, "username");

        assertThat(huddleSummaryView.memberRegistered())
                .isFalse();
    }

    @Test
    public void memberRegisteredIsFalseWhenMemberNotInHuddle() throws Exception {
        Huddle huddle = createTestHuddle();
        Participant participant = new Participant("name", "some_other_username", "email", "discord", false);
        huddle.register(participant);

        HuddleSummaryView huddleSummaryView = HuddleSummaryView
                .toView(huddle, "username");

        assertThat(huddleSummaryView.memberRegistered())
                .isFalse();
    }

    @Test
    public void memberRegisteredIsTrueWhenMemberHuddleParticipant() throws Exception {
        Huddle huddle = createTestHuddle();
        Participant participant = new Participant("name",
                                                  "participant_username", "email", "discord", false);
        huddle.register(participant);

        HuddleSummaryView huddleSummaryView = HuddleSummaryView
                .toView(huddle, "participant_username");

        assertThat(huddleSummaryView.memberRegistered())
                .isTrue();
    }

    @NotNull
    private Huddle createTestHuddle() {
        Huddle huddle = new Huddle("test", ZonedDateTime.now());
        huddle.setId(HuddleId.of(1L));
        return huddle;
    }
}