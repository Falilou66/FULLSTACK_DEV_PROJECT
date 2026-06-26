# SamaBank — Documentation du projet Full-Stack

> Projet d'examen — Plateforme Bancaire Numérique SOA  
> Auteur : Abdoulaye Wade Cisse — UADB Master SI

---

## Table des matières

1. [Vue d'ensemble](#1-vue-densemble)
2. [Architecture générale](#2-architecture-générale)
3. [Backend — samabank-core](#3-backend--samabank-core)
   - [Stack technique](#31-stack-technique)
   - [Structure des dossiers](#32-structure-des-dossiers)
   - [Modèle de données](#33-modèle-de-données)
   - [Modules métier](#34-modules-métier)
   - [Sécurité & Authentification](#35-sécurité--authentification)
   - [API REST — Endpoints](#36-api-rest--endpoints)
   - [Gestion des erreurs](#37-gestion-des-erreurs)
   - [Base de données & Migrations Flyway](#38-base-de-données--migrations-flyway)
   - [Configuration](#39-configuration)
4. [Frontend — samabank-frontend](#4-frontend--samabank-frontend)
   - [Stack technique](#41-stack-technique)
   - [Structure des dossiers](#42-structure-des-dossiers)
   - [Routing & Navigation](#43-routing--navigation)
   - [Core (guards, interceptors, models)](#44-core-guards-interceptors-models)
   - [Features par rôle](#45-features-par-rôle)
5. [Rôles & Permissions](#5-rôles--permissions)
6. [Flux métier principaux](#6-flux-métier-principaux)
7. [Démarrage du projet](#7-démarrage-du-projet)
8. [Comptes de test](#8-comptes-de-test)

---

## 1. Vue d'ensemble

**SamaBank** est une plateforme bancaire numérique composée de deux applications distinctes :

| Partie | Technologie | Port |
|---|---|---|
| `samabank-core` | Spring Boot 3.5 (Java 21) | `8080` |
| `samabank-frontend` | Angular 21 + Tailwind CSS 4 | `4200` |

L'application gère trois profils d'utilisateurs (**Admin**, **Teller/Caissier**, **Customer/Client**), chacun disposant de son propre tableau de bord et de ses propres fonctionnalités. La devise utilisée est le **XOF (Franc CFA)**.

---

## 2. Architecture générale

```
┌─────────────────────────────────────────────────────────┐
│                  samabank-frontend                       │
│         Angular 21 · Tailwind CSS · TypeScript          │
│   Landing │ Login │ /admin │ /teller │ /dashboard       │
└───────────────────────┬─────────────────────────────────┘
                        │ HTTP REST + JWT
                        │ (CORS : localhost:4200)
┌───────────────────────▼─────────────────────────────────┐
│                   samabank-core                          │
│              Spring Boot 3.5 · Java 21                  │
│                                                         │
│  ┌──────────┐ ┌──────────┐ ┌────────────┐ ┌─────────┐  │
│  │   auth   │ │ customer │ │  account   │ │  trans  │  │
│  └──────────┘ └──────────┘ └────────────┘ └─────────┘  │
│  ┌──────────┐ ┌──────────┐ ┌────────────┐              │
│  │  audit   │ │  stats   │ │notification│              │
│  └──────────┘ └──────────┘ └────────────┘              │
└───────────────────────┬─────────────────────────────────┘
                        │ JPA / Hibernate
┌───────────────────────▼─────────────────────────────────┐
│          PostgreSQL  (schéma public)                     │
│   Migrations Flyway V1 → V8                             │
└─────────────────────────────────────────────────────────┘
                        │
┌───────────────────────▼─────────────────────────────────┐
│       Mailpit (SMTP de test, port 1025)                  │
└─────────────────────────────────────────────────────────┘
```

Le backend suit une **architecture modulaire SOA** : chaque domaine métier est isolé dans son propre package `modules/<nom>` avec ses couches `domain`, `application` et `infrastructure`.

---

## 3. Backend — samabank-core

### 3.1 Stack technique

| Composant | Version |
|---|---|
| Java | 21 |
| Spring Boot | 3.5.14 |
| Spring Security | (inclus dans Boot) |
| Spring Data JPA / Hibernate | (inclus dans Boot) |
| PostgreSQL Driver | (inclus dans Boot) |
| Flyway | (inclus dans Boot) |
| JWT (jjwt) | 0.12.6 |
| SpringDoc / Swagger UI | 2.7.0 |
| Spring Boot Mail | (inclus dans Boot) |
| Spring Actuator | (inclus dans Boot) |

### 3.2 Structure des dossiers

```
samabank-core/src/main/java/sn/samabank/
├── SamabankCoreApplication.java        ← Point d'entrée
├── config/
│   ├── DataSeeder.java                 ← Création des 3 utilisateurs de base au démarrage
│   ├── OpenAPIConfig.java              ← Configuration Swagger
│   ├── SamaBankProperties.java         ← Propriétés métier (@ConfigurationProperties)
│   └── SecurityConfig.java             ← Chaîne de filtres Spring Security
├── modules/
│   ├── auth/                           ← Authentification & gestion des utilisateurs
│   ├── customer/                       ← Clients bancaires
│   ├── account/                        ← Comptes bancaires
│   ├── transaction/                    ← Opérations (dépôt, retrait, virement)
│   ├── audit/                          ← Journal d'audit immutable
│   ├── stats/                          ← Statistiques & dashboards
│   └── notification/                   ← Notifications email
└── shared/
    ├── api/ApiResponse.java            ← Enveloppe JSON standard
    ├── api/ApiError.java               ← Format d'erreur standard
    ├── domain/Money.java               ← Value object monétaire
    ├── exception/BusinessException.java
    ├── exception/GlobalExceptionHandler.java
    └── infrastructure/
        ├── CorrelationIdFilter.java    ← Injection X-Correlation-Id dans chaque requête
        └── SecurityContextHelper.java  ← Lecture du contexte de sécurité courant
```

Chaque module suit le même pattern en trois couches :

```
modules/<nom>/
├── domain/          ← Entités JPA + enums + règles métier (méthodes debit/credit, etc.)
├── application/     ← Services (logique applicative) + DTOs (Request/Response)
└── infrastructure/  ← Controllers REST + Repositories Spring Data
```

### 3.3 Modèle de données

#### Entité `User` — table `users`

| Champ | Type | Description |
|---|---|---|
| `id` | UUID | Clé primaire (auto-généré) |
| `username` | VARCHAR(50) | Login unique |
| `email` | VARCHAR(255) | Email unique |
| `password_hash` | VARCHAR(255) | BCrypt (force 12) |
| `role` | ENUM | `CUSTOMER`, `TELLER`, `ADMIN` |
| `status` | ENUM | `ACTIVE`, `LOCKED`, `SUSPENDED` |
| `failed_attempts` | INTEGER | Compteur d'échecs de connexion |
| `last_login_at` | TIMESTAMPTZ | Dernier login réussi |
| `password_change_required` | BOOLEAN | `true` à la création → force le changement au 1er login |
| `version` | INTEGER | Verrou optimiste (optimistic locking) |

> **Règle de sécurité** : après 5 échecs de connexion consécutifs, le compte passe en `LOCKED` automatiquement.

#### Entité `Customer` — table `customers`

| Champ | Type | Description |
|---|---|---|
| `id` | UUID | Clé primaire |
| `user_id` | UUID | Référence vers `users` (1-1) |
| `customer_number` | VARCHAR(20) | Numéro client unique |
| `first_name` / `last_name` | VARCHAR(100) | Nom complet |
| `date_of_birth` | DATE | Date de naissance |
| `email` / `phone` / `address` | — | Coordonnées |
| `status` | ENUM | `ACTIVE`, `SUSPENDED`, `CLOSED` |

#### Entité `Account` — table `accounts`

| Champ | Type | Description |
|---|---|---|
| `id` | UUID | Clé primaire |
| `account_number` | VARCHAR(25) | Numéro de compte unique |
| `customer_id` | UUID | Propriétaire du compte |
| `type` | ENUM | `CURRENT` (courant), `SAVINGS` (épargne) |
| `status` | ENUM | `ACTIVE`, `SUSPENDED`, `CLOSED` |
| `balance` | DECIMAL(15,2) | Solde en XOF |
| `currency` | VARCHAR(3) | Toujours `XOF` |
| `version` | INTEGER | Verrou optimiste (évite les doubles débits) |

> **Règles domaine** : `debit()` vérifie que le compte est actif, que le montant est positif, et que le solde est suffisant. `credit()` vérifie statut et montant positif.

#### Entité `Transaction` — table `transactions`

| Champ | Type | Description |
|---|---|---|
| `id` | UUID | Clé primaire |
| `correlation_id` | UUID | Identifiant unique de l'opération (idempotence) |
| `type` | ENUM | `DEPOSIT`, `WITHDRAWAL`, `TRANSFER` |
| `status` | ENUM | `COMPLETED`, `FAILED`, `PENDING` |
| `source_account_id` | UUID | Compte débiteur (null pour dépôt) |
| `target_account_id` | UUID | Compte crédité (null pour retrait) |
| `amount` | DECIMAL(15,2) | Montant en XOF |
| `executed_by` | UUID | ID de l'utilisateur (teller/admin) qui a fait l'opération |
| `channel` | VARCHAR(20) | Canal : `WEB`, `MOBILE`, `API` |

#### Entité `AuditEvent` — table `audit_events`

Journal **immutable** de toutes les actions sensibles. Chaque événement enregistre : type d'événement, acteur (id + rôle), ressource concernée, IP, canal, et un **payload JSON** libre (`jsonb`).

#### Entité `Notification` — table `notifications`

Notifications email envoyées (bienvenue, reset de mot de passe, etc.). Statut : `PENDING`, `SENT`, `FAILED`.

### 3.4 Modules métier

#### Module `auth`

Gère l'authentification JWT et la gestion des utilisateurs.

**AuthService** — opérations principales :
- `login()` : vérifie username/password, génère un access token (15 min) + refresh token (7 jours), enregistre l'audit
- `refresh()` : renouvelle l'access token à partir d'un refresh token valide
- `logout()` : révoque le refresh token en base
- `changePassword()` : change le mot de passe et marque `passwordChangeRequired = false`
- `forgotPassword()` : génère un token de reset et envoie un email
- `resetPassword()` : consomme le token de reset et met à jour le mot de passe

**UserService** — opérations (réservé ADMIN) :
- Créer un utilisateur, lister, modifier, suspendre, débloquer

**JwtService** : génère et valide les tokens JWT (algorithme HS512, secret en base64).

**JwtAuthenticationFilter** : filtre Spring Security qui extrait le JWT de l'en-tête `Authorization: Bearer <token>` et alimente le `SecurityContext`.

**PasswordChangeRequiredFilter** : redirige vers `/change-password` si `passwordChangeRequired = true`, sauf pour les routes exclues.

#### Module `customer`

**CustomerService** — opérations :
- `create()` : crée un client avec un numéro auto-incrémenté (`CUST-XXXXXXXX`)
- `getAll()` : liste paginée (filtrable par statut)
- `getById()` : détail d'un client
- `update()` : modifie prénom, nom, téléphone, adresse
- `suspend()` / `reactivate()` : change le statut du client

#### Module `account`

**AccountService** — opérations :
- `open()` : ouvre un compte CURRENT ou SAVINGS pour un client actif
- `getAll()` : liste paginée (filtrable par statut et type)
- `getMyAccounts()` : comptes du client connecté (via son `user_id`)
- `getById()` : détail d'un compte (ADMIN/TELLER)
- `suspend()` / `reactivate()` : gestion du statut (ADMIN uniquement)

#### Module `transaction`

**Règles métier configurées** (dans `application.properties`) :
- `max-daily-transfer` : 5 000 000 XOF par jour
- `max-transaction-amount` : 1 000 000 XOF par opération

**TransactionService** — opérations :
- `deposit()` : crédite un compte cible
- `withdraw()` : débite un compte source (vérifie le solde)
- `transfer()` : débite la source et crédite la cible en une opération atomique
- `getHistory()` : historique paginé d'un compte (TELLER/ADMIN)
- `getMyAccountTransactions()` : historique de tous ses comptes (CUSTOMER)
- `getAllTransactions()` : toutes les transactions (ADMIN)
- `getMyTransactions()` : transactions effectuées par le teller connecté

#### Module `audit`

Trace immutable de chaque action sensible. L'`AuditService` est appelé par les autres services après chaque opération critique (login, création de compte, transaction, etc.). Seul l'ADMIN peut consulter les événements d'audit.

#### Module `stats`

Fournit des statistiques agrégées pour les 3 tableaux de bord :

| Dashboard | Données |
|---|---|
| **AdminDashboardStats** | KPIs globaux, tendance des transactions (30j), répartition par type/statut, top 5 tellers, acquisition client, alertes sécurité |
| **TellerDashboardStats** | KPIs du teller, transactions du jour, objectif mensuel, clients récents |
| **CustomerDashboardStats** | Soldes des comptes, transactions récentes |

#### Module `notification`

**NotificationService** + **EmailTemplates** : envoie des emails transactionnels via JavaMail (SMTP Mailpit en local). Types : bienvenue, reset de mot de passe, confirmation de transaction.

### 3.5 Sécurité & Authentification

#### Flux d'authentification

```
Client → POST /api/v1/auth/login
         → AuthService.login()
            → Vérifie username + password (BCrypt)
            → recordSuccessfulLogin() ou recordFailedLogin()
            → Génère accessToken (JWT, 15 min) + refreshToken (hash en BDD, 7j)
            → Retourne LoginResponse { accessToken, refreshToken, role, ... }

Client → Chaque requête : Authorization: Bearer <accessToken>
         → JwtAuthenticationFilter extrait et valide le token
         → Injecte userId (UUID) dans SecurityContext comme principal
         → Les controllers lisent userId via @AuthenticationPrincipal UUID userId
```

#### Gestion des tokens

- **Access token** : JWT signé HS512, durée 15 minutes, contient `userId` et `role`
- **Refresh token** : UUID aléatoire stocké **hashé** en base dans `refresh_tokens`, durée 7 jours

#### Verrou de compte

Après **5 échecs** de login consécutifs → statut `LOCKED`. Déverrouillage possible uniquement par un ADMIN via `PATCH /api/v1/users/{id}/unlock`.

#### Changement de mot de passe obligatoire

Tout nouveau compte a `password_change_required = true`. Le `PasswordChangeRequiredFilter` intercepte toutes les requêtes et renvoie une erreur 403 avec code `PASSWORD_CHANGE_REQUIRED` tant que le mot de passe n'a pas été changé.

### 3.6 API REST — Endpoints

Base URL : `http://localhost:8080/api/v1`  
Documentation interactive : `http://localhost:8080/swagger-ui.html`

#### Authentification — `/api/v1/auth`

| Méthode | Route | Accès | Description |
|---|---|---|---|
| POST | `/login` | Public | Login → retourne JWT |
| POST | `/refresh` | Public | Renouveler l'access token |
| POST | `/logout` | Authentifié | Révoquer le refresh token |
| POST | `/change-password` | Authentifié | Changer son mot de passe |
| POST | `/forgot-password` | Public | Demander un reset par email |
| POST | `/reset-password` | Public | Réinitialiser avec un token |
| GET | `/me` | Authentifié | Profil utilisateur courant |

#### Utilisateurs — `/api/v1/users`

| Méthode | Route | Accès | Description |
|---|---|---|---|
| POST | `/` | ADMIN | Créer un utilisateur |
| GET | `/` | ADMIN | Lister tous les utilisateurs |
| GET | `/{id}` | ADMIN | Détail d'un utilisateur |
| PUT | `/{id}` | ADMIN | Mettre à jour |
| PATCH | `/{id}/suspend` | ADMIN | Suspendre |
| PATCH | `/{id}/unlock` | ADMIN | Débloquer un compte verrouillé |

#### Clients — `/api/v1/customers`

| Méthode | Route | Accès | Description |
|---|---|---|---|
| POST | `/` | TELLER, ADMIN | Créer un client |
| GET | `/` | TELLER, ADMIN | Lister (paginé, filtrable) |
| GET | `/{id}` | TELLER, ADMIN | Détail |
| PUT | `/{id}` | TELLER, ADMIN | Mettre à jour |
| PATCH | `/{id}/suspend` | ADMIN | Suspendre |
| PATCH | `/{id}/reactivate` | ADMIN | Réactiver |

#### Comptes — `/api/v1/accounts`

| Méthode | Route | Accès | Description |
|---|---|---|---|
| GET | `/all` | TELLER, ADMIN | Tous les comptes (paginé) |
| POST | `/` | TELLER, ADMIN | Ouvrir un compte |
| GET | `/` | CUSTOMER | Mes comptes |
| GET | `/{id}` | TELLER, ADMIN | Détail d'un compte |
| PATCH | `/{id}/suspend` | ADMIN | Suspendre |
| PATCH | `/{id}/reactivate` | ADMIN | Réactiver |

#### Transactions — `/api/v1/transactions`

| Méthode | Route | Accès | Description |
|---|---|---|---|
| POST | `/deposit` | TELLER, ADMIN | Effectuer un dépôt |
| POST | `/withdrawal` | TELLER, ADMIN | Effectuer un retrait |
| POST | `/transfer` | TELLER, ADMIN | Effectuer un virement |
| GET | `/my-accounts` | CUSTOMER | Historique de mes comptes |
| GET | `/all` | ADMIN | Toutes les transactions |
| GET | `/my` | TELLER, ADMIN | Mes transactions effectuées |
| GET | `/account/{accountId}` | TELLER, ADMIN | Historique d'un compte |
| GET | `/{id}` | TELLER, ADMIN | Détail d'une transaction |

#### Audit — `/api/v1/audit`

| Méthode | Route | Accès | Description |
|---|---|---|---|
| GET | `/` | ADMIN | Liste paginée des événements |
| GET | `/{id}` | ADMIN | Détail d'un événement |

#### Statistiques — `/api/v1/stats`

| Méthode | Route | Accès | Description |
|---|---|---|---|
| GET | `/admin` | ADMIN | Stats du dashboard admin |
| GET | `/teller` | TELLER | Stats du dashboard teller |
| GET | `/customer` | CUSTOMER | Stats du dashboard client |

#### Pagination — paramètres communs

Tous les endpoints de liste supportent : `page` (défaut 0), `size` (défaut 20), `sort` (ex : `executedAt,desc`).

### 3.7 Gestion des erreurs

Toutes les réponses suivent un format unifié via `ApiResponse<T>` :

```json
// Succès
{
  "success": true,
  "data": { ... },
  "correlationId": "uuid"
}

// Erreur
{
  "success": false,
  "error": {
    "code": "INSUFFICIENT_BALANCE",
    "message": "Solde insuffisant pour cette opération",
    "detail": "Solde disponible : 5000 XOF, Montant demandé : 10000 XOF"
  }
}
```

Le `GlobalExceptionHandler` gère : `BusinessException` (erreurs métier), `MethodArgumentNotValidException` (validation des DTOs), et les exceptions inattendues.

### 3.8 Base de données & Migrations Flyway

| Migration | Description |
|---|---|
| V1 | Tables `users` et `refresh_tokens` |
| V2 | Table `customers` |
| V3 | Table `accounts` |
| V4 | Table `transactions` |
| V5 | Table `audit_events` (avec colonne `jsonb` pour le payload) |
| V6 | Colonne `password_change_required` sur `users` |
| V7 | Table `password_reset_tokens` |
| V8 | Table `notifications` |

### 3.9 Configuration

Fichier : `src/main/resources/application.properties`

```properties
# Base de données
spring.datasource.url=jdbc:postgresql://localhost:5432/samabank
spring.datasource.username=samabank
spring.datasource.password=samabank2025

# JWT
samabank.jwt.access-token-ttl=900        # 15 minutes
samabank.jwt.refresh-token-ttl=604800    # 7 jours

# Règles métier
samabank.business.max-daily-transfer=5000000
samabank.business.max-transaction-amount=1000000
samabank.business.currency=XOF

# CORS (accepte le frontend Angular)
samabank.cors.allowed-origins=http://localhost:4200,http://localhost:4201

# Mail (Mailpit en local)
spring.mail.host=localhost
spring.mail.port=1025
```

---

## 4. Frontend — samabank-frontend

### 4.1 Stack technique

| Composant | Version |
|---|---|
| Angular | 21.1 |
| TypeScript | ~5.9 |
| Tailwind CSS | 4.1 |
| RxJS | ~7.8 |
| uuid | ^14 |
| Vitest | ^4 (tests unitaires) |

> Angular 21 utilise le nouveau moteur de build `@angular/build` (esbuild), les **Signals**, et les **Standalone Components** (pas de `NgModule`).

### 4.2 Structure des dossiers

```
samabank-frontend/src/app/
├── app.ts / app.html / app.css      ← Composant racine (simple <router-outlet>)
├── app.routes.ts                    ← Routes racine (lazy loading)
├── app.config.ts                    ← Configuration Angular (providers, interceptors)
├── core/
│   ├── guards/                      ← Guards de navigation
│   │   ├── auth.guard.ts            ← Redirige vers /login si non authentifié
│   │   ├── role.guard.ts            ← Vérifie le rôle requis
│   │   ├── password-change.guard.ts ← Redirige vers /change-password si requis
│   │   └── redirect-if-authenticated.guard.ts ← Redirige si déjà connecté
│   ├── interceptors/
│   │   ├── jwt.interceptor.ts       ← Ajoute Authorization: Bearer <token>
│   │   ├── correlation.interceptor.ts ← Ajoute X-Correlation-Id à chaque requête
│   │   ├── error.interceptor.ts     ← Gestion globale des erreurs HTTP
│   │   └── password-change.interceptor.ts ← Intercepte les 403 "PASSWORD_CHANGE_REQUIRED"
│   ├── models/                      ← Interfaces TypeScript (miroir des DTOs Java)
│   │   ├── account.model.ts
│   │   ├── api-response.model.ts
│   │   ├── audit.model.ts
│   │   ├── customer.model.ts
│   │   ├── stats.model.ts
│   │   └── transaction.model.ts
│   └── services/                    ← Services HTTP (un par module backend)
└── features/
    ├── landing/                     ← Page d'accueil publique
    ├── auth/                        ← Login + changement de mot de passe
    ├── admin/                       ← Espace administrateur
    ├── teller/                      ← Espace caissier
    └── customer/                    ← Espace client
```

### 4.3 Routing & Navigation

```
/                    → LandingComponent (page publique)
/login               → LoginComponent (bloqué si déjà connecté)
/change-password     → ChangePasswordComponent
/admin/**            → ADMIN_ROUTES (lazy)
/teller/**           → TELLER_ROUTES (lazy)
/dashboard/**        → CUSTOMER_ROUTES (lazy)
**                   → redirect vers /
```

#### Routes Admin (`/admin`)

```
/admin                    → AdminDashboardComponent (KPIs, graphiques)
/admin/users              → UserListComponent (gestion utilisateurs)
/admin/customers          → CustomerListComponent (gestion clients)
/admin/accounts           → AccountListComponent (gestion comptes)
/admin/transactions       → TransactionListComponent (toutes les transactions)
/admin/audit              → AuditListComponent (journal d'audit)
/admin/notifications      → NotificationsListComponent
```

#### Routes Teller (`/teller`)

```
/teller                   → TellerDashboardComponent (KPIs + actions rapides)
/teller/operations        → TellerOperationsComponent (dépôt/retrait/virement)
/teller/customers         → TellerCustomersComponent
/teller/history           → TellerHistoryComponent (mes transactions)
```

#### Routes Customer (`/dashboard`)

```
/dashboard                → CustomerDashboardComponent (soldes + transactions)
/dashboard/accounts       → CustomerAccountsComponent
/dashboard/profile        → CustomerProfileComponent
/dashboard/security       → CustomerSecurityComponent
/dashboard/settings       → CustomerSettingsComponent
/dashboard/help           → CustomerHelpComponent
```

### 4.4 Core (guards, interceptors, models)

#### Guards

| Guard | Comportement |
|---|---|
| `auth.guard` | Si token absent → redirige vers `/login` |
| `role.guard` | Si rôle insuffisant → redirige vers `/` |
| `password-change.guard` | Si `passwordChangeRequired` → redirige vers `/change-password` |
| `redirect-if-authenticated.guard` | Si déjà connecté → redirige vers le dashboard du rôle |

#### Interceptors

| Interceptor | Comportement |
|---|---|
| `jwt.interceptor` | Ajoute `Authorization: Bearer <token>` à chaque requête HTTP |
| `correlation.interceptor` | Ajoute `X-Correlation-Id: <uuid>` (traçabilité bout-en-bout) |
| `error.interceptor` | Gère les erreurs 401 (logout auto) et les erreurs réseau |
| `password-change.interceptor` | Détecte le code `PASSWORD_CHANGE_REQUIRED` et redirige |

#### Modèles TypeScript

Miroir exact des DTOs Java :

```typescript
// account.model.ts
export type AccountType = 'CURRENT' | 'SAVINGS';
export type AccountStatus = 'ACTIVE' | 'SUSPENDED' | 'CLOSED';

export interface AccountResponse {
  id: string;
  accountNumber: string;
  customerId: string;
  type: AccountType;
  status: AccountStatus;
  balance: number;
  currency: string;
  openedAt: string;
}
```

### 4.5 Features par rôle

#### Feature `landing`

Page marketing publique avec 4 sections composant : `HeroSection`, `FeaturesSection`, `StatsSection`, `CtaSection`.

#### Feature `auth`

- **LoginComponent** : formulaire login avec `LoginFormComponent` et `LoginBackgroundComponent`
- **ChangePasswordComponent** : formulaire de changement de mot de passe obligatoire (première connexion)

#### Feature `admin`

Chaque section suit le même pattern : un composant liste principal qui contient un sous-composant **table**, un sous-composant **filtres**, un **panneau de détail** (slide-in), et souvent une **modale de création**.

- **AdminDashboardComponent** : KPI Cards, graphiques (TrendChart, TypeChart, StatusChart), TellerRanking, SecurityAlerts
- **UserListComponent** : table + filtres + panneau détail + modale de création
- **CustomerListComponent** : idem + CustomerCreateModal
- **AccountListComponent** : idem + AccountOpenModal
- **TransactionListComponent** : table filtrable avec panneau détail
- **AuditListComponent** : journal immutable avec filtres et panneau détail
- **NotificationsListComponent** : liste des emails envoyés

**Layout Admin** : `AdminLayoutComponent` avec `AdminSidebarComponent` + `AdminHeaderComponent`.

#### Feature `teller`

- **TellerDashboardComponent** : KPI Cards, graphique d'activité, transactions récentes, clients récents, objectif mensuel, actions rapides
  - **DepositModal** : formulaire de dépôt (numéro de compte + montant)
  - **WithdrawalModal** : formulaire de retrait
  - **TransferModal** : formulaire de virement (compte source → compte cible)
- **TellerOperationsComponent** : vue dédiée aux opérations
- **TellerCustomersComponent** : recherche et affichage des clients
- **TellerHistoryComponent** : historique des opérations effectuées par le teller

**Layout Teller** : `TellerLayoutComponent` avec `TellerSidebarComponent` + `TellerHeaderComponent`.

#### Feature `customer`

- **CustomerDashboardComponent** : vue des comptes et des dernières transactions
- **CustomerAccountsComponent** : liste détaillée des comptes
- **CustomerProfileComponent** : informations personnelles
- **CustomerSecurityComponent** : changement de mot de passe
- **CustomerSettingsComponent** : préférences
- **CustomerHelpComponent** : aide

**Layout Customer** : `CustomerLayoutComponent` avec `CustomerSidebarComponent` + `CustomerHeaderComponent`.

---

## 5. Rôles & Permissions

| Action | CUSTOMER | TELLER | ADMIN |
|---|:---:|:---:|:---:|
| Se connecter | ✅ | ✅ | ✅ |
| Voir ses propres comptes | ✅ | — | — |
| Voir son historique de transactions | ✅ | — | — |
| Créer un client | — | ✅ | ✅ |
| Ouvrir un compte | — | ✅ | ✅ |
| Effectuer dépôt/retrait/virement | — | ✅ | ✅ |
| Voir tous les comptes | — | ✅ | ✅ |
| Voir toutes les transactions | — | — | ✅ |
| Suspendre/réactiver un compte | — | — | ✅ |
| Gérer les utilisateurs | — | — | ✅ |
| Consulter l'audit | — | — | ✅ |
| Dashboard statistiques global | — | — | ✅ |

---

## 6. Flux métier principaux

### Inscription d'un nouveau client (par un Teller)

```
1. Teller → POST /api/v1/auth/users          → crée le compte User (role=CUSTOMER)
2. Teller → POST /api/v1/customers           → crée la fiche Client liée au User
3. Teller → POST /api/v1/accounts            → ouvre un compte bancaire (CURRENT ou SAVINGS)
4. Système → envoie un email de bienvenue via NotificationService
5. Client reçoit ses identifiants et se connecte
6. À la 1ère connexion : PasswordChangeRequiredFilter → redirige vers /change-password
7. Client change son mot de passe → passwordChangeRequired = false
```

### Opération de dépôt (par un Teller)

```
1. Teller → POST /api/v1/transactions/deposit { accountId, amount, description }
2. TransactionService.deposit()
   → Charge le compte Account
   → Appelle account.credit(amount)  ← règle domaine
   → Crée une Transaction(type=DEPOSIT, status=COMPLETED)
   → Sauvegarde Account + Transaction (atomique, @Transactional)
   → Crée un AuditEvent
3. Retourne TransactionResponse
```

### Virement entre comptes

```
1. Teller → POST /api/v1/transactions/transfer { sourceAccountId, targetAccountId, amount }
2. TransactionService.transfer()
   → Charge sourceAccount et targetAccount
   → Vérifie les règles métier (montant max, plafond journalier)
   → sourceAccount.debit(amount)   ← vérifie solde suffisant
   → targetAccount.credit(amount)
   → Crée une Transaction(type=TRANSFER)
   → Sauvegarde les deux comptes + transaction (atomique)
   → AuditEvent
```

### Réinitialisation de mot de passe

```
1. POST /api/v1/auth/forgot-password { email }
   → Génère un token UUID, le stocke dans password_reset_tokens (TTL 1h)
   → Envoie un email avec le lien : {frontend.base-url}/reset-password?token=...

2. POST /api/v1/auth/reset-password { token, newPassword }
   → Valide le token (non expiré, non utilisé)
   → Met à jour le password_hash (BCrypt)
   → Invalide le token
```

---

## 7. Démarrage du projet

### Prérequis

- Java 21
- Maven 3.9+
- Node.js 20+ / npm 10+
- PostgreSQL 15+
- Mailpit (optionnel, pour les emails)

### Backend

```bash
# 1. Créer la base de données
psql -U postgres -c "CREATE USER samabank WITH PASSWORD 'samabank2025';"
psql -U postgres -c "CREATE DATABASE samabank OWNER samabank;"

# 2. Lancer le backend
cd samabank-core
./mvnw spring-boot:run
# API disponible sur http://localhost:8080
# Swagger UI : http://localhost:8080/swagger-ui.html
```

### Frontend

```bash
cd samabank-frontend
npm install
npm start
# Application disponible sur http://localhost:4200
```

### Docker (backend uniquement)

Le `Dockerfile` multi-stage est fourni. Il attend que PostgreSQL (port 5432) et Mailpit (port 1025) soient disponibles avant de démarrer Spring Boot (via `wait-for-it.sh`).

```bash
cd samabank-core
docker build -t samabank-core .
docker run -p 8080:8080 samabank-core
```

---

## 8. Comptes de test

Créés automatiquement au premier démarrage par le `DataSeeder` si la table `users` est vide :

| Username | Mot de passe | Rôle | Remarque |
|---|---|---|---|
| `admin` | `password` | ADMIN | Accès complet |
| `teller01` | `password` | TELLER | Opérations bancaires |
| `client01` | `password` | CUSTOMER | Vue client |

> **Note** : À la première connexion, chaque compte est forcé de changer son mot de passe (`passwordChangeRequired = true`).

---

*Documentation générée le 2026-06-26*
