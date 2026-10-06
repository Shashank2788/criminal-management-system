const $ = s => document.querySelector(s);
const esc = v => String(v ?? '').replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
const csrf = () => decodeURIComponent((document.cookie.match(/XSRF-TOKEN=([^;]+)/) || [])[1] || '');
const en = a => a.map(v => [v, v.replace(/_/g, ' ').toLowerCase()]);

async function api(url, method = 'GET', body) {
  const r = await fetch(url, { method, headers: { 'Content-Type': 'application/json', 'X-XSRF-TOKEN': csrf() }, body: body ? JSON.stringify(body) : undefined });
  if (r.status === 401) { location.href = 'login.html'; throw new Error('Please sign in again.'); }
  if (r.status === 403) throw new Error('Your role does not allow this action.');
  if (!r.ok) { const e = await r.json().catch(() => ({})); throw new Error(e.message || `Request failed (${r.status})`); }
  return r.status === 204 ? null : r.json();
}

let view = 'dashboard', data = [], criminals = [], me = {};

const cfg = {
  criminals: {
    url: '/api/criminals', title: 'Criminals', noun: 'criminal',
    cols: [['fullName', 'Name'], ['alias', 'Alias'], ['age', 'Age'], ['gender', 'Gender'], ['status', 'Status', 'badge']],
    fields: () => [
      { k: 'fullName', l: 'Full name', req: 1 }, { k: 'alias', l: 'Alias' },
      { k: 'age', l: 'Age', t: 'number' }, { k: 'gender', l: 'Gender', opts: en(['Male', 'Female', 'Other']) },
      { k: 'address', l: 'Last known address' }, { k: 'status', l: 'Status', opts: en(['AT_LARGE', 'IN_CUSTODY', 'RELEASED']) }
    ]
  },
  cases: {
    url: '/api/cases', title: 'Cases', noun: 'case',
    cols: [['caseNumber', 'Case no.'], ['crimeType', 'Crime'], ['criminal', 'Criminal', r => esc(r.criminal?.fullName)],
           ['dateOfCrime', 'Date'], ['location', 'Location'], ['status', 'Status', 'badge']],
    fields: () => [
      { k: 'caseNumber', l: 'Case number', req: 1 }, { k: 'crimeType', l: 'Crime type', req: 1 },
      { k: 'criminalId', l: 'Criminal', req: 1, opts: criminals.map(c => [c.id, c.fullName]) },
      { k: 'dateOfCrime', l: 'Date of crime', t: 'date' }, { k: 'location', l: 'Location' },
      { k: 'status', l: 'Status', opts: en(['OPEN', 'UNDER_INVESTIGATION', 'CLOSED']) },
      { k: 'description', l: 'Description', area: 1 }
    ]
  }
};

function toast(msg) {
  const t = $('#toast'); t.textContent = msg; t.classList.add('show');
  setTimeout(() => t.classList.remove('show'), 2800);
}

async function load() {
  if (view === 'dashboard') return renderDash();
  const q = $('#search').value.trim();
  try {
    if (view === 'cases') criminals = await api('/api/criminals');
    data = await api(cfg[view].url + (q ? `?q=${encodeURIComponent(q)}` : ''));
    render();
  } catch (e) { toast(e.message); }
}

function render() {
  const c = cfg[view];
  const head = c.cols.map(x => `<th>${x[1]}</th>`).join('') + '<th></th>';
  const rows = data.map(r => '<tr>' + c.cols.map(([k, , f]) => {
    const v = r[k];
    if (f === 'badge') return `<td><span class="badge ${esc(String(v).toLowerCase())}">${esc(String(v).replace(/_/g, ' ').toLowerCase())}</span></td>`;
    return `<td>${typeof f === 'function' ? f(r) : esc(v)}</td>`;
  }).join('') + `<td><button class="link" data-act="edit" data-id="${r.id}">Edit</button>${me.role === 'ADMIN' ? `<button class="link del" data-act="del" data-id="${r.id}">Delete</button>` : ''}</td></tr>`).join('');
  $('#table').innerHTML = `<thead><tr>${head}</tr></thead><tbody>${rows ||
    `<tr><td class="empty" colspan="${c.cols.length + 1}">No ${c.title.toLowerCase()} found. Add the first record to get started.</td></tr>`}</tbody>`;
}

