package com.edu.servicesImpl;

import com.edu.dto.VerificationStatsDTO;
import com.edu.entity.FraudSignal.FraudSignalType;
import com.edu.repositories.DocumentVerificationResultRepository;
import com.edu.repositories.FraudSignalRepository;
import com.edu.services.VerificationStatisticsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class VerificationStatisticsServiceImpl implements VerificationStatisticsService {

    private final DocumentVerificationResultRepository resultRepository;
    private final FraudSignalRepository fraudSignalRepository;

    public VerificationStatisticsServiceImpl(DocumentVerificationResultRepository resultRepository,
                                             FraudSignalRepository fraudSignalRepository) {
        this.resultRepository = resultRepository;
        this.fraudSignalRepository = fraudSignalRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public VerificationStatsDTO getVerificationStats() {
        long verified = resultRepository.countByIsVerifiedTrue();
        long flagged = resultRepository.countByIsVerifiedFalse();
        
        Double avgScore = resultRepository.getAverageConfidenceScore();
        double averageConfidence = avgScore != null ? avgScore : 0.0;

        List<Object[]> countsData = fraudSignalRepository.getFraudSignalCounts();
        Map<FraudSignalType, Long> fraudSignalCounts = new EnumMap<>(FraudSignalType.class);
        
        // Populate all enums with 0 initially to ensure complete map keys
        for (FraudSignalType type : FraudSignalType.values()) {
            fraudSignalCounts.put(type, 0L);
        }

        for (Object[] row : countsData) {
            if (row[0] instanceof FraudSignalType) {
                fraudSignalCounts.put((FraudSignalType) row[0], (Long) row[1]);
            }
        }

        return new VerificationStatsDTO(verified, flagged, averageConfidence, fraudSignalCounts);
    }
}
