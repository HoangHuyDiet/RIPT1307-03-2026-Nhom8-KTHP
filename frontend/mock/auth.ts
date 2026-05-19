export default {
  // Mock API Đăng nhập
  'POST /api/auth/login': (req: any, res: any) => {
    const { email, password } = req.body;

    // Giả lập kiểm tra email và password
    if (email === 'admin@gmail.com' && password === '123456') {
      res.send({
        status: 'success',
        message: 'Đăng nhập thành công',
        token: 'mock-jwt-token-123456789',
        user: {
          name: 'Admin User',
          email: 'admin@gmail.com',
          role: 'admin'
        }
      });
    } else {
      res.status(401).send({
        status: 'error',
        message: 'Email hoặc mật khẩu không chính xác'
      });
    }
  },

  // Mock API Đăng ký
  'POST /api/auth/register': (req: any, res: any) => {
    const { email } = req.body;

    if (email === 'admin@gmail.com') {
      res.status(400).send({
        status: 'error',
        message: 'Email này đã được sử dụng'
      });
    } else {
      res.send({
        status: 'success',
        message: 'Đăng ký thành công'
      });
    }
  },
};
