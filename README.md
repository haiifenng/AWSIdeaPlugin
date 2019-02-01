# AWS开发者插件

作者：
**Haiifenng**,**Willard**

## 使用方法

访问[最新版本](https://github.com/haiifenng/AWSIdeaPlugin/releases/latest/)，下载AWSIdeaPlugin.zip文件，在IDEA中，打开`Preferences`，点击左侧`Plugins`，
* 点击右侧下方`Install plugin form Disk...` (2018以前版本)
* 点击右上方齿轮图标，选择菜单`Install plugin form Disk...` (2018以后版本)

然后选择刚才下载的文件，根据提示重启IDEA。

## 功能
### 创建更新AWS需要的Libraries

* 在`Project Settings`中的`Libraries`中自动创建名为`aws_lib`的库，同时搜索名称为“release”的Module，自动加载bin\jdbc以及bin\lib目录下的jar包，自动刷新AWS运行环境库文件。

* 同时，可以一键更新AWS的库和依赖文件

> 菜单入口：`Tools`-`AWS Libraries 更新`

> 菜单入口：`Tools`-`AWS 库和依赖更新`

### AWS应用快速管理

#### 应用文件夹快速创建Module

AWS中，创建好应用之后，如果需要针对应用进行编码操作，在对应的应用文件夹上面点右键，选择`Create Module 'XXX'`，菜单会根据选择的文件夹显示具体的Module名称。创建好Module之后，插件会自动将AWS运行库文件和依赖更新处理。

#### `apps`的资源快速软连接

AWS资源使用新的管理方式后，App的资源代码使用独立的Git库管理，和release分开了，插件提供了两种菜单：
* `Create Module And Link` ：将该App资源创建Module，并且使用软连接的形式部署到release资源中
* `Link App` ：仅仅使用软连接的形式部署到release资源中，方便使用该App。如果已经软连接，则会显示Already Linked。

**限制条件：**

需要存在名称为`apps`的Module，该Module是所有App的资源文件

### 根据当前Modules创建Artifacts

方便打包，App的Module的jar包会自动输出到App相应目录。

> 菜单入口：`Tools`-`AWS Artifacts 更新`

### 更新Modules的Dependencies

方便管理Module间的依赖关系。

>菜单入口：`Tools`-`AWS Dependencies 更新`

### 暂停AWS App

在`Settings`-`Other Settings`-`AWS Suspend App`中，维护一个暂停AWS App的列表。
如果本地AWS App的暂停状态和设置不一样，列表中的“应用名称”列会变为红色背景，点击apply或ok按钮会应用当前设置到本地AWS App。

**注意**

选择菜单操作之前，首先在Project中创建名称为`release`的Module


