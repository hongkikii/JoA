package com.mjuAppSW.joA.domain.vote.repository;

import com.mjuAppSW.joA.domain.vote.Vote;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public interface VoteRepository {

    void save(Vote vote);

    Optional<Vote> findById(Long id);

    List<String> findVoteCategoryById(Long id, PageRequest pageRequest);

    Optional<Vote> findTodayVote(Long giveId, Long takeId, Long categoryId);

    List<Vote> findInvalidVotes(Long giveId, Long takeId);

    List<Vote> findValidAllByTakeId(Long id, Pageable pageable);
}
