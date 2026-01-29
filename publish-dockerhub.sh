#!/bin/bash

# ==============================================================================
# Script Bash pour publier les images sur Docker Hub
# ==============================================================================
# Usage: ./publish-dockerhub.sh [username] [version]
# ==============================================================================

set -e  # Arrêter le script en cas d'erreur

# Couleurs
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Fonctions d'affichage
function print_success() {
    echo -e "${GREEN}$1${NC}"
}

function print_info() {
    echo -e "${CYAN}$1${NC}"
}

function print_warning() {
    echo -e "${YELLOW}$1${NC}"
}

function print_error() {
    echo -e "${RED}$1${NC}"
}

# ==============================================================================
# Vérifications préliminaires
# ==============================================================================
print_info "🔍 Vérifications préliminaires..."

# Vérifier que Docker est installé
if ! command -v docker &> /dev/null; then
    print_error "❌ Docker n'est pas installé"
    exit 1
fi

# Vérifier que Docker Compose est installé
if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
    print_error "❌ Docker Compose n'est pas installé"
    exit 1
fi

# Vérifier que Docker est démarré
if ! docker ps &> /dev/null; then
    print_error "❌ Docker n'est pas démarré"
    exit 1
fi

print_success "✅ Vérifications terminées"

# ==============================================================================
# Configuration
# ==============================================================================
DOCKER_USERNAME="${1:-${DOCKER_USERNAME}}"
VERSION="${2:-latest}"

# Demander le nom d'utilisateur si non fourni
if [ -z "$DOCKER_USERNAME" ]; then
    read -p "Entrez votre nom d'utilisateur Docker Hub: " DOCKER_USERNAME
    if [ -z "$DOCKER_USERNAME" ]; then
        print_error "❌ Nom d'utilisateur Docker Hub requis"
        exit 1
    fi
fi

# Demander la version si non fournie
if [ "$VERSION" == "latest" ] && [ -z "$2" ]; then
    read -p "Entrez la version de l'image (appuyez sur Entrée pour 'latest'): " VERSION_INPUT
    if [ -n "$VERSION_INPUT" ]; then
        VERSION=$VERSION_INPUT
    fi
fi

echo ""
print_info "📋 Configuration:"
print_info "   Username: $DOCKER_USERNAME"
print_info "   Version: $VERSION"
echo ""

# Demander confirmation
read -p "Voulez-vous continuer? (o/n) " -n 1 -r
echo ""
if [[ ! $REPLY =~ ^[OoYy]$ ]]; then
    print_warning "⏹️  Opération annulée"
    exit 0
fi

# ==============================================================================
# Connexion à Docker Hub
# ==============================================================================
echo ""
print_info "🔐 Connexion à Docker Hub..."
if ! docker login; then
    print_error "❌ Échec de la connexion à Docker Hub"
    exit 1
fi
print_success "✅ Connecté à Docker Hub"

# ==============================================================================
# Build des images
# ==============================================================================
echo ""
print_info "🔨 Build des images Docker..."
print_info "   (Cela peut prendre plusieurs minutes...)"
echo ""

if ! docker compose build --no-cache; then
    print_error "❌ Échec du build des images"
    exit 1
fi
print_success "✅ Images buildées avec succès"

# ==============================================================================
# Tag des images
# ==============================================================================
echo ""
print_info "🏷️  Tag des images..."

# Backend
print_info "   → Backend: $DOCKER_USERNAME/video-moustass-backend:$VERSION"
docker tag moustass/video-moustass-backend:latest ${DOCKER_USERNAME}/video-moustass-backend:${VERSION}
if [ "$VERSION" != "latest" ]; then
    docker tag moustass/video-moustass-backend:latest ${DOCKER_USERNAME}/video-moustass-backend:latest
fi

# Frontend
print_info "   → Frontend: $DOCKER_USERNAME/video-moustass-frontend:$VERSION"
docker tag moustass/video-moustass-frontend:latest ${DOCKER_USERNAME}/video-moustass-frontend:${VERSION}
if [ "$VERSION" != "latest" ]; then
    docker tag moustass/video-moustass-frontend:latest ${DOCKER_USERNAME}/video-moustass-frontend:latest
fi

print_success "✅ Images taguées avec succès"

# ==============================================================================
# Publication sur Docker Hub
# ==============================================================================
echo ""
print_info "📤 Publication sur Docker Hub..."
print_info "   (Cela peut prendre plusieurs minutes selon votre connexion...)"
echo ""

# Push backend
print_info "   → Push backend:$VERSION..."
if ! docker push ${DOCKER_USERNAME}/video-moustass-backend:${VERSION}; then
    print_error "❌ Échec de la publication du backend"
    exit 1
fi

if [ "$VERSION" != "latest" ]; then
    print_info "   → Push backend:latest..."
    if ! docker push ${DOCKER_USERNAME}/video-moustass-backend:latest; then
        print_error "❌ Échec de la publication du backend:latest"
        exit 1
    fi
fi

# Push frontend
print_info "   → Push frontend:$VERSION..."
if ! docker push ${DOCKER_USERNAME}/video-moustass-frontend:${VERSION}; then
    print_error "❌ Échec de la publication du frontend"
    exit 1
fi

if [ "$VERSION" != "latest" ]; then
    print_info "   → Push frontend:latest..."
    if ! docker push ${DOCKER_USERNAME}/video-moustass-frontend:latest; then
        print_error "❌ Échec de la publication du frontend:latest"
        exit 1
    fi
fi

print_success "✅ Publication terminée avec succès!"

# ==============================================================================
# Récapitulatif
# ==============================================================================
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
print_success "🎉 Publication réussie sur Docker Hub!"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
print_info "📦 Images publiées:"
print_info "   Backend:"
print_info "      → ${DOCKER_USERNAME}/video-moustass-backend:${VERSION}"
if [ "$VERSION" != "latest" ]; then
    print_info "      → ${DOCKER_USERNAME}/video-moustass-backend:latest"
fi
echo ""
print_info "   Frontend:"
print_info "      → ${DOCKER_USERNAME}/video-moustass-frontend:${VERSION}"
if [ "$VERSION" != "latest" ]; then
    print_info "      → ${DOCKER_USERNAME}/video-moustass-frontend:latest"
fi
echo ""
print_info "🔗 Liens Docker Hub:"
print_info "   → https://hub.docker.com/r/${DOCKER_USERNAME}/video-moustass-backend"
print_info "   → https://hub.docker.com/r/${DOCKER_USERNAME}/video-moustass-frontend"
echo ""
print_info "📝 Prochaines étapes:"
print_info "   1. Vérifier les images sur Docker Hub"
print_info "   2. Tester le déploiement avec: docker compose pull && docker compose up -d"
print_info "   3. Partager les images avec votre équipe"
echo ""
print_success "✨ Terminé!"
