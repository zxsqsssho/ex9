// frontend/vue.config.js
module.exports = {
    devServer: {
        port: 8081,
        proxy: {
            '/api': {
                target: 'http://localhost:8080',
                changeOrigin: true,
                secure: false,
                logLevel: 'debug'
            }
        }
    }
}