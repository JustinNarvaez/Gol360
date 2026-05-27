import { useEffect, useState } from 'react'
import { useAuth } from '../context/AuthContext'
import { getUserById, updateUser, changePassword } from '../services/userService'
import type { UserProfile, UserUpdateRequest, ChangePasswordRequest } from '../types/user'

type Tab = 'datos' | 'password' | 'preferencias'

const NOTIF_LABELS: Record<string, string> = {
  GOAL: 'Gol',
  YELLOW_CARD: 'Tarjeta Amarilla',
  RED_CARD: 'Tarjeta Roja',
  MATCH_START: 'Inicio de Partido',
  MATCH_END: 'Fin de Partido',
  SUBSTITUTION: 'Sustitución',
}

const USER_TYPE_LABELS: Record<string, string> = {
  LOCAL_FAN: 'Fanático Local',
  VISIT_FAN: 'Fanático Visitante',
  OPERATOR: 'Operador',
  SUPPORT: 'Soporte',
  COMPLIANCE: 'Cumplimiento',
}

function formatRegisterDate(raw: string | null): string {
  if (!raw) return '—'
  // Spring puede devolver "2026-05-26" o [2026, 5, 26]
  try {
    const d = Array.isArray(raw)
      ? new Date((raw as unknown as number[])[0], (raw as unknown as number[])[1] - 1, (raw as unknown as number[])[2])
      : new Date(raw)
    return d.toLocaleDateString('es-ES', { day: '2-digit', month: 'long', year: 'numeric' })
  } catch {
    return String(raw)
  }
}

function getInitials(p: UserProfile): string {
  return `${p.firstName?.[0] ?? ''}${p.lastName?.[0] ?? ''}`.toUpperCase() || '?'
}

type Msg = { type: 'ok' | 'err'; text: string }

