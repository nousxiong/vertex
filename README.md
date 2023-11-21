# vertex
#### Vert.x的扩展工程，主要目标是整合Spring生态，次要目标是使用Spring风格整合、重构kfos库积累的好的经验
### 项目结构
```
├── vertex-spring-boot 核心模块
│   ├── 1、替换springboot的webserver（包括client）为vertx-web
│   │   ├── 1.1、Vertx.Verticle风格
│   │   ├── 1.2、实现在Vertx.CoroutineVerticle的CoroutineContext上执行spring-webflux构建的kotlin协程
│   │   │   ├── 1.2.1、替换kotlinx.coroutines.reactor.Monokt
│   │   │   └── 1.2.2、使用verticleScope
│   │   ├── 1.3、GracefulShutdown
├── vertex-spring-boot-starter 对应vertex-spring-boot的starter模块
├── vertex-spring-boot-starter-test 用户配合使用vertex-spring-boot的测试模块
├── vertex-actuator-spring-boot-starter 如果要分离（端口）actuator，需要使用此模块
├── vertex-spring-boot-samples http和websocket的示例
```
### 说明
https://github.com/snowdrop/vertx-spring-boot

替换springboot的webserver的实现参考了snowdrop的vertx-spring-boot，但是：

#### 1、snowdrop的vertx-spring-boot未提供Vertx.Verticle风格，这是vertx的核心特性
#### 2、snowdrop的vertx-spring-boot未提供GracefulShutdown
#### 3、snowdrop的vertx-spring-boot定位和本项目不同，其定位是vertx作为springboot的子模块，而本项目是融合（fusing）