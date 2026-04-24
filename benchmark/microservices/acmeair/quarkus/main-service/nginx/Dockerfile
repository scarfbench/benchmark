FROM nginx
RUN rm /etc/nginx/nginx.conf
ADD conf /etc/nginx/

RUN chown -R 1001:root /var/cache/nginx \
    && chmod -R g+w /var/cache/nginx

USER 1001
