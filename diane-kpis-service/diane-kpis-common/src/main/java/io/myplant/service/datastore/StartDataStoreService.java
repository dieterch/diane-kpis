package io.myplant.service.datastore;

import io.myplant.domain.Start;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

@Service
public class StartDataStoreService extends BatchDataStore{
    private static final Logger logger = LoggerFactory.getLogger(StartDataStoreService.class);

    private final JdbcTemplate targetJdbcTemplate;

    public static final String STARTS_TABLE = "state_machine";
    public static final String INSERT_STARTS_SQL = "INSERT INTO " + STARTS_TABLE +
            "(asset_id, action_actual, action_from, action_to, trigger_date, trigger_msg_no, trigger_text, trigger_responsibility, trigger_count, demand_selector_switch, service_selector_switch, av_man_activated_status) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    public static final String DELETE_STARTS_FOR_ASSET_SQL = "DELETE FROM " + STARTS_TABLE  +" WHERE asset_id = ?";

    @Autowired
    public StartDataStoreService(JdbcTemplate targetJdbcTemplate) {
        this.targetJdbcTemplate = targetJdbcTemplate;
    }

    public void deleteAndInsertDeviceStart(final Long assetId, final List<Start> starts, int batchSize) throws SQLException {

        Connection dbConnection = targetJdbcTemplate.getDataSource().getConnection();
        try {
            dbConnection.setAutoCommit(false);
            Object [] deleteParams =  {assetId};
            int [] types = {Types.BIGINT};
            int rows = targetJdbcTemplate.update(DELETE_STARTS_FOR_ASSET_SQL, deleteParams, types);

            if (starts.size() != 0) {

                executeBatch(dbConnection, INSERT_STARTS_SQL, starts, (state, ps)->{
                    try{
                        ps.setLong(1, state.getAssetId());
                        ps.setLong(2, state.getStartDate());
                        ps.setInt(3, state.getScope().getValue());
                        ps.setInt(4, state.getValidStart());
                        ps.setLong(5, state.getTimeToMainsParallel());
                        ps.setInt(6, state.getTripsBeforeRampUpMainsParallel());
                        ps.setInt(7, state.getTripsBeforeMainsParallel());
                    }
                    catch(Exception ex){
                        logger.error("assetId " + assetId + ": Error while deleting or inserting DeviceStatus to DB", ex);
                        return false;
                    }
                    return true;
                });
            }
            dbConnection.commit();
            logger.info("assetId {}: inserted {} states  in database", assetId, starts.size());
        }
        catch(Exception e) {
            logger.error("assetId " + assetId + ": Error while deleting or inserting DeviceStatus to DB", e);
            dbConnection.rollback();
        }
        finally {
            dbConnection.close();
        }
        return;
    }


}

