# Sirui小程序后端服务

# 项目主要依赖
- springboot
- mybatis
- mybatis-plus
- druid
- swagger
- tencentcloud-sdk-java-vod
- wechatpay-java

# 基本结构
## 数据库表结构

### admin_user表

后台管理系统用户表

使用role字段进行权限管理

| 列名        | 数据类型 | 长度 | 默认     | 主键 | 注释                                                  |
| ----------- | -------- | ---- | -------- | ---- | ----------------------------------------------------- |
| id          | varchar  | 255  |          | 1    | 管理系统用户id                                        |
| username    | varchar  | 255  |          |      | 用户名                                                |
| password    | varchar  | 255  |          |      | 密码                                                  |
| role        | varchar  | 255  | ROLE_SUB |      | 用户角色权限(ROLE_ADMIN / ROLE_SUB) (管理员 / 子用户) |
| create_date | datetime |      |          |      | 创建时间                                              |
| update_date | datetime |      |          |      | 更新时间                                              |
| del_flag    | char     | 1    |          |      | 逻辑删除标记                                          |
| remarks     | varchar  | 255  |          |      | 备注消息                                              |

### order表

订单表

| 列名         | 数据类型 | 长度 | 默认 | 主键 | 注释                                                         |
| ------------ | -------- | ---- | ---- | ---- | ------------------------------------------------------------ |
| id           | varchar  | 255  |      | 1    | 主键(订单编号)                                               |
| pre_order_id | varchar  | 255  |      |      | 父订单的订单id(延长直播时间的订单用)                         |
| openid       | varchar  | 255  |      |      | 下单用户id                                                   |
| project_id   | varchar  | 255  |      |      | 场地表主键                                                   |
| game_name    | varchar  | 255  |      |      | 赛事名称                                                     |
| team_a       | varchar  | 255  |      |      | 主队id                                                       |
| team_b       | varchar  | 255  |      |      | 客队id                                                       |
| score_a      | varchar  | 255  |      |      | 主队得分                                                     |
| score_b      | varchar  | 255  |      |      | 客队得分                                                     |
| start_time   | varchar  | 255  |      |      | 直播开始时间(时间戳(精确到秒)                                |
| stop_time    | varchar  | 255  |      |      | 直播结束时间(时间戳(精确到秒)                                |
| total        | varchar  | 255  |      |      | 支付金额                                                     |
| pay_flag     | char     | 1    |      |      | 支付状态 (1-支付成功, 2-未支付, 3-支付失败, 4-支付成功后中途停止直播) |

### order_replay表

订单视频存放表

| 列名     | 数据类型 | 长度 | 默认 | 主键 | 备注                                                         |
| -------- | -------- | ---- | ---- | ---- | ------------------------------------------------------------ |
| id       | varchar  | 255  |      | 1    | 主键                                                         |
| order_id | varchar  | 255  |      |      | 订单id                                                       |
| file_id  | varchar  | 255  |      |      | 腾讯云vod存储唯一标识                                        |
| title    | varchar  | 255  |      |      | 视频标题                                                     |
| replay   | varchar  | 255  |      |      | 播放地址                                                     |
| type     | char     | 1    |      |      | 0-全程视频，1-主队单点剪辑，2-客队单点剪辑，3-主队精彩集锦，4-客队精彩集锦，5-主队进球片段，6-客队进球片段，7-进球集锦，8-全场集锦 |

### order_team表

队伍表

由于甲方需要作队伍的检索查询，分出一个表

| 列名 | 数据类型 | 长度 | 默认 | 主键 | 备注     |
| ---- | -------- | ---- | ---- | ---- | -------- |
| id   | varchar  | 255  |      | 1    | 队伍id   |
| name | varchar  | 255  |      |      | 队伍名称 |

### project表

场地表

| 列名         | 数据类型 | 长度 | 默认 | 主键 | 备注                                   |
| ------------ | -------- | ---- | ---- | ---- | -------------------------------------- |
| id           | varchar  | 255  |      | 1    | 主键                                   |
| project_id   | varchar  | 255  |      |      | 甲方那边给的场地id                     |
| name         | varchar  | 255  |      |      | 场地名称                               |
| price        | varchar  | 255  |      |      | 场地收费标准(/0.5h)                    |
| overlay_flag | char     | 1    |      |      | 场地记分牌类型(0-默认, 1-足球, 2-篮球) |

### project_share表

场地共享表

后台管理系统中可以将自己的场地共享给其他子用户

| 列名       | 数据类型 | 长度 | 主键 | 备注               |
| ---------- | -------- | ---- | ---- | ------------------ |
| id         | varchar  | 255  | 1    | 主键               |
| project_id | varchar  | 255  |      | 场地id             |
| user_id    | varchar  | 255  |      | 后台管理系统用户id |

#### replay_collection表

视频收藏表

| 列名      | 数据类型 | 长度 | 主键 | 注释           |
| --------- | -------- | ---- | ---- | -------------- |
| id        | varchar  | 255  | 1    | 主键           |
| openid    | varchar  | 255  |      | 微信用户id     |
| replay_id | varchar  | 255  |      | 视频回放表主键 |

### user表

微信用户表

| 列名        | 数据类型 | 长度 | 主键 | 注释                                               |
| ----------- | -------- | ---- | ---- | -------------------------------------------------- |
| id          | varchar  | 255  | 1    | 微信提供的openid                                   |
| unionid     | varchar  | 255  |      | 开放平台用户唯一标识(好像只有多个小程序时才用得到) |
| session_key | varchar  | 255  |      | 会话密钥                                           |
| username    | varchar  | 255  |      | 用户昵称                                           |
| avatar      | varchar  | 255  |      | 用户头像url                                        |

### user_collection表

用户收藏订单表

| 列名    | 数据类型 | 长度 | 主键 | 注释           |
| ------- | -------- | ---- | ---- | -------------- |
| id      | varchar  | 255  | 1    | 主键           |
| openid  | varchar  | 255  |      | 微信用户openid |
| orderid | varchar  | 255  |      | 订单id         |





## 项目结构

```
src/main/java/com/west2/
├── common                  // 统一返回参数表
├── config                  // 配置文件
├── controller              // 对外开放接口
├── entity                  // 实体类
│   ├── base                // 基础封装实体类
│   └── service             // 返回展示封装类
├── logs                    // 放置打印日志模块
├── target                  // 放置打包后源码模块
├── service                 // 业务服务
├── mapper                  // 持久层
└── utils                   // 工具类
└── SiruiApplication.java   // 启动类
```
### common

#### BaseResult

返回结果基础类

封装了返回结果的基本信息和初始化方法

#### CommonResult

返回结果常用类

封装了具体返回的数据结构

使用方法:

~~~java
// 初始化
CommonResult result = new CommonResult().init();

// 请求成功(携带数据)
result.success("key", value);
// 请求失败 
result.fail(MsgCodeUtil.MSG_CODE_UNKNOWN);
result.failCustom(MsgCodeUtil.MSG_CODE_UNKNOWN, "错误信息");
// 在返回结果中插入其他字段数据
result.put("key", value);

// 返回结果
return (CommonResult) result.end();
~~~

#### MsgCodeUtil

返回值状态码

封装了常用的返回值状态码，以及对应的错误信息

### component

组件，目前只有延时队列

#### DelayQueueManager

延时任务管理类

#### TaskBase

延时任务队列中基本元素

#### DelayTask

延时任务队列中元素

#### LiveLeapTask

直播即时剪辑延时任务

(业务需要在调用接口时，截取前20s后10s的视频片段)

(腾讯云接口建议截取片段时 当前时间大于目标片段结束时间 90s以上，否则腾讯云接口时移服务404(所以采用了延时队列))

### config

#### CorsConfig

跨域配置

#### DruidConfig

Druid数据池配置

#### KuaiShouClientConfig

快手客户端配置并注入

#### VodClientConfig

腾讯云sdk，云点播服务配置并注入(好像因为注入顺序有些问题暂时没法用,可以先直接构造)

#### MybatisPlusConfig

配置Mybatis-Plus

#### RedisConfig

配置Redis并注入

#### RuisConfig

甲方接口、直播流、微信等自定义的配置

从application.yml中读取并注入

#### RunInitConfig

应用启动监听，启动时进行微信access-token的获取(目前没有业务用到)

#### Swagger2Config

swagger2的文档配置

#### TokenInterceptor

后台管理系统获取并拦截请求，根据请求头中token进行校验

#### WebConfiguration

后台管理系统权限拦截

#### WxMappingJackson2HttpMessageConverter

http请求类型转换

### controller

#### AdminController

后台管理系统相关接口

![image-20230223135943984](https://gitee.com/sky-dog/note/raw/master/img/202302231359142.png)

#### CommonController

其他数据接口

主要是一些回调，和一些测试用的

![image-20230223140056905](https://gitee.com/sky-dog/note/raw/master/img/202302231400952.png)

#### KuaishouController

快手相关接口

目前只做了回调校验

![image-20230223140139264](https://gitee.com/sky-dog/note/raw/master/img/202302231401303.png)

#### OrderController

主要业务

订单相关的接口

![image-20230223140236548](https://gitee.com/sky-dog/note/raw/master/img/202302231402633.png)

#### ProjectController

场地相关接口

![image-20230223140310974](https://gitee.com/sky-dog/note/raw/master/img/202302231403014.png)

#### UserController

微信小程序用户侧接口

![image-20230223140342052](https://gitee.com/sky-dog/note/raw/master/img/202302231403104.png)

### entity

#### base

封装了共有数据的数据库实体类

其中

* isNewRecord
    * true - create操作
    * false - update操作
* id
    * 数据库主键
* delFlag
    * 逻辑删除
* createDate
    * 数据创建时间
* updateDate
    * 数据更新时间
* remarks
    * 备注信息

#### vo

封装了一些与前端交互的实体

#### 其他

数据库表对应实体类

### mapper

持久层

与数据库表一一对应



### service

业务层

#### CrudService

封装了一些数据库操作

* save(T entity); 	// 根据isNewRecord字段来判断是create/update
* delete(T entity);   // 按主键逻辑删除数据库中对应记录
* findList(QueryWrapper<T> wrapper);    // 查询多条未被删除的数据
* findAllList(QueryWrapper<T> wrapper);    // 查询所有数据(包括被逻辑删除的)
* get(String id);    // 按主键查询一条数据



#### VodService

腾讯云点播相关的api服务

#### WxService

微信相关的api服务

### utils

一些简单工具类

#### BeanCustomerUtil

对象深拷贝工具

#### DateTimeUtil

时间、日期、时间戳转换工具

#### Digest/EncodeUtil/Exceptions

加解密相关工具

#### HttpRequestUtil

Http请求工具

#### JwtUtil

jwtToken生成、校验工具

#### RedisListenerUtil

redis监听工具类

access-token过期后自动获取

#### RedisUtil

redis工具类

#### SignUtil

微信签名工具类

#### StreamUtil

腾讯云推流地址生成工具类

#### TokenUtil

token工具类














# 项目文件配置

src目录下的`main/resource/application.yml`文件


```yaml
# 服务器端口配置
server:
  port: 12345
# 访问api前缀配置
apiPath: api
spring:
  # mysql配置
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    host: mysql
    url: jdbc:mysql://${spring.datasource.host}:3306/sirui?serverTimezone=GMT%2b8
    username: root
    password: 123456
  # redis配置
  redis:
    host: redis
    port: 6379
  mvc:
    pathmatch:
      matching-strategy: ant_path_matcher # 解决springboot和swagger2版本冲突
# mybatis配置
mybatis:
  mapper-locations: classpath:mapper/*.xml
  configuration:
    map-underscore-to-camel-case: true
# mybatis-plus配置
mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
    map-underscore-to-camel-case: true
  global-config:
    db-config:
      logic-delete-value: 1
      logic-not-delete-value: 0

# 小程序信息的配置
ruis:
  wx:
    # 小程序id和密钥
    app:
      id: wx129a87c8f2c167ee
      secret: cbba2246c7feb07455f9b0263bd1e088
    # 微信支付配置
    pay:
      mchid: 1625199959
      serialNumber: 15DD9513B269D7F57B2711DCB162C99543EA543D
      APIv3: Jay182124Jay182124Jay182124Jay18
      privateKeyPath: /cert/apiclient_key.pem
      wechatPayCertificatePath: /cert/wechatpay_59094A954A19F0AE16AD3CBBA061340F1D4D5B93.pem
  # 腾讯推拉流配置
  stream:
    key: dbe98d38317e2dbb9bdeedf3337da6da
    protocol: webrtc://
    pushURL: push.ruisport.cn/live/
    playURL: live.ruisport.cn/live/
    suffix:
  # 思锐接口配置
  api:
    url: https://id.ruisport.cn/customer/api/aaa/plan.php
    status: https://id.ruisport.cn/customer/api/aaa/status.php
    username: sirui@ruisport.cn
    pwd: e10adc3949ba59abbe56e057f20f883e
    overlayURL: https://service.ruisport.cn/h5/control.html
  # 腾讯云密钥配置
  tencentCloud:
    SecretId: AKIDPksrfHi075oYAh7nKiyETXQ8A5rKgw1C
    SecretKey: 760mT2Siv77mNrCMJ3aw6KOJ8lC2gARb
```



# 项目启动

项目路径在/home/ruis/wechatApp下

Nginx服务在默认安装路径下

在项目根目录的控制台中打包源代码
```shell
mvn clean package
```
将target目录下打包好的jar包、src/main/resource下的cert文件夹和根目录中的Docker、docker-compose.yaml文件按下列方式放置

```
AnyName/
├── java                    // java相关配置
│   ├── cert                // 微信支付证书
│   └── Dockerfile          // Docker构建镜像文件
│   └── *.jar               // 打包的java源代码
└── docker-compose.yaml     // docker-compose配置
```
然后在这个目录下输入(需要有docker/docker-compose环境)

```shell
docker-compose up --build
```

# 部署环境
```shell
[root@VM-0-11-centos ~]# docker version
Client: Docker Engine - Community
 Version:           20.10.21
 API version:       1.41
 Go version:        go1.18.7
 Git commit:        baeda1f
 Built:             Tue Oct 25 18:04:24 2022
 OS/Arch:           linux/amd64
 Context:           default
 Experimental:      true

Server: Docker Engine - Community
 Engine:
  Version:          20.10.21
  API version:      1.41 (minimum version 1.12)
  Go version:       go1.18.7
  Git commit:       3056208
  Built:            Tue Oct 25 18:02:38 2022
  OS/Arch:          linux/amd64
  Experimental:     false
 containerd:
  Version:          1.6.12
  GitCommit:        a05d175400b1145e5e6a735a6710579d181e7fb0
 runc:
  Version:          1.1.4
  GitCommit:        v1.1.4-0-g5fd4c4d
 docker-init:
  Version:          0.19.0
  GitCommit:        de40ad0
[root@VM-0-11-centos ~]# docker-compose version
docker-compose version 1.25.5, build 8a1c60f6
docker-py version: 4.1.0
CPython version: 3.7.5
OpenSSL version: OpenSSL 1.1.0l  10 Sep 2019

```