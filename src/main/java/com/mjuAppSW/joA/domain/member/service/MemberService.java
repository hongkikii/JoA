package com.mjuAppSW.joA.domain.member.service;

import static com.mjuAppSW.joA.common.constant.Constants.MemberStatus.STEP_1_STOP_STATUS;
import static com.mjuAppSW.joA.common.constant.Constants.MemberStatus.STEP_2_STOP_STATUS;
import static com.mjuAppSW.joA.common.constant.Constants.MemberStatus.STEP_3_STOP_STATUS;

import com.mjuAppSW.joA.domain.college.MCollege;
import com.mjuAppSW.joA.domain.member.Member;
import com.mjuAppSW.joA.domain.member.exception.LoginIdAlreadyExistedException;
import com.mjuAppSW.joA.domain.member.exception.MemberAlreadyExistedException;
import com.mjuAppSW.joA.domain.member.exception.PermanentBanException;
import com.mjuAppSW.joA.domain.member.infrastructure.ImageUploader;
import com.mjuAppSW.joA.domain.member.infrastructure.PasswordManager;
import com.mjuAppSW.joA.domain.member.infrastructure.repository.MemberRepository;
import com.mjuAppSW.joA.domain.member.exception.MemberNotFoundException;
import com.mjuAppSW.joA.geography.college.PCollege;
import com.mjuAppSW.joA.geography.college.PCollegeService;
import com.mjuAppSW.joA.geography.location.Location;
import com.mjuAppSW.joA.geography.location.exception.AccessStoppedException;
import com.mjuAppSW.joA.geography.location.infrastructure.LocationRepository;
import jakarta.transaction.Transactional;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Builder
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PCollegeService pCollegeService;
    private final LocationRepository locationRepository;
    private final ImageUploader imageUploader;
    private final PasswordManager passwordManager;

    @Transactional
    public void create(Long sessionId, String name, String loginId,
                         String password, String uEmail, MCollege mCollege) {
        passwordManager.validate(password);
        String salt = passwordManager.createSalt();
        String hashedPassword = passwordManager.createHashed(password, salt);
        Member member = Member.builder()
                            .name(name)
                            .loginId(loginId)
                            .password(hashedPassword)
                            .salt(salt)
                            .uEmail(uEmail)
                            .college(mCollege)
                            .sessionId(sessionId).build();
        memberRepository.save(member);
        PCollege pCollege = pCollegeService.findById(mCollege.getId());
        Location location = new Location(member.getId(), pCollege);
        locationRepository.save(location);
    }

    public void delete(Member member) {
        imageUploader.delete(member.getUrlCode());
        locationRepository.deleteById(member.getId());
        member.setWithdrawal();
    }
}
