FROM node:14.17.3-alpine as Builder
RUN mkdir -p /app
WORKDIR /app
COPY . /app
RUN npm install -g cnpm -registry=https://registry.npm.taobao.org

RUN cnpm install -g vue && \
    cnpm install && \
    cnpm run build

FROM nginx:1.20.0
RUN mkdir -p /app
WORKDIR /app
COPY --from=Builder /app/dist /app
RUN chmod -R 755 /app
# 这里nginx配置复制过去
ADD nginx.template /etc/nginx/conf.d
EXPOSE 80
EXPOSE 443
WORKDIR /etc/nginx/conf.d
ENTRYPOINT envsubst '$URL'  < nginx.template > default.conf && cat default.conf && nginx -g 'daemon off;'