export default function ProfilePage() {
  const { user } = useAuth()

  const [profile, setProfile] = useState<UserProfile | null>(null)
  const [loading, setLoading] = useState(true)
  const [fetchError, setFetchError] = useState<string | null>(null)
  const [activeTab, setActiveTab] = useState<Tab>('datos')

  // Datos personales
  const [editingData, setEditingData] = useState(false)
  const [dataForm, setDataForm] = useState<UserUpdateRequest>({ firstName: '', lastName: '', userName: '', email: '' })
  const [savingData, setSavingData] = useState(false)
  const [dataMsg, setDataMsg] = useState<Msg | null>(null)

  // Contraseña
  const [pwdForm, setPwdForm] = useState<ChangePasswordRequest>({ currentPassword: '', newPassword: '', confirmPassword: '' })
  const [savingPwd, setSavingPwd] = useState(false)
  const [pwdMsg, setPwdMsg] = useState<Msg | null>(null)

  useEffect(() => {
    if (!user) return
    getUserById(user.id)
      .then((p) => {
        setProfile(p)
        setDataForm({ firstName: p.firstName, lastName: p.lastName, userName: p.userName, email: p.email })
      })
      .catch(() => setFetchError('No se pudo cargar el perfil.'))
      .finally(() => setLoading(false))
  }, [user])

  function startEdit() {
    if (!profile) return
    setDataForm({ firstName: profile.firstName, lastName: profile.lastName, userName: profile.userName, email: profile.email })
    setDataMsg(null)
    setEditingData(true)
  }

  function cancelEdit() {
    setEditingData(false)
    setDataMsg(null)
  }

  async function saveData() {
    if (!user) return
    setSavingData(true)
    setDataMsg(null)
    try {
      const updated = await updateUser(user.id, dataForm)
      setProfile(updated)
      setEditingData(false)
      setDataMsg({ type: 'ok', text: 'Datos actualizados correctamente.' })
    } catch {
      setDataMsg({ type: 'err', text: 'No se pudieron guardar los cambios.' })
    } finally {
      setSavingData(false)
    }
  }

  async function savePassword() {
    if (!user) return
    if (pwdForm.newPassword !== pwdForm.confirmPassword) {
      setPwdMsg({ type: 'err', text: 'Las contraseñas nuevas no coinciden.' })
      return
    }
    setSavingPwd(true)
    setPwdMsg(null)
    try {
      await changePassword(user.id, pwdForm)
      setPwdForm({ currentPassword: '', newPassword: '', confirmPassword: '' })
      setPwdMsg({ type: 'ok', text: 'Contraseña actualizada correctamente.' })
    } catch {
      setPwdMsg({ type: 'err', text: 'No se pudo cambiar la contraseña. Verifica la contraseña actual.' })
    } finally {
      setSavingPwd(false)
    }
  }

  if (loading) {
    return (
      <div className="profile-page">
        <div className="matches-state">
          <div className="spinner" />
          <p>Cargando perfil...</p>
        </div>
      </div>
    )
  }

  if (fetchError || !profile) {
    return (
      <div className="profile-page">
        <div className="matches-state error">
          <p>{fetchError ?? 'Perfil no disponible.'}</p>
        </div>
      </div>
    )
  }

  const prefs = profile.preferences

  return (
    <div className="profile-page">

      {/* ── Header ── */}
      <div className="profile-header-card">
        <div className="profile-avatar">{getInitials(profile)}</div>
        <div className="profile-meta">
          <h2>{profile.firstName} {profile.lastName}</h2>
          <p>@{profile.userName} · {profile.email}</p>
          <div className="profile-meta-badges">
            <span className="profile-type-badge">{USER_TYPE_LABELS[profile.userType] ?? profile.userType}</span>
            {profile.registerDate && (
              <span className="profile-since">Miembro desde {formatRegisterDate(profile.registerDate)}</span>
            )}
          </div>
        </div>
      </div>

      {/* ── Tabs ── */}
      <div className="profile-tabs">
        {(['datos', 'password', 'preferencias'] as Tab[]).map((tab) => (
          <button
            key={tab}
            className={`profile-tab${activeTab === tab ? ' active' : ''}`}
            onClick={() => setActiveTab(tab)}
          >
            {tab === 'datos' ? 'Datos' : tab === 'password' ? 'Contraseña' : 'Preferencias'}
          </button>
        ))}
      </div>

      {/* ── Tab: Datos personales ── */}
      {activeTab === 'datos' && (
        <div className="profile-section-card">
          <h3 className="profile-section-title">Datos Personales</h3>

          {!editingData ? (
            <>
              {([
                ['Nombre', profile.firstName],
                ['Apellido', profile.lastName],
                ['Usuario', `@${profile.userName}`],
                ['Email', profile.email],
              ] as [string, string][]).map(([label, value]) => (
                <div key={label} className="profile-field">
                  <span className="profile-field-label">{label}</span>
                  <span className="profile-field-value">{value}</span>
                </div>
              ))}
              {dataMsg && (
                <p className={dataMsg.type === 'ok' ? 'profile-msg-success' : 'profile-msg-error'}>
                  {dataMsg.text}
                </p>
              )}
              <button className="profile-edit-btn" onClick={startEdit}>Editar datos</button>
            </>
          ) : (
            <div className="profile-form">
              {([
                { key: 'firstName', label: 'Nombre', type: 'text' },
                { key: 'lastName',  label: 'Apellido', type: 'text' },
                { key: 'userName',  label: 'Usuario', type: 'text' },
                { key: 'email',     label: 'Email', type: 'email' },
              ] as { key: keyof UserUpdateRequest; label: string; type: string }[]).map(({ key, label, type }) => (
                <div key={key} className="profile-form-group">
                  <label className="profile-form-label">{label}</label>
                  <input
                    className="profile-form-input"
                    type={type}
                    value={dataForm[key]}
                    onChange={(e) => setDataForm((prev) => ({ ...prev, [key]: e.target.value }))}
                  />
                </div>
              ))}
              {dataMsg && (
                <p className={dataMsg.type === 'ok' ? 'profile-msg-success' : 'profile-msg-error'}>
                  {dataMsg.text}
                </p>
              )}
              <div className="profile-form-actions">
                <button className="profile-btn-save" onClick={saveData} disabled={savingData}>
                  {savingData ? 'Guardando...' : 'Guardar'}
                </button>
                <button className="profile-btn-cancel" onClick={cancelEdit}>Cancelar</button>
              </div>
            </div>
          )}
        </div>
      )}

      {/* ── Tab: Contraseña ── */}
      {activeTab === 'password' && (
        <div className="profile-section-card">
          <h3 className="profile-section-title">Cambiar Contraseña</h3>
          <div className="profile-form">
            {([
              { key: 'currentPassword', label: 'Contraseña actual' },
              { key: 'newPassword',     label: 'Nueva contraseña' },
              { key: 'confirmPassword', label: 'Confirmar nueva contraseña' },
            ] as { key: keyof ChangePasswordRequest; label: string }[]).map(({ key, label }) => (
              <div key={key} className="profile-form-group">
                <label className="profile-form-label">{label}</label>
                <input
                  className="profile-form-input"
                  type="password"
                  value={pwdForm[key]}
                  onChange={(e) => setPwdForm((prev) => ({ ...prev, [key]: e.target.value }))}
                />
              </div>
            ))}
            {pwdMsg && (
              <p className={pwdMsg.type === 'ok' ? 'profile-msg-success' : 'profile-msg-error'}>
                {pwdMsg.text}
              </p>
            )}
            <div className="profile-form-actions">
              <button className="profile-btn-save" onClick={savePassword} disabled={savingPwd}>
                {savingPwd ? 'Guardando...' : 'Cambiar contraseña'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ── Tab: Preferencias ── */}
      {activeTab === 'preferencias' && (
        <div className="profile-section-card">
          <h3 className="profile-section-title">Mis Preferencias</h3>

          {!prefs ? (
            <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem' }}>
              No tienes preferencias configuradas.
            </p>
          ) : (
            <>
              {prefs.favNations.length > 0 && (
                <div className="pref-group">
                  <p className="pref-group-label">Selecciones Favoritas</p>
                  <div className="pref-chips-list">
                    {prefs.favNations.map((t) => (
                      <span key={t.id} className="pref-team-chip">
                        {t.flagUrl && (
                          <img
                            src={t.flagUrl}
                            alt={t.name}
                            className="pref-team-flag"
                            onError={(e) => { (e.target as HTMLImageElement).style.display = 'none' }}
                          />
                        )}
                        {t.name}
                      </span>
                    ))}
                  </div>
                </div>
              )}

              {prefs.favStadiums.length > 0 && (
                <div className="pref-group">
                  <p className="pref-group-label">Estadios Favoritos</p>
                  <div className="pref-chips-list">
                    {prefs.favStadiums.map((s) => (
                      <span key={s.id} className="pref-chip">
                        🏟 {s.name}{s.city ? `, ${s.city}` : ''}
                      </span>
                    ))}
                  </div>
                </div>
              )}

              {prefs.favNotifications.length > 0 && (
                <div className="pref-group">
                  <p className="pref-group-label">Notificaciones</p>
                  <div className="pref-chips-list">
                    {prefs.favNotifications.map((n) => (
                      <span key={n} className="pref-notif-chip">
                        {NOTIF_LABELS[n] ?? n}
                      </span>
                    ))}
                  </div>
                </div>
              )}

              {prefs.favCities && prefs.favCities.length > 0 && (
                <div className="pref-group">
                  <p className="pref-group-label">Ciudades de Interés</p>
                  <div className="pref-chips-list">
                    {prefs.favCities.map((c) => (
                      <span key={c.id} className="pref-chip">
                        ✈ {c.name}, {c.country}
                      </span>
                    ))}
                  </div>
                </div>
              )}
            </>
          )}
        </div>
      )}
    </div>
  )
}
