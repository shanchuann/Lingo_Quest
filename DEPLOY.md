# 部署到 Vercel — Lingo Quest 实操指南

> 仓库:<https://github.com/shanchuann/Lingo_Quest>
>
> 一句话结论:**Vercel 只托管 Vue 前端**。Spring Boot + MySQL + Redis 跑不了 Vercel,要放到能跑常驻进程的平台(本文用 Railway)。按下面走完,你会得到一个可用的 `https://xxx.vercel.app`。

---

## 0. 先分清哪些能放 Vercel

| 组件 | 技术栈 | 能放 Vercel 吗 | 实际放哪 |
|---|---|---|---|
| 前端 SPA | Vue 3 + Vite 6 | 可以,静态托管 | Vercel |
| 后端 API | Spring Boot 3 (JVM 常驻) | **不行** | Railway / Render / 自己的服务器 |
| MySQL 8 | 有状态数据库 | 不行 | Railway MySQL / 云数据库 |
| Redis 7 | 有状态缓存(可选) | 不行 | 可不用,见 2.6;或 Railway Redis / Upstash |
| DeepSeek | 外网 HTTP API | — | 直接调用 |

原因:Vercel 是静态资源 + Serverless 函数,没有常驻 JVM,也不提供数据库;Spring Boot 启动要 JVM、连接池、事件监听器,放进 Serverless 跑不起来。

目标架构:

~~~
浏览器
  │
  ▼
