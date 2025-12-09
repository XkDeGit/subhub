# Spring Cloud Gateway 接入方案

## 一、Gateway 在微服务架构中的作用

### 1.1 什么是 API 网关

API 网关是微服务架构中的统一入口，所有外部请求都先经过网关，再由网关路由到具体的微服务。

```
客户端请求
    ↓
┌─────────────────────────────────────────┐
│       Spring Cloud Gateway              │
│  (统一入口：端口 8080)                   │
│                                         │
│  功能：                                  │
│  - 路由转发                              │
│  - 负载均衡                              │
│  - 认证鉴权                              │
│  - 限流熔断                              │
│  - 日志监控                              │
└─────────────────────────────────────────┘
    ↓          ↓          ↓
┌────────┐  ┌────────┐  ┌────────┐
│  Sub   │  │  Item  │  │ Order  │
│Manager │  │Service │  │Service │
│ :8071  │  │ :8072  │  │ :8073  │
└────────┘  └────────┘  └────────┘
```

### 1.2 Gateway 核心优势

1. **统一入口**：客户端只需要知道网关地址，不需要知道各个微服务的地址
2. **安全性**：隐藏内部服务细节，统一进行认证鉴权
3. **负载均衡**：自动将请求分发到多个服务实例
4. **限流熔断**：保护后端服务不被过载
5. **统一处理**：日志、监控、跨域等横切关注点

## 二、Gateway 技术选型

### 2.1 Spring Cloud Gateway vs Zuul

| 特性 | Spring Cloud Gateway | Zuul 1.x | Zuul 2.x |
|------|---------------------|----------|----------|
| 底层技术 | WebFlux (响应式) | Servlet (阻塞式) | Netty (非阻塞) |
| 性能 | ⭐⭐⭐⭐⭐ 高 | ⭐⭐⭐ 中 | ⭐⭐⭐⭐ 高 |
| Spring 生态 | ✅ 官方推荐 | ⚠️ 维护模式 | ❌ 非官方 |
| 学习曲线 | 中等 | 简单 | 较陡 |
| 社区活跃度 | ⭐⭐⭐⭐⭐ | ⭐⭐ | ⭐⭐⭐ |

**结论：选择 Spring Cloud Gateway**
- Spring 官方推荐，与 Spring Boot 3.x 完美集成
- 基于 WebFlux 响应式编程，性能更好
- 社区活跃，文档丰富

## 三、Gateway 接入步骤详解

### 步骤 1：创建 Gateway 模块

**作用**：
- 创建独立的网关服务模块
- 与业务服务解耦，独立部署

**操作**：
```bash
subhub/
├── gateway/              # 新建网关模块
│   ├── pom.xml
│   └── src/
│       └── main/
│           ├── java/
│           │   └── com/eva/gateway/
│           │       └── GatewayApplication.java
│           └── resources/
│               └── application.yaml
├── sub-manager/          # 已有的服务
└── pom.xml              # 父 POM
```

**父 POM 修改**：
```xml
<modules>
    <module>sub-manager</module>
    <module>gateway</module>  <!-- 新增 -->
</modules>
```

### 步骤 2：添加 Gateway 依赖

**作用**：
- 引入 Spring Cloud Gateway 核心依赖
- 引入服务发现依赖，让网关能从 Nacos 发现服务

**pom.xml 配置**：
```xml
<dependencies>
    <!-- Spring Cloud Gateway 核心依赖 -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-gateway</artifactId>
    </dependency>

    <!-- Nacos 服务发现 -->
    <dependency>
        <groupId>com.alibaba.cloud</groupId>
        <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
    </dependency>

    <!-- 负载均衡器 -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-loadbalancer</artifactId>
    </dependency>
</dependencies>
```

**⚠️ 重要提醒**：
- Gateway 基于 WebFlux，**不要**引入 `spring-boot-starter-web`
- 两者冲突会导致启动失败

### 步骤 3：创建启动类

**作用**：
- 标记这是一个 Spring Boot 应用
- 启用服务发现功能

**GatewayApplication.java**：
```java
package com.eva.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient  // 启用服务发现
public class GatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
```

