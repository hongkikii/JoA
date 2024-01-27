package com.mjuAppSW.joA.common.auth;

import com.mjuAppSW.joA.domain.member.Member;
import com.mjuAppSW.joA.domain.member.infrastructure.MemberRepository;
import com.mjuAppSW.joA.domain.member.exception.PermanentBanException;
import com.mjuAppSW.joA.domain.memberProfile.exception.MemberNotFoundException;
import com.mjuAppSW.joA.geography.location.exception.AccessStoppedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberChecker {

    private final MemberRepository memberRepository;

    public Member findBySessionId(Long sessionId) {
        return memberRepository.findBysessionId(sessionId)
                .orElseThrow(MemberNotFoundException::new);
    }

    public Member findFilterBySessionId(Long sessionId) {
        return memberRepository.findBysessionId(sessionId)
                .filter(member -> {
                    if (member.getStatus() == 1 || member.getStatus() == 2) {
                        throw new AccessStoppedException();
                    }
                    if (member.getStatus() == 3) {
                        throw new PermanentBanException();
                    }
                    return true;
                })
                .orElseThrow(MemberNotFoundException::new);
    }

    public Member findById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(MemberNotFoundException::new);
    }

    public Member findByLoginId(String loginId) {
        return memberRepository.findByloginId(loginId)
                .orElseThrow(MemberNotFoundException::new);
    }

    public void checkStopped(Member member) {
        if (member.getStatus() == 1 || member.getStatus() == 2 || member.getStatus() == 3) {
            throw new AccessStoppedException();
        }
    }

    public boolean isStopped(Member member) {
        return member.getStatus() == 1 || member.getStatus() == 2 || member.getStatus() == 3;
    }
}
