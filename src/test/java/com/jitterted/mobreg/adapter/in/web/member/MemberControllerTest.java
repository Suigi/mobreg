package com.jitterted.mobreg.adapter.in.web.member;

import com.jitterted.mobreg.adapter.in.web.OAuth2UserFactory;
import com.jitterted.mobreg.application.DefaultMemberService;
import com.jitterted.mobreg.application.EnsembleService;
import com.jitterted.mobreg.application.EnsembleServiceFactory;
import com.jitterted.mobreg.application.MemberService;
import com.jitterted.mobreg.application.port.DummyNotifier;
import com.jitterted.mobreg.application.port.DummyVideoConferenceScheduler;
import com.jitterted.mobreg.application.port.InMemoryEnsembleRepository;
import com.jitterted.mobreg.application.port.InMemoryMemberRepository;
import com.jitterted.mobreg.application.port.MemberRepository;
import com.jitterted.mobreg.domain.Ensemble;
import com.jitterted.mobreg.domain.Member;
import com.jitterted.mobreg.domain.MemberFactory;
import com.jitterted.mobreg.domain.MemberId;
import com.jitterted.mobreg.domain.MemberStatus;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import java.time.ZonedDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

class MemberControllerTest {

    private static final MemberService CRASH_TEST_DUMMY_MEMBER_SERVICE = null;

    @Test
    void ensembleFormContainsMemberIdForOAuth2User() throws Exception {
        InMemoryEnsembleRepository ensembleRepository = new InMemoryEnsembleRepository();
        ensembleRepository.save(new Ensemble("GET Test", ZonedDateTime.now()));
        EnsembleService ensembleService = EnsembleServiceFactory.createServiceWith(ensembleRepository);

        InMemoryMemberRepository memberRepository = new InMemoryMemberRepository();
        Member member = MemberFactory.createMember(11, "name", "ghuser");
        memberRepository.save(member);
        MemberService memberService = new DefaultMemberService(memberRepository);

        MemberController memberController = new MemberController(ensembleService, memberService);

        Model model = new ConcurrentModel();
        OAuth2User oAuth2UserWithMemberRole = OAuth2UserFactory.createOAuth2UserWithMemberRole("ghuser", "ROLE_MEMBER");
        SecurityContextImpl securityContext = new SecurityContextImpl(new OAuth2AuthenticationToken(oAuth2UserWithMemberRole,
                                                                                                    Set.of(new SimpleGrantedAuthority("ROLE_MEMBER")),
                                                                                                    "github"));
        memberController.showEnsemblesForUser(model,
                                              oAuth2UserWithMemberRole,
                                              securityContext);

        assertThat((String) model.getAttribute("firstName"))
                .isEqualTo("name");
        assertThat((String) model.getAttribute("githubUsername"))
                .isEqualTo("ghuser");

        MemberRegisterForm memberRegisterForm = (MemberRegisterForm) model.getAttribute("register");

        assertThat(memberRegisterForm.getMemberId())
                .isEqualTo(11);
    }

    @Test
    void memberRegistersForEnsembleWillBeRegisteredForThatEnsemble() throws Exception {
        InMemoryEnsembleRepository ensembleRepository = new InMemoryEnsembleRepository();
        Ensemble ensemble = ensembleRepository.save(new Ensemble("Test", ZonedDateTime.now()));
        InMemoryMemberRepository memberRepository = new InMemoryMemberRepository();
        EnsembleService ensembleService = new EnsembleService(ensembleRepository, memberRepository,
                                                              new DummyNotifier(), new DummyVideoConferenceScheduler());
        MemberController memberController = new MemberController(ensembleService, CRASH_TEST_DUMMY_MEMBER_SERVICE);

        MemberRegisterForm memberRegisterForm = createMemberFormFor(ensemble, memberRepository);
        String redirectPage = memberController.accept(memberRegisterForm);

        assertThat(redirectPage)
                .isEqualTo("redirect:/member/register");

        assertThat(ensemble.acceptedMembers())
                .extracting(MemberId::id)
                .containsOnly(memberRegisterForm.getMemberId());
    }

    @Test
    void memberDeclinesWillBeDeclinedForEnsemble() throws Exception {
        InMemoryEnsembleRepository ensembleRepository = new InMemoryEnsembleRepository();
        Ensemble ensemble = ensembleRepository.save(new Ensemble("Test", ZonedDateTime.now()));
        InMemoryMemberRepository memberRepository = new InMemoryMemberRepository();
        EnsembleService ensembleService = new EnsembleService(ensembleRepository, memberRepository,
                                                              new DummyNotifier(), new DummyVideoConferenceScheduler());
        MemberController memberController = new MemberController(ensembleService, CRASH_TEST_DUMMY_MEMBER_SERVICE);

        MemberRegisterForm memberRegisterForm = createMemberFormFor(ensemble, memberRepository);
        String redirectPage = memberController.decline(memberRegisterForm);

        assertThat(redirectPage)
                .isEqualTo("redirect:/member/register");
        assertThat(ensemble.memberStatusFor(MemberId.of(memberRegisterForm.getMemberId())))
                .isEqualByComparingTo(MemberStatus.DECLINED);
    }

    @Test
    void memberJoiningAsSpectatorBecomesSpectator() throws Exception {
        InMemoryEnsembleRepository ensembleRepository = new InMemoryEnsembleRepository();
        Ensemble ensemble = ensembleRepository.save(new Ensemble("Test", ZonedDateTime.now()));
        InMemoryMemberRepository memberRepository = new InMemoryMemberRepository();
        EnsembleService ensembleService = new EnsembleService(ensembleRepository, memberRepository,
                                                              new DummyNotifier(), new DummyVideoConferenceScheduler());
        MemberController memberController = new MemberController(ensembleService, CRASH_TEST_DUMMY_MEMBER_SERVICE);

        MemberRegisterForm memberRegisterForm = createMemberFormFor(ensemble, memberRepository);
        String redirectPage = memberController.joinAsSpectator(memberRegisterForm);

        assertThat(redirectPage)
                .isEqualTo("redirect:/member/register");
        assertThat(ensemble.spectators())
                .containsExactly(MemberId.of(memberRegisterForm.getMemberId()));
    }

    @NotNull
    private MemberRegisterForm createMemberFormFor(Ensemble ensemble, MemberRepository memberRepository) {
        MemberRegisterForm memberRegisterForm = new MemberRegisterForm();
        memberRegisterForm.setEnsembleId(ensemble.getId().id());

        Member member = MemberFactory.createMember(8, "name", "username");
        memberRepository.save(member);
        memberRegisterForm.setMemberId(member.getId().id());

        return memberRegisterForm;
    }

}
