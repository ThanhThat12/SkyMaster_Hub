#!/bin/bash

# Deploy script for Flight Schedules App
# Usage: ./deploy.sh [test|prod]

ENV=$1

if [ -z "$ENV" ]; then
    echo "❌ Error: Please specify environment (test or prod)"
    echo "Usage: ./deploy.sh [test|prod]"
    exit 1
fi

echo "🚀 Starting deployment for environment: $ENV"

# Set variables based on environment
if [ "$ENV" == "test" ]; then
    IMAGE_TAG="test-latest"
    CONTAINER_NAME="flight-app-test"
    PORT="8081"
    PROFILE="test"
    echo "📦 Deploying TEST environment..."
    
elif [ "$ENV" == "prod" ]; then
    IMAGE_TAG="product-latest"
    CONTAINER_NAME="flight-app-prod"
    PORT="8080"
    PROFILE="production"
    echo "📦 Deploying PRODUCTION environment..."
    
else
    echo "❌ Invalid environment. Use 'test' or 'prod'"
    exit 1
fi

# Pull latest image
echo "⬇️  Pulling image: vipcb04/flight-schedules:$IMAGE_TAG"
docker pull vipcb04/flight-schedules:$IMAGE_TAG

# Stop and remove old container if exists
echo "🛑 Stopping old container..."
docker stop $CONTAINER_NAME 2>/dev/null || true
docker rm $CONTAINER_NAME 2>/dev/null || true

# Run new container
echo "🚀 Starting new container..."
docker run -d \
  --name $CONTAINER_NAME \
  -p $PORT:8080 \
  -e SPRING_PROFILES_ACTIVE=$PROFILE \
  -e SPRING_DATASOURCE_URL="jdbc:postgresql://dpg-d5n37bdactks73c7lj5g-a.singapore-postgres.render.com:5432/airlab" \
  -e SPRING_DATASOURCE_USERNAME="airlab_user" \
  -e SPRING_DATASOURCE_PASSWORD="ifhIjDDTmlUhT5owTW4oMc1mQhJjeTB9" \
  --restart unless-stopped \
  vipcb04/flight-schedules:$IMAGE_TAG

# Check if container is running
sleep 3
if docker ps | grep -q $CONTAINER_NAME; then
    echo "✅ Deployment successful!"
    echo "🌐 Application running at: http://localhost:$PORT"
    docker logs --tail 50 $CONTAINER_NAME
else
    echo "❌ Deployment failed!"
    docker logs $CONTAINER_NAME
    exit 1
fi

echo ""
echo "📊 Container status:"
docker ps | grep $CONTAINER_NAME
