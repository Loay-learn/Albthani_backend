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


    // ─── إرسال إشعار اكتمال التحويل ───
    public void sendTransferCompleted(String toEmail, TransferRequest transfer, String confirmationImageUrl) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("Albthani<support@albthani.com>");
            helper.setTo(toEmail);
            helper.setSubject("✅ تم اكتمال تحويلك - صرافة البطحاني");
            helper.setText(buildCompletedEmail(transfer, confirmationImageUrl), true);

            mailSender.send(message);
        } catch (Exception e) {
            // ما نوقف المشروع لو فشل الإيميل
            System.err.println("فشل إرسال إشعار الاكتمال: " + e.getMessage());
        }
    }

    // ─── تصميم إيميل الاكتمال ───
    private String buildCompletedEmail(TransferRequest transfer, String confirmationImageUrl) {
        String confirmationBlock = confirmationImageUrl != null ? """
        <div class="confirmation-wrapper">
            <div class="section-label">إشعار التأكيد</div>
            <img src="%s" alt="إشعار التأكيد" style="width:100%%;border-radius:12px;border:1px solid rgba(212,175,55,0.2);"/>
        </div>
    """.formatted(confirmationImageUrl) : "";

        return """
    <!DOCTYPE html>
    <html dir="rtl" lang="ar">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <style>
            @import url('https://fonts.googleapis.com/css2?family=Tajawal:wght@400;700;900&display=swap');
            * { margin:0; padding:0; box-sizing:border-box; }
            body { background:#0a0f1e; font-family:'Tajawal',Arial,sans-serif; padding:40px 20px; }
            .wrapper { max-width:520px; margin:auto; }
            .card { background:linear-gradient(145deg,#111827,#1a2235); border-radius:24px; overflow:hidden; border:1px solid rgba(212,175,55,0.2); box-shadow:0 25px 60px rgba(0,0,0,0.5); }
            .header { background:linear-gradient(135deg,#b8962e,#d4af37,#f0d060); padding:36px 40px; text-align:center; }
            .logo-icon { font-size:42px; margin-bottom:8px; }
            .brand { color:#0a0f1e; font-size:22px; font-weight:900; letter-spacing:1px; }
            .body { padding:32px 40px; text-align:center; }
            .success-icon { width:72px; height:72px; background:linear-gradient(135deg,#059669,#10b981); border-radius:50%%; display:flex; align-items:center; justify-content:center; margin:0 auto 20px; font-size:32px; box-shadow:0 0 30px rgba(16,185,129,0.3); }
            .greeting { color:#d4af37; font-size:13px; font-weight:700; letter-spacing:3px; text-transform:uppercase; margin-bottom:12px; }
            .title { color:#ffffff; font-size:24px; font-weight:700; margin-bottom:8px; }
            .subtitle { color:#6b7280; font-size:14px; margin-bottom:28px; line-height:1.6; }
            .ref-badge { display:inline-block; background:rgba(212,175,55,0.1); border:1px solid rgba(212,175,55,0.3); color:#d4af37; font-size:14px; font-weight:900; padding:8px 20px; border-radius:20px; margin-bottom:28px; letter-spacing:1px; }
            .details-card { background:rgba(255,255,255,0.03); border:1px solid rgba(255,255,255,0.06); border-radius:16px; padding:20px; margin-bottom:20px; text-align:right; }
            .detail-row { display:flex; justify-content:space-between; align-items:center; padding:10px 0; border-bottom:1px solid rgba(255,255,255,0.04); }
            .detail-row:last-child { border-bottom:none; padding-bottom:0; }
            .detail-label { color:#6b7280; font-size:12px; }
            .detail-value { color:#e5e7eb; font-size:13px; font-weight:700; }
            .amount-highlight { color:#d4af37; font-size:16px; font-weight:900; }
            .section-label { color:#6b7280; font-size:11px; letter-spacing:2px; text-transform:uppercase; margin-bottom:12px; text-align:center; }
            .confirmation-wrapper { background:rgba(255,255,255,0.02); border:1px solid rgba(212,175,55,0.15); border-radius:16px; padding:20px; margin-bottom:20px; }
            .divider { border:none; border-top:1px solid rgba(255,255,255,0.06); margin:20px 0; }
            .warning { color:#4b5563; font-size:12px; line-height:1.7; }
            .footer { background:rgba(0,0,0,0.2); padding:20px 40px; text-align:center; border-top:1px solid rgba(255,255,255,0.04); }
            .footer-text { color:#374151; font-size:11px; letter-spacing:1px; }
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
                    <div class="success-icon">✅</div>
                    <div class="greeting">تم التحويل بنجاح</div>
                    <h1 class="title">طلبك مكتمل!</h1>
                    <p class="subtitle">تم معالجة طلب التحويل الخاص بك بنجاح.<br>تفاصيل العملية أدناه.</p>

                    <div class="ref-badge">رقم الطلب: #%d</div>

                    <!-- تفاصيل التحويل -->
                    <div class="details-card">
                        <div class="detail-row">
                            <span class="detail-label">من عملة</span>
                            <span class="detail-value">%s %s</span>
                        </div>
                        <div class="detail-row">
                            <span class="detail-label">إلى عملة</span>
                            <span class="detail-value">%s %s</span>
                        </div>
                        <div class="detail-row">
                            <span class="detail-label">المبلغ الأصلي</span>
                            <span class="detail-value">%s %s</span>
                        </div>
                        <div class="detail-row">
                            <span class="detail-label">سعر الصرف</span>
                            <span class="detail-value">%s</span>
                        </div>
                        <div class="detail-row">
                            <span class="detail-label">المبلغ المحوّل</span>
                            <span class="amount-highlight">%s %s</span>
                        </div>
                        <div class="detail-row">
                            <span class="detail-label">حساب العميل</span>
                            <span class="detail-value">%s</span>
                        </div>
                        <div class="detail-row">
                            <span class="detail-label">بنك العميل</span>
                            <span class="detail-value">%s</span>
                        </div>
                    </div>

                    %s

                    <hr class="divider">
                    <p class="warning">
                        إذا كان لديك أي استفسار، تواصل معنا عبر الواتساب.<br>
                        شكراً لثقتك بصرافة البطحاني.
                    </p>
                </div>

                <div class="footer">
                    <p class="footer-text">© 2026 صرافة البطحاني — جميع الحقوق محفوظة</p>
                </div>
            </div>
        </div>
    </body>
    </html>
    """.formatted(
                transfer.getReferenceNumber(),
                transfer.getFromCurrency(), getFlag(transfer.getFromCurrency()),
                transfer.getToCurrency(), getFlag(transfer.getToCurrency()),
                transfer.getAmount(), transfer.getFromCurrency(),
                transfer.getRateUsed(),
                transfer.getConvertedAmount(), transfer.getToCurrency(),
                transfer.getCustomerAccountNumber() != null ? transfer.getCustomerAccountNumber() : "—",
                transfer.getCustomerBankName() != null ? transfer.getCustomerBankName() : "—",
                confirmationBlock
        );
    }

    private String getFlag(String currency) {
        return switch (currency) {
            case "USD" -> "🇺🇸"; case "SAR" -> "🇸🇦"; case "AED" -> "🇦🇪";
            case "KWD" -> "🇰🇼"; case "EGP" -> "🇪🇬"; case "EUR" -> "🇪🇺";
            case "GBP" -> "🇬🇧"; case "SDG" -> "🇸🇩"; case "QAR" -> "🇶🇦";
            default -> "🏳️";
        };
    }
}