**Spring Boot 自动配置**：
- `@SpringBootApplication` 触发自动配置扫描
- 检测到 Gateway 依赖，自动配置：
  - `GatewayAutoConfiguration`：网关核心配置
  - `GatewayLoadBalancerClientAutoConfiguration`：负载均衡配置
  - `GatewayReactiveLoadBalancerClientAutoConfiguration`：响应式负载均衡
- 检测到 Nacos Discovery，自动配置：
  - `NacosDiscoveryAutoConfiguration`：Nacos 服务发现
  - `NacosDiscoveryClientConfiguration`：服务注册客户端

### 步骤 4：配置基础信息

**作用**：
- 配置服务名称、端口
- 配置 Nacos 连接信息
- 让网关自己也注册到 Nacos（可选）

**application.yaml**：
```yaml
server:
  port: 8080  # 网关端口，对外统一入口

spring:
  application:
    name: gateway-service  # 服务名称
  cloud:
    nacos:
      discovery:
        server-addr: 192.168.2.214:8848  # Nacos 地址
        namespace: public
        group: DEFAULT_GROUP
```

**Spring Boot 自动配置的工作**：
1. 读取配置文件
2. 创建 Nacos 注册中心客户端
3. 将 gateway-service 注册到 Nacos
4. 定期发送心跳保持注册状态
5. 从 Nacos 拉取服务列表

### 步骤 5：配置路由规则

**作用**：
- 定义请求如何转发到后端服务
- 配置路由的匹配规则

**两种配置方式**：

#### 方式一：配置文件方式（推荐学习）

```yaml
spring:
  cloud:
    gateway:
      routes:  # 路由规则列表
        # 路由1：转发到订阅管理服务
        - id: sub-manager-route          # 路由唯一标识
          uri: lb://sub-manager-service  # 目标服务（lb = LoadBalancer）
          predicates:                    # 匹配条件
            - Path=/sub/**               # 请求路径匹配
          filters:                       # 过滤器（可选）
            - StripPrefix=1              # 去掉路径前缀

        # 路由2：转发到商品服务（示例）
        - id: item-service-route
          uri: lb://item-service
          predicates:
            - Path=/item/**
          filters:
            - StripPrefix=1
```

**配置详解**：

1. **id**：路由的唯一标识符，用于区分不同路由

2. **uri**：目标服务地址
   - `lb://`：使用 LoadBalancer 负载均衡
   - `sub-manager-service`：服务名（从 Nacos 获取）
   - 也可以写固定地址：`http://localhost:8071`

3. **predicates**（断言）：匹配条件，满足才转发
   - `Path=/sub/**`：匹配路径
   - 其他断言：
     - `Method=GET,POST`：匹配 HTTP 方法
     - `Header=X-Request-Id, \d+`：匹配请求头
     - `Query=token`：匹配查询参数
     - `After=2023-01-01T00:00:00`：匹配时间

4. **filters**（过滤器）：请求处理
   - `StripPrefix=1`：去掉 1 层路径前缀
     - 客户端请求：`/sub/users/123`
     - 转发到服务：`/users/123`（去掉了 /sub）
   - 其他过滤器：
     - `AddRequestHeader=X-Request-Source, gateway`：添加请求头
     - `AddResponseHeader=X-Response-Time, ${NOW}`：添加响应头
     - `RewritePath=/sub/(?<segment>.*), /${segment}`：重写路径
     - `RequestRateLimiter`：限流

**请求转发流程**：
```
客户端请求：http://localhost:8080/sub/users/123
    ↓
Gateway 接收请求
    ↓
匹配路由规则（Path=/sub/**）
    ↓
应用过滤器（StripPrefix=1，去掉 /sub）
    ↓
从 Nacos 获取 sub-manager-service 的实例列表
    ↓
负载均衡选择一个实例（如 192.168.1.100:8071）
    ↓
转发请求：http://192.168.1.100:8071/users/123
    ↓
sub-manager 服务处理
    ↓
响应返回给客户端
```

#### 方式二：Java 代码方式（灵活）

