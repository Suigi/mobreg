package com.jitterted.mobreg.adapter.out.mobtimer;

import com.jitterted.mobreg.application.MemberService;
import com.jitterted.mobreg.application.port.InMemoryMemberRepository;
import com.jitterted.mobreg.domain.Huddle;
import com.jitterted.mobreg.domain.Member;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.*;

class MobParticipantsDtoCreationTest {

    @Test
    public void convertsHuddleToMobParticipantsDto() throws Exception {
        Huddle huddle = new Huddle("test", ZonedDateTime.now());
        MemberService memberService = new MemberService(new InMemoryMemberRepository());
        Member member = new Member("Participant", "");
        Member savedMember = memberService.save(member);
        huddle.acceptedBy(savedMember.getId());

        MobParticipantsDto dto = MobParticipantsDto.from(huddle, memberService);

        assertThat(dto.getMob())
                .extracting(PersonDto::getName)
                .containsOnly("Participant");
    }
}