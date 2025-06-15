;(function(config) {
    config.mode = 'development'
    if(!config.hasOwnProperty('devServer')) {
        config.devServer = { }
    }
    config.devServer.historyApiFallback = true
})(config);