function openForm(item) {
  const c = cfg[view], f = $('#form');
  f.innerHTML = `<h2>${item ? 'Edit' : 'Add'} ${c.noun}</h2>` + c.fields().map(x => {
    const val = x.k === 'criminalId' ? item?.criminal?.id : item?.[x.k];
    const attrs = `name="${x.k}" ${x.req ? 'required' : ''}`;
    const input = x.opts
      ? `<select ${attrs}>${x.opts.map(([v, l]) => `<option value="${esc(v)}" ${String(v) === String(val ?? '') ? 'selected' : ''}>${esc(l)}</option>`).join('')}</select>`
      : x.area ? `<textarea ${attrs} rows="3">${esc(val)}</textarea>`
      : `<input ${attrs} type="${x.t || 'text'}" value="${esc(val)}">`;
    return `<label>${x.l}${input}</label>`;
  }).join('') + `<div class="actions"><button type="button" class="ghost" id="cancel">Cancel</button><button class="primary">Save ${c.noun}</button></div>`;

  $('#cancel').onclick = () => $('#dlg').close();
  f.onsubmit = async e => {
    e.preventDefault();
    const b = Object.fromEntries(new FormData(f));
    for (const k in b) if (b[k] === '') b[k] = null;
    if (b.age != null) b.age = +b.age;
    if (view === 'cases') { b.criminal = { id: +b.criminalId }; delete b.criminalId; }
    try {
      await api(item ? `${c.url}/${item.id}` : c.url, item ? 'PUT' : 'POST', b);
      $('#dlg').close(); toast(`${c.noun[0].toUpperCase() + c.noun.slice(1)} saved`); load();
    } catch (err) { toast(err.message); }
  };
  $('#dlg').showModal();
}

document.querySelectorAll('nav button').forEach(b => b.onclick = () => {
  view = b.dataset.view;
  document.querySelectorAll('nav button').forEach(x => x.classList.toggle('active', x === b));
  showView();
  $('#search').value = '';
  load();
});

$('#add').onclick = async () => {
  if (view === 'cases' && !criminals.length) return toast('Add a criminal record before creating a case.');
  openForm();
};

let timer;
$('#search').oninput = () => { clearTimeout(timer); timer = setTimeout(load, 250); };

$('#table').onclick = async e => {
  const b = e.target.closest('button[data-act]'); if (!b) return;
  const item = data.find(r => r.id == b.dataset.id);
  if (b.dataset.act === 'edit') return openForm(item);
  const warn = view === 'criminals' ? ' This also deletes all of their cases.' : '';
  if (!confirm(`Delete this ${cfg[view].noun}?${warn}`)) return;
  try { await api(`${cfg[view].url}/${item.id}`, 'DELETE'); toast('Deleted'); load(); } catch (err) { toast(err.message); }
};

function showView() {
  const d = view === 'dashboard';
  $('#title').textContent = d ? 'Dashboard' : cfg[view].title;
  if (!d) $('#add').textContent = `Add ${cfg[view].noun}`;
  ['#search', '#add', '.table-wrap'].forEach(x => $(x).hidden = d);
  $('#dash').hidden = !d;
}

async function renderDash() {
  try {
    const s = await api('/api/stats');
    const tile = (n, l) => `<div class="tile"><b>${n}</b><span>${l}</span></div>`;
    const types = Object.entries(s.casesByCrimeType);
    const max = Math.max(1, ...types.map(t => t[1]));
    $('#dash').innerHTML = `<div class="tiles">${tile(s.totalCriminals, 'Criminals on record')}${tile(s.atLarge, 'At large')}${tile(s.inCustody, 'In custody')}${tile(s.totalCases, 'Total cases')}${tile(s.openCases, 'Open cases')}${tile(s.underInvestigation, 'Under investigation')}${tile(s.closedCases, 'Closed cases')}</div>
      <div class="panel"><h3>Cases by crime type</h3>${types.map(([k, v]) => `<div class="bar"><span>${esc(k)}</span><i style="width:${v / max * 100}%"></i><em>${v}</em></div>`).join('') || '<p>No cases recorded yet. Add a case to see the breakdown.</p>'}</div>`;
  } catch (e) { toast(e.message); }
}

$('#logout').onclick = async () => { await api('/logout', 'POST').catch(() => {}); location.href = 'login.html'; };

(async () => {
  try { me = await api('/api/me'); $('#who').textContent = `${me.username} (${me.role.toLowerCase()})`; } catch {}
  showView(); load();
})();
