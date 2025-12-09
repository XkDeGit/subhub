# SubHub - 订阅管理微服务平台

<div align="center">

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2023.0.0-blue.svg)](https://spring.io/projects/spring-cloud)
[![Maven](https://img.shields.io/badge/Maven-3.9.11-red.svg)](https://maven.apache.org/)

基于 Spring Cloud Alibaba 的微服务订阅管理平台

</div>

## 📖 项目介绍

SubHub 是一个采用微服务架构的订阅管理平台，基于 Spring Cloud Alibaba 技术栈构建。项目采用 Maven 多模块管理，支持服务注册发现、负载均衡、声明式服务调用等微服务核心功能。

### 核心特性

- 🚀 **微服务架构**：基于 Spring Cloud 的分布式微服务体系
- 🎯 **服务治理**：使用 Nacos 实现服务注册、发现与配置管理
- 🔄 **负载均衡**：Spring Cloud LoadBalancer 客户端负载均衡
- 📞 **服务调用**：OpenFeign 声明式 HTTP 客户端
- 💾 **数据持久化**：MySQL + MyBatis Plus 数据访问层
- 📊 **服务监控**：Spring Boot Actuator 健康检查
- 🛠️ **开发工具**：Lombok 简化代码开发

## 🏗️ 技术架构

### 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 17 | 编程语言 |
| Spring Boot | 3.2.0 | 基础框架 |
| Spring Cloud | 2023.0.0 | 微服务框架 |
| Spring Cloud Alibaba | 2022.0.0.0 | 阿里微服务组件 |
| Nacos | - | 服务注册与配置中心 |
| OpenFeign | - | 声明式服务调用 |
| MySQL | 8.0.33 | 关系型数据库 |
| MyBatis Plus | 3.5.5 | ORM 框架 |
| Lombok | 1.18.30 | 代码简化工具 |
| Maven | 3.9.11 | 项目构建工具 |

### 架构图

```
┌─────────────────────────────────────────────────────┐
│                   Nacos Server                      │
│         (服务注册中心 & 配置中心)                      │
│              192.168.2.214:8848                     │
└─────────────────────────────────────────────────────┘
                          ▲
                          │ 注册/发现/配置
          ┌───────────────┼───────────────┐
          │               │               │
    ┌─────▼─────┐   ┌─────▼─────┐   ┌─────▼─────┐
    │  Gateway  │   │   Sub-    │   │  Other    │
    │  (待开发)  │   │  Manager  │   │  Services │
    │           │   │  :8071    │   │  (待开发)  │
    └───────────┘   └───────────┘   └───────────┘
                          │
                          │ OpenFeign
                          ▼
                    ┌───────────┐
                    │   Item    │
                    │  Service  │
                    │  (待开发)  │
                    └───────────┘
```

## 📁 项目结构

```
subhub/
├── pom.xml                                 # 父项目 POM，统一管理依赖版本
├── .gitignore                              # Git 忽略文件配置
├── README.md                               # 项目说明文档（本文件）
├── CLAUDE.md                               # 详细技术文档
└── sub-manager/                            # 订阅管理服务模块
    ├── pom.xml                             # 子模块 POM
    └── src/
        ├── main/
        │   ├── java/
        │   │   └── com/eva/
        │   │       ├── SubManagerApplication.java      # 启动类
        │   │       └── submanager/
        │   │           ├── client/
        │   │           │   └── ItemClient.java         # Feign 客户端
        │   │           └── controller/
        │   │               └── HealthController.java   # 健康检查接口
        │   └── resources/
        │       └── application.yaml                    # 配置文件
        └── test/                                       # 测试代码
```

## 🚀 快速开始

### 环境要求

在开始之前，请确保已安装以下软件：

- Java 17+
- Maven 3.6+
- MySQL 8.0+
- Nacos Server 2.x

### 安装步骤

#### 1. 安装 JDK 17

```bash
# Debian/Ubuntu
sudo apt update
sudo apt install -y openjdk-17-jdk

# 验证安装
java -version
```

#### 2. 安装 Maven 3.9.11

```bash
# 下载并解压
cd /opt
wget https://dlcdn.apache.org/maven/maven-3/3.9.11/binaries/apache-maven-3.9.11-bin.tar.gz
tar -xzf apache-maven-3.9.11-bin.tar.gz
rm apache-maven-3.9.11-bin.tar.gz

# 配置环境变量
sudo tee /etc/profile.d/java-maven.sh > /dev/null <<'EOF'
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export MAVEN_HOME=/opt/apache-maven-3.9.11
export PATH=$JAVA_HOME/bin:$MAVEN_HOME/bin:$PATH
EOF

# 加载环境变量
source /etc/profile.d/java-maven.sh

# 验证安装
mvn -version
```

#### 3. 配置 Nacos

确保 Nacos Server 已启动并可访问：

```bash
# 测试 Nacos 连接
curl http://192.168.2.214:8848/nacos
```

**Nacos 默认配置：**
- 地址：`192.168.2.214:8848`
- 用户名：`nacos`
- 密码：`nacos`
- 命名空间：`public`
- 控制台：http://192.168.2.214:8848/nacos

#### 4. 克隆项目（如果从 Git 仓库）

```bash
git clone <repository-url>
cd subhub
```

#### 5. 构建项目

```bash
# 加载环境变量（如果还未加载）
source /etc/profile.d/java-maven.sh

# 编译和打包
mvn clean install
```

#### 6. 运行服务

```bash
# 方式一：使用 Maven 插件运行
cd sub-manager
mvn spring-boot:run

# 方式二：运行打包后的 WAR 文件
cd sub-manager/target
java -jar sub-manager.war
```

### 验证服务

#### 1. 检查服务健康状态

```bash
curl http://localhost:8071/actuator/health
```

预期返回：
```json
{
  "status": "UP"
}
```

#### 2. 查看 Nacos 服务注册

1. 打开浏览器访问：http://192.168.2.214:8848/nacos
2. 使用用户名 `nacos` 和密码 `nacos` 登录
3. 点击左侧菜单"服务管理" -> "服务列表"
4. 确认 `sub-manager-service` 已成功注册

## ⚙️ 配置说明

### application.yaml

主要配置项说明：

```yaml
spring:
  application:
    name: sub-manager-service              # 服务名称
  cloud:
    nacos:
      server-addr: 192.168.2.214:8848      # Nacos 地址
      discovery:
        namespace: public                   # 命名空间
        group: DEFAULT_GROUP                # 分组
        cluster-name: DEFAULT               # 集群名称

server:
  port: 8071                                # 服务端口

feign:
  okhttp:
    enabled: true                           # 启用 OKHttp
```

### Maven 配置

父 POM 统一管理所有依赖版本，子模块无需指定版本号。主要版本配置：

```xml
<properties>
    <java.version>17</java.version>
    <spring-boot.version>3.2.0</spring-boot.version>
    <spring-cloud.version>2023.0.0</spring-cloud.version>
    <spring-cloud-alibaba.version>2022.0.0.0</spring-cloud-alibaba.version>
</properties>
```

## 🔧 开发指南

### 常用 Maven 命令

```bash
# 清理编译
mvn clean compile

# 运行测试
mvn test

# 打包（跳过测试）
mvn clean package -DskipTests

# 安装到本地仓库
mvn clean install

# 查看依赖树
mvn dependency:tree

# 运行应用
mvn spring-boot:run
```

### 添加新的微服务模块

1. 在父 POM 中添加模块：

```xml
<modules>
    <module>sub-manager</module>
    <module>your-new-service</module>
</modules>
```

2. 创建新模块目录和 pom.xml
3. 继承父 POM
4. 添加必要的依赖
5. 创建 Spring Boot 启动类

### 服务间调用（OpenFeign）

示例代码：

```java
@FeignClient(name = "item-service")
public interface ItemClient {

    @GetMapping("/items/{id}")
    ItemDTO getItemById(@PathVariable("id") Long id);

    @PostMapping("/items")
    ItemDTO createItem(@RequestBody ItemDTO item);
}
```

## 📊 API 文档

### Sub-Manager 服务

#### 健康检查

```http
GET http://localhost:8071/actuator/health
```

响应：
```json
{
  "status": "UP"
}
```

#### 更多 API

*待开发...*

## 🐛 问题排查

### Nacos 连接失败

**问题现象：**
```
java.net.ConnectException: Connection refused
```

**解决方案：**
1. 检查 Nacos 服务是否启动
2. 验证网络连通性：`ping 192.168.2.214`
3. 确认配置文件中的 Nacos 地址正确
4. 检查防火墙设置

### 服务未注册到 Nacos

**排查步骤：**
1. 确认 `spring-cloud-starter-alibaba-nacos-discovery` 依赖已添加
2. 检查 `@EnableDiscoveryClient` 注解是否添加
3. 查看应用日志中的 Nacos 相关信息
4. 验证 Nacos 控制台是否可访问

### Maven 依赖下载慢

**解决方案：**

配置国内镜像源（阿里云）：

```bash
# 编辑 ~/.m2/settings.xml
<mirrors>
    <mirror>
        <id>aliyun</id>
        <mirrorOf>central</mirrorOf>
        <name>Aliyun Maven</name>
        <url>https://maven.aliyun.com/repository/public</url>
    </mirror>
</mirrors>
```

## 📝 开发规范

### 代码规范

- 编码格式：UTF-8
- 包命名：com.eva.*
- 使用 Lombok 简化代码
- 遵循阿里巴巴 Java 开发手册

### Git 提交规范

```
<type>(<scope>): <subject>

<body>

<footer>
```

**Type 类型：**
- `feat`: 新功能
- `fix`: 修复 bug
- `docs`: 文档更新
- `style`: 代码格式调整
- `refactor`: 重构
- `test`: 测试相关
- `chore`: 构建/工具链相关

**示例：**
```
feat(sub-manager): 添加订阅创建接口

- 实现订阅创建业务逻辑
- 添加参数校验
- 完成单元测试

Closes #123
```

## 🗺️ 开发路线

### 当前状态

- ✅ 项目框架搭建
- ✅ Nacos 服务注册发现
- ✅ OpenFeign 服务调用配置
- ✅ 健康检查接口

### 待开发功能

- [ ] 数据库配置和表结构设计
- [ ] 订阅管理核心业务功能
- [ ] 用户认证和授权
- [ ] API 网关模块
- [ ] 分布式事务处理
- [ ] 日志收集与监控
- [ ] API 文档（Swagger/OpenAPI）
- [ ] 单元测试和集成测试
- [ ] Docker 容器化部署
- [ ] CI/CD 持续集成

## 📚 参考文档

- [Spring Boot 官方文档](https://spring.io/projects/spring-boot)
- [Spring Cloud 官方文档](https://spring.io/projects/spring-cloud)
- [Spring Cloud Alibaba 文档](https://github.com/alibaba/spring-cloud-alibaba)
- [Nacos 官方文档](https://nacos.io/zh-cn/docs/what-is-nacos.html)
- [MyBatis Plus 文档](https://baomidou.com/)

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request！

1. Fork 本仓库
2. 创建特性分支：`git checkout -b feature/your-feature`
3. 提交更改：`git commit -am 'feat: add some feature'`
4. 推送分支：`git push origin feature/your-feature`
5. 提交 Pull Request

## 📄 许可证

[MIT License](LICENSE)

## 👥 联系方式

- 项目负责人：Your Name
- 邮箱：your.email@example.com
- 项目地址：https://github.com/your-org/subhub

---

<div align="center">

**⭐ 如果这个项目对你有帮助，请给一个 Star！⭐**

Made with ❤️ by Eva Team

</div>
