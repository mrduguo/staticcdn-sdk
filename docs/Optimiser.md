Static CDN Optimiser
====================

INLINE OPTIONS
--------------

#### options list

name  | description
------------:|:---------------
 sio-data-url=enabled  |  embed image as data url
 sio-data-url=disabled  |  disable image as data url even it meet autoDataUrlMaxFileSize
 sio-img-optimise=disabled  | disable image optimization (doesn't apply to individual sprite image)
 sio-img-optimise=enabled  | enable image optimization
 sio-auto-sprite=disabled  | disable the image sprite even it meet sprite requirement
 sio-auto-sprite=enabled  | enable sprite even it doesn't meet sprite requirement
 sio-auto-sprite-name  |  all sprite image in a given css file will generated as 'default', you can give different name to generate multiple sprites
 sio-css-embed=enabled  | embed css to html page
 sio-js-embed=enabled  | embed js to html page
 sio-js-async=enabled  | add async flag to script tag on html page




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
