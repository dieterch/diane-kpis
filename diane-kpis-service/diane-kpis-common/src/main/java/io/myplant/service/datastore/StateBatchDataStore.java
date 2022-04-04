package io.myplant.service.datastore;

import io.myplant.domain.DeviceState;
import io.myplant.model.*;
import io.myplant.service.ScopeMapperService;
import io.myplant.utils.StopWatch;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class StateBatchDataStore extends BatchDataStore {

    private static final Logger logger = LoggerFactory.getLogger(StateBatchDataStore.class);
    public static final int BATCH_SIZE = 2000;

    private final JdbcTemplate targetJdbcTemplate;
    private final ScopeMapperService scopeService;

    //asset_id, action_actual, action_from, action_to, trigger_date, trigger_msg_no, trigger_text, trigger_responsibility, trigger_count, demand_selector_switch, service_selector_switch, av_man_activated_status
    public static final String STATEMACHINETABLE = "state_machine";

    public static final String INSERT_DEVICE_STATUS_SQL = "INSERT INTO " + STATEMACHINETABLE +
            "(asset_id, action_actual, action_from, action_to, trigger_date, trigger_msg_no,  " +
            "trigger_count, aws, bws, avss, available_state, ieee_state, kiel_vz_state, kiel_vu_state, scope," +
            "outageNumber, OH, CumOH,AOH,CumAOH,PH,CumPH,OHatLastFOO,HSLF) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?,?,?,?,?,?,?,?,?)";


    public static final String DELETE_DEVICE_STATUS_FOR_ASSET_SQL = "DELETE FROM " + STATEMACHINETABLE + " WHERE asset_id = ?";

    public static final String DELETE_WITH_ID_SQL = "DELETE FROM " + STATEMACHINETABLE + " WHERE id = ?";

    public int deleteDeviceStatus(final Long assetId) throws SQLException {
        int rows = 0;
        Connection dbConnection = targetJdbcTemplate.getDataSource().getConnection();
        try {
            dbConnection.setAutoCommit(false);

            Object[] deleteParams = {assetId};
            int[] types = {Types.BIGINT};

            rows = targetJdbcTemplate.update(DELETE_DEVICE_STATUS_FOR_ASSET_SQL, deleteParams, types);

            dbConnection.commit();
            logger.info("assetId {}: deleted {} device status in database", assetId, rows);
        } catch (Exception e) {
            logger.error("assetId " + assetId + ": Error while deleting DeviceStatus from DB.", e);
            dbConnection.rollback();
        } finally {
            dbConnection.close();
        }
        return rows;
    }

    public int deleteDeviceStatus(final Long assetId, List<Long> statesToDelete, int batchSize) throws SQLException {
        int rows = 0;
        Connection dbConnection = targetJdbcTemplate.getDataSource().getConnection();
        try {
            dbConnection.setAutoCommit(false);
            executeBatch(dbConnection, DELETE_WITH_ID_SQL, statesToDelete,
                    (state, ps) -> {
                        try {
                            ps.setLong(1, state);
                        } catch (Exception ex) {
                            logger.error("assetId " + assetId + ": Error while deleting or inserting DeviceStatus to DB", ex);
                            return false;
                        }
                        return true;
                    });

            dbConnection.commit();
            logger.info("assetId {}: Deleted {} states  in database", assetId, statesToDelete.size());
        } catch (Exception e) {
            logger.error("assetId " + assetId + ": Error while deleting " + statesToDelete.size() + " DeviceStatus from DB.", e);
            dbConnection.rollback();
        } finally {
            dbConnection.close();
        }
        return rows;
    }


    @Async
    public CompletableFuture<Void> deleteAndInsertDeviceStatusDiffAsync(final Long assetId, final List<DeviceState> deviceStatusToSave, int batchSize) throws SQLException {
        deleteAndInsertDeviceStatusDiff(assetId, deviceStatusToSave, batchSize);
        return CompletableFuture.completedFuture((Void) null);
    }

    public void deleteAndInsertDeviceStatusDiff(final Long assetId, final List<DeviceState> deviceStatusToSave) throws SQLException {
        deleteAndInsertDeviceStatusDiff(assetId, deviceStatusToSave, BATCH_SIZE);
    }

    public void deleteAndInsertDeviceStatusDiff(final Long assetId, final List<DeviceState> deviceStatusToSave, int batchSize) throws SQLException {
        StopWatch watch = new StopWatch();
        watch.start();

        List<DeviceState> storedStates = null;
        storedStates = queryDeviceStates(List.of(assetId), null, null);
        logger.info("assetId {}: query {} states from database tooks {}", assetId, storedStates.size(), watch.pretty(watch.stop()));
        watch.start();


        List<Long> statesToDelete = new ArrayList<>();
        List<DeviceState> statesToInsert = new ArrayList<>();

        if (deviceStatusToSave.isEmpty() && !storedStates.isEmpty()) {
            int rows = deleteDeviceStatus(assetId);
            logger.info("assetId {}: delete {} old entries from db", assetId, rows);
            return;
        }

        if (deviceStatusToSave.isEmpty()) {
            logger.info("assetId {}: no old and no new states", assetId);
            return;
        }

        if (storedStates.isEmpty()) {
            statesToInsert = deviceStatusToSave;
        } else {
            findDiff(deviceStatusToSave, storedStates, statesToInsert, statesToDelete);
        }

        if (statesToDelete.isEmpty() && statesToInsert.isEmpty()) {
            logger.info("assetId {}: nothing to change in database", assetId);
            return;
        }

        if (!statesToDelete.isEmpty()) {
            deleteDeviceStatus(assetId, statesToDelete, batchSize);
        }


        Connection dbConnection = targetJdbcTemplate.getDataSource().getConnection();
        try {
            dbConnection.setAutoCommit(false);


            if (!statesToInsert.isEmpty()) {

                // this version tooks
                // assetId 7: inserted 59858 states  in database
                // assetId 7: save states to db tooks 57m 26s 760ms

                //                List<DeviceState> finalStatesToInsert = statesToInsert;
                //                targetJdbcTemplate.batchUpdate(INSERT_DEVICE_STATUS_SQL, new
                // BatchPreparedStatementSetter() {
                //                    public void setValues(PreparedStatement ps, int i) throws
                // SQLException {
                //                        DeviceState state = (DeviceState)
                // finalStatesToInsert.get(i);
                //                        ps.setLong(1, state.getAssetId());
                //                        ps.setInt(2, state.getActionActual());
                //                        ps.setLong(3, state.getActionFrom());
                //                        ps.setLong(4, state.getActionTo());
                //                        ps.setLong(5, state.getTriggerDate());
                //                        ps.setLong(6, state.getTriggerMsgNo());
                //                        ps.setString(7, state.getTriggerText());
                //                        ps.setString(8, state.getTriggerResponsibility());
                //                        ps.setLong(9, state.getTriggerCount());
                //                        ps.setInt(10, state.getAws());
                //                        ps.setInt(11, state.getBws());
                //                        ps.setInt(12, state.getAvss());
                //                    }
                //
                //                    public int getBatchSize() {
                //                        return finalStatesToInsert.size();
                //                    }
                //                });
                //
                //                dbConnection.commit();
                //                }


                // this version:
                // assetId 7: inserted 59864 states  in database
                // assetId 7: save states to db tooks 2m 3s 455ms


                executeBatch(dbConnection, INSERT_DEVICE_STATUS_SQL, statesToInsert, (state, ps) -> {
                    try {
                        fillPrepareStatement(state, ps);
                    } catch (Exception ex) {
                        logger.error("assetId " + assetId + ": Error while deleting or inserting DeviceStatus to DB", ex);
                        return false;
                    }
                    return true;
                });
            }
            dbConnection.commit();
            logger.info("assetId {}: inserted {} states  in database", assetId, statesToInsert.size());
        } catch (Exception e) {
            logger.error("assetId " + assetId + ": Error while deleting or inserting DeviceStatus to DB", e);
            dbConnection.rollback();
        } finally {
            dbConnection.close();
        }
        return;
    }

    private void fillPrepareStatement(DeviceState state, PreparedStatement ps) throws SQLException {
        int index = 1;
        ps.setLong(index++, state.getAssetId());
        ps.setInt(index++, (state.getActionActual() == null) ? 0 : state.getActionActual().getValue());
        ps.setLong(index++, state.getActionFrom());
        ps.setLong(index++, state.getActionTo());
        ps.setLong(index++, state.getTriggerDate());
        ps.setLong(index++, state.getTriggerMsgNo());
        ps.setLong(index++, state.getTriggerCount());

        ps.setInt(index++, (state.getAws() == null) ? 0 : state.getAws().getValue());
        ps.setInt(index++, (state.getBws() == null) ? 0 : state.getBws().getValue());
        ps.setInt(index++, (state.getAvss() == null) ? 0 : state.getAvss().getValue());
        ps.setInt(index++, (state.getAvailableState() == null) ? 0 : state.getAvailableState().getValue());
        ps.setInt(index++, (state.getIeeeState() == null) ? 0 : state.getIeeeState().getValue());
        ps.setInt(index++, (state.getKielVzState() == null) ? 0 : state.getKielVzState().getValue());
        ps.setInt(index++, (state.getKielVuState() == null) ? 0 : state.getKielVuState().getValue());

        ps.setInt(index++, (state.getScope() == null) ? 0 : state.getScope().getValue());

        ps.setLong(index++, state.getOutageNumber());

        ps.setLong(index++, state.getOH());
        ps.setLong(index++, state.getCumOH());
        ps.setLong(index++, state.getAOH());
        ps.setLong(index++, state.getCumAOH());
        ps.setLong(index++, state.getPH());
        ps.setLong(index++, state.getCumPH());
        ps.setLong(index++, state.getOHatLastFOO());
        ps.setLong(index++, state.getHSLF());
    }


    public void deleteAndInsertDeviceStatus(final Long assetId, final List<DeviceState> deviceStatusToSave) throws SQLException {

        Connection dbConnection = targetJdbcTemplate.getDataSource().getConnection();
        try {
            dbConnection.setAutoCommit(false);

            Object[] deleteParams = {assetId};
            int[] types = {Types.BIGINT};

            int rows = targetJdbcTemplate.update(DELETE_DEVICE_STATUS_FOR_ASSET_SQL, deleteParams, types);

            if (!deviceStatusToSave.isEmpty()) {
                targetJdbcTemplate.batchUpdate(INSERT_DEVICE_STATUS_SQL, new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        DeviceState state = deviceStatusToSave.get(i);
                        fillPrepareStatement(state, ps);
                    }

                    @Override
                    public int getBatchSize() {
                        return deviceStatusToSave.size();
                    }
                });
            }
            dbConnection.commit();
            logger.info("assetId {}: Deleted {} and inserted {} states  in database", assetId, rows, deviceStatusToSave.size());
        } catch (Exception e) {
            logger.error("assetId " + assetId + ": Error while deleting or inserting DeviceStatus to DB", e);
            dbConnection.rollback();
        } finally {
            dbConnection.close();
        }
    }

    public String getQuery(final List<Long> assetIds, final Long from, final Long to) {
        String query = "SELECT * FROM " + STATEMACHINETABLE + " WHERE asset_id = ";
        boolean first = true;
        for (Long assetId : assetIds) {
            if (first)
                query += assetId;
            else
                query += " OR asset_id = " + assetId;
            first = false;
        }
        if (from != null && to != null) {
            query += " AND ((action_from BETWEEN " + from + " AND " + to
                    + " OR action_to BETWEEN " + from + " AND " + to + ") "
                    + " OR (action_from < " + from + " AND action_to > " + to + ")) ";
        } else {
            if (from != null)
                query += " AND action_to >= " + from;
            if (to != null)
                query += " AND action_from <= " + to;
        }
        query += " ORDER BY action_from"; // DESC";
        return query;
    }


    public List<DeviceState> queryDeviceStates(final List<Long> assetIds, final Long from, final Long to) throws SQLException {

        Connection dbConnection = targetJdbcTemplate.getDataSource().getConnection();
        try {
            dbConnection.setAutoCommit(true);

            //               "(asset_id, action_actual, action_from, action_to, trigger_date, trigger_msg_no,  " +
            //                     "trigger_count, scope, aws, bws, avss, availableState, ieeeState, kielVzState, kielVuState) " +

            // read all values
            List<DeviceState> storedList = this.targetJdbcTemplate.query(getQuery(assetIds, from, to),
                    (rs, rowNum) -> {
                        DeviceState state = new DeviceState();
                        int index = 1;
                        state.setId(rs.getLong(index++));
                        state.setAssetId(rs.getLong(index++));
                        state.setActionActual(EngineAction.getNameByValue(rs.getInt(index++)));
                        state.setActionFrom(rs.getLong(index++));
                        state.setActionTo(rs.getLong(index++));
                        state.setTriggerDate(rs.getLong(index++));
                        state.setTriggerMsgNo(rs.getLong(index++));
                        state.setTriggerCount(rs.getLong(index++));
                        state.setAws(DemandSelectorSwitchStates.getNameByValue(rs.getInt(index++)));
                        state.setBws(ServiceSelectorSwitchStates.getNameByValue(rs.getInt(index++)));
                        state.setAvss(AvailableStates.getNameByValue(rs.getInt(index++)));
                        state.setAvailableState(AvailableStates.getNameByValue(rs.getInt(index++)));
                        state.setIeeeState(IeeeStates.getNameByValue(rs.getInt(index++)));
                        state.setKielVzState(IeeeStates.getNameByValue(rs.getInt(index++)));
                        state.setKielVuState(IeeeStates.getNameByValue(rs.getInt(index++)));

                        state.setScope(ScopeType.getNameByValue(rs.getInt(index++)));

                        state.setOutageNumber(rs.getLong(index++));

                        //state.setDurationHrs(rs.getLong(index++));
                        state.setOH(rs.getLong(index++));
                        state.setCumOH(rs.getLong(index++));
                        state.setAOH(rs.getLong(index++));
                        state.setCumAOH(rs.getLong(index++));
                        state.setPH(rs.getLong(index++));
                        state.setCumPH(rs.getLong(index++));
                        state.setOHatLastFOO(rs.getLong(index++));
                        state.setHSLF(rs.getLong(index++));

                        return state;
                    });
            return storedList;
        } catch (Exception e) {
            logger.error("assetId " + StringUtils.join(assetIds, ",") + ": Error while reading DeviceState", e);
            return new ArrayList<>();
        } finally {
            dbConnection.close();
        }
    }

    public Map<Long, List<DeviceState>> queryDeviceStatesMap(final List<Long> assetIds, final Long from, final Long to) throws SQLException {
        HashMap<Long, List<DeviceState>> result = new HashMap<>();

        List<DeviceState> deviceStates = queryDeviceStates(assetIds, from, to);
        for (DeviceState state : deviceStates) {
            if (!result.containsKey(state.getAssetId()))
                result.put(state.getAssetId(), new ArrayList<>());
            result.get(state.getAssetId()).add(state);
        }
        return result;
    }

    public void findDiff(
            final List<DeviceState> newStates,
            final List<DeviceState> storedSates,
            final List<DeviceState> outStates2Insert,
            final List<Long> outStatesToDelete) {
        Collections.sort(storedSates);
        Collections.sort(newStates);
        int i = 0;
        for (; i < storedSates.size(); i++) {
            if (i >= newStates.size())
                break;
            DeviceState storedState = storedSates.get(i);
            DeviceState newState = newStates.get(i);
            if(!storedState.equals(newState)) {
                break;
            }
        }

        for (int s = i; s < storedSates.size(); s++) {
            DeviceState deviceState = storedSates.get(s);
            outStatesToDelete.add(deviceState.getId());
        }

        for (int s = i; s < newStates.size(); s++) {
            outStates2Insert.add(newStates.get(s));
        }


    }
}
