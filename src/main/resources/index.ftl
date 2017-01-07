<#include "header.ftl">
<div class="banner">
	<div class="container">
		<div class="span_1_of_1">
			<h2>布料图像垂直搜索系统</h2>
			<div class="search">
				<form action="/upload" method="post" enctype=multipart/form-data>
					<label for="search_text" class="upload_text">选择布料图片</label>
					<input type="file" name="file" id="search_text" class="upload_button" >
					<input type="submit" id="search_button" value="开始匹配">
					<p>
					<label for="change_param" class="upload_text">
						<a id="change_param" href="/parameter">搜索参数设置</a>
					</label>
					</p>
				</form>
			</div>
		</div>
	</div>
</div>
<div class="contentbox">
	<h2>精品推荐</h2>
	<div class="content">
		<div class="imgbox">
			<div class="img_name"><a href="/detail?fabricid=61">针织锦纶</a></div>
			<img src="images/cloth001.jpg" alt="">
		</div>
		<div class="imgbox">
			<div class="img_name"><a href="/detail?fabricid=1009">涤纶化纤面料</a></div>
			<img src="images/cloth002.jpg" alt="">
		</div>
		<div class="imgbox">
			<div class="img_name"><a href="/detail?url=">黑色涤纶提花布</a></div>			
			<img src="images/cloth03.jpg" alt="">
		</div>
		<div class="imgbox">
			<div class="img_name"><a href="/detail?url=">弹力氨纶</a></div>		
			<img src="images/cloth04.jpg" alt="">
		</div>
		<div class="imgbox">
			<div class="img_name"><a href="/detail?url=">锦棉蕾丝</a></div>		
			<img src="images/cloth05.jpg" alt="">
		</div>
		<div class="imgbox">
			<div class="img_name"><a href="/detail?url=">涤棉迷彩印花</a></div>		
			<img src="images/cloth06.jpg" alt="">
		</div>
		<div class="imgbox">
			<div class="img_name"><a href="/detail?url=">全棉印花牛仔面料</a></div>	
			<img src="images/cloth07.jpg" alt="">
		</div>
		<div class="imgbox">
			<div class="img_name"><a href="/detail?url=">全涤无妨麂皮绒</a></div>		
			<img src="images/cloth08.jpg" alt="">
		</div>
	</div>
</div>
<#include "footer.ftl">
