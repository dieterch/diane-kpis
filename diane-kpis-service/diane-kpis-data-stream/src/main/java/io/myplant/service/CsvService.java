package io.myplant.service;

import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.function.Function;

@Service
public class CsvService {
    private final Log logger = LogFactory.getLog(CsvService.class);

    public <T> void writeCsvFile(List<T> items, String destinationFile) {
        try {
            try (Writer writer = new FileWriter(destinationFile);) {
                StatefulBeanToCsv beanToCsv = new StatefulBeanToCsvBuilder(writer).build();
                beanToCsv.write(items);
            }
        } catch (Exception ex) {
        }
    }

    public <T> void createCSVFile(List<T> items, String destinationFile, Function<T, String[]> getHeader, Function<T, String[]> getRow) throws IOException {
        if (items.size() == 0) {
            logger.warn("No content to write fro file: " + destinationFile);
            return;
        }

        try (FileWriter out = new FileWriter(destinationFile);
             CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT
                     .withHeader(getHeader.apply(items.get(0))))) {
            for (T item : items) {
                printer.printRecord(getRow.apply(item));
            }
        }
    }
}

