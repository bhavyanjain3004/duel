package com.duel.repository;

import com.duel.domain.MatchRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MatchRecordRepository extends JpaRepository<MatchRecord, Long> {
}
