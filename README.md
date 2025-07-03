# KTimer Client

**[English](#english) | [中文](#chinese)**

---

## English

### Overview

KTimer Client is a Java-based client library for connecting to KTimer server, providing distributed timer and task scheduling capabilities. It supports real-time task scheduling, callback handling, and reliable message processing.

### Features

- 🕐 **Distributed Timer**: Schedule tasks with precise timing control
- 🔄 **Asynchronous Processing**: Non-blocking task scheduling and execution
- 📡 **Real-time Communication**: TCP-based persistent connection with heartbeat mechanism
- 🛡️ **Fault Tolerance**: Automatic reconnection and error handling
- 🎯 **Priority Support**: Task scheduling with different priority levels
- 📊 **Monitoring**: Built-in logging and task tracking
- 🔌 **Extensible**: Custom callback handlers for different message types

### Requirements

- Java 21 or higher
- Network connectivity to KTimer server
- Dependencies: Lombok, SLF4J, Jackson

### Quick Start

#### 1. Installation

Add the dependency to your `build.gradle`:

```gradle
dependencies {
    implementation 'space.jamestang:ktimer-client:1.0.0'
}
```

#### 2. Basic Usage

```java
import space.jamestang.ktimer.KTimerClient;
import space.jamestang.ktimer.message.enums.TimerPriority;

// Create client instance
KTimerClient client = new KTimerClient(
    "localhost",           // server host
    8080,                 // server port
    "my-client-id",       // unique client ID
    "instance-1",         // instance ID
    "my-service"          // service name
);

// Register callback handler
client.registerCallbackHandler(MyTask.class, new MyTaskCallbackHandler());

// Start client asynchronously
client.startAsync();

// Schedule a task
client.scheduleTask(
    "unique-task-id",     // task ID
    new MyTask("data"),   // payload
    5000L,               // delay in milliseconds
    TimerPriority.HIGH,  // priority
    Map.of("tag1", "value1") // tags
);
```

#### 3. Custom Callback Handler

```java
public class MyTaskCallbackHandler implements CallbackHandler<MyTask> {
    @Override
    public void handle(MyTask task) {
        // Process your task here
        System.out.println("Processing task: " + task.getData());
    }
}
```

### Configuration

#### Environment Variables

- `KTIMER_HEARTBEAT_INTERVAL`: Heartbeat interval in milliseconds (default: 5000)
- `KTIMER_ENVIRONMENT`: Environment name (default: "default")

#### Client Configuration

```java
KTimerClient client = new KTimerClient(host, port, clientId, instanceId, serviceName);
client.setVersion("1.0.0");  // Set client version
```

### API Reference

#### Core Methods

- `startAsync()`: Start client asynchronously
- `start()`: Start client with blocking mode
- `scheduleTask()`: Schedule a task for execution
- `registerCallbackHandler()`: Register message callback handler
- `awaitShutdown()`: Wait for client shutdown

#### Task Scheduling

```java
public void scheduleTask(
    String uniqueTaskID,        // Unique task identifier
    Object payload,             // Task payload data
    Long delay,                 // Delay in milliseconds
    TimerPriority priority,     // Task priority (optional)
    Map<String, String> tags    // Additional metadata (optional)
)
```

### Best Practices

1. **Connection Management**: Use `startAsync()` for non-blocking startup
2. **Error Handling**: Implement proper exception handling in callback handlers
3. **Resource Management**: Ensure proper client shutdown
4. **Task ID Management**: Use unique task IDs to avoid conflicts
5. **Monitoring**: Enable debug logging for development

### Troubleshooting

#### Common Issues

1. **"Client is not running"**: Ensure client is started before scheduling tasks
2. **Connection refused**: Check server availability and network connectivity
3. **Serialization errors**: Ensure payload objects are serializable

#### Debug Logging

Enable debug logging in `logback.xml`:

```xml
<logger name="space.jamestang.ktimer" level="DEBUG"/>
```

### Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

### License

This project is licensed under the MIT License.

---

## Chinese

### 概述

KTimer Client 是一个基于 Java 的客户端库，用于连接 KTimer 服务器，提供分布式定时器和任务调度功能。它支持实时任务调度、回调处理和可靠的消息处理。

### 功能特性

- 🕐 **分布式定时器**: 支持精确的定时任务调度
- 🔄 **异步处理**: 非阻塞式任务调度和执行
- 📡 **实时通信**: 基于 TCP 的持久连接，带有心跳机制
- 🛡️ **容错能力**: 自动重连和错误处理
- 🎯 **优先级支持**: 支持不同优先级的任务调度
- 📊 **监控功能**: 内置日志记录和任务跟踪
- 🔌 **可扩展**: 支持自定义回调处理器

### 环境要求

- Java 8 或更高版本
- 与 KTimer 服务器的网络连接
- 依赖项：Lombok、SLF4J、Jackson

### 快速开始

#### 1. 安装

在你的 `build.gradle` 中添加依赖：

```gradle
dependencies {
    implementation 'space.jamestang:ktimer-client:1.0.0'
}
```

#### 2. 基本用法

```java
import space.jamestang.ktimer.KTimerClient;
import space.jamestang.ktimer.message.enums.TimerPriority;

// 创建客户端实例
KTimerClient client = new KTimerClient(
    "localhost",           // 服务器主机
    8080,                 // 服务器端口
    "my-client-id",       // 唯一客户端ID
    "instance-1",         // 实例ID
    "my-service"          // 服务名称
);

// 注册回调处理器
client.registerCallbackHandler(MyTask.class, new MyTaskCallbackHandler());

// 异步启动客户端
client.startAsync();

// 调度任务
client.scheduleTask(
    "unique-task-id",     // 任务ID
    new MyTask("data"),   // 载荷数据
    5000L,               // 延迟时间（毫秒）
    TimerPriority.HIGH,  // 优先级
    Map.of("tag1", "value1") // 标签
);
```

#### 3. 自定义回调处理器

```java
public class MyTaskCallbackHandler implements CallbackHandler<MyTask> {
    @Override
    public void handle(MyTask task) {
        // 在这里处理你的任务
        System.out.println("处理任务: " + task.getData());
    }
}
```

### 配置

#### 环境变量

- `KTIMER_HEARTBEAT_INTERVAL`: 心跳间隔（毫秒，默认：5000）
- `KTIMER_ENVIRONMENT`: 环境名称（默认："default"）

#### 客户端配置

```java
KTimerClient client = new KTimerClient(host, port, clientId, instanceId, serviceName);
client.setVersion("1.0.0");  // 设置客户端版本
```

### API 参考

#### 核心方法

- `startAsync()`: 异步启动客户端
- `start()`: 阻塞模式启动客户端
- `scheduleTask()`: 调度任务执行
- `registerCallbackHandler()`: 注册消息回调处理器
- `awaitShutdown()`: 等待客户端关闭

#### 任务调度

```java
public void scheduleTask(
    String uniqueTaskID,        // 唯一任务标识符
    Object payload,             // 任务载荷数据
    Long delay,                 // 延迟时间（毫秒）
    TimerPriority priority,     // 任务优先级（可选）
    Map<String, String> tags    // 附加元数据（可选）
)
```

### 最佳实践

1. **连接管理**: 使用 `startAsync()` 进行非阻塞启动
2. **错误处理**: 在回调处理器中实现适当的异常处理
3. **资源管理**: 确保正确关闭客户端
4. **任务ID管理**: 使用唯一的任务ID避免冲突
5. **监控**: 在开发环境中启用调试日志

### 故障排除

#### 常见问题

1. **"Client is not running"**: 确保在调度任务前启动客户端
2. **连接被拒绝**: 检查服务器可用性和网络连接
3. **序列化错误**: 确保载荷对象可序列化

#### 调试日志

在 `logback.xml` 中启用调试日志：

```xml
<logger name="space.jamestang.ktimer" level="DEBUG"/>
```

### 贡献

1. Fork 项目仓库
2. 创建功能分支
3. 提交更改
4. 推送到分支
5. 创建 Pull Request

### 许可证

本项目采用 MIT 许可证。

---

## Examples

### Complete Example

```java
import space.jamestang.ktimer.KTimerClient;
import space.jamestang.ktimer.core.CallbackHandler;
import space.jamestang.ktimer.message.enums.TimerPriority;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KTimerExample {
    
    public static void main(String[] args) throws IOException {
        // 创建客户端 / Create client
        KTimerClient client = new KTimerClient(
            "localhost", 8080, 
            "example-client", "instance-1", "example-service"
        );
        
        // 注册回调处理器 / Register callback handlers
        client.registerCallbackHandler(String.class, new StringTaskHandler());
        client.registerCallbackHandler(CustomTask.class, new CustomTaskHandler());
        
        // 异步启动客户端 / Start client asynchronously
        client.startAsync();
        
        // 调度不同类型的任务 / Schedule different types of tasks
        client.scheduleTask("task-1", "Hello World", 1000L, TimerPriority.HIGH, null);
        client.scheduleTask("task-2", new CustomTask("data"), 5000L, TimerPriority.NORMAL, 
            Map.of("category", "business"));
        
        // 等待关闭 / Wait for shutdown
        client.awaitShutdown();
    }
    
    static class StringTaskHandler implements CallbackHandler<String> {
        @Override
        public void handle(String message) {
            log.info("Processing string task: {}", message);
        }
    }
    
    static class CustomTaskHandler implements CallbackHandler<CustomTask> {
        @Override
        public void handle(CustomTask task) {
            log.info("Processing custom task: {}", task.getData());
        }
    }
    
    static class CustomTask {
        private String data;
        
        public CustomTask(String data) {
            this.data = data;
        }
        
        public String getData() {
            return data;
        }
    }
}
```

---

**Contact**: James-Heller@Outlook.com  
**Repository**: https://github.com/James-Heller/KTimer-Client
