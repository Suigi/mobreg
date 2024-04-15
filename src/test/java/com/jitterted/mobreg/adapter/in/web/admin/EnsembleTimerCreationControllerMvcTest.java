package com.jitterted.mobreg.adapter.in.web.admin;

import com.jitterted.mobreg.adapter.in.web.TestAdminConfiguration;
import com.jitterted.mobreg.application.EnsembleTimerHolder;
import com.jitterted.mobreg.application.TestEnsembleServiceBuilder;
import com.jitterted.mobreg.application.port.EnsembleRepository;
import com.jitterted.mobreg.application.port.MemberRepository;
import com.jitterted.mobreg.domain.Ensemble;
import com.jitterted.mobreg.domain.EnsembleBuilder;
import com.jitterted.mobreg.domain.EnsembleId;
import com.jitterted.mobreg.domain.EnsembleTimer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("mvc")
@WebMvcTest({EnsembleTimerCreationController.class})
@Import(TestAdminConfiguration.class)
@WithMockUser(username = "admin", authorities = {"ROLE_MEMBER", "ROLE_ADMIN"})
class EnsembleTimerCreationControllerMvcTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    EnsembleRepository ensembleRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    EnsembleTimerHolder ensembleTimerHolder;

    @Test
    void postToTimerViewRedirects() throws Exception {
        createAndSaveEnsembleInRepositoryForId(113);
        mockMvc.perform(post("/admin/create-timer/113").with(csrf()))
               .andExpect(status().is3xxRedirection());
    }

    @Test
    void getForTimerViewEndpointReturns200OK() throws Exception {
        createTimerForEnsembleWithId(113);

        mockMvc.perform(get("/admin/timer-view/113"))
               .andExpect(status().isOk());
    }

    @Test
    void postToStartTimerEndpointReturns204NoContent() throws Exception {
        createTimerForEnsembleWithId(113);

        mockMvc.perform(post("/admin/start-timer/113").with(csrf()))
               .andExpect(status().isNoContent());
    }

    @Test
    void postToNextRotationEndpointReturns204NoContent() throws Exception {
        createAndStartTimerForEnsembleWithId(113);
        ensembleTimerHolder.handleTickFor(EnsembleId.of(113),
                                          Instant.now().plus(EnsembleTimer.DEFAULT_TIMER_DURATION));

        mockMvc.perform(post("/admin/rotate-timer/113").with(csrf()))
               .andExpect(status().isNoContent());
    }

    @Test
    void postToPauseTimerEndpointReturns204NoContent() throws Exception {
        createAndStartTimerForEnsembleWithId(113);

        mockMvc.perform(post("/admin/pause-timer/113").with(csrf()))
               .andExpect(status().isNoContent());

    }

    @Test
    void postToResumeTimerEndpointReturns204NoContent() throws Exception {
        createAndStartTimerForEnsembleWithId(113);

        mockMvc.perform(post("/admin/resume-timer/113").with(csrf()))
               .andExpect(status().isNoContent());
    }

    // -- Encapsulated Setup

    private void createAndStartTimerForEnsembleWithId(int ensembleId) throws Exception {
        createTimerForEnsembleWithId(ensembleId);
        mockMvc.perform(post("/admin/start-timer/113").with(csrf()));
    }

    private void createTimerForEnsembleWithId(int ensembleId) throws Exception {
        createAndSaveEnsembleInRepositoryForId(ensembleId);
        mockMvc.perform(post("/admin/create-timer/" + ensembleId).with(csrf()));
    }

    private void createAndSaveEnsembleInRepositoryForId(long ensembleId) {
        Ensemble ensemble = new EnsembleBuilder().id(ensembleId)
                                                 .startsNow()
                                                 .build();
        new TestEnsembleServiceBuilder()
                .withEnsembleRepository(ensembleRepository)
                .withMemberRepository(memberRepository)
                .saveEnsemble(ensemble)
                .withThreeParticipants();
    }
}