<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

        <title>${title} | Kweet</title>
    </head>
    <body>

        <@greet person="${name}"!/>

        Your email address is ${email}

        <#--<#include "/copyright_footer.html">-->
    </body>
</html>

<#macro greet person color="black">
<font size="+2" color="${color}">Hello ${person}!</font>
</#macro>