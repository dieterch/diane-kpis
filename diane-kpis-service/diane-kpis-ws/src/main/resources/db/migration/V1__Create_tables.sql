CREATE TABLE state_machine (
    id BIGINT NOT NULL AUTO_INCREMENT,

    asset_id INT,
    action_actual SMALLINT,
    action_from BIGINT,
    action_to BIGINT,

    trigger_date BIGINT,
    trigger_msg_no INT,
    trigger_count SMALLINT,

    aws SMALLINT,
    bws SMALLINT,
    avss SMALLINT,

    available_state SMALLINT,
    ieee_state SMALLINT,
    kiel_vz_state SMALLINT,
    kiel_vu_state SMALLINT,

    scope SMALLINT,

    outageNumber INT,
    OH BIGINT,
    CumOH BIGINT,
    AOH BIGINT,
    CumAOH BIGINT,
    PH BIGINT,
    CumPH BIGINT,
    OHatLastFOO BIGINT,
    HSLF BIGINT,

    PRIMARY KEY (id)
);


ALTER TABLE state_machine ADD INDEX index_asset_id (asset_id DESC);
ALTER TABLE state_machine ADD INDEX index_asset_id_from_to (asset_id, action_from, action_to DESC);


CREATE TABLE overwrite_state (
    id BIGINT NOT NULL AUTO_INCREMENT,

    asset_id INT,

    action_actual SMALLINT,
    action_from BIGINT,
    action_to BIGINT,

    trigger_date BIGINT,
    trigger_msg_no INT,
    -- trigger_text VARCHAR(250),

    duration BIGINT,

    scope  SMALLINT,
    ieee_state SMALLINT,

    description VARCHAR(1000),

    PRIMARY KEY (id)
);

ALTER TABLE overwrite_state ADD INDEX index_asset_id (asset_id DESC);
ALTER TABLE overwrite_state ADD INDEX index_asset_id_from_to (asset_id, action_from DESC);


CREATE TABLE asset_information (
    id INT,
    ram_start_date VARCHAR(14),
    commission_date VARCHAR(14),

    timezone VARCHAR(100),
    av_calc_type SMALLINT,

    PRIMARY KEY (id)
);

CREATE TABLE starting_rel (
     id BIGINT NOT NULL AUTO_INCREMENT,

     asset_id INT,

     start_date BIGINT,
     scope  SMALLINT,
     valid_start INT,
     time_to_mains_parallel INT,
     trips_before_ramp_up_mains_parallel SMALLINT,
     trips_before_mains_parallel SMALLINT,

     PRIMARY KEY (id)
);

ALTER TABLE starting_rel ADD INDEX index_asset_id (asset_id DESC);
ALTER TABLE starting_rel ADD INDEX index_asset_id_from_to (asset_id, start_date DESC);
