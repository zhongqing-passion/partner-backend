-- auto-generated definition
create table user
(
    id            bigint auto_increment comment '主键'
        primary key,
    user_name     varchar(40)                        null comment '昵称',
    user_account  varchar(256)                       null comment '账号',
    user_password varchar(256)                       not null comment '密码',
    avatar        varchar(512)                       null comment '头像',
    gender        tinyint                            null comment '性别',
    phone         varchar(256)                       null comment '手机号',
    email         varchar(256)                       null comment '邮箱',
    status        int      default 0                 not null comment '用户状态',
    is_delete     tinyint  default 0                 not null comment '是否删除(逻辑删除)',
    created_time  datetime default CURRENT_TIMESTAMP null comment '创建时间',
    modified_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    user_role     int      default 0                 not null comment '用户角色，普通用户:0 管理员:1',
    planet_code   varchar(512)                       null comment '星球编号'
)
    comment '用户';

