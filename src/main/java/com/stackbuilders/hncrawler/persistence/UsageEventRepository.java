package com.stackbuilders.hncrawler.persistence;

import com.stackbuilders.hncrawler.domain.FilterType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UsageEventRepository extends JpaRepository<UsageEventEntity, Long> {

    List<UsageEventEntity> findAllByOrderByRequestedAtDesc();

    List<UsageEventEntity> findByFilterAppliedOrderByRequestedAtDesc(FilterType filterApplied);
}
