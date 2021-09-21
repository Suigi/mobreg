package com.jitterted.mobreg.adapter.out.jdbc;

import com.jitterted.mobreg.domain.Member;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

public class MemberEntityTranslationTest {

    @Test
    public void memberWithRolesTranslatedToEntityWithRoles() throws Exception {
        Member member = new Member("first", "githubuser", "ROLE_USER", "ROLE_MEMBER");

        MemberEntity memberEntity = MemberEntity.from(member);

        assertThat(memberEntity.getRoles())
                .containsOnly("ROLE_USER", "ROLE_MEMBER");
    }

    @Test
    public void entityWithRolesTranslatedToMemberWithRoles() throws Exception {
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setFirstName("first");
        memberEntity.setGithubUsername("githubusername");
        memberEntity.setId(11L);
        memberEntity.setRoles(Set.of("ROLE_ONE", "ROLE_TWO"));

        Member member = memberEntity.asMember();

        assertThat(member.roles())
                .containsOnly("ROLE_ONE", "ROLE_TWO");
    }

    @Test
    public void entityWithEmailTranslatedToDomainMemberWithEmail() throws Exception {
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setEmail("member@example.com");
        memberEntity.setFirstName("member");
        memberEntity.setGithubUsername("githubusername");
        memberEntity.setId(31L);
        memberEntity.setRoles(Set.of("ROLE_ONE", "ROLE_TWO"));

        Member member = memberEntity.asMember();

        assertThat(member.email())
                .isEqualTo("member@example.com");
    }

    @Test
    public void domainMemberWithEmailTranslatedToDataEntityWithEmail() throws Exception {
        Member member = new Member("member", "ghuser", "ROLE_THREE");
        member.changeEmailTo("ghuser@example.com");

        MemberEntity memberEntity = MemberEntity.from(member);

        assertThat(memberEntity.getEmail())
                .isEqualTo("ghuser@example.com");
    }
}
