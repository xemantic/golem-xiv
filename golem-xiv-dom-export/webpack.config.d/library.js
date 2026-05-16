if (config.mode === 'production') {
    const webpack = require('webpack');
    config.plugins = config.plugins || [];
    config.plugins.push(new webpack.BannerPlugin({
        banner: ';globalThis.golemDomExport=globalThis["golem-xiv:golem-xiv-dom-export"].com.xemantic.golem.dom.export;',
        raw: true,
        footer: true,
    }));
}