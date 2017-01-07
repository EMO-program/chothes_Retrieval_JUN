<#include "header.ftl">
<div class="image_to_find">
  <div class="row">
    <div class="col-md-5">
			<img src="${result.queryPath}" class="img-responsive" alt=""/>
	  <label>${result.queryName?remove_ending(".jpg")}</label>
    </div>
    <div class="col-md-5" col_1>
      <h4>根据上传图片找到以下结果</h4>
    </div>
  </div>
</div>
<div class="grid_1">
  <div class="row">
	<#list result.resList as res>
	<div class="col-md-2 col_1">
		<label>${res?substring(res?index_of("/")+1)?remove_ending(".jpg")}</label>
		<a href="/detail?fabricid=${res?substring(res?index_of("/")+1)?remove_ending(".jpg")}"><img src="${res}" class="img-responsive" alt=""/></a>
	</div>
	</#list>
  </div>
  <div class="clearfix"> </div>
</div>
<#include "footer.ftl">