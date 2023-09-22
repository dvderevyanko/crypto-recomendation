package com.crypto.service.parser;

import com.crypto.service.parser.dto.DayPriceRecord;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PriceFileReader {

    List<DayPriceRecord> readFile(MultipartFile file);
}
