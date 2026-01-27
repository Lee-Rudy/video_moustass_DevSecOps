const API_BASE = import.meta.env.VITE_API_URL || '';

function normalizeToken(token) {
  if (!token) return '';
  return String(token).replace(/^Bearer\s+/i, '').trim();
}

function authHeaders(token) {
  const raw = normalizeToken(token);
  if (!raw) return {};
  return { Authorization: `Bearer ${raw}`, 'X-Auth-Token': raw };
}

/**
 * Login : POST /api/login avec { mail, password }.
 * @returns { Promise<{ token, userId, name, isAdmin }> }
 */
export async function login(mail, password) {
  const res = await fetch(API_BASE + '/api/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ mail, password }),
  });
  if (!res.ok) throw new Error('Identifiants incorrects');
  return res.json();
}

/**
 * GET /api/users : utilisateurs non-admin (id, name) pour le champ « Envoyé à ».
 */
export async function getUsers(token) {
  const res = await fetch(API_BASE + '/api/users', { headers: authHeaders(token) });
  if (!res.ok) throw new Error('Erreur chargement utilisateurs');
  return res.json();
}

/**
 * POST /api/orders (multipart) : transaction_send_to, montant, video_name, video (File/Blob).
 * Ne pas ajouter Content-Type : le navigateur fixe multipart/form-data + boundary pour FormData.
 * @returns { Promise<{ id, steps }> }
 */
export async function createOrder(formData, token) {
  const res = await fetch(API_BASE + '/api/orders', {
    method: 'POST',
    headers: authHeaders(token),
    body: formData,
  });
  if (!res.ok) {
    const j = await res.json().catch(() => ({}));
    throw new Error(j.error || 'Erreur enregistrement ordre');
  }
  return res.json();
}

/**
 * GET /api/orders/received : ordres reçus par l'utilisateur connecté.
 */
export async function getOrdersReceived(token) {
  const res = await fetch(API_BASE + '/api/orders/received', { headers: authHeaders(token) });
  if (!res.ok) throw new Error('Erreur chargement ordres');
  return res.json();
}

/**
 * POST /api/orders/:id/validate : scan, déchiffrement, vérification. Retourne { success, videoBase64 } ou erreur.
 */
export async function validateOrder(id, token) {
  const res = await fetch(API_BASE + '/api/orders/' + id + '/validate', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...authHeaders(token) },
  });
  const data = await res.json().catch(() => ({}));
  if (!res.ok) throw new Error(data.error || 'Erreur validation');
  return data;
}

/**
 * GET /api/logs : tous les logs d'audit triés par date décroissante.
 */
export async function getLogs(token) {
  const res = await fetch(API_BASE + '/api/logs', { headers: authHeaders(token) });
  if (!res.ok) throw new Error('Erreur chargement logs');
  return res.json();
}

/**
 * POST /api/inscription/create : créer un nouvel utilisateur (admin uniquement).
 */
export async function createUser(userData, token) {
  const res = await fetch(API_BASE + '/api/inscription/create', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...authHeaders(token) },
    body: JSON.stringify(userData),
  });
  if (!res.ok) {
    const j = await res.json().catch(() => ({}));
    throw new Error(j.message || 'Erreur création utilisateur');
  }
  return res.json();
}

/**
 * GET /api/inscription/users : récupérer tous les utilisateurs (admin uniquement).
 */
export async function getAllUsers(token) {
  const res = await fetch(API_BASE + '/api/inscription/users', { headers: authHeaders(token) });
  if (!res.ok) throw new Error('Erreur chargement utilisateurs');
  return res.json();
}

/**
 * DELETE /api/inscription/users/:id : supprimer un utilisateur (admin uniquement).
 */
export async function deleteUser(userId, token) {
  const res = await fetch(API_BASE + '/api/inscription/users/' + userId, {
    method: 'DELETE',
    headers: authHeaders(token),
  });
  if (!res.ok) throw new Error('Erreur suppression utilisateur');
}
