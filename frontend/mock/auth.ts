export default {
  'POST /api/auth/login': (req: any, res: any) => {
    const { email, password } = req.body;

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
        message: 'Đăng ký thành công, mã OTP đang được gửi đến email của bạn'
      });
    }
  },


  'POST /api/auth/verify-otp': (req: any, res: any) => {
    const { email, otp, type } = req.body;
    if (otp === '123456') {
      if (type === 'login') {
        res.send({
          status: 'success',
          token: 'mock-jwt-token-after-otp-123456789',
          user: {
            name: 'User Đăng Nhập OTP',
            email: email,
            role: 'user'
          }
        });
      } else {
        res.send({
          status: 'success',
          message: 'Xác thực tài khoản thành công!'
        });
      }
    } else {
      res.status(400).send({
        status: 'error',
        message: 'Mã OTP không chính xác. Hãy nhập mã: 123456 để thử nghiệm!'
      });
    }
  },
  'POST /api/auth/resend-otp': (req: any, res: any) => {
    res.send({
      status: 'success',
      message: 'Mã OTP mới đã được gửi'
    });
  },



  'POST /api/auth/forgot-password': (req: any, res: any) => {
    const { email } = req.body;

    if (email === 'admin@gmail.com') {
      res.send({
        status: 'success',
        message: 'Yêu cầu đổi mật khẩu thành công, mã OTP đã gửi đến email của bạn!'
      });
    } else {
      res.status(404).send({
        status: 'error',
        message: 'Tài khoản Email này không tồn tại trong hệ thống!'
      });
    }
  },
};
