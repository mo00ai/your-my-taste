create table batch_job_execution_seq
(
    ID         bigint not null,
    UNIQUE_KEY char   not null,
    constraint BATCH_JOB_EXEC_SEQ_UNIQUE_KEY_UN
        unique (UNIQUE_KEY)
);

create table batch_job_instance
(
    JOB_INSTANCE_ID bigint       not null
        primary key,
    VERSION         bigint       null,
    JOB_NAME        varchar(100) not null,
    JOB_KEY         varchar(32)  not null,
    constraint JOB_INST_UN
        unique (JOB_NAME, JOB_KEY)
);

create table batch_job_execution
(
    JOB_EXECUTION_ID bigint        not null
        primary key,
    VERSION          bigint        null,
    JOB_INSTANCE_ID  bigint        not null,
    CREATE_TIME      datetime(6)   not null,
    START_TIME       datetime(6)   null,
    END_TIME         datetime(6)   null,
    STATUS           varchar(10)   null,
    EXIT_CODE        varchar(2500) null,
    EXIT_MESSAGE     varchar(2500) null,
    LAST_UPDATED     datetime(6)   null,
    constraint JOB_INST_EXEC_FK
        foreign key (JOB_INSTANCE_ID) references batch_job_instance (JOB_INSTANCE_ID)
);

create table batch_job_execution_context
(
    JOB_EXECUTION_ID   bigint        not null
        primary key,
    SHORT_CONTEXT      varchar(2500) not null,
    SERIALIZED_CONTEXT text          null,
    constraint JOB_EXEC_CTX_FK
        foreign key (JOB_EXECUTION_ID) references batch_job_execution (JOB_EXECUTION_ID)
);

create table batch_job_execution_params
(
    JOB_EXECUTION_ID bigint        not null,
    PARAMETER_NAME   varchar(100)  not null,
    PARAMETER_TYPE   varchar(100)  not null,
    PARAMETER_VALUE  varchar(2500) null,
    IDENTIFYING      char(1)       not null,
    constraint JOB_EXEC_PARAMS_FK
        foreign key (JOB_EXECUTION_ID) references batch_job_execution (JOB_EXECUTION_ID)
);

create table batch_job_seq
(
    ID         bigint not null,
    UNIQUE_KEY char   not null,
    constraint BATCH_JOB_SEQ_UNIQUE_KEY_UN
        unique (UNIQUE_KEY)
);

create table batch_step_execution
(
    STEP_EXECUTION_ID  bigint        not null
        primary key,
    VERSION            bigint        not null,
    STEP_NAME          varchar(100)  not null,
    JOB_EXECUTION_ID   bigint        not null,
    CREATE_TIME        datetime(6)   not null,
    START_TIME         datetime(6)   null,
    END_TIME           datetime(6)   null,
    STATUS             varchar(10)   null,
    COMMIT_COUNT       bigint        null,
    READ_COUNT         bigint        null,
    FILTER_COUNT       bigint        null,
    WRITE_COUNT        bigint        null,
    READ_SKIP_COUNT    bigint        null,
    WRITE_SKIP_COUNT   bigint        null,
    PROCESS_SKIP_COUNT bigint        null,
    ROLLBACK_COUNT     bigint        null,
    EXIT_CODE          varchar(2500) null,
    EXIT_MESSAGE       varchar(2500) null,
    LAST_UPDATED       datetime(6)   null,
    constraint BATCH_STEP_EXEC_SEQ_UNIQUE_KEY_UN
        foreign key (JOB_EXECUTION_ID) references batch_job_execution (JOB_EXECUTION_ID)
);

create table batch_step_execution_context
(
    STEP_EXECUTION_ID  bigint        not null
        primary key,
    SHORT_CONTEXT      varchar(2500) not null,
    SERIALIZED_CONTEXT text          null,
    constraint STEP_EXEC_CTX_FK
        foreign key (STEP_EXECUTION_ID) references batch_step_execution (STEP_EXECUTION_ID)
);

create table batch_step_execution_seq
(
    ID         bigint    not null,
    UNIQUE_KEY char(255) not null,
    constraint UNIQUE_KEY_UN
        unique (UNIQUE_KEY)
);