https://lingo-quest.vercel.app            ← Vercel:Vue 3 静态文件
  │   /api/** 由 vercel.json 的 rewrite 反向代理(浏览器无跨域)
  ▼
https://lingo-quest-api.up.railway.app    ← Railway:Spring Boot(Docker)
  ├── MySQL 8(Railway 插件)
  ├── Redis 7(Railway 插件)
  └── DeepSeek API(外网)

好处:前端 `src/api/http.ts` 里的 `baseURL: '/api'` 一行都不用改,后端 CORS 也不用动。
~~~

---

## 1. 仓库准备

```bash
git status                 # 本地确认没有把 .env / 密钥加进来
git push origin main       # 确保 GitHub 上是最新代码
```

- 前端必须能构建通过。本次已在本地跑过 `pnpm build`(`vue-tsc -b && vite build`),**当前 main 是通过的**。
- 仓库根目录的 `.env` 不要提交;密钥只在 Railway / Vercel 的环境变量里配。

---

## 2. 先部署后端(Railway,约 10 分钟)

Vercel 的前端需要先有一个能访问的后端地址,所以顺序是**后端 → 数据库 → 前端**。

### 2.1 创建后端服务

1. 打开 <https://railway.com>,用 GitHub 登录。
2. **New Project → Deploy from GitHub repo → 选 `Lingo_Quest`**(首次要授权 Railway 访问仓库)。
3. 进入服务 → **Settings → Source → Root Directory** 填 `backend`。
   - 仓库里已经带了 `backend/Dockerfile`(多阶段 Maven + JRE 21),Railway 会自动用它构建,不用手改构建命令。
   - 构建全程在容器里完成,**你本机不需要装 JDK 21**(本机默认 `java` 是 8 也没关系);想在本地打包才需要 JDK 21。
4. 首次构建 3~6 分钟,Deploy Logs 出现 `Started LingoQuestApplication` 即成功。

### 2.2 加数据库

在同一个 Project 里:

1. **New → Database → Add MySQL**
2. **New → Database → Add Redis**(可选。只能加 MySQL 就跳过这一步,按 **2.6** 用 `REDIS_ENABLED=false` 跑)

服务名默认是 `MySQL` 和 `Redis`(下一步的变量引用依赖这个名字)。

### 2.3 配置后端环境变量

打开后端服务 → **Variables → Raw Editor**,粘贴并改值:

| 变量 | 值 |
|---|---|
| `MYSQL_HOST` | `${{MySQL.MYSQLHOST}}` |
| `MYSQL_PORT` | `${{MySQL.MYSQLPORT}}` |
| `MYSQL_DATABASE` | `lingo_quest` |
| `MYSQL_USERNAME` | `${{MySQL.MYSQLUSER}}` |
| `MYSQL_PASSWORD` | `${{MySQL.MYSQLPASSWORD}}` |
| `REDIS_ENABLED` | `true`(没有 Redis 服务时填 `false`,见 2.6) |
| `REDIS_HOST` | `${{Redis.REDISHOST}}`(无 Redis 可删) |
| `REDIS_PORT` | `${{Redis.REDISPORT}}`(无 Redis 可删) |
| `REDIS_PASSWORD` | `${{Redis.REDISPASSWORD}}`(无 Redis 可删) |
| `JWT_SECRET` | 随机长字符串(见下) |
| `DEEPSEEK_API_KEY` | `sk-...` |
| `SERVER_PORT` | `8080` |

说明:

- `${{服务名.变量名}}` 是 Railway 的服务间引用语法;如果你把 MySQL 服务改名成 `mysql`,前缀也要跟着改。
- 生成 JWT_SECRET:`node -e "console.log(require('crypto').randomBytes(48).toString('base64'))"`(不要用示例里的默认值)。
- 变量名必须是 `MYSQL_*` / `REDIS_*`,因为 `application.yml` 读的是这一套;**不要**改成 `SPRING_DATASOURCE_*`。
- 端口:容器已 `EXPOSE 8080`,应用默认监听 8080;如果 Railway 提示端口探测失败,把 `SERVER_PORT` 设成 `${{PORT}}` 即可(代码已兼容平台注入的 `PORT`)。

### 2.4 生成公网域名

后端服务 → **Settings → Networking → Public Networking → Generate Domain**,得到形如:

~~~
https://lingo-quest-api-production-xxxx.up.railway.app
~~~

验证:

```bash
curl https://<你的域名>/api/v3/api-docs     # 返回一大段 OpenAPI JSON 即通
# Swagger UI: https://<你的域名>/api/swagger-ui.html
```

> 注意后端 `server.servlet.context-path` 是 `/api`,所以**健康路径也带 /api 前缀**。

### 2.5 初始化数据库(结构 + 种子数据)

`database/init/` 下有 9 个 SQL,**必须按编号 01→09 顺序执行**(30 个格子、30 道题、成就、商城、词书都靠它们)。

先选**怎么从本机连到数据库**:

**方式 1(推荐,不用把数据库暴露到公网):Railway CLI 隧道**

你截图里的 "Private by default" 意思是数据库默认只有项目内服务能连;本机要连,用隧道最省事,也不产生公网流量费。

~~~bash
npm i -g @railway/cli        # 需要 Node,你机器上有 Node 22
railway login                # 浏览器授权
railway link                 # 选 project(LingoQuest)→ environment → 任意 service
railway connect MySQL        # 直接打开本机 mysql 客户端,连的就是 Railway 上的库
~~~

连上后在 mysql 里建库并逐个导入(Windows 路径用 `/`,不要用反斜杠):

~~~sql
CREATE DATABASE IF NOT EXISTS lingo_quest CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE lingo_quest;
source C:/Users/15375/Desktop/Documents/ruanjianjiegou/qimo/LingoQuest/database/init/01_schema.sql
source C:/Users/15375/Desktop/Documents/ruanjianjiegou/qimo/LingoQuest/database/init/02_seed_tiles.sql
-- …… 03 到 09 同样写法,按编号顺序
~~~

想让 DBeaver/Navicat 连:改用 `railway connect MySQL --tunnel-only`,它会打印 `127.0.0.1:<端口>`,GUI 连这个地址即可(终端别关)。

**方式 2:开公网入口(不想装 CLI 时)**

MySQL 服务 → Connect → Public Access → Deploy 激活 → 复制 `MYSQL_PUBLIC_URL`;GUI 里打开 SSL(require/trust 即可),库名 `railway` 或新建 `lingo_quest`。导完建议关掉公网入口:按 egress 计费,也少一个暴露面。后端服务走 `${{MySQL.MYSQLHOST}}` 内网,不依赖公网。

下面的导入命令两种方式都能用:走公网时把 `127.0.0.1` 和端口换成 `MYSQL_PUBLIC_URL` 里的 host/port。

**导入方式 A:图形化(最省事)**

用 Navicat / DBeaver / MySQL Workbench 连上去 → 新建数据库 `lingo_quest`(字符集 `utf8mb4`、排序 `utf8mb4_unicode_ci`)→ 依次运行 `01` 到 `09` 的 SQL 文件。

**导入方式 B:命令行**

bash / Git Bash(密码用 `MYSQL_PWD` 传,避免每个文件都提示输入):

```bash
export H=containers-us-west-xxx.railway.app
export P=12345
export MYSQL_PWD='从 Connect 面板复制的 root 密码'
mysql -h "$H" -P "$P" -u root -e "CREATE DATABASE IF NOT EXISTS lingo_quest CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci"
for f in database/init/*.sql; do echo "== $f"; mysql -h "$H" -P "$P" -u root lingo_quest < "$f"; done
```

PowerShell(Windows,没有 bash 时;用 `source` 命令而不是 `<` 重定向,避开 cmd 引号问题):

```powershell
$h="containers-us-west-xxx.railway.app"; $p="12345"; $env:MYSQL_PWD="从 Connect 面板复制的密码"
mysql -h $h -P $p -u root -e "CREATE DATABASE IF NOT EXISTS lingo_quest CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci"
Get-ChildItem database/init/*.sql | Sort-Object Name | ForEach-Object {
  Write-Host "== $($_.Name)"
  $f = $_.FullName.Replace('\','/')
  mysql -h $h -P $p -u root lingo_quest -e "source '$f'"
}
```

导完抽查:

```bash
mysql -h "$H" -P "$P" -u root -e "SELECT COUNT(*) FROM lingo_quest.tile; SELECT COUNT(*) FROM lingo_quest.question;"
# 期望:30 / 30
```

### 2.6 只用 MySQL、不装 Redis(可选)

可以。项目已支持"无 Redis"模式:设置 `REDIS_ENABLED=false`,三处依赖 Redis 的能力会自动降级为**进程内实现**,接口和前端行为不变:

| 能力 | 有 Redis | 无 Redis(降级) |
|---|---|---|
| AI 响应缓存(24h) | Redis key | JVM 内 `ConcurrentHashMap`(带 TTL,上限 512 条) |
| AI 限流(10 次/分钟/用户) | Redis INCR + EXPIRE | JVM 内计数器 |
| 周榜(Redis ZSET) | `lingo:rank:weekly:*` | JVM 内排行榜 |

代价(单实例演示完全够用):

- 服务重启/重新部署后**周榜清空**;总榜读 MySQL 的 `user.experience`,不受影响;
- 缓存和限流只在单实例内生效(本项目就是单实例,无影响)。

操作:把 2.3 里的 `REDIS_HOST` / `REDIS_PORT` / `REDIS_PASSWORD` 三行删掉,并把 `REDIS_ENABLED` 改成 `false`,重新 Deploy。Spring 仍会打印 Spring Data Redis 的扫描日志,但没有任何代码去连 Redis,属于正常现象。

> 之后想恢复 Redis 又不想在 Railway 开:用 Upstash 免费实例,填
> `REDIS_ENABLED=true`、`REDIS_HOST=xxx.upstash.io`、`REDIS_PORT=6379`、`REDIS_PASSWORD=...`,
> 并额外加 `SPRING_DATA_REDIS_SSL_ENABLED=true`(Upstash 强制 TLS)。

本地验证无 Redis 模式:

```bash
cd backend
REDIS_ENABLED=false mvn spring-boot:run      # Windows PowerShell: $env:REDIS_ENABLED="false"; mvn spring-boot:run
curl http://localhost:8080/api/v3/api-docs    # 200 说明装配成功
```

---

## 3. 让 Vercel 指向你的后端

打开 `frontend/vercel.json`,把占位域名替换成 2.4 得到的地址:

```jsonc
{
  "rewrites": [
    // 关键:所有 /api/** 转发到后端,保留 /api 前缀
    { "source": "/api/:path*", "destination": "https://lingo-quest-api-production-xxxx.up.railway.app/api/:path*" },
    // SPA 兜底:/game、/ranking 刷新不会 404
    { "source": "/(.*)", "destination": "/index.html" }
  ]
}
```

然后提交:

```bash
git add frontend/vercel.json frontend/public frontend/package.json \
        backend/Dockerfile backend/.dockerignore backend/src \
        .env.example DEPLOY.md README.md
git commit -m "chore(deploy): Vercel config, backend Dockerfile, optional Redis fallback"
git push
```

**一定先改域名再部署**,否则线上所有接口都是 502。

> 说明:Vercel 的 `rewrites` 不支持环境变量,所以后端域名只能写在这里。若不想把域名写进仓库,可改用 Vercel Serverless Function 代理(`frontend/api/` 目录 + `BACKEND_ORIGIN` 环境变量),代价是每个请求多一次函数冷启动。

---

## 4. 部署前端到 Vercel

### 4.1 Dashboard(推荐)

1. <https://vercel.com> → **Add New… → Project → Import Git Repository → Lingo_Quest**。
2. Configure Project:
   - **Framework Preset**:`Vite`
   - **Root Directory**:**`frontend`** ← 最容易踩的坑,不设会报找不到 package.json
   - **Build Command**:`pnpm build`
   - **Output Directory**:`dist`
   - **Install Command**:`pnpm install --frozen-lockfile`
3. **Environment Variables:留空**(前端只发相对路径 `/api`,不需要 `VITE_API_BASE`)。
4. **Deploy**,1~2 分钟出 `https://<project>.vercel.app`。

`frontend/vercel.json` 里的构建参数会覆盖面板上的同名设置,两边保持一致即可。

### 4.2 CLI 方式

```bash
cd frontend
pnpm dlx vercel login      # 或:npm i -g vercel && vercel login
vercel                     # 生成预览地址
vercel --prod              # 发布到生产域名
```

CLI 从 `frontend/` 目录运行,会自动识别 Vite 与 `dist`;首次会让你选 scope、起项目名。

### 4.3 后续更新

Vercel 已和 GitHub 绑定:push 到默认分支自动生产部署,PR 自动生成 Preview 环境。改完后端只 push 后端代码,Railway 也会自动重新构建。

想用自己的域名(如 `lingo-quest.example.com`):Vercel 项目 → **Settings → Domains → Add**,按提示加一条 CNAME/A 记录即可;国内域名解析到 Vercel 可能受网络影响,演示优先用 `.vercel.app` 域名。

---

## 5. 上线验收清单

| 检查项 | 预期 |
|---|---|
| 打开首页 | 正常渲染,浏览器标签有彩色方块 favicon |
| 注册新账号 | 200,跳转登录 |
| 登录 | 拿到 JWT,进入 Dashboard |
| 刷新 `/game` | 不 404(SPA 兜底生效) |
| 掷骰子 / 答一道题 | 有反馈、经验值变化 |
| AI 导师(语法纠错) | 有返回(确认 `DEEPSEEK_API_KEY` 已配、余额充足) |
| 排行榜 / 成就 / 周报 | 有数据(种子数据已导入) |
| DevTools Network | `/api/**` 全部 200,且域名是你的 vercel.app(不是 localhost) |

---

## 6. 常见问题排查

| 现象 | 原因 | 解决 |
|---|---|---|
| 首页正常,直接访问/刷新 `/game` 404 | SPA 路由缺 fallback | 确认 `frontend/vercel.json` 存在且含 `/(.*) → /index.html`;Root Directory 必须是 `frontend` |
| 页面能开,接口全部 502 | vercel.json 里还是占位域名 / Railway 服务挂了 / 没生成域名 | 换成真实域名;看 Railway Deploy Logs |
| 接口 404(不是 502) | 代理路径少了 `/api` | destination 必须是 `https://host/api/:path*`(`/` 也要) |
| CORS 报错 | 你改成前端直连后端了,而后端 `CorsConfig` 只放行 `localhost` | 回到代理方案;或把 `https://*.vercel.app` 加进 `allowedOriginPatterns` |
| Build 失败:`vue-tsc: not found` / `ENOENT package.json` | Root Directory 没设成 `frontend` | 重新设置 Root Directory |
| Build 失败:TypeScript 报错 | 本地没过 | 本地 `pnpm build` 复现修掉再 push |
| Railway 部署成功但访问 502 | 平台端口与应用端口不一致 | `SERVER_PORT=8080`;或改成 `${{PORT}}`;确认 Generated Domain 已开 |
| 登录后立即 401、被踢回登录页 | `JWT_SECRET` 变更导致旧 token 失效 | 清掉 localStorage 重新登录;以后固定 secret |
| 后端启动报数据库连接失败 | 用了公网代理地址或密码不对 | 应用内用 Railway **私有网络**主机名(即引用变量);公网 TCP Proxy 只给本机导入数据用 |
| AI 导师 / 周榜报 500,日志里有 Redis 连接异常 | 没加 Redis 服务也没关开关 | 加 `REDIS_ENABLED=false`(见 2.6),或补一个 Redis 实例 |
| 首页字体加载慢 | Google Fonts 在国内被限速 | 换 `fonts.googleapis.cn` 或自托管字体,不影响功能 |
| 生产有新版但页面没变 | 浏览器缓存 index.html | 强制刷新(Ctrl+F5) |
| 免费额度/休眠 | Render 免费实例会休眠冷启动 | Railway 不休眠;或用定时请求保活 |

---

## 7. 备选后端方案(不想用 Railway 时)

| 方案 | 适合 | 代价 |
|---|---|---|
| Render(Docker)+ Aiven MySQL + Upstash Redis | 想全免费 | 拼 3 家服务;Web 服务 15 分钟无请求休眠,冷启动约 50s |
| 自己的云服务器(阿里云/腾讯云轻量)+ `docker compose` | 最稳、最自由、国内访问快 | 要自己配域名、HTTPS(Caddy/Nginx + Let's Encrypt),国内域名需备案 |
| Fly.io(Docker) | 想就近部署 | MySQL/Redis 需额外找托管 |

只部署前端做纯 UI 展示:把 `frontend/vercel.json` 里的 `/api` rewrite 删掉即可——页面能开、样式能看,但登录/棋盘等接口会失败,适合交截图。

---

## 8. 成本与注意事项

- **Vercel Hobby**:免费,个人项目够用(100 GB 带宽/月);商用需 Pro。
- **Railway**:无免费档,Hobby $5/月起,含 $5 用量;演示完可以暂停服务省钱。
- 只想加 MySQL(平台不支持 Redis / 不想多开服务):按 2.6 设 `REDIS_ENABLED=false`,功能不受影响,只是周榜重启会清空。
- **DeepSeek**:按 token 计费,提前充一点,别在演示时余额不足。
- 密钥只放平台环境变量;**不要把 `.env`、`DEEPSEEK_API_KEY`、JWT_SECRET 提交进 git**。
- 演示前一晚先访问一次线上站点,确认 Railway 服务、数据库、Redis 都还活着。
