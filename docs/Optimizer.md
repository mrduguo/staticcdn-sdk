Static CDN Optimizer
====================

GLOBAL OPTIONS
--------------

name  | value
------------:|:---------------
 version  |  null (default), any positive numbers
 profile  |  safe (default), relaxed
 cdnBaseUrl  |  null (default will use built-in shared CDN), any url which have access the optimized files 
 autoEmbedCss  |  false (default) , true
 autoDataUrlMaxFileSize  | null (default), any positive numbers
 autoSpriteMinFileSize  | null (default), any positive numbers
 autoSpriteMaxFileSize  | null (default), any positive numbers


The global options can be passed via maven/gradle plugin or comments in first text file with json format. Sample global optimizer options:
```json
staticcdn-optimizer-options=
{
    "autoSpriteMinFileSize":1,
    "autoSpriteMaxFileSize":10000
}
```



INLINE OPTIONS
--------------

#### options list

    name                    | description
---------------------------:|:---------------
 sio-data-url=enabled       |  embed image as data url
 sio-data-url=disabled      |  disable image as data url even it meet autoDataUrlMaxFileSize
 sio-img-optimize=disabled  | disable image optimization (doesn't apply to individual sprite image)
 sio-img-optimize=enabled   | enable image optimization
 sio-img-quality=0.1        | reduce the image quality, support range from 0.01 to 1.0
 sio-auto-sprite=disabled   | disable the image sprite even it meet sprite requirement
 sio-auto-sprite=enabled    | enable sprite even it doesn't meet sprite requirement
 sio-auto-sprite-name       |  all sprite image in a given css file will generated as 'default', you can give different name to generate multiple sprites
 sio-css-embed=enabled      | embed css to html page
 sio-css-sync=enabled       | load css synchronously with javascript, works great in Chrome/Safari, functional work in other browsers  
 sio-js-embed=enabled       | embed js to html page
 sio-js-async=enabled       | add async flag to script tag on html page
 sio-url-protocol=https     | use http: or https: prefix in front of default //staticcdn.io url reference 
 sio-use-cdn=enabled        | used in include file to delivery the resource via StaticCDN.io
  



#### options sample

Inline image as data url:
```css
.logo{
    background-image: url(../img/logo-dark-bg.png?sio-data-url=enabled);
    width: 270px;
    height: 30px;
    display: block;
}
```

To add async flag to the script tag:
```html
<script src="js/jquery.js?sio-js-async=enabled" type="text/javascript"></script>
```



#### sprite requirement

Image will only be part of sprite if meet following requirements in the same class definition:

1. has background-image
1. has width
1. has height
1. doesn't has background-size
1. doesn't has sio-auto-sprite=disabled
1. doesn't has sio-data-url=enabled
1. doesn't has background-repeat other than no-repeat
1. meet any of requirements:
    1. has sio-auto-sprite=enabled
    1. has sio-auto-sprite-name
    1. match autoSpriteMinFileSize and autoSpriteMaxFileSize

A sample sprite image css
```css
.price-free{
    background-image:url(../img/icons/price-free.png?sio-auto-sprite=enabled);
    width: 93px;
    height: 67px;
}
```
