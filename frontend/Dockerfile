# Multi-stage Dockerfile for Angular 19 Frontend

# Stage 1: Build Angular production distribution
FROM node:22-alpine AS builder
WORKDIR /app

# Copy dependency specifications first for Docker cache optimization
COPY package*.json ./
RUN npm ci

# Copy full frontend sources and compile for production
COPY . .
RUN npm run build -- --configuration production

# Stage 2: Lightweight Nginx server
FROM nginx:alpine

# Remove default Nginx welcome page and configure custom reverse proxy
RUN rm -rf /etc/nginx/conf.d/*
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy production assets from builder stage
COPY --from=builder /app/dist/frontend/browser /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
