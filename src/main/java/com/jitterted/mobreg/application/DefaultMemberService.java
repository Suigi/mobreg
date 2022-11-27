package com.jitterted.mobreg.application;

import com.jitterted.mobreg.application.port.MemberRepository;
import com.jitterted.mobreg.domain.Member;
import com.jitterted.mobreg.domain.MemberId;

import java.time.ZoneId;
import java.util.List;

public class DefaultMemberService implements MemberService {
    private final MemberRepository memberRepository;

    public DefaultMemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public Member findById(MemberId memberId) {
        return memberRepository
                .findById(memberId)
                .orElseThrow(MemberNotFoundByIdException::new);
    }

    @Override
    public Member findByGithubUsername(String username) {
        return memberRepository
                .findByGithubUsername(username.toLowerCase())
                .orElseThrow(() -> new MemberNotFoundByGitHubUsernameException(username));
    }

    @Override
    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    @Override
    public void changeEmail(Member member, String newEmail) {
        member.changeEmailTo(newEmail);
        save(member);
    }

    @Override
    public void changeTimeZone(Member member, String timeZone) {
        member.changeTimeZoneTo(ZoneId.of(timeZone));
        save(member);
    }

    @Override
    public void changeFirstName(Member member, String newFirstName) {
        // find Member via MemberId
        // make the change on the found Member
        // save the changed Member in the Repository
        member.changeFirstNameTo(newFirstName);
        save(member);
    }

    @Override
    public Member save(Member member) {
        return memberRepository.save(member);
    }
}
