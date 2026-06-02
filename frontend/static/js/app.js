const API = '';
let token = localStorage.getItem('token');
let userId = localStorage.getItem('userId');

function showMsg(text, type) {
    const el = document.getElementById('msg');
    el.textContent = text;
    el.className = 'msg ' + type;
    setTimeout(() => {
        el.className = 'msg';
    }, 3000);
}

async function api(url, method = 'GET', body = null) {
    const opts = { method, headers: { 'Content-Type': 'application/json' } };
    if (token) opts.headers.Authorization = 'Bearer ' + token;
    if (body) opts.body = JSON.stringify(body);
    const res = await fetch(API + url, opts);
    return res.json();
}

async function register() {
    const r = await api('/api/user/register', 'POST', {
        username: document.getElementById('username').value,
        password: document.getElementById('password').value
    });
    if (r.code === 200) {
        token = r.data.token;
        userId = r.data.userId;
        localStorage.setItem('token', token);
        localStorage.setItem('userId', userId);
        showMsg('Register success', 'success');
        showLoggedIn();
    } else {
        showMsg(r.msg, 'error');
    }
}

async function login() {
    const r = await api('/api/user/login', 'POST', {
        username: document.getElementById('username').value,
        password: document.getElementById('password').value
    });
    if (r.code === 200) {
        token = r.data.token;
        userId = r.data.userId;
        localStorage.setItem('token', token);
        localStorage.setItem('userId', userId);
        showMsg('Login success', 'success');
        showLoggedIn();
    } else {
        showMsg(r.msg, 'error');
    }
}

function logout() {
    token = null;
    userId = null;
    localStorage.clear();
    location.reload();
}

function showLoggedIn() {
    document.getElementById('auth-section').style.display = 'none';
    document.getElementById('product-section').style.display = 'block';
    document.getElementById('order-section').style.display = 'block';
    document.getElementById('user-info').innerHTML = '<button onclick="logout()" class="btn-secondary">Logout</button>';
    loadProducts();
}

async function loadProducts() {
    const r = await api('/api/product/list?page=1&size=20');
    if (r.code === 200) {
        document.getElementById('product-list').innerHTML = r.data.map((p) => `
            <div class="product-item">
                <div class="product-info">
                    <h3>${p.name}</h3>
                    <p>${p.description || ''}</p>
                    <span class="original-price">Price $${p.price}</span>
                    <span class="price">Sale $${p.seckillPrice || p.price}</span>
                </div>
                <button onclick="doSeckill(${p.id})">Buy Now</button>
            </div>
        `).join('');
    }
}

async function doSeckill(productId) {
    const r = await api('/api/order/seckill', 'POST', { productId, userId: Number(userId) });
    if (r.code === 200) {
        showMsg('Order created: ' + r.data, 'success');
        loadOrders();
    } else {
        showMsg(r.msg, 'error');
    }
}

async function loadOrders() {
    const r = await api('/api/order/user/' + userId);
    if (r.code === 200) {
        const statusMap = { 0: 'Unpaid', 1: 'Paid', 2: 'Cancelled', 3: 'Timed Out' };
        document.getElementById('order-list').innerHTML = r.data.map((o) => `
            <div class="order-item">
                <strong>${o.productName}</strong> - $${o.orderPrice}
                <span class="status-${o.status}">${statusMap[o.status]}</span>
                ${o.status === 0 ? `<button onclick="payOrder(${o.id})">Pay</button><button onclick="cancelOrder(${o.id})" class="btn-secondary">Cancel</button>` : ''}
            </div>
        `).join('') || '<p>No orders yet</p>';
    }
}

async function payOrder(orderId) {
    const r = await api('/api/order/' + orderId + '/pay', 'POST');
    if (r.code === 200) {
        showMsg('Payment success', 'success');
        loadOrders();
    } else {
        showMsg(r.msg, 'error');
    }
}

async function cancelOrder(orderId) {
    const r = await api('/api/order/' + orderId + '/cancel', 'POST');
    if (r.code === 200) {
        showMsg('Order cancelled', 'success');
        loadOrders();
    } else {
        showMsg(r.msg, 'error');
    }
}

if (token && userId) {
    showLoggedIn();
}
