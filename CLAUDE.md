# SubHub 项目理解

## 项目概述

SubHub 是一个基于 Spring Cloud Alibaba 的微服务项目，采用 Maven 多模块架构。项目名称暗示这是一个订阅管理中心（Subscription Hub）。

## 技术架构

### 核心技术栈

**基础框架**
- Java 17
- Spring Boot 3.2.0
- Spring Cloud 2023.0.0
- Spring Cloud Alibaba 2022.0.0.0

**服务治理**
- Nacos：服务注册与发现（192.168.2.214:8848）
- OpenFeign：声明式服务调用
- Spring Cloud LoadBalancer：客户端负载均衡
- OKHttp：高性能 HTTP 客户端

**数据层**
- MySQL 8.0.33：关系型数据库
- MyBatis Plus 3.5.5：ORM 框架

**开发工具**
- Lombok 1.18.30：简化 Java 代码
- Spring Boot Actuator：健康检查和监控

## 项目结构

```
subhub/                         # 父项目（聚合模块）
├── pom.xml                     # 父 POM，管理依赖版本
├── .gitignore                  # Git 忽略配置
├── CLAUDE.md                   # 项目文档
└── sub-manager/                # 子模块：订阅管理服务
    ├── pom.xml                 # 子模块 POM
    └── src/
        ├── main/
        │   ├── java/
        │   │   └── com/eva/
        │   │       ├── SubManagerApplication.java      # 启动类
        │   │       └── submanager/
        │   │           ├── client/
        │   │           │   └── ItemClient.java         # Feign 客户端
        │   │           └── controller/
        │   │               └── HealthController.java   # 健康检查端点
        │   └── resources/
        │       └── application.yaml                    # 配置文件
        └── test/                                       # 测试代码
```

## 当前模块

### sub-manager（订阅管理服务）

**服务配置**
- 服务名称：`sub-manager-service`
- 运行端口：8071
- 打包方式：WAR

**功能说明**
- 启用 Nacos 服务注册发现（@EnableDiscoveryClient）
- 启用 OpenFeign 客户端（@EnableFeignClients）
- 提供健康检查接口
- 通过 Feign 调用其他服务（ItemClient）

## 架构特点

1. **微服务架构**：采用 Spring Cloud 微服务体系，支持服务拆分和独立部署
2. **服务注册发现**：使用 Nacos 实现服务的自动注册和发现
3. **声明式调用**：通过 OpenFeign 简化服务间调用
4. **统一依赖管理**：父 POM 统一管理所有依赖版本，子模块继承使用
5. **可扩展性**：多模块结构便于添加新的微服务模块

## 开发规范

- 编码：UTF-8
- JDK 版本：17
- 包命名：com.eva.*
- 打包格式：WAR（适用于传统部署）

## 待完善功能

根据现有结构推测，项目可能需要：
1. 数据库配置和实体类定义
2. 业务逻辑层（Service）实现
3. 数据访问层（Mapper）实现
4. 完整的 RESTful API 接口
5. 配置中心（Nacos Config）集成
6. 分布式事务处理
7. 统一异常处理
8. API 网关模块
9. 其他业务微服务模块

## 环境要求

- Java 17+
- Maven 3.6+
- Nacos Server（192.168.2.214:8848）
- MySQL 8.0+

## 开发环境配置

### 已安装软件

**JDK 17**
- 版本：OpenJDK 17.0.17
- 安装路径：/usr/lib/jvm/java-17-openjdk-amd64
- 环境变量：JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64

**Maven 3.9.11**
- 版本：Apache Maven 3.9.11
- 安装路径：/opt/apache-maven-3.9.11
- 环境变量：MAVEN_HOME=/opt/apache-maven-3.9.11

**环境变量配置文件**：/etc/profile.d/java-maven.sh

### Nacos 配置

**Nacos Server 地址**
- 地址：192.168.2.214:8848
- 访问地址：http://192.168.2.214:8848/nacos

