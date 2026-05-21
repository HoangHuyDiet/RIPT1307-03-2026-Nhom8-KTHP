package com.smartfinance.smart_finance_hub.service.impl;

import com.smartfinance.smart_finance_hub.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

  private final JavaMailSender javaMailSender;
  private final SpringTemplateEngine templateEngine;

  @Override
  public void sendOtpEmail(String toEmail, String otpCode) {
    try {
      MimeMessage message = javaMailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setTo(toEmail);
      helper.setSubject("Xác thực tài khoản Smart Finance Hub");

      Context thymeleafContext = new Context();
      thymeleafContext.setVariable("otpCode", otpCode);

      String htmlContent = templateEngine.process("otp-email-template", thymeleafContext);

      helper.setText(htmlContent, true);

      javaMailSender.send(message);
      log.info("Đã biên dịch và gửi email OTP qua Thymeleaf thành công đến: {}", toEmail);

    } catch (MessagingException e) {
      log.error("Lỗi hệ thống khi gửi email OTP đến {}: {}", toEmail, e.getMessage(), e);
      throw new RuntimeException("Không thể gửi email OTP, vui lòng thử lại sau!");
    }
  }
}