```java
package com.eva.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            // 订阅管理服务路由
            .route("sub-manager-route", r -> r
                .path("/sub/**")
                .filters(f -> f.stripPrefix(1))
                .uri("lb://sub-manager-service")
            )
            // 商品服务路由
            .route("item-service-route", r -> r
                .path("/item/**")
                .filters(f -> f.stripPrefix(1))
                .uri("lb://item-service")
            )
            .build();
    }
}
```

### 步骤 6：自动路由配置（高级）

**作用**：
- 自动为 Nacos 中的所有服务创建路由
- 不需要手动配置每个服务的路由规则

**配置**：
```yaml
spring:
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true  # 启用自动路由
          lower-case-service-id: true  # 服务名小写
```

**自动路由规则**：
- 访问规则：`http://gateway:8080/{服务名}/{路径}`
- 示例：`http://localhost:8080/sub-manager-service/users/123`
- 自动转发到：`http://sub-manager-service/users/123`

**优缺点**：
- ✅ 优点：自动发现，无需手动配置
- ❌ 缺点：暴露服务名，不够灵活，建议学习后使用手动配置

## 四、Spring Boot 自动配置原理

### 4.1 核心自动配置类

当你添加 Gateway 依赖后，Spring Boot 会自动加载以下配置：

#### 1. GatewayAutoConfiguration
```java
@Configuration
@EnableConfigurationProperties
@ConditionalOnProperty(name = "spring.cloud.gateway.enabled", matchIfMissing = true)
public class GatewayAutoConfiguration {
    // 自动配置：
    // - RouteDefinitionLocator：路由定义加载器
    // - RouteLocator：路由定位器
    // - RoutePredicateFactory：路由断言工厂
    // - GatewayFilterFactory：网关过滤器工厂
    // - GlobalFilter：全局过滤器
}
```

**作用**：
- 创建路由处理的核心组件
- 注册所有内置的断言工厂（Path, Method, Header 等）
- 注册所有内置的过滤器工厂（StripPrefix, AddRequestHeader 等）

#### 2. GatewayLoadBalancerClientAutoConfiguration
```java
@Configuration
@ConditionalOnClass({LoadBalancerClient.class, RibbonAutoConfiguration.class})
public class GatewayLoadBalancerClientAutoConfiguration {
    // 自动配置：
    // - LoadBalancerClientFilter：负载均衡过滤器
    // - 处理 lb:// 协议的 URI
}
```

**作用**：
- 让 Gateway 能够识别 `lb://` 协议
- 集成 LoadBalancer 实现负载均衡
- 自动从 Nacos 获取服务实例列表

#### 3. GatewayDiscoveryClientAutoConfiguration
```java
@Configuration
@ConditionalOnProperty(value = "spring.cloud.gateway.discovery.locator.enabled")
public class GatewayDiscoveryClientAutoConfiguration {
    // 自动配置：
    // - DiscoveryClientRouteDefinitionLocator：基于服务发现的路由定义
    // - 自动为所有注册的服务创建路由
}
```

**作用**：
- 实现自动路由功能
- 监听 Nacos 服务变化，动态更新路由

### 4.2 自动配置生效条件

```java
@ConditionalOnClass(Gateway.class)           // classpath 中有 Gateway 类
@ConditionalOnProperty(...)                  // 配置属性满足条件
@ConditionalOnBean(...)                      // Spring 容器中有特定 Bean
@ConditionalOnMissingBean(...)              // Spring 容器中没有特定 Bean
```

### 4.3 配置属性绑定

Spring Boot 自动将配置文件映射到 Java 对象：

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: xxx
          uri: xxx
```

自动映射到：
```java
@ConfigurationProperties("spring.cloud.gateway")
public class GatewayProperties {
    private List<RouteDefinition> routes;
    // getters/setters
}
```

## 五、Gateway 工作流程

```
1. 客户端请求到达 Gateway
        ↓
2. DispatcherHandler 处理请求（WebFlux 核心）
        ↓
3. RoutePredicateHandlerMapping 匹配路由
   - 遍历所有路由定义
   - 应用 Predicate 断言
   - 找到匹配的路由
        ↓
