# ==============================================================================
# Script PowerShell pour publier les images sur Docker Hub
# ==============================================================================
# Usage: .\publish-dockerhub.ps1
# ==============================================================================

param(
    [string]$DockerUsername = $env:DOCKER_USERNAME,
    [string]$Version = "latest"
)

# Couleurs pour l'affichage
function Write-ColorOutput($ForegroundColor) {
    $fc = $host.UI.RawUI.ForegroundColor
    $host.UI.RawUI.ForegroundColor = $ForegroundColor
    if ($args) {
        Write-Output $args
    }
    $host.UI.RawUI.ForegroundColor = $fc
}

function Write-Success { Write-ColorOutput Green $args }
function Write-Info { Write-ColorOutput Cyan $args }
function Write-Warning { Write-ColorOutput Yellow $args }
function Write-Error { Write-ColorOutput Red $args }

# ==============================================================================
# Vérifications préliminaires
# ==============================================================================
Write-Info "🔍 Vérifications préliminaires..."

# Vérifier que Docker est installé
if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    Write-Error "❌ Docker n'est pas installé ou n'est pas dans le PATH"
    exit 1
}

# Vérifier que Docker Compose est installé
if (-not (Get-Command docker-compose -ErrorAction SilentlyContinue)) {
    Write-Error "❌ Docker Compose n'est pas installé ou n'est pas dans le PATH"
    exit 1
}

# Vérifier que Docker est démarré
try {
    docker ps | Out-Null
} catch {
    Write-Error "❌ Docker n'est pas démarré. Veuillez démarrer Docker Desktop."
    exit 1
}

# Demander le nom d'utilisateur Docker Hub si non fourni
if (-not $DockerUsername) {
    $DockerUsername = Read-Host "Entrez votre nom d'utilisateur Docker Hub"
    if (-not $DockerUsername) {
        Write-Error "❌ Nom d'utilisateur Docker Hub requis"
        exit 1
    }
}

# Demander la version si non fournie
if ($Version -eq "latest") {
    $VersionInput = Read-Host "Entrez la version de l'image (appuyez sur Entrée pour 'latest')"
    if ($VersionInput) {
        $Version = $VersionInput
    }
}

Write-Success "✅ Vérifications terminées"
Write-Info ""
Write-Info "📋 Configuration:"
Write-Info "   Username: $DockerUsername"
Write-Info "   Version: $Version"
Write-Info ""

# Demander confirmation
$confirm = Read-Host "Voulez-vous continuer? (o/n)"
if ($confirm -ne "o" -and $confirm -ne "O" -and $confirm -ne "oui" -and $confirm -ne "Oui") {
    Write-Warning "⏹️  Opération annulée"
    exit 0
}

# ==============================================================================
# Connexion à Docker Hub
# ==============================================================================
Write-Info ""
Write-Info "🔐 Connexion à Docker Hub..."
docker login
if ($LASTEXITCODE -ne 0) {
    Write-Error "❌ Échec de la connexion à Docker Hub"
    exit 1
}
Write-Success "✅ Connecté à Docker Hub"

# ==============================================================================
# Build des images
# ==============================================================================
Write-Info ""
Write-Info "🔨 Build des images Docker..."
Write-Info "   (Cela peut prendre plusieurs minutes...)"
Write-Info ""

docker compose build --no-cache
if ($LASTEXITCODE -ne 0) {
    Write-Error "❌ Échec du build des images"
    exit 1
}
Write-Success "✅ Images buildées avec succès"

# ==============================================================================
# Tag des images
# ==============================================================================
Write-Info ""
Write-Info "🏷️  Tag des images..."

# Backend
Write-Info "   → Backend: $DockerUsername/video-moustass-backend:$Version"
docker tag moustass/video-moustass-backend:latest ${DockerUsername}/video-moustass-backend:${Version}
if ($Version -ne "latest") {
    docker tag moustass/video-moustass-backend:latest ${DockerUsername}/video-moustass-backend:latest
}

# Frontend
Write-Info "   → Frontend: $DockerUsername/video-moustass-frontend:$Version"
docker tag moustass/video-moustass-frontend:latest ${DockerUsername}/video-moustass-frontend:${Version}
if ($Version -ne "latest") {
    docker tag moustass/video-moustass-frontend:latest ${DockerUsername}/video-moustass-frontend:latest
}

Write-Success "✅ Images taguées avec succès"

# ==============================================================================
# Publication sur Docker Hub
# ==============================================================================
Write-Info ""
Write-Info "📤 Publication sur Docker Hub..."
Write-Info "   (Cela peut prendre plusieurs minutes selon votre connexion...)"
Write-Info ""

# Push backend
Write-Info "   → Push backend:$Version..."
docker push ${DockerUsername}/video-moustass-backend:${Version}
if ($LASTEXITCODE -ne 0) {
    Write-Error "❌ Échec de la publication du backend"
    exit 1
}

if ($Version -ne "latest") {
    Write-Info "   → Push backend:latest..."
    docker push ${DockerUsername}/video-moustass-backend:latest
    if ($LASTEXITCODE -ne 0) {
        Write-Error "❌ Échec de la publication du backend:latest"
        exit 1
    }
}

# Push frontend
Write-Info "   → Push frontend:$Version..."
docker push ${DockerUsername}/video-moustass-frontend:${Version}
if ($LASTEXITCODE -ne 0) {
    Write-Error "❌ Échec de la publication du frontend"
    exit 1
}

if ($Version -ne "latest") {
    Write-Info "   → Push frontend:latest..."
    docker push ${DockerUsername}/video-moustass-frontend:latest
    if ($LASTEXITCODE -ne 0) {
        Write-Error "❌ Échec de la publication du frontend:latest"
        exit 1
    }
}

Write-Success "✅ Publication terminée avec succès!"

# ==============================================================================
# Récapitulatif
# ==============================================================================
Write-Info ""
Write-Info "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
Write-Success "🎉 Publication réussie sur Docker Hub!"
Write-Info "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
Write-Info ""
Write-Info "📦 Images publiées:"
Write-Info "   Backend:"
Write-Info "      → ${DockerUsername}/video-moustass-backend:${Version}"
if ($Version -ne "latest") {
    Write-Info "      → ${DockerUsername}/video-moustass-backend:latest"
}
Write-Info ""
Write-Info "   Frontend:"
Write-Info "      → ${DockerUsername}/video-moustass-frontend:${Version}"
if ($Version -ne "latest") {
    Write-Info "      → ${DockerUsername}/video-moustass-frontend:latest"
}
Write-Info ""
Write-Info "🔗 Liens Docker Hub:"
Write-Info "   → https://hub.docker.com/r/${DockerUsername}/video-moustass-backend"
Write-Info "   → https://hub.docker.com/r/${DockerUsername}/video-moustass-frontend"
Write-Info ""
Write-Info "📝 Prochaines étapes:"
Write-Info "   1. Vérifier les images sur Docker Hub"
Write-Info "   2. Tester le déploiement avec: docker compose pull && docker compose up -d"
Write-Info "   3. Partager les images avec votre équipe"
Write-Info ""
Write-Success "✨ Terminé!"
