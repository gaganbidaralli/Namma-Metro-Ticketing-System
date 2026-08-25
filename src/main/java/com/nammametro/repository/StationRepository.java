package com.nammametro.repository;

import com.nammametro.model.MetroLine;
import com.nammametro.model.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StationRepository extends JpaRepository<Station, Long> {
    Optional<Station> findByStationCode(String stationCode);
    List<Station> findByLineNameOrderBySequenceNumAsc(MetroLine lineName);
    List<Station> findByIsInterchangeTrue();
    List<Station> findAllByOrderByLineNameAscSequenceNumAsc();
}
