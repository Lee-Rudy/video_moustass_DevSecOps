const API_BASE = import.meta.env.VITE_API_URL || '';

/**
 * Login : POST /api/login avec { mail, password }.
 * @returns { Promise<{ token, userId, name }> }
 * @throws si 401 ou erreur réseau
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
