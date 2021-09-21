package com.jitterted.mobreg.adapter.in.web.member;

import com.jitterted.mobreg.domain.HuddleService;
import com.jitterted.mobreg.domain.Member;
import com.jitterted.mobreg.domain.MemberId;
import com.jitterted.mobreg.domain.MemberService;
import com.jitterted.mobreg.domain.OAuth2UserFactory;
import com.jitterted.mobreg.domain.port.HuddleRepository;
import com.jitterted.mobreg.domain.port.MemberRepository;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@Tag("mvc")
@WithMockUser(username = "username", authorities = {"ROLE_MEMBER"})
public class MemberEndpointConfigurationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    HuddleService huddleService;

    @MockBean
    HuddleRepository huddleRepository;

    @MockBean
    MemberService memberService;

    @MockBean
    MemberRepository memberRepository;

    @MockBean
    GrantedAuthoritiesMapper grantedAuthoritiesMapper;

    @Test
    public void getOfMemberRegisterEndpointReturns200Ok() throws Exception {
        Member member = new Member("Ted", "tedyoung", "ROLE_MEMBER");
        member.setId(MemberId.of(1L));
        when(memberService.findByGithubUsername("tedyoung")).thenReturn(member);
        mockMvc.perform(get("/member/register")
                                // TODO: roles aren't needed here anymore
                                .with(OAuth2UserFactory.oAuth2User("ROLE_MEMBER")))
               .andExpect(status().isOk());
    }

    @Test
    public void postToRegisterRedirects() throws Exception {
        mockMvc.perform(post("/member/register")
                                .param("huddleId", "1")
                                .param("memberId", "1")
                                // TODO: roles aren't needed here anymore
                                .with(OAuth2UserFactory.oAuth2User("ROLE_MEMBER"))
                                .with(csrf()))
               .andExpect(status().is3xxRedirection());
    }

    @Test
    public void getOfMemberProfileEndpointReturns200Ok() throws Exception {
        Member member = new Member("Ted", "tedyoung", "ROLE_MEMBER");
        member.setId(MemberId.of(1L));
        when(memberService.findByGithubUsername("tedyoung")).thenReturn(member);
        mockMvc.perform(get("/member/profile")
                                .with(OAuth2UserFactory.oAuth2User("ROLE_MEMBER")))
               .andExpect(status().isOk());
    }

}