**Nacos 默认配置**
- 命名空间：public（默认命名空间）
- 用户名：nacos（默认）
- 密码：nacos（默认）
- 集群名称：DEFAULT（默认）
- 分组：DEFAULT_GROUP（默认）

**服务注册配置**

在 sub-manager/src/main/resources/application.yaml 中配置：

```yaml
spring:
  application:
    name: sub-manager-service # 服务名称
  cloud:
    nacos:
      discovery:
        server-addr: 192.168.2.214:8848 # Nacos 注册中心地址
        namespace: public                # 命名空间（可选，默认为 public）
        group: DEFAULT_GROUP             # 分组（可选，默认为 DEFAULT_GROUP）
        cluster-name: DEFAULT            # 集群名称（可选，默认为 DEFAULT）
        username: nacos                  # Nacos 用户名（如果开启了鉴权）
        password: nacos                  # Nacos 密码（如果开启了鉴权）
```

**Nacos 配置中心（可选）**

如果需要使用 Nacos 作为配置中心，需要添加依赖和配置：

```xml
<!-- Nacos 配置管理 -->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
</dependency>
```

并在 bootstrap.yaml 中配置：

```yaml
spring:
  application:
    name: sub-manager-service
  cloud:
    nacos:
      config:
        server-addr: 192.168.2.214:8848
        file-extension: yaml              # 配置文件格式
        namespace: public                 # 命名空间
        group: DEFAULT_GROUP              # 分组
        username: nacos
        password: nacos
```

**Nacos 控制台访问**
- 控制台地址：http://192.168.2.214:8848/nacos
- 登录用户名：nacos
- 登录密码：nacos

**服务列表查看**

启动服务后，可以在 Nacos 控制台查看：
1. 访问 http://192.168.2.214:8848/nacos
2. 登录（用户名：nacos，密码：nacos）
3. 点击左侧菜单"服务管理" -> "服务列表"
4. 可以看到已注册的服务 sub-manager-service

## 部署说明

### 本地开发运行

1. 确保 Nacos 服务可访问：
```bash
curl http://192.168.2.214:8848/nacos
```

2. 构建项目：
```bash
cd /root/spring/subhub
source /etc/profile.d/java-maven.sh
mvn clean install
```

3. 运行服务：
```bash
cd sub-manager
mvn spring-boot:run
```

4. 验证服务：
- 健康检查：http://localhost:8071/actuator/health
- Nacos 控制台查看服务注册状态

### 常用 Maven 命令

```bash
# 编译项目
mvn clean compile

# 打包项目（跳过测试）
mvn clean package -DskipTests

# 运行测试
mvn test

# 安装到本地仓库
mvn clean install

# 查看依赖树
mvn dependency:tree

# 运行 Spring Boot 应用
mvn spring-boot:run
```

## 问题排查

### Nacos 连接问题

如果服务无法注册到 Nacos：
1. 检查 Nacos 服务是否启动：`curl http://192.168.2.214:8848/nacos`
2. 检查网络连通性：`ping 192.168.2.214`
3. 检查防火墙设置
4. 查看应用日志，搜索 "nacos" 相关错误信息

### 常见错误

**java.net.ConnectException: Connection refused**
- 原因：无法连接到 Nacos 服务器
- 解决：检查 Nacos 地址是否正确，Nacos 服务是否启动

**com.alibaba.nacos.api.exception.NacosException: failed to req API**
- 原因：Nacos API 请求失败
- 解决：检查 Nacos 版本兼容性，确认用户名密码是否正确

## 下一步计划

1. 配置 MySQL 数据库连接
2. 创建数据库表结构
3. 实现业务功能模块
4. 添加单元测试和集成测试
5. 配置日志系统
6. 添加 API 文档（Swagger/OpenAPI）
7. 实现统一异常处理
8. 添加其他微服务模块
