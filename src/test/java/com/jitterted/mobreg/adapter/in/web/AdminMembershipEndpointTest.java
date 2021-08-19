package com.jitterted.mobreg.adapter.in.web;


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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminMembershipController.class)
@Tag("mvc")
@WithMockUser(username = "admin", authorities = {"ROLE_MEMBER","ROLE_ADMIN"})
public class AdminMembershipEndpointTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    MemberRepository memberRepository;

    @MockBean
    HuddleRepository huddleRepository;

    @MockBean
    GrantedAuthoritiesMapper grantedAuthoritiesMapper;

    @Test
    public void getOfMemberAdminPageIsStatus200Ok() throws Exception {
        mockMvc.perform(get("/admin/members"))
               .andExpect(status().isOk());
    }

}
