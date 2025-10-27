const PROXY_CONFIG = {
    "/licence-plate": {
        "target": "http://localhost:8085",
        "secure": false,
        "changeOrigin": true,
        "bypass": function (req, res, proxyOptions) {
            req.headers["Host"] = "localhost:4220";
        }
    }
};

module.exports = PROXY_CONFIG;
