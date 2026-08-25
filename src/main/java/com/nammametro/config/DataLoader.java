package com.nammametro.config;

import com.nammametro.model.MetroLine;
import com.nammametro.model.Station;
import com.nammametro.repository.StationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    private final StationRepository stationRepository;
    private final DataSource dataSource;

    public DataLoader(StationRepository stationRepository, DataSource dataSource) {
        this.stationRepository = stationRepository;
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) {
        if (stationRepository.count() == 0) {
            log.info("Populating Namma Metro station master dataset...");
            try {
                ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
                populator.addScript(new ClassPathResource("data-stations.sql"));
                populator.setContinueOnError(true);
                populator.execute(dataSource);
            } catch (Exception e) {
                log.warn("SQL script execution exception: {}", e.getMessage());
            }

            if (stationRepository.count() == 0) {
                log.info("Populating stations via programmatic seed...");
                seedStationsProgrammatically();
            }

            log.info("Stations initialization complete. Total stations in DB: {}", stationRepository.count());
        }
    }

    private void seedStationsProgrammatically() {
        List<Station> stations = new ArrayList<>();

        // Purple Line Stations (Challaghatta to Whitefield)
        stations.add(new Station(null, "CHL", "Challaghatta", "ಚಲ್ಲಘಟ್ಟ", MetroLine.PURPLE, 1, 0.0, false, 12.8985, 77.4682));
        stations.add(new Station(null, "KNG", "Kengeri", "ಕೆಂಗೇರಿ", MetroLine.PURPLE, 2, 1.5, false, 12.9080, 77.4770));
        stations.add(new Station(null, "KBT", "Kengeri Bus Terminal", "ಕೆಂಗೇರಿ ಬಸ್ ಟರ್ಮಿನಲ್", MetroLine.PURPLE, 3, 2.7, false, 12.9145, 77.4871));
        stations.add(new Station(null, "PTG", "Pattanagere", "ಪಟ್ಟಣಗೆರೆ", MetroLine.PURPLE, 4, 4.1, false, 12.9234, 77.4985));
        stations.add(new Station(null, "JNB", "Jnanabharathi", "ಜ್ಞಾನಭಾರತಿ", MetroLine.PURPLE, 5, 5.4, false, 12.9301, 77.5080));
        stations.add(new Station(null, "RRN", "Rajarajeshwari Nagar", "ರಾಜರಾಜೇಶ್ವರಿನಗರ", MetroLine.PURPLE, 6, 6.7, false, 12.9392, 77.5188));
        stations.add(new Station(null, "NYN", "Pantharapalya Nayandahalli", "ಪಂತರಪಾಳ್ಯ ನಾಯಂಡಹಳ್ಳಿ", MetroLine.PURPLE, 7, 8.1, false, 12.9468, 77.5284));
        stations.add(new Station(null, "MSR", "Mysuru Road", "ಮೈಸೂರು ರಸ್ತೆ", MetroLine.PURPLE, 8, 9.4, false, 12.9535, 77.5375));
        stations.add(new Station(null, "DPN", "Deepanjali Nagar", "ದೀಪಾಂಜಲಿ ನಗರ", MetroLine.PURPLE, 9, 10.7, false, 12.9582, 77.5458));
        stations.add(new Station(null, "ATG", "Attiguppe", "ಅತ್ತಿಗುಪ್ಪೆ", MetroLine.PURPLE, 10, 12.0, false, 12.9620, 77.5539));
        stations.add(new Station(null, "VJN", "Vijayanagar", "ವಿಜಯನಗರ", MetroLine.PURPLE, 11, 13.3, false, 12.9665, 77.5620));
        stations.add(new Station(null, "HSH", "Hosahalli", "ಹೊಸಹಳ್ಳಿ", MetroLine.PURPLE, 12, 14.6, false, 12.9710, 77.5701));
        stations.add(new Station(null, "MGD", "Magadi Road", "ಮಾಗಡಿ ರಸ್ತೆ", MetroLine.PURPLE, 13, 16.0, false, 12.9738, 77.5790));
        stations.add(new Station(null, "KSR", "Krantivira Sangolli Rayanna Railway Station", "ಕ್ರಾಂತಿವೀರ ಸಂಗೊಳ್ಳಿ ರಾಯಣ್ಣ", MetroLine.PURPLE, 14, 17.5, false, 12.9774, 77.5880));
        stations.add(new Station(null, "MJC_P", "Nadaprabhu Kempegowda Station Majestic", "ನಾಡಪ್ರಭು ಕೆಂಪೇಗೌಡ ನಿಲ್ದಾಣ ಮೆಜೆಸ್ಟಿಕ್", MetroLine.PURPLE, 15, 18.5, true, 12.9757, 77.5728));
        stations.add(new Station(null, "SMC", "Sir M. Visveshwaraya Central College", "ಸರ್ ಎಂ. ವಿಶ್ವೇಶ್ವರಯ್ಯ ಕಾಲೇಜು", MetroLine.PURPLE, 16, 19.6, false, 12.9745, 77.5855));
        stations.add(new Station(null, "VDS", "Dr. B.R. Ambedkar Vidhana Soudha", "ವಿಧಾನ ಸೌಧ", MetroLine.PURPLE, 17, 20.4, false, 12.9796, 77.5925));
        stations.add(new Station(null, "CBP", "Cubbon Park", "ಕಬ್ಬನ್ ಪಾರ್ಕ್", MetroLine.PURPLE, 18, 21.3, false, 12.9808, 77.6001));
        stations.add(new Station(null, "MGR", "MG Road", "ಎಂ.ಜಿ. ರಸ್ತೆ", MetroLine.PURPLE, 19, 22.4, false, 12.9756, 77.6066));
        stations.add(new Station(null, "TRN", "Trinity", "ಟ್ರಿನಿಟಿ", MetroLine.PURPLE, 20, 23.6, false, 12.9729, 77.6169));
        stations.add(new Station(null, "HLS", "Halasuru", "ಹಲಸೂರು", MetroLine.PURPLE, 21, 24.8, false, 12.9750, 77.6264));
        stations.add(new Station(null, "IDN", "Indiranagar", "ಇಂದಿರಾನಗರ", MetroLine.PURPLE, 22, 26.1, false, 12.9784, 77.6387));
        stations.add(new Station(null, "SVR", "Swami Vivekananda Road", "ಸ್ವಾಮಿ ವಿವೇಕಾನಂದ ರಸ್ತೆ", MetroLine.PURPLE, 23, 27.5, false, 12.9859, 77.6492));
        stations.add(new Station(null, "BYP", "Baiyappanahalli", "ಬೈಯ್ಯಪ್ಪನಹಳ್ಳಿ", MetroLine.PURPLE, 24, 28.8, false, 12.9912, 77.6580));
        stations.add(new Station(null, "BNH", "Benniganahalli", "ಬೆನ್ನಿಗಾನಹಳ್ಳಿ", MetroLine.PURPLE, 25, 30.2, false, 12.9982, 77.6690));
        stations.add(new Station(null, "KRP", "KR Pura (Krishnarajapura)", "ಕೆ.ಆರ್. ಪುರ", MetroLine.PURPLE, 26, 31.6, false, 13.0024, 77.6795));
        stations.add(new Station(null, "SGP", "Singayyanapalya", "ಸಿಂಗಯ್ಯನಪಾಳ್ಯ", MetroLine.PURPLE, 27, 33.0, false, 13.0051, 77.6912));
        stations.add(new Station(null, "GRP", "Garudacharapalya", "ಗರುಡಾಚಾರ್‌ಪಾಳ್ಯ", MetroLine.PURPLE, 28, 34.3, false, 13.0018, 77.7025));
        stations.add(new Station(null, "HDI", "Hoodi", "ಹೂಡಿ", MetroLine.PURPLE, 29, 35.6, false, 12.9965, 77.7135));
        stations.add(new Station(null, "STP", "Seetharampalya", "ಸೀತಾರಾಮಪಾಳ್ಯ", MetroLine.PURPLE, 30, 37.0, false, 12.9892, 77.7214));
        stations.add(new Station(null, "KDH", "Kundalahalli", "ಕುಂದಲಹಳ್ಳಿ", MetroLine.PURPLE, 31, 38.3, false, 12.9801, 77.7291));
        stations.add(new Station(null, "NLH", "Nallurhalli", "ನಲ್ಲೂರಹಳ್ಳಿ", MetroLine.PURPLE, 32, 39.7, false, 12.9735, 77.7370));
        stations.add(new Station(null, "SSH", "Sri Sathya Sai Hospital", "ಶ್ರೀ ಸತ್ಯಸಾಯಿ ಆಸ್ಪತ್ರೆ", MetroLine.PURPLE, 33, 41.0, false, 12.9698, 77.7445));
        stations.add(new Station(null, "PTA", "Pattandur Agrahara", "ಪಟ್ಟಂದೂರು ಅಗ್ರಹಾರ", MetroLine.PURPLE, 34, 42.4, false, 12.9682, 77.7521));
        stations.add(new Station(null, "KTP", "Kadugodi Tree Park", "ಕಾಡುಗೋಡಿ ಟ್ರೀ ಪಾರ್ಕ್", MetroLine.PURPLE, 35, 43.6, false, 12.9730, 77.7592));
        stations.add(new Station(null, "HFC", "Hopefarm Channasandra", "ಹೋಪ್‌ಫಾರ್ಮ್ ಚನ್ನಸಂದ್ರ", MetroLine.PURPLE, 36, 44.8, false, 12.9835, 77.7601));
        stations.add(new Station(null, "WFD", "Whitefield (Kadugodi)", "ವೈಟ್‌ಫೀಲ್ಡ್ (ಕಾಡುಗೋಡಿ)", MetroLine.PURPLE, 37, 46.0, false, 12.9958, 77.7609));

        // Green Line Stations (Madavara to Silk Institute)
        stations.add(new Station(null, "MDV", "Madavara (BIEC)", "ಮಾದಾವರ (ಬಿಐಇಸಿ)", MetroLine.GREEN, 1, 0.0, false, 13.0645, 77.4795));
        stations.add(new Station(null, "CKB", "Chikkabidarakallu", "ಚಿಕ್ಕಬಿದರಕಲ್ಲು", MetroLine.GREEN, 2, 1.4, false, 13.0532, 77.4891));
        stations.add(new Station(null, "MJN", "Manjunathanagar", "ಮಂಜುನಾಥನಗರ", MetroLine.GREEN, 3, 2.7, false, 13.0440, 77.4988));
        stations.add(new Station(null, "NGS", "Nagasandra", "ನಾಗಸಂದ್ರ", MetroLine.GREEN, 4, 4.0, false, 13.0375, 77.5042));
        stations.add(new Station(null, "DSR", "Dasarahalli", "ದಾಸರಹಳ್ಳಿ", MetroLine.GREEN, 5, 5.2, false, 13.0305, 77.5125));
        stations.add(new Station(null, "JLH", "Jalahalli", "ಜಾಲಹಳ್ಳಿ", MetroLine.GREEN, 6, 6.4, false, 13.0234, 77.5198));
        stations.add(new Station(null, "PNI", "Peenya Industry", "ಪೀಣ್ಯ ಇಂಡಸ್ಟ್ರಿ", MetroLine.GREEN, 7, 7.6, false, 13.0162, 77.5255));
        stations.add(new Station(null, "PNY", "Peenya", "ಪೀಣ್ಯ", MetroLine.GREEN, 8, 8.8, false, 13.0110, 77.5312));
        stations.add(new Station(null, "GGP", "Goraguntepalya", "ಗೊರಗುಂಟೆಪಾಳ್ಯ", MetroLine.GREEN, 9, 10.0, false, 13.0078, 77.5398));
        stations.add(new Station(null, "YWP", "Yeshwantpur", "ಯಶವಂತಪುರ", MetroLine.GREEN, 10, 11.3, false, 13.0034, 77.5495));
        stations.add(new Station(null, "SSF", "Sandal Soap Factory", "ಸ್ಯಾಂಡಲ್ ಸೋಪ್ ಫ್ಯಾಕ್ಟರಿ", MetroLine.GREEN, 11, 12.5, false, 12.9995, 77.5552));
        stations.add(new Station(null, "MLK", "Mahalakshmi", "ಮಹಾಲಕ್ಷ್ಮಿ", MetroLine.GREEN, 12, 13.7, false, 12.9950, 77.5598));
        stations.add(new Station(null, "RJN", "Rajajinagar", "ರಾಜಾಜಿನಗರ", MetroLine.GREEN, 13, 14.9, false, 12.9902, 77.5530));
        stations.add(new Station(null, "KVR", "Kuvempu Road", "ಕುವೆಂಪು ರಸ್ತೆ", MetroLine.GREEN, 14, 16.1, false, 12.9845, 77.5562));
        stations.add(new Station(null, "SRP", "Srirampura", "ಶ್ರೀರಾಮಪುರ", MetroLine.GREEN, 15, 17.3, false, 12.9810, 77.5620));
        stations.add(new Station(null, "SMP", "Sampige Road", "ಸಂಪಿಗೆ ರಸ್ತೆ", MetroLine.GREEN, 16, 18.5, false, 12.9780, 77.5680));
        stations.add(new Station(null, "MJC_G", "Nadaprabhu Kempegowda Station Majestic", "ನಾಡಪ್ರಭು ಕೆಂಪೇಗೌಡ ನಿಲ್ದಾಣ ಮೆಜೆಸ್ಟಿಕ್", MetroLine.GREEN, 17, 19.5, true, 12.9757, 77.5728));
        stations.add(new Station(null, "CKP", "Chickpete", "ಚಿಕ್ಕಪೇಟೆ", MetroLine.GREEN, 18, 20.7, false, 12.9695, 77.5750));
        stations.add(new Station(null, "KRM", "Krishna Rajendra Market", "ಕೃಷ್ಣ ರಾಜೇಂದ್ರ ಮಾರುಕಟ್ಟೆ", MetroLine.GREEN, 19, 21.8, false, 12.9628, 77.5765));
        stations.add(new Station(null, "NTC", "National College", "ನ್ಯಾಷನಲ್ ಕಾಲೇಜು", MetroLine.GREEN, 20, 22.9, false, 12.9555, 77.5780));
        stations.add(new Station(null, "LBG", "Lalbagh", "ಲಾಲ್‌ಬಾಗ್", MetroLine.GREEN, 21, 24.1, false, 12.9482, 77.5802));
        stations.add(new Station(null, "SEC", "South End Circle", "ಸೌತ್ ಎಂಡ್ ಸರ್ಕಲ್", MetroLine.GREEN, 22, 25.3, false, 12.9405, 77.5818));
        stations.add(new Station(null, "JYN", "Jayanagar", "ಜಯನಗರ", MetroLine.GREEN, 23, 26.5, false, 12.9320, 77.5828));
        stations.add(new Station(null, "RVR", "Rashtreeya Vidyalaya Road (RV Road)", "ರಾಷ್ಟ್ರೀಯ ವಿದ್ಯಾಲಯ ರಸ್ತೆ", MetroLine.GREEN, 24, 27.7, false, 12.9235, 77.5835));
        stations.add(new Station(null, "BSK", "Banashankari", "ಬನಶಂಕರಿ", MetroLine.GREEN, 25, 29.0, false, 12.9150, 77.5735));
        stations.add(new Station(null, "JPN", "Jaya Prakash Nagar", "ಜಯಪ್ರಕಾಶ ನಗರ", MetroLine.GREEN, 26, 30.2, false, 12.9075, 77.5730));
        stations.add(new Station(null, "YLC", "Yelachenahalli", "ಯಲಚೇನಹಳ್ಳಿ", MetroLine.GREEN, 27, 31.5, false, 12.8995, 77.5720));
        stations.add(new Station(null, "KKC", "Konanakunte Cross", "ಕೊಣನಕುಂಟೆ ಕ್ರಾಸ್", MetroLine.GREEN, 28, 32.8, false, 12.8895, 77.5680));
        stations.add(new Station(null, "DDK", "Doddakallasandra", "ದೊಡ್ಡಕಲ್ಲಸಂದ್ರ", MetroLine.GREEN, 29, 34.0, false, 12.8805, 77.5615));
        stations.add(new Station(null, "VJH", "Vajarahalli", "ವಾಜರಹಳ್ಳಿ", MetroLine.GREEN, 30, 35.2, false, 12.8710, 77.5540));
        stations.add(new Station(null, "TGP", "Thalaghattapura", "ತಲಘಟ್ಟಪುರ", MetroLine.GREEN, 31, 36.4, false, 12.8625, 77.5450));
        stations.add(new Station(null, "SKI", "Silk Institute", "ಸಿಲ್ಕ್ ಇನ್‌ಸ್ಟಿಟ್ಯೂಟ್", MetroLine.GREEN, 32, 37.8, false, 12.8540, 77.5350));

        stationRepository.saveAll(stations);
        log.info("Programmatic seed complete. Inserted {} stations across Purple and Green lines.", stations.size());
    }
}
