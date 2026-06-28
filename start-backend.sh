#!/usr/bin/env bash
# ============================================================
#  SamaBank – Démarrage de tous les microservices
# ============================================================
#
#  Usage :
#    ./start-backend.sh          → lance avec les JARs existants
#    ./start-backend.sh --build  → recompile tous les services puis lance
#    ./start-backend.sh --stop   → arrête tous les services en cours
#
# ============================================================

set -euo pipefail

BACKEND_DIR="$(cd "$(dirname "$0")/backend" && pwd)"
LOG_DIR="$BACKEND_DIR/logs"
PID_DIR="$BACKEND_DIR/.pids"

mkdir -p "$LOG_DIR" "$PID_DIR"

# ── Couleurs ──────────────────────────────────────────────
RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'
CYAN='\033[0;36m'; BOLD='\033[1m'; RESET='\033[0m'

log()  { echo -e "${CYAN}[SamaBank]${RESET} $*"; }
ok()   { echo -e "${GREEN}  ✓${RESET} $*"; }
warn() { echo -e "${YELLOW}  ⚠${RESET} $*"; }
fail() { echo -e "${RED}  ✗${RESET} $*"; }
sep()  { echo -e "${CYAN}──────────────────────────────────────────────${RESET}"; }

# ── Ordre de démarrage ────────────────────────────────────
#  (Discovery d'abord, Gateway en dernier)
declare -a SERVICES=(
  "samabank-discovery:8761"
  "samabank-auth-service:8081"
  "samabank-customer-service:8082"
  "samabank-account-service:8083"
  "samabank-transaction-service:8084"
  "samabank-audit-service:8085"
  "samabank-notification-service:8086"
  "samabank-stats-service:8087"
  "samabank-gateway:8080"
)

# ── Délai (secondes) avant de démarrer le service suivant ─
declare -A START_DELAY=(
  ["samabank-discovery"]=10
  ["samabank-auth-service"]=6
  ["samabank-customer-service"]=5
  ["samabank-account-service"]=5
  ["samabank-transaction-service"]=5
  ["samabank-audit-service"]=5
  ["samabank-notification-service"]=5
  ["samabank-stats-service"]=5
  ["samabank-gateway"]=8
)

# ── Arrêt de tous les services ───────────────────────────
stop_all() {
  log "Arrêt de tous les microservices SamaBank..."
  sep
  local stopped=0
  for entry in "${SERVICES[@]}"; do
    local svc="${entry%%:*}"
    local pid_file="$PID_DIR/$svc.pid"
    if [[ -f "$pid_file" ]]; then
      local pid
      pid=$(cat "$pid_file")
      if kill -0 "$pid" 2>/dev/null; then
        kill "$pid" 2>/dev/null && ok "$svc (PID $pid) arrêté"
        ((stopped++))
      else
        warn "$svc – PID $pid introuvable (déjà arrêté ?)"
      fi
      rm -f "$pid_file"
    else
      warn "$svc – aucun fichier PID trouvé"
    fi
  done
  sep
  log "$stopped service(s) arrêté(s). Logs conservés dans $LOG_DIR"
  exit 0
}

# ── Recompilation ────────────────────────────────────────
build_all() {
  log "Recompilation de tous les microservices (mvn package -DskipTests)..."
  sep
  for entry in "${SERVICES[@]}"; do
    local svc="${entry%%:*}"
    local svc_dir="$BACKEND_DIR/$svc"
    echo -e "${BOLD}→ Build : $svc${RESET}"
    if mvn -f "$svc_dir/pom.xml" package -DskipTests -q; then
      ok "$svc compilé"
    else
      fail "$svc : ÉCHEC de la compilation"
      exit 1
    fi
  done
  sep
  log "Tous les services compilés avec succès."
}

# ── Vérifier qu'un port est ouvert ──────────────────────
wait_for_port() {
  local port=$1
  local svc=$2
  local max_wait=60
  local elapsed=0
  printf "  Attente du port %s" "$port"
  while ! nc -z localhost "$port" 2>/dev/null; do
    sleep 2
    elapsed=$((elapsed + 2))
    printf "."
    if [[ $elapsed -ge $max_wait ]]; then
      echo ""
      fail "$svc ne répond pas sur le port $port après ${max_wait}s"
      return 1
    fi
  done
  echo ""
  return 0
}

