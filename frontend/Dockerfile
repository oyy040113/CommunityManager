# ---- Build stage ----
FROM node:20-alpine AS build
WORKDIR /app

COPY package.json package-lock.json* ./
RUN npm ci --prefer-offline

COPY . .
RUN npm run build

# ---- Runtime: static files only, served by nginx ----
# The dist/ folder is copied into the nginx image via docker-compose volumes
# so this stage just produces the artifact.
FROM scratch AS export
COPY --from=build /app/dist /dist