4. FilteringWebHandler 应用过滤器链
   - Pre 过滤器（请求前）
     * StripPrefix
     * AddRequestHeader
     * LoadBalancerClient（负载均衡）
   - 转发请求到目标服务
   - Post 过滤器（响应后）
     * AddResponseHeader
     * 日志记录
        ↓
5. 返回响应给客户端
```

## 六、关键组件说明

### 6.1 Route（路由）
- Gateway 的基本构建块
- 由 ID、目标 URI、断言集合、过滤器集合组成

### 6.2 Predicate（断言）
- 用于匹配 HTTP 请求
- 如果断言为真，则路由匹配
- 支持多种内置断言：Path, Method, Header, Query 等

### 6.3 Filter（过滤器）
- 在请求转发前后修改请求和响应
- 两种类型：
  - GatewayFilter：单个路由的过滤器
  - GlobalFilter：全局过滤器，应用于所有路由

### 6.4 LoadBalancer（负载均衡）
- 从 Nacos 获取服务实例列表
- 使用轮询策略选择实例
- 支持自定义负载均衡策略

## 七、学习建议

### 第一阶段：基础路由
1. 创建 Gateway 模块
2. 配置简单的路由规则（手动配置）
3. 测试请求转发是否正常
4. 理解 Path 断言和 StripPrefix 过滤器

### 第二阶段：服务发现集成
1. 配置 Nacos 服务发现
2. 使用 `lb://` 协议实现负载均衡
3. 启动多个 sub-manager 实例测试负载均衡
4. 观察 Nacos 控制台的服务实例

### 第三阶段：过滤器应用
1. 使用内置过滤器（AddRequestHeader, RewritePath）
2. 编写自定义全局过滤器（日志、鉴权）
3. 理解过滤器链的执行顺序
4. 实现请求耗时统计

### 第四阶段：高级功能
1. 限流配置（RequestRateLimiter）
2. 熔断降级（Spring Cloud Circuit Breaker）
3. 动态路由（基于配置中心）
4. Gateway 监控和性能优化

## 八、常见问题

### Q1: Gateway 和普通 Spring Boot Web 有什么区别？
A: Gateway 基于 WebFlux（响应式），使用 Netty 作为服务器，非阻塞 I/O，性能更高。普通 Web 基于 Servlet，阻塞 I/O。

### Q2: 为什么不能同时引入 spring-boot-starter-web？
A: Gateway 使用 WebFlux（Reactor Netty），Web 使用 Servlet（Tomcat），两者冲突。

### Q3: lb:// 协议是如何工作的？
A: Gateway 的 LoadBalancerClientFilter 拦截 lb:// URI，通过 DiscoveryClient 从 Nacos 获取服务实例，使用 LoadBalancer 选择一个实例替换 URI。

### Q4: 路由匹配顺序是怎样的？
A: 按照配置文件中的顺序（或代码中的定义顺序）依次匹配，找到第一个匹配的路由就停止。

### Q5: 如何调试 Gateway 路由？
A: 开启 DEBUG 日志：
```yaml
logging:
  level:
    org.springframework.cloud.gateway: DEBUG
```

## 九、下一步实践建议

建议按以下顺序实践：

1. **先创建基础 Gateway 模块**
   - 添加依赖
   - 创建启动类
   - 配置基础信息

2. **配置一条简单路由**
   - 手动配置 sub-manager 的路由
   - 测试转发是否成功

3. **观察自动配置**
   - 启动时查看日志，看加载了哪些自动配置
   - 使用 `/actuator/gateway/routes` 端点查看路由

4. **逐步添加功能**
   - 添加过滤器
   - 配置多个路由
   - 实现自定义过滤器

## 十、参考资料

- [Spring Cloud Gateway 官方文档](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/)
- [Spring WebFlux 文档](https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html)
- [Nacos Discovery 文档](https://github.com/alibaba/spring-cloud-alibaba/wiki/Nacos-discovery)

---

准备好了吗？我们可以开始实践第一步：创建 Gateway 模块！
