<#include "header.ftl">
<link rel="stylesheet" href="css/parameter.css">
<div class="param">
    <form action="/parameter" method="post">
        <p>
            <label for="scale-ratio">
                <span>拉伸比例</span>
                <input id="scale-ratio" type="text" name="scaleRatio" value="4">
            </label>
        </p>
        <p>
            <label for="same-color-num-percent">
                <span>相同颜色Bin百分比</span>
                <input id="same-color-num-percent" type="text" name="sameColorNumPercent" value=0.3>
            </label>
        </p>
        <p>
            <label for="">
                <span>相同颜色统计量百分比</span>
                <input id="same-color-quality-percent" type="text" name="sameColorQualityPercent" value=0.85>
            </label>
        </p>
        <p>
            <label for="">
                <span>中间结果集数量</span>
                <input id="intermediate-result" type="text" name="intermediateResult" value=100>
            </label>
        </p>
        <input type="submit" value="参数修改" id="sumit">
    </form>
</div>
<#include "footer.ftl">



