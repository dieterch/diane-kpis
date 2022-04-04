package io.myplant.service;

import io.myplant.model.IeeeStates;
import io.myplant.model.ScopeType;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Scanner;

@Service
public class ScopeMapperService {
    private static final Logger logger = LoggerFactory.getLogger(ScopeMapperService.class);

    private final String FILE_NAME = "/kiel/msg_scope_map.csv";
    private final String MAP_SEPERATOR = ",";

    private static HashMap<Long, ScopeType> messageScopeMap = null;

    public ScopeType getScope(Long message, IeeeStates state) {
        if (!IeeeStates.isOutageForScope(state))
            return ScopeType.None;
        if (message == 0 && IeeeStates.isOutageForScope(state))
            return ScopeType.INNIO_Genset;

        if (!getScopeMap().containsKey(message))
            return ScopeType.Unclear;

        if (getScopeMap().get(message) != null)
            return getScopeMap().get(message);
        return ScopeType.Unclear;
    }

    public synchronized HashMap<Long, ScopeType> getScopeMap() {
        if (messageScopeMap == null) {
            messageScopeMap = readScopeMap();
        }
        return messageScopeMap;
    }

    private HashMap<Long, ScopeType> readScopeMap() {
        HashMap<Long, ScopeType> result = new HashMap<>();

        //Get file from resources folder
        ClassLoader classLoader = getClass().getClassLoader();


//        getClass().getResourceAsStream(this.getClass().getResource(FILE_NAME));
//        URL resource = this.getClass().getResource(FILE_NAME);
//        logger.info("try to read: " + resource.toString());
//
//        File mapFile = new File(this.getClass().getResource(FILE_NAME).getFile());
        try (InputStream in = getClass().getResourceAsStream(FILE_NAME);
             BufferedReader reader = new BufferedReader(new InputStreamReader(in));
             Scanner scanner = new Scanner(reader)) {

            int lineCount = 0;
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                lineCount++;
                String[] split = line.split(MAP_SEPERATOR);
                if (split != null && split.length == 2 && StringUtils.isNoneEmpty(split[1])) {

                    try {
                        result.put(Long.parseLong(split[0]), ScopeType.getNameByValue(split[1]));
                    } catch (Exception ex) {
                        logger.error("invalid format of scope mapping csv file on line " + lineCount, ex);
                    }


                }
            }
        } catch (IOException ex) {
            logger.error("problem to read mapping file: " + FILE_NAME, ex);
        }

        return result;
    }
}