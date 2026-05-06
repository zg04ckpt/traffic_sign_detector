# client3

This template should help get you started developing with Vue 3 in Vite.

## Recommended IDE Setup

[VS Code](https://code.visualstudio.com/) + [Vue (Official)](https://marketplace.visualstudio.com/items?itemName=Vue.volar) (and disable Vetur).

## Recommended Browser Setup

- Chromium-based browsers (Chrome, Edge, Brave, etc.):
  - [Vue.js devtools](https://chromewebstore.google.com/detail/vuejs-devtools/nhdogjmejiglipccpnnnanhbledajbpd)
  - [Turn on Custom Object Formatter in Chrome DevTools](http://bit.ly/object-formatters)
- Firefox:
  - [Vue.js devtools](https://addons.mozilla.org/en-US/firefox/addon/vue-js-devtools/)
  - [Turn on Custom Object Formatter in Firefox DevTools](https://fxdx.dev/firefox-devtools-custom-object-formatters/)

## Type Support for `.vue` Imports in TS

TypeScript cannot handle type information for `.vue` imports by default, so we replace the `tsc` CLI with `vue-tsc` for type checking. In editors, we need [Volar](https://marketplace.visualstudio.com/items?itemName=Vue.volar) to make the TypeScript language service aware of `.vue` types.

## Customize configuration

See [Vite Configuration Reference](https://vite.dev/config/).

## Project Setup

```sh
npm install
```

## API Connection (Gateway)

1. Copy `.env.example` to `.env.local`.
2. Set `VITE_GATEWAY_URL` to match how you run the backend:
   - **IDE / local Spring Boot:** `http://localhost:8081` (default `server.port` for `api-gateway`).
   - **Kubernetes only (Docker Desktop NodePort):** `http://localhost:30081`.
   If nothing listens on the chosen URL, Vite shows `socket hang up` for `/api/*`.
3. Start backend stack first (discovery → config → gateway → services), then run client3.

### HTTP 502 on `/api/tap-du-lieu` (Bad Gateway)

The gateway forwards to **`dataset-service`** via Eureka (`lb://DATASET-SERVICE`). You get **502** when that instance is missing or unhealthy — usually:

- Services still use Config Server defaults (`postgres`, `discovery-server` hostnames) while everything runs **on your PC** → DB/Eureka URLs do not resolve and **`dataset-service` never registers**.
- **Fix:** run JVM services with **`--spring.profiles.active=local`** so Config Server loads `*-local.yaml` (Postgres/Eureka on **`localhost`**). Restart **config-server** after updating config files.
- Ensure **PostgreSQL** is up and databases/users match config (e.g. `dataset_db`, user `server5`). Start order: **discovery-server** → **config-server** → **dataset-service** → **api-gateway**.

Client3 now calls:
- `/api/mo-hinh`
- `/api/tap-du-lieu`
- `/api/huan-luyen/*`
- `/api/trainings/*`

Network/API failures are handled as safe UI messages (no uncaught exception flow in views).

### Compile and Hot-Reload for Development

```sh
npm run dev
```

### Type-Check, Compile and Minify for Production

```sh
npm run build
```