# ── Démarrer un service ──────────────────────────────────
start_service() {
  local svc=$1
  local port=$2
  local svc_dir="$BACKEND_DIR/$svc"
  local log_file="$LOG_DIR/$svc.log"
  local pid_file="$PID_DIR/$svc.pid"

  # Chercher le JAR (exclure les jars "original-*")
  local jar
  jar=$(find "$svc_dir/target" -maxdepth 1 -name "*.jar" ! -name "original-*" 2>/dev/null | head -1)

  if [[ -z "$jar" ]]; then
    fail "$svc : JAR introuvable dans $svc_dir/target/"
    fail "Lancez d'abord : ./start-backend.sh --build"
    exit 1
  fi

  # Si déjà en cours sur ce port, on skip
  if nc -z localhost "$port" 2>/dev/null; then
    warn "$svc : port $port déjà occupé — service ignoré (peut-être déjà lancé)"
    return 0
  fi

  echo -e "${BOLD}→ Démarrage : $svc (port $port)${RESET}"
  echo "   JAR  : $(basename "$jar")"
  echo "   Log  : $log_file"

  # Lancement en arrière-plan
  java -jar "$jar" \
    --spring.output.ansi.enabled=ALWAYS \
    >> "$log_file" 2>&1 &

  local pid=$!
  echo "$pid" > "$pid_file"
  echo "   PID  : $pid"

  # Attendre que le port s'ouvre
  wait_for_port "$port" "$svc"
  ok "$svc démarré"
}

# ── Afficher le statut final ─────────────────────────────
show_status() {
  sep
  echo -e "${BOLD}  Statut des microservices SamaBank${RESET}"
  sep
  printf "  %-35s %-8s  %-6s  %s\n" "Service" "Port" "PID" "État"
  printf "  %-35s %-8s  %-6s  %s\n" "-------" "----" "---" "----"
  for entry in "${SERVICES[@]}"; do
    local svc="${entry%%:*}"
    local port="${entry##*:}"
    local pid_file="$PID_DIR/$svc.pid"
    local pid="-"
    local state
    if [[ -f "$pid_file" ]]; then
      pid=$(cat "$pid_file")
    fi
    if nc -z localhost "$port" 2>/dev/null; then
      state="${GREEN}UP${RESET}"
    else
      state="${RED}DOWN${RESET}"
    fi
    printf "  %-35s %-8s  %-6s  " "$svc" "$port" "$pid"
    echo -e "$state"
  done
  sep
  echo -e "  Eureka Dashboard : ${CYAN}http://localhost:8761${RESET}"
  echo -e "  API Gateway      : ${CYAN}http://localhost:8080${RESET}"
  sep
  echo -e "  Logs → ${LOG_DIR}"
  echo -e "  Pour arrêter : ${YELLOW}./start-backend.sh --stop${RESET}"
  sep
}

# ── Point d'entrée ───────────────────────────────────────
case "${1:-}" in
  --stop)  stop_all ;;
  --build) build_all ;;
  --status) show_status; exit 0 ;;
  --help)
    echo "Usage: $0 [--build] [--stop] [--status]"
    echo "  (sans option) : lance les services avec les JARs existants"
    echo "  --build       : recompile puis lance"
    echo "  --stop        : arrête tous les services"
    echo "  --status      : affiche l'état des services"
    exit 0 ;;
  "")  ;;
  *) echo "Option inconnue : $1  (--help pour l'aide)"; exit 1 ;;
esac

echo ""
echo -e "${BOLD}${CYAN}"
echo "   ███████╗ █████╗ ███╗   ███╗ █████╗ ██████╗  █████╗ ███╗  ██╗██╗  ██╗"
echo "   ██╔════╝██╔══██╗████╗ ████║██╔══██╗██╔══██╗██╔══██╗████╗ ██║██║ ██╔╝"
echo "   ███████╗███████║██╔████╔██║███████║██████╔╝███████║██╔██╗██║█████╔╝    By Hackira :) !!!"
echo "   ╚════██║██╔══██║██║╚██╔╝██║██╔══██║██╔══██╗██╔══██║██║╚████║██╔═██╗ "
echo "   ███████║██║  ██║██║ ╚═╝ ██║██║  ██║██████╔╝██║  ██║██║ ╚███║██║  ██╗"
echo "   ╚══════╝╚═╝  ╚═╝╚═╝     ╚═╝╚═╝  ╚═╝╚═════╝ ╚═╝  ╚═╝╚═╝  ╚══╝╚═╝  ╚═╝"
echo -e "${RESET}"
sep
log "Démarrage de ${#SERVICES[@]} microservices..."
sep
echo ""

for entry in "${SERVICES[@]}"; do
  svc="${entry%%:*}"
  port="${entry##*:}"
  delay="${START_DELAY[$svc]:-5}"

  start_service "$svc" "$port"

  # Petite pause entre chaque service (sauf le dernier)
  if [[ "$entry" != "${SERVICES[-1]}" ]]; then
    echo "   (pause ${delay}s avant le prochain service)"
    sleep "$delay"
    echo ""
  fi
done

echo ""
show_status
