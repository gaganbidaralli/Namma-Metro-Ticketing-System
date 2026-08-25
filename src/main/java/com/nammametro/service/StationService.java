package com.nammametro.service;

import com.nammametro.dto.StationDTO;
import com.nammametro.model.MetroLine;
import com.nammametro.model.Station;
import com.nammametro.repository.StationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StationService {

    private final StationRepository stationRepository;

    public StationService(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    public List<StationDTO> getAllStations() {
        return stationRepository.findAllByOrderByLineNameAscSequenceNumAsc()
                .stream()
                .map(StationDTO::new)
                .collect(Collectors.toList());
    }

    public List<StationDTO> getStationsByLine(MetroLine line) {
        return stationRepository.findByLineNameOrderBySequenceNumAsc(line)
                .stream()
                .map(StationDTO::new)
                .collect(Collectors.toList());
    }

    public Optional<Station> getStationByCode(String code) {
        return stationRepository.findByStationCode(code);
    }

    public Optional<Station> getStationById(Long id) {
        return stationRepository.findById(id);
    }

    public Optional<Station> findStationByCodeOrId(String codeOrId) {
        try {
            Long id = Long.parseLong(codeOrId);
            Optional<Station> byId = stationRepository.findById(id);
            if (byId.isPresent()) return byId;
        } catch (NumberFormatException ignored) {}
        return stationRepository.findByStationCode(codeOrId);
    }
}