create table category
(
    id            bigint auto_increment
        primary key,
    created_at    datetime(6)  null,
    updated_at    datetime(6)  null,
    display_order int          null,
    name          varchar(255) not null,
    constraint UK46ccwnsi9409t36lurvtyljak
        unique (name)
);

create table favor
(
    id   bigint auto_increment
        primary key,
    name varchar(255) not null,
    constraint UKt6r4x9u0eh1a9yg98paeungkd
        unique (name)
);

create table hashtag
(
    id   bigint auto_increment
        primary key,
    name varchar(50) not null,
    constraint UKtnicok67w95ajkoau49jeg9fm
        unique (name)
);

create table image
(
    id               bigint auto_increment
        primary key,
    created_at       datetime(6)                                 null,
    updated_at       datetime(6)                                 null,
    file_extension   varchar(50)                                 not null,
    file_size        bigint                                      not null,
    origin_file_name varchar(255)                                not null,
    type             enum ('BOARD', 'DEFAULT', 'REVIEW', 'USER') not null,
    upload_file_name varchar(255)                                not null,
    url              varchar(255)                                not null
);

create table noti_seq
(
    next_val bigint null
);

create table notification_content
(
    id              bigint auto_increment
        primary key,
    created_at      datetime(6)  null,
    updated_at      datetime(6)  null,
    content         varchar(255) not null,
    redirection_url varchar(255) null
);

create table pk_criteria
(
    id        bigint auto_increment
        primary key,
    is_active bit                                               not null,
    point     int                                               not null,
    type      enum ('EVENT', 'LIKE', 'POST', 'RESET', 'REVIEW') not null
);

create table pk_term
(
    id         bigint auto_increment
        primary key,
    end_date   datetime(6) not null,
    start_date datetime(6) not null,
    term       int         not null
);

create table qrtz_calendars
(
    SCHED_NAME    varchar(120) not null,
    CALENDAR_NAME varchar(190) not null,
    CALENDAR      blob         not null,
    primary key (SCHED_NAME, CALENDAR_NAME)
);

create table qrtz_fired_triggers
(
    SCHED_NAME        varchar(120) not null,
    ENTRY_ID          varchar(95)  not null,
    TRIGGER_NAME      varchar(190) not null,
    TRIGGER_GROUP     varchar(190) not null,
    INSTANCE_NAME     varchar(190) not null,
    FIRED_TIME        bigint       not null,
    SCHED_TIME        bigint       not null,
    PRIORITY          int          not null,
    STATE             varchar(16)  not null,
    JOB_NAME          varchar(190) null,
    JOB_GROUP         varchar(190) null,
    IS_NONCONCURRENT  varchar(1)   null,
    REQUESTS_RECOVERY varchar(1)   null,
    primary key (SCHED_NAME, ENTRY_ID)
);

create index IDX_QRTZ_FT_INST_JOB_REQ_RCVRY
    on qrtz_fired_triggers (SCHED_NAME, INSTANCE_NAME, REQUESTS_RECOVERY);

create index IDX_QRTZ_FT_JG
    on qrtz_fired_triggers (SCHED_NAME, JOB_GROUP);

create index IDX_QRTZ_FT_J_G
    on qrtz_fired_triggers (SCHED_NAME, JOB_NAME, JOB_GROUP);

create index IDX_QRTZ_FT_TG
    on qrtz_fired_triggers (SCHED_NAME, TRIGGER_GROUP);

create index IDX_QRTZ_FT_TRIG_INST_NAME
    on qrtz_fired_triggers (SCHED_NAME, INSTANCE_NAME);

create index IDX_QRTZ_FT_T_G
    on qrtz_fired_triggers (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP);

create table qrtz_job_details
(
    SCHED_NAME        varchar(120) not null,
    JOB_NAME          varchar(190) not null,
    JOB_GROUP         varchar(190) not null,
    DESCRIPTION       varchar(250) null,
    JOB_CLASS_NAME    varchar(250) not null,
    IS_DURABLE        varchar(1)   not null,
    IS_NONCONCURRENT  varchar(1)   not null,
    IS_UPDATE_DATA    varchar(1)   not null,
    REQUESTS_RECOVERY varchar(1)   not null,
    JOB_DATA          blob         null,
    primary key (SCHED_NAME, JOB_NAME, JOB_GROUP)
);

