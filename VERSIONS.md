# 版本切换说明（26.2 / 26.3）

当前项目已切换到 **MC 26.2**（为发布准备）。源码（src/**/*.java）在两个版本间通用，**未做任何删除**。
以下记录两套构建配置的差异，便于回切 26.3 或做双版本。

## 当前状态：26.2
- `gradle.properties`
  - minecraft_version = **26.2**
  - fabric_version = **0.161.0+26.2**
  - modmenu_version = **20.0.3**
  - flashback_modrinth_version = **Um7qd3Na**（0.43.6 for 26.2，现改为本地文件依赖）
- `build.gradle`
  - Flashback：`compileOnly files("libs/Flashback-0.43.6-for-MC26.2.jar")`（本地 jar，绕开慢速 maven）
- `fabric.mod.json`：`"minecraft": "~26.2"`

## 回切到 26.3（改回以下值即可）
- `gradle.properties`
  - minecraft_version = **26.3**
  - fabric_version = **0.161.0+26.3**
  - modmenu_version = **21.0.0**
  - flashback_modrinth_version = **cV1pa0YV**（0.43.6 for 26.3）
- `build.gradle`
  - Flashback：`compileOnly "maven.modrinth:4das1Fjq:cV1pa0YV"`
- `fabric.mod.json`：`"minecraft": "~26.3"`

> 注：26.3 的 Flashback jar 仍缓存在本机 Gradle 依赖缓存中
> （`~/.gradle/caches/modules-2/files-2.1/maven.modrinth/4das1Fjq/cV1pa0YV/`），
> 回切 26.3 时无需重新下载。

## 26.2 构建前置
需要手动下载 Flashback 26.2 jar（192MB）放到项目根 `libs/` 目录下：
`libs/Flashback-0.43.6-for-MC26.2.jar`

直链：
`https://cdn.modrinth.com/data/4das1Fjq/versions/Um7qd3Na/Flashback-0.43.6-for-MC26.2.jar`
