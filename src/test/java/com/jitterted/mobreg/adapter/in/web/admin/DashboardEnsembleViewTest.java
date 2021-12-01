package com.jitterted.mobreg.adapter.in.web.admin;

import com.jitterted.mobreg.application.EnsembleService;
import com.jitterted.mobreg.application.EnsembleServiceFactory;
import com.jitterted.mobreg.application.MemberFactory;
import com.jitterted.mobreg.application.MemberService;
import com.jitterted.mobreg.application.port.InMemoryEnsembleRepository;
import com.jitterted.mobreg.application.port.InMemoryMemberRepository;
import com.jitterted.mobreg.domain.Ensemble;
import com.jitterted.mobreg.domain.Member;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import org.springframework.web.server.ResponseStatusException;

import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.*;

public class DashboardEnsembleViewTest {

    @Test
    public void detailViewOfExistingHuddleByItsIdIsReturned() throws Exception {
        MemberService memberService = new MemberService(new InMemoryMemberRepository());
        InMemoryEnsembleRepository huddleRepository = new InMemoryEnsembleRepository();
        EnsembleService ensembleService = EnsembleServiceFactory.createServiceWith(huddleRepository);
        Ensemble savedEnsemble = huddleRepository.save(new Ensemble("Ensemble #1", ZonedDateTime.now()));
        AdminDashboardController adminDashboardController = new AdminDashboardController(ensembleService, memberService);

        Model model = new ConcurrentModel();
        adminDashboardController.ensembleDetailView(model, 0L);

        EnsembleDetailView huddle = (EnsembleDetailView) model.getAttribute("ensemble");

        assertThat(huddle.name())
                .isEqualTo(savedEnsemble.name());
    }

    @Test
    public void detailViewOfExistingHuddleWithOneParticipantReturnsHuddleWithParticipantView() throws Exception {
        MemberService memberService = new MemberService(new InMemoryMemberRepository());
        Member member = MemberFactory.createMember(11, "name", "github");
        memberService.save(member);

        InMemoryEnsembleRepository huddleRepository = new InMemoryEnsembleRepository();
        EnsembleService ensembleService = EnsembleServiceFactory.createServiceWith(huddleRepository);
        Ensemble ensemble = new Ensemble("Ensemble #1", ZonedDateTime.now());
        ensemble.acceptedBy(member.getId());
        huddleRepository.save(ensemble);
        AdminDashboardController adminDashboardController = new AdminDashboardController(ensembleService, memberService);

        Model model = new ConcurrentModel();
        adminDashboardController.ensembleDetailView(model, ensemble.getId().id());

        EnsembleDetailView huddleView = (EnsembleDetailView) model.getAttribute("ensemble");

        assertThat(huddleView.memberViews())
                .hasSize(1);
    }

    @Test
    public void detailViewOfNonExistentHuddleReturns404NotFound() throws Exception {
        MemberService memberService = new MemberService(new InMemoryMemberRepository());
        InMemoryEnsembleRepository huddleRepository = new InMemoryEnsembleRepository();
        EnsembleService ensembleService = EnsembleServiceFactory.createServiceWith(huddleRepository);
        AdminDashboardController adminDashboardController = new AdminDashboardController(ensembleService, memberService);
        Model model = new ConcurrentModel();

        assertThatThrownBy(() -> {
            adminDashboardController.ensembleDetailView(model, 0L);
        }).isInstanceOf(ResponseStatusException.class)
          .extracting("status")
          .isEqualTo(HttpStatus.NOT_FOUND);
    }

}
