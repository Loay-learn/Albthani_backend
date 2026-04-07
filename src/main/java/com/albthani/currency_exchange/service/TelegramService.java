package com.albthani.currency_exchange.service;

import com.albthani.currency_exchange.model.entity.TransferRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TelegramService {

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.chat.id}")
    private String chatId;

    private final RestTemplate restTemplate = new RestTemplate();

    // ─── إرسال إشعار طلب جديد ───
    public void sendNewTransferNotification(TransferRequest transfer) {

        String message = """
        🔔 طلب تحويل جديد!
        
        👤 العميل: %s
        التحويل: %s ← %s
        المبلغ: %s
        بعد التحويل: %s
        تم التحويل في: %s - %s
        واتساب: %s
        بنك العميل: %s
        اسم الحساب: %s
        رقم الحساب: %s
        """.formatted(
                transfer.getUser().getFullName(),
                transfer.getToCurrency(),
                transfer.getFromCurrency(),
                transfer.getAmount(),
                transfer.getConvertedAmount(),
                transfer.getBankAccount().getBankName(),
                transfer.getBankAccount().getAccountNumber(),
                transfer.getWhatsappNumber(),
                transfer.getCustomerBankName(),
                transfer.getCustomerAccountName(),
                transfer.getCustomerAccountNumber()
        );

        sendMessage(message);
    }

    // ─── إرسال إشعار تحديث الحالة ───
    public void sendStatusUpdateNotification(TransferRequest transfer) {
        String emoji = switch (transfer.getStatus()) {
            case COMPLETED -> "✅";
            case REJECTED  -> "❌";
            default        -> "⏳";
        };

        String message = """
            %s تحديث طلب #%d
            
            📊 الحالة: %s
            👤 العميل: %s
            👨‍💼 الأدمن: %s
            """.formatted(
                emoji,
                transfer.getReferenceNumber(),
                transfer.getStatus(),
                transfer.getUser().getFullName(),
                transfer.getProcessedBy().getFullName()
        );

        sendMessage(message);
    }

    // ─── إرسال الرسالة ───
    private void sendMessage(String message) {
        try {
            String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";

            // ✅ أرسل البيانات في الـ Body
            Map<String, String> body = new HashMap<>();
            body.put("chat_id", chatId);
            body.put("text", message);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

            restTemplate.postForObject(url, entity, String.class);

        } catch (Exception e) {
            System.err.println("فشل إرسال إشعار التيليجرام: " + e.getMessage());
        }
    }
}