create index IDX_QRTZ_J_GRP
    on qrtz_job_details (SCHED_NAME, JOB_GROUP);

create index IDX_QRTZ_J_REQ_RECOVERY
    on qrtz_job_details (SCHED_NAME, REQUESTS_RECOVERY);

create table qrtz_locks
(
    SCHED_NAME varchar(120) not null,
    LOCK_NAME  varchar(40)  not null,
    primary key (SCHED_NAME, LOCK_NAME)
);

create table qrtz_paused_trigger_grps
(
    SCHED_NAME    varchar(120) not null,
    TRIGGER_GROUP varchar(190) not null,
    primary key (SCHED_NAME, TRIGGER_GROUP)
);

create table qrtz_scheduler_state
(
    SCHED_NAME        varchar(120) not null,
    INSTANCE_NAME     varchar(190) not null,
    LAST_CHECKIN_TIME bigint       not null,
    CHECKIN_INTERVAL  bigint       not null,
    primary key (SCHED_NAME, INSTANCE_NAME)
);

create table qrtz_triggers
(
    SCHED_NAME     varchar(120) not null,
    TRIGGER_NAME   varchar(190) not null,
    TRIGGER_GROUP  varchar(190) not null,
    JOB_NAME       varchar(190) not null,
    JOB_GROUP      varchar(190) not null,
    DESCRIPTION    varchar(250) null,
    NEXT_FIRE_TIME bigint       null,
    PREV_FIRE_TIME bigint       null,
    PRIORITY       int          null,
    TRIGGER_STATE  varchar(16)  not null,
    TRIGGER_TYPE   varchar(8)   not null,
    START_TIME     bigint       not null,
    END_TIME       bigint       null,
    CALENDAR_NAME  varchar(190) null,
    MISFIRE_INSTR  smallint     null,
    JOB_DATA       blob         null,
    primary key (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP),
    constraint qrtz_triggers_ibfk_1
        foreign key (SCHED_NAME, JOB_NAME, JOB_GROUP) references qrtz_job_details (SCHED_NAME, JOB_NAME, JOB_GROUP)
);

