#!/bin/bash

#############################################
# Webhook Deployment Listener
# Listens for GitHub Actions webhook and 
# automatically pulls and deploys new images
#############################################

# Configuration
WEBHOOK_SECRET="${WEBHOOK_SECRET:-your-secret-key-here-change-this}"
WEBHOOK_PORT="${WEBHOOK_PORT:-9000}"
PROJECT_DIR="/home/$(whoami)/flight-app"
LOG_FILE="/var/log/deploy-webhook.log"

# Colors for logging
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Logging function
log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a $LOG_FILE
}

log_success() {
    echo -e "${GREEN}[$(date '+%Y-%m-%d %H:%M:%S')] ✓ $1${NC}" | tee -a $LOG_FILE
}

log_error() {
    echo -e "${RED}[$(date '+%Y-%m-%d %H:%M:%S')] ✗ $1${NC}" | tee -a $LOG_FILE
}

log_info() {
    echo -e "${YELLOW}[$(date '+%Y-%m-%d %H:%M:%S')] ℹ $1${NC}" | tee -a $LOG_FILE
}

# Deploy function
deploy() {
    log_info "Starting deployment process..."
    
    cd $PROJECT_DIR || {
        log_error "Failed to change to project directory: $PROJECT_DIR"
        return 1
    }
    
    # Pull latest image
    log_info "Pulling latest Docker image..."
    if docker compose pull; then
        log_success "Successfully pulled latest image"
    else
        log_error "Failed to pull Docker image"
        return 1
    fi
    
    # Restart containers
    log_info "Restarting containers..."
    if docker compose up -d; then
        log_success "Containers restarted successfully"
    else
        log_error "Failed to restart containers"
        return 1
    fi
    
    # Wait for health check
    log_info "Waiting for services to be healthy..."
    sleep 5
    
    # Check if containers are running
    if docker compose ps | grep -q "Up"; then
        log_success "Deployment completed successfully!"
        
        # Show running containers
        log_info "Running containers:"
        docker compose ps >> $LOG_FILE 2>&1
        
        return 0
    else
        log_error "Containers are not running properly"
        docker compose logs --tail=50 >> $LOG_FILE 2>&1
        return 1
    fi
}

# Validate secret
validate_secret() {
    local received_secret="$1"
    
    if [ "$received_secret" == "$WEBHOOK_SECRET" ]; then
        return 0
    else
        return 1
    fi
}

# HTTP response helper
send_response() {
    local status_code="$1"
    local message="$2"
    
    echo -e "HTTP/1.1 $status_code\r"
    echo -e "Content-Type: application/json\r"
    echo -e "Connection: close\r"
    echo -e "\r"
    echo -e "{\"status\": \"$status_code\", \"message\": \"$message\"}\r"
}

# Main webhook listener
log_success "Webhook listener started on port $WEBHOOK_PORT"
log_info "Project directory: $PROJECT_DIR"
log_info "Waiting for deployment requests..."

while true; do
    # Listen for incoming requests
    {
        # Read the request
        read -r request_line
        
        # Read headers
        declare -A headers
        while IFS=': ' read -r key value; do
            value=$(echo "$value" | tr -d '\r')
            [ -z "$key" ] && break
            headers["$key"]="$value"
        done
        
        # Extract webhook secret from headers
        received_secret="${headers[X-Webhook-Secret]}"
        
        log_info "Received webhook request"
        
        # Validate secret
        if validate_secret "$received_secret"; then
            log_success "Valid webhook secret received"
            
            # Send immediate response
            send_response "200 OK" "Deployment started"
            
            # Deploy in background to not block the webhook
            (deploy) &
            
        else
            log_error "Invalid webhook secret received: '$received_secret'"
            send_response "401 Unauthorized" "Invalid webhook secret"
        fi
        
    } | nc -l -p $WEBHOOK_PORT -q 1
    
    # Small delay before listening again
    sleep 1
done
