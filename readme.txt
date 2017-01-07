1. upload文件夹存放上传匹配的图像。
2. 数据库布料样品图片存放于src/main/resource/static中，其中Fabric-MA是混合布料图像库，Fabric-PC是存色布料图像库。
3. 代码入口文件是src/main/java/yanjun/main/App.java. 启动后浏览器输入0.0.0.0:4567进入主操作页面。
4. 项目所依赖第三方包通过maven管理，详见pom.xml文件。
5. 项目配置文件位于src/main/java/yanjun/properties/Config.java. 包括数据库配置，图库文件选择，HSV划分等。