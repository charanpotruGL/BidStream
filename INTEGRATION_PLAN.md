# BidStream — Frontend ↔ Backend Integration Plan

Single entry point: everything reaches the backend through the **API Gateway on port 8080**.
The frontend never talks to individual services directly.

## 1. Service & Port Map

| Service             | Eureka name            | Port  | Notes                                     |
| ------------------- | ---------------------- | ----- | ----------------------------------------- |
| Eureka Server       | `service-registry`     | 8761  | Service discovery UI at `/`               |
| API Gateway         | `ApiGateway`           | 8080  | **Only endpoint the frontend needs**      |
| User Service        | `user-service`         | 8081  | Auth (register/login), user CRUD          |
| Auction Service     | `auction-service`      | 8082  | Auctions CRUD + search                     |
| Bid Processing      | `bid-processing-service`| 8083 | Bids, highest-bid, Kafka producer         |
| Notification Service| `notification-service` | 8084  | Notifications, Kafka consumer, polling    |
| Kafka               | —                      | 9092  | Async event bus (auction-created, bid-placed) |

Gateway routes (`GateWayconfig.java`): `/api/auth/**`, `/api/users/**` → user-service;
`/api/auctions/**` → auction-service; `/api/bids/**` → bid-processing-service;
`/api/notifications/**` → notification-service. No `stripPrefix` — the full `/api/...`
path is forwarded, so every client call uses the same `/api` prefix.

## 2. Frontend Configuration

### Env vars (`.env`, copy of `.env.example`)

```
VITE_API_URL=            # empty => same-origin dev proxy (recommended)
VITE_ENV=development
VITE_POLL_AUCTION_MS=5000
VITE_POLL_BIDS_MS=3000
VITE_POLL_NOTIFICATIONS_MS=10000
VITE_WS_URL=/ws          # reserved for future WebSocket
```

Only `VITE_`-prefixed variables are exposed to the client (`vite-env.d.ts` types them).
Vite's dev server proxies both `/api` and `/ws` to `http://localhost:8080`
(`VITE_PROXY_TARGET` overrides). In production the reverse proxy/nginx must do the
same: forward `/api` to the gateway and pass through `Upgrade`/`Connection` headers
for `/ws`.

### API client (`src/api/client.ts`)

```ts
// base URL: import.meta.env.VITE_API_URL || ""   (axios)
const client = axios.create({
  baseURL: import.meta.env.VITE_API_URL || "",
  timeout: 15000,
  headers: { "Content-Type": "application/json" },
});
```

- Request interceptor attaches `Authorization: Bearer <token>` when present.
- Response interceptor: on 401 it clears the session and redirects to `/login`
  (skipped when already there).
- Session helpers: `setToken/getToken/clearToken`, `setUserId/getUserId/clearUserId`.
- `getErrorMessage(error)` normalizes `{message, fieldErrors}` bodies into a
  readable message — every mutation surfaces errors via `toast.error(...)`.

### Data shapes (verified against backend DTOs)

- `AuthResponse` → `{token, tokenType, userId, username, email, role, expiresIn}`
- `UserResponse` → `{id, username, email, fullName, role, active, createdAt}`
- `AuctionResponse` → matches the frontend `Auction` (includes `createdAt`,
  `currentPrice`, `highestBidId`, `highestBidderId`).
- Auction list endpoint returns a Spring `Page<AuctionResponse>`; the page parses
  `content` and `totalPages`.

## 3. Auth Flow

1. Login/Register → `POST /api/auth/login` / `POST /api/auth/register` → `AuthResponse`.
2. `setToken` + `setUserId` persist to localStorage; Zustand `useAuthStore` holds
   `user`, `token`, and actions.
3. `SessionBootstrap` (in `App.tsx`) restores token + userId on load and fetches
   `GET /api/users/{id}` to rebuild the session; anonymous users get the Login CTA.
4. `RequireAuth` wraps `/seller-hub` and `/notifications`.
5. Gateway `SecurityConfig` permits `/api/auth/**`, `/actuator/**`, `/eureka/**`;
   everything else needs a valid JWT. The JWT filter also stamps `X-User-Id` /
   `X-User-Role` headers used downstream for ownership checks.

## 4. Real-time Updates

WebSockets are **not yet implemented** (backend or frontend). Live updates use
React Query polling:

- Auction detail: `refetchInterval` 5s (auction) + 3s (bids, highest bid).
- Notifications: polling interval configurable via `VITE_POLL_NOTIFICATIONS_MS`.
- All polling intervals are centralized in `.env` so they can be tuned without code
  changes. The `/ws` proxy + `VITE_WS_URL` are already wired for a future WebSocket
  upgrade path.

## 5. Error Handling & Edge Cases

- 400/422 field errors → rendered per-field from `fieldErrors` map.
- 401 → interceptor redirect to `/login`.
- 404 → page-level "not found" states.
- Network/timeout (15s) → friendly message via `getErrorMessage`.
- Malformed JSON, expired JWT, and unknown routes are exercised by the test suite
  (see §6).

## 6. Verifying the Integration

```powershell
# Backend must be running (Eureka 8761, Gateway 8080, services 8081-8084, Kafka 9092)

# 1. Full API suite (63 tests) against the gateway:
powershell -ExecutionPolicy Bypass -File scripts\api-tests.ps1 -BaseUrl http://localhost:8080

# 2. Frontend (dev, with /api proxy to the gateway):
cd bidstream-frontend-complete
npm install
npm run dev        # http://localhost:5173

# 3. Production build / typecheck:
npm run build
```

Latest run: **63/63 passed** (auth, CRUD, bids, notifications, Kafka event flow,
gateway edge cases). Logs land in `scripts/api-tests-<timestamp>.log`.

## 7. Operational Best Practices

- **Keep the gateway as the only public surface.** Never expose 8081–8084 directly.
- **Secrets** live in env vars, never in code or committed files (`.env.local` is
  gitignored).
- **Config-driven knobs:** API URL, polling intervals, feature flags all come from
  `VITE_` env vars.
- **Load tests / scaling:** frontend is a static bundle — scale behind a CDN/reverse
  proxy; each backend service is stateless and can scale behind Eureka + the gateway.
  Kafka decouples bid processing from notifications, so neither block on the other.
- **Observability:** all services expose actuator health endpoints on the gateway
  (`/actuator/**`), and Eureka shows registration health at 8761.
