// Login page logic
document.addEventListener('DOMContentLoaded', () => {
    // If already logged in, redirect to main page
    if (isAuthenticated()) {
        window.location.href = 'index.html';
        return;
    }
    
    const loginForm = document.getElementById('loginForm');
    const loginBtn = document.getElementById('loginBtn');
    const loginBtnText = document.getElementById('loginBtnText');
    const loginBtnSpinner = document.getElementById('loginBtnSpinner');
    const errorMessage = document.getElementById('errorMessage');
    
    loginForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        
        const username = document.getElementById('username').value.trim();
        const password = document.getElementById('password').value;
        
        if (!username || !password) {
            showError('Vui lòng nhập đầy đủ thông tin');
            return;
        }
        
        // Show loading state
        loginBtn.disabled = true;
        loginBtnText.style.display = 'none';
        loginBtnSpinner.style.display = 'inline';
        errorMessage.style.display = 'none';
        
        try {
            const response = await fetch(`${API_BASE}/api/auth/login`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ username, password })
            });
            
            const data = await response.json();
            
            if (response.ok && data.success) {
                // Save auth data
                saveAuth(data.token, {
                    employeeId: data.employeeId,
                    username: data.username,
                    fullName: data.fullName
                });
                
                // Redirect to main page
                window.location.href = 'index.html';
            } else {
                showError(data.error || 'Đăng nhập thất bại');
            }
        } catch (error) {
            showError('Lỗi kết nối. Vui lòng thử lại.');
            console.error('Login error:', error);
        } finally {
            loginBtn.disabled = false;
            loginBtnText.style.display = 'inline';
            loginBtnSpinner.style.display = 'none';
        }
    });
    
    function showError(message) {
        errorMessage.textContent = message;
        errorMessage.style.display = 'block';
    }
});


