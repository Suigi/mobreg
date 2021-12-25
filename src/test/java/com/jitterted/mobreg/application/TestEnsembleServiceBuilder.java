package com.jitterted.mobreg.application;

import com.jitterted.mobreg.application.port.DummyVideoConferenceScheduler;
import com.jitterted.mobreg.application.port.EnsembleRepository;
import com.jitterted.mobreg.application.port.InMemoryEnsembleRepository;
import com.jitterted.mobreg.application.port.InMemoryMemberRepository;
import com.jitterted.mobreg.application.port.MemberRepository;
import com.jitterted.mobreg.application.port.Notifier;
import com.jitterted.mobreg.application.port.VideoConferenceScheduler;
import com.jitterted.mobreg.domain.Ensemble;
import com.jitterted.mobreg.domain.EnsembleId;
import com.jitterted.mobreg.domain.Member;
import com.jitterted.mobreg.domain.MemberId;
import org.jetbrains.annotations.NotNull;

import java.time.ZonedDateTime;

public class TestEnsembleServiceBuilder {
    private EnsembleRepository ensembleRepository;
    private MemberRepository memberRepository;
    private Notifier notifier;
    private EnsembleId lastEnsembleId;
    private Ensemble lastEnsemble;
    private MemberId lastMemberId;
    private VideoConferenceScheduler videoConferenceScheduler = new DummyVideoConferenceScheduler();

    public TestEnsembleServiceBuilder() {
        inMemoryRepositories();
    }

    public TestEnsembleServiceBuilder ensembleRepository(EnsembleRepository ensembleRepository) {
        this.ensembleRepository = ensembleRepository;
        return this;
    }

    public EnsembleService build() {
        return new EnsembleService(ensembleRepository, memberRepository, notifier, videoConferenceScheduler);
    }

    public TestEnsembleServiceBuilder memberRepository(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
        return this;
    }

    public TestEnsembleServiceBuilder notifier(Notifier notifier) {
        this.notifier = notifier;
        return this;
    }

    public TestEnsembleServiceBuilder saveEnsembleStartingNow(String ensembleName) {
        Ensemble ensemble = new Ensemble(ensembleName, ZonedDateTime.now());
        return saveEnsemble(ensemble);
    }

    @NotNull
    public TestEnsembleServiceBuilder saveEnsemble(Ensemble ensemble) {
        lastEnsemble = ensembleRepository.save(ensemble);
        lastEnsembleId = lastEnsemble.getId();
        return this;
    }

    public TestEnsembleServiceBuilder saveMemberAndAccept(String firstName, String githubUsername) {
        Member member = new Member(firstName, githubUsername);
        MemberId memberId = memberRepository.save(member).getId();
        lastEnsemble.acceptedBy(memberId);
        return this;
    }

    public TestEnsembleServiceBuilder inMemoryRepositories() {
        ensembleRepository(new InMemoryEnsembleRepository());
        memberRepository(new InMemoryMemberRepository());
        return this;
    }

    public EnsembleId lastEnsembleId() {
        return lastEnsembleId;
    }

}
