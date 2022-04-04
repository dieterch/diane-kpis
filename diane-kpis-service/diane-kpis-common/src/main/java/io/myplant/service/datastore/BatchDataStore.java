package io.myplant.service.datastore;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.function.BiFunction;

public class BatchDataStore {
    public static final int BATCH_SIZE = 2000;

    public  <T /*extends BatchDataEntity*/> void executeBatch(Connection dbConnection, String sql, List<T> items, BiFunction<T, PreparedStatement, Boolean> setPrepareStatement) throws SQLException {
        try (PreparedStatement ps = dbConnection.prepareStatement(sql); ) {

            int count = 0;
            for (T item : items) {
                setPrepareStatement.apply(item, ps);

                ps.addBatch();

                if (++count % BATCH_SIZE == 0) {
                    //logger.info("assetId {}: start execute batch count {} ", assetId, count);
                    ps.executeBatch();
                    //logger.info("assetId {}: finished execute batch count {} ", assetId, count);
                }
            }
            if (count % BATCH_SIZE != 0) {
                ps.executeBatch(); // insert remaining records
            }
        }
    }

}
