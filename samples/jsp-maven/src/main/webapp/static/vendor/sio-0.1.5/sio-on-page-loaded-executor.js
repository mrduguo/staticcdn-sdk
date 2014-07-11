$(function () {
    if (SIO && SIO.onPageLoaded) {
        for (var i = 0; i < SIO.onPageLoaded.length; i++) {
            SIO.onPageLoaded[i]();
        }
    }
});