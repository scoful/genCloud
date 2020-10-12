# 1. 功能
1. 双击选中实现简单翻译
2. 双击选中时间戳实现转可理解格式化日期

# 2. 如何打包
1. gradle先clean，再build
2. 在项目目录下路径，genCloud\build\idea-sandbox\plugins，可以找到同名项目文件夹
3. 右键打包成zip文件
4. 导入idea，通过硬盘安装插件

# 3. 使用方法
-   Clone项目，自己打包。或者：[点击下载最新版本](https://github.com/scoful/genCloud/releases)
-   打开IDEA， File -> Setting -> Plugins -> Install plugin from disk -> 选择genCloud_v.x.zip安装并重启IDEA。
-   修改快捷方式，默认的是随便写不能触发的快捷键，File -> Setting -> Keymap -> 搜索MyTranslate - > 右键 add Mouse Shortcut. 设置成双击选择。
-   双击选中内容，即可看到效果。

# 4. 参考文献
-   https://jetbrains.org/intellij/sdk/docs/intro/welcome.html
-   https://blog.xiaohansong.com/idea-plugin-development.html
-   https://juejin.im/post/6844904127990857742
-   https://github.com/Skykai521/ECTranslation

