package com.jitterted.mobreg.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class MemberServiceLookupByUsernameTest {

    @Test
    public void githubUsernameWithUppercaseLettersFoundInAllLowercaseDatabase() throws Exception {
        MemberService memberService = new MemberService(new InMemoryMemberRepository());
        memberService.save(new Member("Mixed", "mixedcase", "ROLE_USER", "ROLE_MEMBER"));

        assertThat(memberService.findByGithubUsername("mIxEdCASE"))
                .isNotNull();
    }

}