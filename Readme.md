# 1. 功能
1. 选中内容后按快捷键，实现简单英汉互译
2. 选中时间戳后按快捷键，实现时间戳转换成可理解日期格式
3. 选中2行mybatis的sql日志后按快捷键，实现日志自动组装成可执行SQL语句
4. 选中cron表达式后按快捷键，实现显示下5次运行时间

# 2. 使用方法
1. Clone项目，自己打包。或者：[点击下载最新版本](https://github.com/scoful/genCloud/releases)
2. 打开IDEA， File -> Setting -> Plugins -> Install plugin from disk -> 选择genCloud_v.x.zip安装并重启IDEA。
3. 修改快捷键，默认的快捷键是：Ctrl+Shift+T，File -> Setting -> Keymap -> 搜索MyTranslate - > 右键 add Mouse Shortcut. 设置成双击选择。
4. 双击选中内容，即可看到效果。
5. 还可以先鼠标选中内容，然后点击菜单：Edit-MyTranslate，即可看到效果。

# 3. 如何在IDEA运行源码
1. Clone项目代码到本地，通过IDEA打开
2. 右侧Gradle侧边栏-genCloud-Tasks-intellij-runIde

# 4. 如何打包
1. 修改build.gradle和plugin.xml修改版本信息或change-notes
2. gradle先clean，再build
3. 在项目目录下路径，genCloud\build\idea-sandbox\plugins，可以找到同名项目文件夹
4. 右键打包成zip文件，文件名后加版本号，例：genCloud_v1.2.3.zip
5. 导入idea，通过硬盘安装插件

# 5. 参考文献
-   https://jetbrains.org/intellij/sdk/docs/intro/welcome.html
-   https://blog.xiaohansong.com/idea-plugin-development.html
-   https://juejin.im/post/6844904127990857742
-   https://github.com/Skykai521/ECTranslation

