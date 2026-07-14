import http from "node:http";

const NEXT_PORT = 3001;
const LISTEN_PORT = 3000;

// code-server sends "/absports/3000/..." to port 3000
// Next.js expects "/codeeditor/default/absports/3000/..." (its basePath)
// So we prepend "/codeeditor/default" to whatever arrives

const PREFIX = "/codeeditor/default";

const server = http.createServer((req, res) => {
  const path = PREFIX + req.url;

  const options = {
    hostname: "127.0.0.1",
    port: NEXT_PORT,
    path: path,
    method: req.method,
    headers: { ...req.headers, host: `127.0.0.1:${NEXT_PORT}` },
  };

  const proxyReq = http.request(options, (proxyRes) => {
    res.writeHead(proxyRes.statusCode, proxyRes.headers);
    proxyRes.pipe(res, { end: true });
  });

  proxyReq.on("error", (err) => {
    console.error("Proxy error:", err.message);
    res.writeHead(502);
    res.end("Bad Gateway");
  });

  req.pipe(proxyReq, { end: true });
});

server.listen(LISTEN_PORT, "0.0.0.0", () => {
  console.log(`SageMaker proxy :${LISTEN_PORT} → next(:${NEXT_PORT})`);
});
