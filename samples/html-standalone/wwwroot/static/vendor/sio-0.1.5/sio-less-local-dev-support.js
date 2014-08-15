if (typeof OPTIMISED === 'undefined') OPTIMISED = false;
if (!OPTIMISED) {
    localStorage.clear();
    less = { env: 'development'};
    lessScript = document.createElement('script');
    lessScript.type = 'text/javascript';
    lessScript.src = 'static/vendor/less-1.7.3/less-173.js';
    (document.getElementsByTagName('head')[0] || document.getElementsByTagName('body')[0]).appendChild(lessScript);
}