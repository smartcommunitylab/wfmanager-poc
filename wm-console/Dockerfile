# Runtime stage
FROM node:20-alpine

WORKDIR /app

COPY . .
RUN yarn install
EXPOSE 5173

ENTRYPOINT ["yarn", "dev"]