package com.crypto.service.parser.csv;

import com.crypto.service.parser.PriceFileReader;
import com.crypto.service.parser.dto.DayPriceRecord;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CSVPriceReader implements PriceFileReader {

    public static String TYPE = "text/csv";
    private static final String[] HEADERS = new String[]{"timestamp", "symbol", "price"};

    @Override
    public List<DayPriceRecord> readFile(MultipartFile file) {
        validateFile(file);
        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(fileReader,
                     CSVFormat.DEFAULT.builder()
                             .setHeader(HEADERS)
                             .setSkipHeaderRecord(true)
                             .build())) {
            return csvParser.getRecords().stream()
                    .map(csvRecord -> {
                        DayPriceRecord price = new DayPriceRecord();
                        price.setTimestamp(Long.parseLong(csvRecord.get(HEADERS[0])));
                        price.setCurrency(csvRecord.get(HEADERS[1]));
                        price.setPrice(new BigDecimal(csvRecord.get(HEADERS[2])));
                        return price;
                    }).collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("CSV file parsing error: " + e.getMessage());
        }
    }

    private void validateFile(MultipartFile file) {
        if (!TYPE.equals(file.getContentType())) {
            throw new RuntimeException();
        }
    }

}