create table qrtz_blob_triggers
(
    SCHED_NAME    varchar(120) not null,
    TRIGGER_NAME  varchar(190) not null,
    TRIGGER_GROUP varchar(190) not null,
    BLOB_DATA     blob         null,
    primary key (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP),
    constraint qrtz_blob_triggers_ibfk_1
        foreign key (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP) references qrtz_triggers (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
);

create index SCHED_NAME
    on qrtz_blob_triggers (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP);

create table qrtz_cron_triggers
(
    SCHED_NAME      varchar(120) not null,
    TRIGGER_NAME    varchar(190) not null,
    TRIGGER_GROUP   varchar(190) not null,
    CRON_EXPRESSION varchar(120) not null,
    TIME_ZONE_ID    varchar(80)  null,
    primary key (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP),
    constraint qrtz_cron_triggers_ibfk_1
        foreign key (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP) references qrtz_triggers (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
);

create table qrtz_simple_triggers
(
    SCHED_NAME      varchar(120) not null,
    TRIGGER_NAME    varchar(190) not null,
    TRIGGER_GROUP   varchar(190) not null,
    REPEAT_COUNT    bigint       not null,
    REPEAT_INTERVAL bigint       not null,
    TIMES_TRIGGERED bigint       not null,
    primary key (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP),
    constraint qrtz_simple_triggers_ibfk_1
        foreign key (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP) references qrtz_triggers (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
);

create table qrtz_simprop_triggers
(
    SCHED_NAME    varchar(120)   not null,
    TRIGGER_NAME  varchar(190)   not null,
    TRIGGER_GROUP varchar(190)   not null,
    STR_PROP_1    varchar(512)   null,
    STR_PROP_2    varchar(512)   null,
    STR_PROP_3    varchar(512)   null,
    INT_PROP_1    int            null,
    INT_PROP_2    int            null,
    LONG_PROP_1   bigint         null,
    LONG_PROP_2   bigint         null,
    DEC_PROP_1    decimal(13, 4) null,
    DEC_PROP_2    decimal(13, 4) null,
    BOOL_PROP_1   varchar(1)     null,
    BOOL_PROP_2   varchar(1)     null,
    primary key (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP),
    constraint qrtz_simprop_triggers_ibfk_1
        foreign key (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP) references qrtz_triggers (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
);

create index IDX_QRTZ_T_C
    on qrtz_triggers (SCHED_NAME, CALENDAR_NAME);

create index IDX_QRTZ_T_G
    on qrtz_triggers (SCHED_NAME, TRIGGER_GROUP);

create index IDX_QRTZ_T_J
    on qrtz_triggers (SCHED_NAME, JOB_NAME, JOB_GROUP);

create index IDX_QRTZ_T_JG
    on qrtz_triggers (SCHED_NAME, JOB_GROUP);

create index IDX_QRTZ_T_NEXT_FIRE_TIME
    on qrtz_triggers (SCHED_NAME, NEXT_FIRE_TIME);

create index IDX_QRTZ_T_NFT_MISFIRE
    on qrtz_triggers (SCHED_NAME, MISFIRE_INSTR, NEXT_FIRE_TIME);

create index IDX_QRTZ_T_NFT_ST
    on qrtz_triggers (SCHED_NAME, TRIGGER_STATE, NEXT_FIRE_TIME);

create index IDX_QRTZ_T_NFT_ST_MISFIRE
    on qrtz_triggers (SCHED_NAME, MISFIRE_INSTR, NEXT_FIRE_TIME, TRIGGER_STATE);

create index IDX_QRTZ_T_NFT_ST_MISFIRE_GRP
    on qrtz_triggers (SCHED_NAME, MISFIRE_INSTR, NEXT_FIRE_TIME, TRIGGER_GROUP, TRIGGER_STATE);

create index IDX_QRTZ_T_N_G_STATE
    on qrtz_triggers (SCHED_NAME, TRIGGER_GROUP, TRIGGER_STATE);

create index IDX_QRTZ_T_N_STATE
    on qrtz_triggers (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP, TRIGGER_STATE);

create index IDX_QRTZ_T_STATE
    on qrtz_triggers (SCHED_NAME, TRIGGER_STATE);

create table store
(
    id           bigint auto_increment
        primary key,
    created_at   datetime(6)    null,
    deleted_at   datetime(6)    null,
    updated_at   datetime(6)    null,
    address      varchar(255)   null,
    description  varchar(255)   null,
    mapx         decimal(10, 7) not null,
    mapy         decimal(10, 7) not null,
    name         varchar(255)   not null,
    road_address varchar(255)   null,
    category_id  bigint         not null,
    constraint UKf5mdbnvt3ic06cpqk2fnotmis
        unique (name, mapx, mapy),
    constraint FKo36xk5h32w3adfalrcm6ptis
        foreign key (category_id) references category (id)
);

create table user_match_info_category_seq
(
    next_val bigint null
);

create table user_match_info_store_seq
(
    next_val bigint null
);

create table users
(
    id            bigint auto_increment
        primary key,
    created_at    datetime(6)                    null,
    deleted_at    datetime(6)                    null,
    updated_at    datetime(6)                    null,
    address       varchar(255)                   not null,
    age           int                            not null,
    email         varchar(255)                   not null,
    follower      int                            not null,
    following     int                            not null,
    gender        enum ('ANY', 'FEMALE', 'MALE') null,
    level         enum ('NORMAL', 'PK')          not null,
    nickname      varchar(255)                   not null,
    password      varchar(255)                   not null,
    point         int                            not null,
    posting_count int                            not null,
    role          enum ('ADMIN', 'USER')         not null,
    image_id      bigint                         null,
    constraint UK6dotkott2kjsp8vw4d0m25fb7
        unique (email),
    constraint UK94dj9ry3k3tmcsyg8eatp7vvn
        unique (image_id),
    constraint FKlqj25c28swu46s4jbudd7hore
        foreign key (image_id) references image (id)
);

create table board
(
    id            bigint auto_increment
        primary key,
    created_at    datetime(6)                                   null,
    deleted_at    datetime(6)                                   null,
    updated_at    datetime(6)                                   null,
    access_policy enum ('CLOSED', 'FCFS', 'OPEN', 'TIMEATTACK') not null,
    contents      varchar(255)                                  not null,
    open_limit    int                                           null,
    open_time     datetime(6)                                   null,
    title         varchar(255)                                  not null,
    type          enum ('N', 'O')                               not null,
    store_id      bigint                                        not null,
    user_id       bigint                                        not null,
    status        enum ('CLOSED', 'FCFS', 'OPEN', 'TIMEATTACK') not null,
    constraint FK5vlh90qyii65ixwsbnafd55ud
        foreign key (user_id) references users (id),
    constraint FKqrcx4shwcq3xlx22i147o9dps
        foreign key (store_id) references store (id)
);

create table board_hashtag
(
    id         bigint auto_increment
        primary key,
    board_id   bigint not null,
    hashtag_id bigint not null,
    constraint FK2f8xm9sdi3i2m5r2gbo0968t0
        foreign key (hashtag_id) references hashtag (id),
    constraint FKgnj9sg4e3ru7ta9sa7fss2nlv
        foreign key (board_id) references board (id)
);

create table board_image
(
    id       bigint auto_increment
        primary key,
    board_id bigint not null,
    image_id bigint not null,
    constraint FK27rfqpc9pf352xv9fhr4vklci
        foreign key (image_id) references image (id)
            on delete cascade,
    constraint FKp567mlnww479xgirmd98kcqnp
        foreign key (board_id) references board (id)
);

create table comment
(
    id             bigint auto_increment
        primary key,
    created_at     datetime(6)  null,
    updated_at     datetime(6)  null,
    contents       varchar(255) not null,
    deleted_at     datetime(6)  null,
    board_id       bigint       not null,
    parent_comment bigint       null,
    root_comment   bigint       null,
    user_id        bigint       null,
    constraint FK1o5ni2rc96hm68w7uiuxatn12
        foreign key (root_comment) references comment (id),
    constraint FKk5dgrgaxq2cnqqo788r2gysxo
        foreign key (parent_comment) references comment (id),
    constraint FKlij9oor1nav89jeat35s6kbp1
        foreign key (board_id) references board (id),
    constraint FKqm52p1v3o13hy268he0wcngr5
        foreign key (user_id) references users (id)
);

create table event
(
    id         bigint auto_increment
        primary key,
    contents   varchar(255) not null,
    end_date   date         not null,
    is_active  bit          not null,
    name       varchar(255) not null,
    start_date date         not null,
    user_id    bigint       not null,
    constraint FK31rxexkqqbeymnpw4d3bf9vsy
        foreign key (user_id) references users (id)
);

create table board_event
(
    id         bigint auto_increment
        primary key,
    created_at datetime(6) null,
    updated_at datetime(6) null,
    board_id   bigint      not null,
    event_id   bigint      not null,
    constraint FKa842v972cv5lf63bc7t0h6nwk
        foreign key (board_id) references board (id),
    constraint FKoix9haitanmbf65eg5fo6gkf8
        foreign key (event_id) references event (id)
);

create table follow
(
    id           bigint auto_increment
        primary key,
    follower_id  bigint not null,
    following_id bigint not null,
    constraint FK9oqsjovu9bl95dwt8ibiy2oey
        foreign key (following_id) references users (id),
    constraint FKjikg34txcxnhcky26w14fvfcc
        foreign key (follower_id) references users (id)
);

create table likes
(
    id         bigint auto_increment
        primary key,
    created_at datetime(6) null,
    board_id   bigint      not null,
    user_id    bigint      not null,
    constraint FK5cq36196j3ww17d7r95qdm4td
        foreign key (board_id) references board (id),
    constraint FKnvx9seeqqyy71bij291pwiwrg
        foreign key (user_id) references users (id)
);

create table notification_info
(
    id                      bigint                                                    not null
        primary key,
    created_at              datetime(6)                                               null,
    updated_at              datetime(6)                                               null,
    category                enum ('INDIVIDUAL', 'MARKETING', 'SUBSCRIBERS', 'SYSTEM') not null,
    is_read                 bit                                                       not null,
    notification_content_id bigint                                                    not null,
    notification_target     bigint                                                    not null,
    constraint FKfv5v471g6r1lak3gu0smnjkhi
        foreign key (notification_target) references users (id),
    constraint FKpb3knrbekad3namogsk229sod
        foreign key (notification_content_id) references notification_content (id)
);

create table notification_setting
(
    category varchar(255) not null,
    accepted bit          not null,
    user_id  bigint       not null,
    primary key (category, user_id),
    constraint FKmk226jk5f6j26wg7moshwhdx8
        foreign key (user_id) references users (id)
);

create table party
(
    id                     bigint auto_increment
        primary key,
    created_at             datetime(6)                                                   null,
    description            varchar(255)                                                  null,
    enable_random_matching bit                                                           not null,
    max_members            int                                                           not null,
    meeting_date           date                                                          null,
    now_members            int                                                           not null,
    party_status           enum ('CANCELED', 'DELETED', 'EXPIRED', 'FULL', 'RECRUITING') not null,
    title                  varchar(255)                                                  not null,
    user_id                bigint                                                        not null,
    store_id               bigint                                                        null,
    constraint FKgte0ch57rdjr9y17l6dtnp6oa
        foreign key (store_id) references store (id)
            on delete cascade,
    constraint FKovyvfds7dj7unwvquf3687j3i
        foreign key (user_id) references users (id)
            on delete cascade
);

create table chat
(
    id         bigint auto_increment
        primary key,
    created_at datetime(6)  null,
    message    varchar(255) not null,
    party_id   bigint       not null,
    user_id    bigint       not null,
    constraint FK1x766u663l7m0mxuj0o72muu
        foreign key (user_id) references users (id),
    constraint FKan05u3weewkna57iteoykmqe2
        foreign key (party_id) references party (id)
);

create table party_match_info
(
    id           bigint auto_increment
        primary key,
    created_at   datetime(6)                                               null,
    max_age      int                                                       null,
    min_age      int                                                       null,
    gender       enum ('ANY', 'FEMALE', 'MALE')                            null,
    match_status enum ('IDLE', 'MATCHING', 'WAITING_HOST', 'WAITING_USER') null,
    meeting_date date                                                      null,
    region       varchar(255)                                              null,
    party_id     bigint                                                    not null,
    store_id     bigint                                                    null,
    constraint UKh85xybwrodp21atetkfcfagrb
        unique (store_id),
    constraint UKsca0uf0gr8ue2uh12rnje65bp
        unique (party_id),
    constraint FK1imfyrpj70nlomd38o55b708d
        foreign key (store_id) references store (id),
    constraint FKglxrvco5qkyfs2nk7q1uybqh6
        foreign key (party_id) references party (id)
            on delete cascade
);

create table pk_log
(
    id         bigint auto_increment
        primary key,
    created_at datetime(6)                                       not null,
    pk_type    enum ('EVENT', 'LIKE', 'POST', 'RESET', 'REVIEW') not null,
    point      int                                               not null,
    user_id    bigint                                            not null,
    constraint FK6x6mvmyudbclsf97xpkwjq2aw
        foreign key (user_id) references users (id)
);

create table pk_term_rank
(
    id         bigint auto_increment
        primary key,
    point      int    not null,
    ranking    int    not null,
    pk_term_id bigint not null,
    user_id    bigint not null,
    constraint FKj52poekt1r3e8i8ree6pou8mj
        foreign key (pk_term_id) references pk_term (id),
    constraint FKl8nj51uepxkns6i0c0a1dskda
        foreign key (user_id) references users (id)
);

create table review
(
    id           bigint auto_increment
        primary key,
    created_at   datetime(6)  null,
    updated_at   datetime(6)  null,
    contents     varchar(255) not null,
    is_presented bit          not null,
    is_validated bit          not null,
    score        tinyint      not null,
    image_id     bigint       null,
    store_id     bigint       not null,
    user_id      bigint       not null,
    constraint UKc6cgb1awbhkdhsvgykdx8ikq3
        unique (image_id),
    constraint FK2bu91x77t5ea5nb14e39mqcqs
        foreign key (image_id) references image (id),
    constraint FK6cpw2nlklblpvc7hyt7ko6v3e
        foreign key (user_id) references users (id),
    constraint FK74d12ba8sxxu9vpnc59b43y30
        foreign key (store_id) references store (id)
);

create table store_bucket
(
    id        bigint auto_increment
        primary key,
    is_opened bit          not null,
    name      varchar(255) not null,
    user_id   bigint       not null,
    constraint FKk3yhb1qmlmbsk3wrhfunkss8g
        foreign key (user_id) references users (id)
);

create table bucket_item
(
    id        bigint auto_increment
        primary key,
    store_id  bigint not null,
    bucket_id bigint not null,
    constraint FK7v0l122k2jv00sj7yc3cve69d
        foreign key (bucket_id) references store_bucket (id),
    constraint FKpj3nsiwnbel3douqt357p5j5j
        foreign key (store_id) references store (id)
);

create table user_favor
(
    id       bigint auto_increment
        primary key,
    favor_id bigint not null,
    user_id  bigint not null,
    constraint FKfvisau9py077yj8y9faetakqy
        foreign key (favor_id) references favor (id),
    constraint FKibvfs2851d7lompxolqbj93lw
        foreign key (user_id) references users (id)
);

create table user_match_info
(
    id               bigint auto_increment
        primary key,
    created_at       datetime(6)                                               null,
    max_age          int                                                       null,
    min_age          int                                                       null,
    match_started_at datetime(6)                                               null,
    match_status     enum ('IDLE', 'MATCHING', 'WAITING_HOST', 'WAITING_USER') null,
    meeting_date     date                                                      null,
    region           varchar(255)                                              null,
    title            varchar(255)                                              null,
    user_age         int                                                       null,
    user_gender      enum ('ANY', 'FEMALE', 'MALE')                            null,
    user_id          bigint                                                    not null,
    constraint FK77rpqxprvi3kdqgprfwnpo85b
        foreign key (user_id) references users (id)
            on delete cascade
);

create table party_invitation
(
    id                 bigint auto_increment
        primary key,
    created_at         datetime(6)                                                   null,
    invitation_status  enum ('CONFIRMED', 'EXITED', 'KICKED', 'REJECTED', 'WAITING') not null,
    invitation_type    enum ('INVITATION', 'RANDOM', 'REQUEST')                      not null,
    party_id           bigint                                                        not null,
    user_id            bigint                                                        not null,
    user_match_info_id bigint                                                        null,
    constraint FKbk88cpb1xvkbnrq18dcre4j13
        foreign key (user_match_info_id) references user_match_info (id),
    constraint FKc44bpdpo4lorfob8nmd6nmj4x
        foreign key (party_id) references party (id)
            on delete cascade,
    constraint FKlxycgrsowlie5jq18c2nt5sd7
        foreign key (user_id) references users (id)
            on delete cascade
);

create table user_match_info_category
(
    id                 bigint not null
        primary key,
    category_id        bigint not null,
    user_match_info_id bigint not null,
    constraint FK7fwrcch9dpd3ts8m3us4wp7j1
        foreign key (user_match_info_id) references user_match_info (id)
            on delete cascade,
    constraint FKtclx38yl6r9qbusq5xl48220k
        foreign key (category_id) references category (id)
            on delete cascade
);

create table user_match_info_store
(
    id                 bigint not null
        primary key,
    store_id           bigint not null,
    user_match_info_id bigint not null,
    constraint FKeen3n3apjqi73ei5oh9fy6qhy
        foreign key (user_match_info_id) references user_match_info (id)
            on delete cascade,
    constraint FKlx7x0hba2kb801ijtkcv0jpw9
        foreign key (store_id) references store (id)
            on delete cascade
);

