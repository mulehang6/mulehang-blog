# Mulehang Blog JMeter 性能测试方案

## 1. 测试目标

- 验证博客系统核心接口在并发访问下的响应性能
- 识别登录、文章列表、文章搜索等高频接口的性能瓶颈
- 为课程设计报告提供可复现实验过程、测试结果截图与结论

## 2. 测试工具

- 工具名称：Apache JMeter 5.6.3
- 工具目录：`E:\tools\apache-jmeter-5.6.3`
- 测试方式：非 GUI 模式执行，生成 `.jtl` 结果文件和 HTML Dashboard 报告

## 3. 被测接口

| 业务名称 | 请求方法 | 接口路径 | 说明 |
| --- | --- | --- | --- |
| 用户登录 | `POST` | `/api/v1/auth/login` | 典型事务型接口 |
| 文章列表 | `GET` | `/api/v1/articles?pageNo=1&pageSize=10` | 高频读取接口 |
| 文章搜索 | `GET` | `/api/v1/articles/search?keyword=Spring&pageNo=1&pageSize=10` | 模拟真实检索场景 |

## 4. 测试前置条件

1. 前端是否运行对 JMeter 无要求，但后端接口必须可访问。
2. 默认后端地址应为 `http://127.0.0.1:8080`。
3. 数据库、Redis、RabbitMQ 需要处于可用状态。
4. 若本地不启用 Elasticsearch，请显式设置 `ELASTICSEARCH_ENABLED=false` 后再启动后端。
5. 需要准备一个可用测试账号，用于登录接口压测。

## 5. 测试计划文件

- JMeter 测试计划：`coursework/jmeter/mulehang-blog-performance-test-plan.jmx`

该测试计划支持通过命令行参数覆盖以下变量：

| 参数 | 默认值 | 说明 |
| --- | --- | --- |
| `host` | `127.0.0.1` | 后端主机 |
| `port` | `8080` | 后端端口 |
| `protocol` | `http` | 协议 |
| `users` | `20` | 并发用户数 |
| `rampUp` | `10` | 启动时间（秒） |
| `loops` | `5` | 循环次数 |
| `username` | `admin` | 登录用户名 |
| `password` | `mulehang-sb-666` | 登录密码 |
| `keyword` | `Spring` | 搜索关键词 |

## 6. 实际执行命令

在 PowerShell 中执行：

```powershell
$root = 'D:\JetBrains\projects\idea_projects\mulehang-blog'
$out = Join-Path $root 'coursework\output\jmeter-run'
New-Item -ItemType Directory -Force -Path $out | Out-Null

& 'E:\tools\apache-jmeter-5.6.3\bin\jmeter.bat' `
  -n `
  -t (Join-Path $root 'coursework\jmeter\mulehang-blog-performance-test-plan.jmx') `
  -l (Join-Path $out 'results.jtl') `
  -e `
  -o (Join-Path $out 'html-report') `
  -Jhost=127.0.0.1 `
  -Jport=8080 `
  -Jusers=20 `
  -JrampUp=10 `
  -Jloops=5 `
  -Jusername=admin `
  -Jpassword=mulehang-sb-666 `
  -Jkeyword=Spring
```

执行产物：

- 结果文件：`coursework/output/jmeter-run/results.jtl`
- HTML 报告：`coursework/output/jmeter-run/html-report/index.html`

## 7. 建议保留的截图

1. `View Results Tree`
2. `Summary Report`
3. `Aggregate Report`
4. HTML Dashboard 首页概览
5. 响应时间曲线
6. 吞吐量曲线

## 8. 结果记录模板

| 业务名称 | 并发数 | 循环次数 | 平均响应时间(ms) | 90%响应时间(ms) | 最大响应时间(ms) | 吞吐量(TPS) | 错误率 | 结论 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 用户登录 | 20 | 5 | 32.81 | 63 | 85 | 10.27 | 0.00% | 通过 |
| 文章列表 | 20 | 5 | 11.43 | 15 | 23 | 10.40 | 0.00% | 通过 |
| 文章搜索 | 20 | 5 | 8.67 | 10 | 107 | 10.41 | 0.00% | 通过 |

## 9. 报告撰写建议

1. 先解释为什么选择登录、文章列表、文章搜索这 3 个接口作为压力点。
2. 说明本轮使用 `20` 并发用户、`10` 秒 Ramp-Up、每用户 `5` 次循环。
3. 结合图表解释平均响应时间、`P90`、TPS、错误率，并指出登录事务高于查询事务是符合业务特征的。
4. 最后给出结论：系统在当前课程设计规模的负载下运行稳定，但仍可继续观察搜索链路的最大响应时间波动。
