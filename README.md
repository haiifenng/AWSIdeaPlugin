# AWS开发者插件

作者：
**Haiifenng**,**Willard**

## 使用方法

下载[AWSIdeaPlugin.zip](https://github.com/haiifenng/AWSIdeaPlugin/releases/download/v2.2.0/AWSIdeaPlugin.zip)文件，在IDEA中，打开Preferences，点击左侧Plugins，点击右侧下方"Install plugin form disk..."，然后选择刚才下载的文件，根据提示重启IDEA。

## 功能
### 自动创建或者更新AWS需要的Libraries

在Project Settings中的Libraries中自动创建名为“aws_lib”的库，同时搜索名称为“release”的Module，自动加载bin\jdbc以及bin\lib目录下的jar包，达到自动刷新AWS运行环境库文件的目的。

菜单入口：Tools-AWS Libraries 更新

### 根据当前Modules创建Artifacts

方便打包，App的Module的jar包会自动输出到App相应目录。

菜单入口：Tools-AWS Artifacts 更新

### 更新Modules的Dependencies

方便管理Module间的依赖关系。

菜单入口：Tools-AWS Dependencies 更新

### 暂停AWS App

在Settings-Other Settings-AWS Suspend App中，维护一个暂停AWS App的列表。
如果本地AWS App的暂停状态和设置不一样，列表中的“应用名称”列会变为红色背景，点击apply或ok按钮会应用当前设置到本地AWS App。

**注意**

选择菜单操作之前，首先在Project中创建名称为“release”的Module
