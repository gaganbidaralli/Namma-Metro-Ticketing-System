package com.nammametro.repository;

import com.nammametro.model.GateLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GateLogRepository extends JpaRepository<GateLog, Long> {
    List<GateLog> findByTicketIdOrderByTimestampDesc(Long ticketId);
    List<GateLog> findByStationIdOrderByTimestampDesc(Long stationId);
    List<GateLog> findTop50ByOrderByTimestampDesc();
}
