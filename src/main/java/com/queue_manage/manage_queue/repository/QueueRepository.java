package com.queue_manage.manage_queue.repository;


import com.queue_manage.manage_queue.model.*;
import com.queue_manage.manage_queue.model.QueueEntry.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QueueRepository extends JpaRepository<QueueEntry, Long> {

    List<QueueEntry> findByStatusOrderByJoinedAtAsc(Status status);

    List<QueueEntry> findAllByOrderByJoinedAtAsc();

    Optional<QueueEntry> findFirstByStatusOrderByJoinedAtAsc(Status status);

    long countByStatus(Status status);

    @Query("SELECT AVG(TIMESTAMPDIFF(MINUTE, e.joinedAt, e.servedAt)) FROM QueueEntry e WHERE e.status = 'SERVED' AND e.servedAt IS NOT NULL")
    Double findAverageWaitTime();

    List<QueueEntry> findByStatusIn(List<Status> statuses);
}
