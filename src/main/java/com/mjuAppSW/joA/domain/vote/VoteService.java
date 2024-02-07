package com.mjuAppSW.joA.domain.vote;

import com.mjuAppSW.joA.domain.member.Member;
import com.mjuAppSW.joA.domain.member.service.MemberService;
import com.mjuAppSW.joA.domain.vote.exception.InvalidVoteExistedException;
import com.mjuAppSW.joA.domain.vote.repository.VoteJpaRepository;
import com.mjuAppSW.joA.domain.vote.repository.VoteRepository;
import com.mjuAppSW.joA.geography.block.exception.BlockAccessForbiddenException;
import com.mjuAppSW.joA.domain.vote.dto.request.VoteRequest;
import com.mjuAppSW.joA.domain.vote.dto.response.VoteContent;
import com.mjuAppSW.joA.domain.vote.dto.response.VoteListResponse;
import com.mjuAppSW.joA.domain.vote.exception.VoteAlreadyExistedException;
import com.mjuAppSW.joA.domain.vote.exception.VoteCategoryNotFoundException;
import com.mjuAppSW.joA.domain.vote.voteCategory.VoteCategory;
import com.mjuAppSW.joA.domain.vote.voteCategory.VoteCategoryRepository;
import com.mjuAppSW.joA.geography.block.BlockRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class VoteService {
    private final VoteRepository voteRepository;
    private final VoteCategoryRepository voteCategoryRepository;
    private final BlockRepository blockRepository;
    private final MemberService memberService;

    @Transactional
    public void send(VoteRequest request) {
        Member giveMember = memberService.getNormalBySessionId(request.getGiveId());
        Member takeMember = memberService.getById(request.getTakeId());
        VoteCategory voteCategory = findVoteCategoryById(request.getCategoryId());

        Long giveMemberId = giveMember.getId();
        Long takeMemberId = takeMember.getId();

        checkEqualVote(giveMemberId, takeMemberId, voteCategory.getId());
        checkInvalidVote(giveMemberId, takeMemberId);
        checkBlock(giveMemberId, takeMemberId);

        createVote(giveMember, takeMember, voteCategory, request.getHint());
    }

    private VoteCategory findVoteCategoryById(Long id) {
        return voteCategoryRepository.findById(id)
                .orElseThrow(VoteCategoryNotFoundException::new);
    }

    private void checkEqualVote(Long giveId, Long takeId, Long categoryId) {
        voteRepository.findTodayVote(giveId, takeId, categoryId)
                .ifPresent(vote -> {
                    throw new VoteAlreadyExistedException();});
    }

    private void checkInvalidVote(Long giveId, Long takeId) {
        if (voteRepository.findInvalidVotes(giveId, takeId).size() != 0) {
            throw new InvalidVoteExistedException();
        }
    }

    private void createVote(Member giveMember, Member takeMember, VoteCategory voteCategory, String hint) {
        voteRepository.save(Vote.builder()
                            .giveId(giveMember.getId())
                            .member(takeMember)
                            .voteCategory(voteCategory)
                            .date(LocalDateTime.now())
                            .hint(hint)
                            .build());
    }

    public VoteListResponse get(Long sessionId) {
        Member findTakeMember = memberService.getNormalBySessionId(sessionId);
        return VoteListResponse.of(getVoteList(findTakeMember.getId()));
    }

    private List<VoteContent> getVoteList(Long id) {
        Pageable pageable = PageRequest.of(0, 30);
        return findVotesByTakeId(id, pageable).stream()
                                            .map(this::makeVoteContent)
                                            .collect(Collectors.toList());
    }

    private VoteContent makeVoteContent(Vote vote) {
        return VoteContent.builder()
                        .voteId(vote.getId())
                        .categoryId(vote.getVoteCategory().getId())
                        .hint(vote.getHint())
                        .build();
    }

    private void checkBlock(Long giveId, Long takeId) {
        if (blockRepository.findBlockByIds(giveId, takeId).size() != 0) {
            throw new BlockAccessForbiddenException();
        }
    }

    private List<Vote> findVotesByTakeId(Long id, Pageable pageable) {
        return voteRepository.findValidAllByTakeId(id, pageable);
    }
}
