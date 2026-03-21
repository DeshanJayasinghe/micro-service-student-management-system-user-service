#!/bin/bash
# Azure Container Apps Deployment Script

RESOURCE_GROUP="sms-resource-group"
LOCATION="eastus"
ENVIRONMENT="sms-environment"
REGISTRY="ghcr.io"

# Create resource group
az group create --name $RESOURCE_GROUP --location $LOCATION

# Create Container Apps environment
az containerapp env create \
  --name $ENVIRONMENT \
  --resource-group $RESOURCE_GROUP \
  --location $LOCATION

# Deploy Auth Service
az containerapp create \
  --name sms-auth-service \
  --resource-group $RESOURCE_GROUP \
  --environment $ENVIRONMENT \
  --image ${REGISTRY}/OWNER/REPO/auth-service:latest \
  --target-port 8081 \
  --ingress external \
  --min-replicas 1 \
  --max-replicas 3 \
  --env-vars \
    "DB_HOST=secretref:db-host" \
    "DB_PASSWORD=secretref:db-password" \
    "JWT_SECRET=secretref:jwt-secret" \
    "KAFKA_BOOTSTRAP_SERVERS=kafka:29092"

# Deploy User Service
az containerapp create \
  --name sms-user-service \
  --resource-group $RESOURCE_GROUP \
  --environment $ENVIRONMENT \
  --image ${REGISTRY}/OWNER/REPO/user-service:latest \
  --target-port 8082 \
  --ingress external \
  --min-replicas 1 \
  --max-replicas 3 \
  --env-vars \
    "DB_HOST=secretref:db-host" \
    "DB_PASSWORD=secretref:db-password" \
    "JWT_SECRET=secretref:jwt-secret" \
    "KAFKA_BOOTSTRAP_SERVERS=kafka:29092" \
    "AUTH_SERVICE_URL=https://sms-auth-service"

# Deploy Course Service
az containerapp create \
  --name sms-course-service \
  --resource-group $RESOURCE_GROUP \
  --environment $ENVIRONMENT \
  --image ${REGISTRY}/OWNER/REPO/course-service:latest \
  --target-port 8083 \
  --ingress external \
  --min-replicas 1 \
  --max-replicas 3 \
  --env-vars \
    "DB_HOST=secretref:db-host" \
    "DB_PASSWORD=secretref:db-password" \
    "JWT_SECRET=secretref:jwt-secret" \
    "KAFKA_BOOTSTRAP_SERVERS=kafka:29092" \
    "USER_SERVICE_URL=https://sms-user-service"

# Deploy Marks Service
az containerapp create \
  --name sms-marks-service \
  --resource-group $RESOURCE_GROUP \
  --environment $ENVIRONMENT \
  --image ${REGISTRY}/OWNER/REPO/marks-service:latest \
  --target-port 8084 \
  --ingress external \
  --min-replicas 1 \
  --max-replicas 3 \
  --env-vars \
    "DB_HOST=secretref:db-host" \
    "DB_PASSWORD=secretref:db-password" \
    "KAFKA_BOOTSTRAP_SERVERS=kafka:29092"

echo "Deployment complete!"
