# RuoYi-Hikvision 前端镜像
# 构建: 前端 vue 项目 -> nginx 静态服务

# ---------- 构建阶段 ----------
FROM node:22-alpine AS builder

WORKDIR /app

COPY package.json pnpm-lock.yaml* ./
RUN npm install --registry=https://registry.npmmirror.com || true

COPY . .

RUN npm run build:prod

# ---------- 运行阶段 ----------
FROM nginx:1.31.1-alpine

COPY --from=builder /app/dist /usr/share/nginx/html

COPY nginx.conf /etc/nginx/nginx.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
