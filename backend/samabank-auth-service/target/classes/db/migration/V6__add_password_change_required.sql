-- Ajouter la colonne avec valeur par défaut TRUE (les utilisateurs existants doivent changer leur mot de passe)
ALTER TABLE users
    ADD COLUMN password_change_required BOOLEAN NOT NULL DEFAULT TRUE;

-- Mettre à jour les utilisateurs existants qui ont déjà un historique de connexion
-- (optionnel : les marquer comme déjà changé si vous le souhaitez)
-- UPDATE users SET password_change_required = FALSE WHERE last_login_at IS NOT NULL;

-- Index pour les requêtes fréquentes sur ce champ
CREATE INDEX idx_users_password_change_required ON users(password_change_required) WHERE password_change_required = TRUE;