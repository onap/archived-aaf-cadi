FROM alpine:3.6
RUN apk add --update iptables curl bash
COPY start.sh /start.sh
RUN chmod 755 /start.sh
#CMD start.sh
ENTRYPOINT ["/start.sh"]
