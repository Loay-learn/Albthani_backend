package com.albthani.currency_exchange.service;

import com.albthani.currency_exchange.model.entity.TransferRequest;
import com.albthani.currency_exchange.response.exception.BusinessException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;


    // ─── إرسال OTP ───
    public void sendOtp(String toEmail, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message, true, "UTF-8");

            helper.setFrom("Albthani<support@albthani.com>");
            helper.setTo(toEmail);
            helper.setSubject("كود التحقق - Currency Exchange");
            helper.setText(buildOtpEmail(otp), true); // true = HTML

            mailSender.send(message);

        } catch (Exception e) {
            throw new BusinessException("فشل إرسال البريد الإلكتروني" + e.getMessage());
        }
    }

    // ─── تصميم الإيميل ───
    private String buildOtpEmail(String otp) {
        return """
                <!DOCTYPE html>
                <html dir="rtl" lang="ar">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <style>
                        @import url('https://fonts.googleapis.com/css2?family=Tajawal:wght@400;700;900&display=swap');
                
                        * { margin: 0; padding: 0; box-sizing: border-box; }
                
                        body {
                            background: #0a0f1e;
                            font-family: 'Tajawal', Arial, sans-serif;
                            padding: 40px 20px;
                        }
                
                        .wrapper {
                            max-width: 520px;
                            margin: auto;
                        }
                
                        .card {
                            background: linear-gradient(145deg, #111827, #1a2235);
                            border-radius: 24px;
                            overflow: hidden;
                            border: 1px solid rgba(212, 175, 55, 0.2);
                            box-shadow: 0 25px 60px rgba(0,0,0,0.5);
                        }
                
                        .header {
                            background: linear-gradient(135deg, #b8962e, #d4af37, #f0d060);
                            padding: 36px 40px;
                            text-align: center;
                        }
                
                        .logo-icon {
                            font-size: 42px;
                            margin-bottom: 8px;
                        }
                
                        .brand {
                            color: #0a0f1e;
                            font-size: 22px;
                            font-weight: 900;
                            letter-spacing: 1px;
                        }
                
                        .body {
                            padding: 40px;
                            text-align: center;
                        }
                
                        .greeting {
                            color: #d4af37;
                            font-size: 13px;
                            font-weight: 700;
                            letter-spacing: 3px;
                            text-transform: uppercase;
                            margin-bottom: 12px;
                        }
                
                        .title {
                            color: #ffffff;
                            font-size: 24px;
                            font-weight: 700;
                            margin-bottom: 8px;
                        }
                
                        .subtitle {
                            color: #6b7280;
                            font-size: 14px;
                            margin-bottom: 36px;
                            line-height: 1.6;
                        }
                
                        .otp-wrapper {
                            background: rgba(212, 175, 55, 0.05);
                            border: 1px solid rgba(212, 175, 55, 0.3);
                            border-radius: 16px;
                            padding: 28px;
                            margin: 0 auto 32px;
                            position: relative;
                        }
                
                        .otp-label {
                            color: #6b7280;
                            font-size: 11px;
                            letter-spacing: 2px;
                            text-transform: uppercase;
                            margin-bottom: 12px;
                        }
                
                        .otp-code {
                            font-size: 48px;
                            font-weight: 900;
                            letter-spacing: 12px;
                            background: linear-gradient(135deg, #b8962e, #f0d060);
                            -webkit-background-clip: text;
                            -webkit-text-fill-color: transparent;
                            background-clip: text;
                        }
                
                        .timer-badge {
                            display: inline-block;
                            background: rgba(239, 68, 68, 0.1);
                            border: 1px solid rgba(239, 68, 68, 0.3);
                            color: #f87171;
                            font-size: 12px;
                            font-weight: 700;
                            padding: 6px 16px;
                            border-radius: 20px;
                            margin-bottom: 32px;
                        }
                
                        .divider {
                            border: none;
                            border-top: 1px solid rgba(255,255,255,0.06);
                            margin: 0 0 28px;
                        }
                
                        .warning {
                            color: #4b5563;
                            font-size: 12px;
                            line-height: 1.7;
                        }
                
                        .footer {
                            background: rgba(0,0,0,0.2);
                            padding: 20px 40px;
                            text-align: center;
                            border-top: 1px solid rgba(255,255,255,0.04);
                        }
                
                        .footer-text {
                            color: #374151;
                            font-size: 11px;
                            letter-spacing: 1px;
                        }
                
                        .gold { color: #d4af37; }
                    </style>
                </head>
                <body>
                    <div class="wrapper">
                        <div class="card">
                            <div class="header">
                                <div class="logo-icon">💱</div>
                                <div class="brand">صرافة البطحاني</div>
                            </div>
                
                            <div class="body">
                                <div class="greeting">رمز التحقق</div>
                                <h1 class="title">تحقق من هويتك</h1>
                                <p class="subtitle">
                                    أدخل الرمز أدناه لإتمام عملية التسجيل
                                </p>
                
                                <div class="otp-wrapper">
                                    <div class="otp-label">رمز التحقق الخاص بك</div>
                                    <div class="otp-code">%s</div>
                                </div>
                
                                <div class="timer-badge">⏱ صالح لمدة 10 دقائق فقط</div>
                
                                <hr class="divider">
                
                                <p class="warning">
                                    إذا لم تطلب هذا الرمز، يُرجى تجاهل هذه الرسالة.<br>
                                    لا تشارك هذا الرمز مع أي شخص.
                                </p>
                            </div>
                
                            <div class="footer">
                                <p class="footer-text">© 2026 صرافة البطحاني — جميع الحقوق محفوظة</p>
                            </div>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(otp);
    }


    // ─── إرسال إشعار اكتمال التحويل (نسخة تدعم تعدد الصور) ───
    public void sendTransferCompleted(String toEmail, TransferRequest transfer) {
        try {
            MimeMessage message = mailSender.createMimeMessage();

            // استخدام true للإشارة إلى أن الرسالة تحتوي على ملحقات أو HTML معقد
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("Albthani <support@albthani.com>");
            helper.setTo(toEmail);
            helper.setSubject("✅ تم اكتمال تحويلك - رقم الطلب #" + transfer.getReferenceNumber());

            // لاحظ هنا: نمرر 'transfer' فقط للميثود التي تبني الـ HTML
            // الميثود ستقرأ قائمة الصور تلقائياً من داخل الكائن
            String emailContent = buildCompletedEmail(transfer);
            helper.setText(emailContent, true);

            mailSender.send(message);

        } catch (Exception e) {
            // طباعة تفصيلية للخطأ في السجلات لمتابعة أي مشكلة في SMTP أو الـ Template
            System.err.println("❌ فشل إرسال إشعار الاكتمال للطلب " + transfer.getReferenceNumber() + ": " + e.getMessage());
        }
    }


    // ─── تصميم إيميل الاكتمال ───
    private String buildCompletedEmail(TransferRequest transfer) {

        StringBuilder imagesHtml = new StringBuilder();
        if (transfer.getConfirmationImages() != null && !transfer.getConfirmationImages().isEmpty()) {
            imagesHtml.append("<div class=\"confirmation-wrapper\">");
            imagesHtml.append("<div class=\"section-label\">صور التأكيد</div>");
            for (String url : transfer.getConfirmationImages()) {
                imagesHtml.append(String.format("""
                            <div style="margin-bottom: 12px;">
                                <img src="%s" alt="صورة التأكيد"
                                     style="width:100%%; border-radius:10px; border:0.5px solid #e5e7eb; display:block;"/>
                            </div>
                        """, url));
            }
            imagesHtml.append("</div>");
        }

        String confirmationBlock = imagesHtml.toString();
        return """
                <!DOCTYPE html>
                <html dir="ltr" lang="ar">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <style>
                        @import url('https://fonts.googleapis.com/css2?family=Tajawal:wght@400;600;700;800&display=swap');
                        * { margin:0; padding:0; box-sizing:border-box; }
                        body { background:#f0f2f5; font-family:'Tajawal',Arial,sans-serif; padding:40px 16px; }
                        .wrapper { max-width:520px; margin:auto; }
                        .card { background:#fff; border-radius:20px; overflow:hidden; border:0.5px solid #e5e7eb; box-shadow:0 4px 24px rgba(0,0,0,0.07); }
                        .header { background:linear-gradient(135deg,#1a1a2e 0%%,#16213e 60%%,#0f3460 100%%); padding:32px 36px; text-align:center; }
                        .logo-box { width:52px; height:52px; background:linear-gradient(135deg,#d4af37,#f0d060); border-radius:14px; display:flex; align-items:center; justify-content:center; margin:0 auto 14px; box-shadow:0 4px 16px rgba(212,175,55,0.35); }
                        .brand { color:#d4af37; font-size:18px; font-weight:700; letter-spacing:1px; }
                        .brand-sub { color:rgba(212,175,55,0.5); font-size:10px; letter-spacing:3px; margin-top:4px; }
                        .body { padding:32px 36px; }
                        .success-ring { width:64px; height:64px; background:#ecfdf5; border:2px solid #a7f3d0; border-radius:50%%; display:flex; align-items:center; justify-content:center; margin:0 auto 16px; }
                        .check-icon { width:28px; height:28px; }
                        .status-label { color:#6b7280; font-size:11px; font-weight:600; letter-spacing:3px; text-transform:uppercase; margin-bottom:6px; text-align:center; }
                        .title { color:#111827; font-size:22px; font-weight:700; text-align:center; margin-bottom:8px; }
                        .subtitle { color:#6b7280; font-size:13px; text-align:center; line-height:1.7; margin-bottom:24px; }
                        .ref-badge { display:block; text-align:center; margin-bottom:24px; }
                        .ref-badge span { display:inline-block; background:#fffbeb; border:1px solid #fcd34d; color:#92400e; font-size:13px; font-weight:700; padding:8px 20px; border-radius:24px; letter-spacing:1px; }
                        .details-card { background:#f9fafb; border:0.5px solid #e5e7eb; border-radius:14px; margin-bottom:20px; overflow:hidden; }
                        .detail-row { display:flex; justify-content:space-between; align-items:center; padding:14px 20px; border-bottom:0.5px solid #f3f4f6; }
                        .detail-row:last-child { border-bottom:none; background:#fffbeb; }
                        .detail-label { color:#6b7280; font-size:12px; }
                        .detail-value { color:#111827; font-size:13px; font-weight:600; }
                        .detail-value-total { color:#92400e; font-size:17px; font-weight:800; }
                        .currency-badge { display:inline-block; font-size:11px; font-weight:700; padding:2px 8px; border-radius:6px; margin-right:6px; }
                        .badge-blue { background:#eff6ff; border:0.5px solid #bfdbfe; color:#1d4ed8; }
                        .badge-green { background:#f0fdf4; border:0.5px solid #bbf7d0; color:#15803d; }
                        .confirmation-wrapper { background:#f9fafb; border:0.5px solid #e5e7eb; border-radius:14px; padding:16px 20px; margin-bottom:20px; }
                        .section-label { color:#6b7280; font-size:11px; letter-spacing:2px; text-transform:uppercase; margin-bottom:12px; text-align:center; }
                        .divider { border:none; border-top:0.5px solid #f3f4f6; margin:20px 0; }
                        .warning { color:#9ca3af; font-size:12px; line-height:1.8; text-align:center; }
                        .footer { background:#f9fafb; border-top:0.5px solid #f3f4f6; padding:16px 36px; text-align:center; }
                        .footer-text { color:#d1d5db; font-size:11px; letter-spacing:1px; }
                    </style>
                </head>
                <body>
                <div class="wrapper">
                  <div class="card">
                
                    <div class="header">
                      <div class="logo-box">
                        <svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="#1a1a2e" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
                          <path d="M12 2L2 7l10 5 10-5-10-5z"/>
                          <path d="M2 17l10 5 10-5"/>
                          <path d="M2 12l10 5 10-5"/>
                        </svg>
                      </div>
                      <div class="brand">صرافة البطحاني</div>
                      <div class="brand-sub">EXCHANGE PLATFORM</div>
                    </div>
                
                    <div class="body">
                      <div class="success-ring">
                        <svg class="check-icon" viewBox="0 0 24 24" fill="none" stroke="#059669" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                          <path d="M20 6L9 17l-5-5"/>
                        </svg>
                      </div>
                      <div class="status-label">تمّت العملية بنجاح</div>
                      <h1 class="title">تحويل مكتمل ✓</h1>
                      <p class="subtitle">تم تنفيذ طلب التحويل بنجاح.<br>يمكنك متابعة التفاصيل أدناه.</p>
                
                      <div class="ref-badge">
                        <span>رقم المرجع: #%s</span>
                      </div>
                
                      <div class="details-card">
                        <div class="detail-row">
                          <span class="detail-label">من عملة</span>
                          <span class="detail-value">
                            <span class="currency-badge badge-blue">%s</span>%s
                          </span>
                        </div>
                        <div class="detail-row">
                          <span class="detail-label">إلى عملة</span>
                          <span class="detail-value">
                            <span class="currency-badge badge-green">%s</span>%s
                          </span>
                        </div>
                        <div class="detail-row">
                          <span class="detail-label">المبلغ المحوَّل</span>
                          <span class="detail-value">%s %s</span>
                        </div>
                        <div class="detail-row">
                          <span class="detail-label">سعر الصرف المُطبَّق</span>
                          <span class="detail-value">%s</span>
                        </div>
                        <div class="detail-row">
                          <span class="detail-label">المبلغ المستلَم بعد التحويل</span>
                          <span class="detail-value-total">%s %s</span>
                        </div>
                      </div>
                
                      %s
                
                      <hr class="divider">
                      <p class="warning">
                        إذا لم تقم بهذه العملية، يُرجى التواصل مع الدعم فوراً.<br>
                        فريق الدعم متاح على مدار الساعة.
                      </p>
                    </div>
                
                    <div class="footer">
                      <p class="footer-text">© 2026 منصة العملات — جميع الحقوق محفوظة</p>
                    </div>
                  </div>
                </div>
                </body>
                </html>
                """.formatted(
                transfer.getReferenceNumber(),
                transfer.getFromCurrency(), getCurrencyNameAr(transfer.getFromCurrency()),
                transfer.getToCurrency(), getCurrencyNameAr(transfer.getToCurrency()),
                transfer.getAmount(), getCurrencyNameAr(transfer.getFromCurrency()),
                transfer.getRateUsed(),
                transfer.getConvertedAmount(), getCurrencyNameAr(transfer.getToCurrency()),
                confirmationBlock
        );
    }

    private String getCurrencyNameAr(String currency) {
        return switch (currency) {
            case "USD" -> "دولار أمريكي";
            case "SAR" -> "ريال سعودي";
            case "AED" -> "درهم إماراتي";
            case "KWD" -> "دينار كويتي";
            case "EGP" -> "جنيه مصري";
            case "EUR" -> "يورو";
            case "GBP" -> "جنيه إسترليني";
            case "SDG" -> "جنيه سوداني";
            case "QAR" -> "ريال قطري";
            default -> currency;
        };
    }
}
