<!DOCTYPE html>
<html>
<head>
    <title>X</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <!-- 引入 Bootstrap -->
    <link href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css" rel="stylesheet">
    <!-- HTML5 Shiv 和 Respond.js 用于让 IE8 支持 HTML5元素和媒体查询 -->
    <!-- 注意： 如果通过 file://  引入 Respond.js 文件，则该文件无法起效果 -->
    <!--[if lt IE 9]>
    <script src="https://oss.maxcdn.com/libs/html5shiv/3.7.0/html5shiv.js"></script>
    <script src="https://oss.maxcdn.com/libs/respond.js/1.3.0/respond.min.js"></script>
    <![endif]-->

    <style type="text/css">
        .baseText {font-size: 200%}
        .mainDiv {text-align: center;
            margin: 20px;
            background-color: #1b6d85;
            border-style: solid;
            border-width: 20px;
            border-color: #000000;
        }
    </style>
</head>
<body>
<div style="position:fixed; width: 100%;height: 100%; top: 0; right:0;">
    <div class="container" >

    <div class="row">
        <div class="col-lg-3"></div>
        <div class="col-lg-6 mainDiv">

            <div class="baseText">
                ${year}年${month}月${day}日 ${week}
            </div>
            <div class="baseText">
                ${dateWithFestival}
            </div>
            <div class="baseText">
                ${zhYear}年 ${zhMonth}月 ${zhDay}日
            </div>
            <div class="baseText">
                ${lunarTiangan+lunarDizhi}[${zodiac}]年
            </div>

            <div class="baseText">
            ${ganZhiMonth}月${ganZhiDay}日
            </div>

        </div>
        <div class="col-lg-3"></div>
    </div>
</div>
</div>

<!-- jQuery (Bootstrap 的 JavaScript 插件需要引入 jQuery) -->
<script src="https://code.jquery.com/jquery.js"></script>
<!-- 包括所有已编译的插件 -->
<script src="js/bootstrap.min.js"></script>
</body>


</html>