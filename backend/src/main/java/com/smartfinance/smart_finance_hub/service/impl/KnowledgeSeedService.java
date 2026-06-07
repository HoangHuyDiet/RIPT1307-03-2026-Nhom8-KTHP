package com.smartfinance.smart_finance_hub.service.impl;

import com.smartfinance.smart_finance_hub.config.condition.RagEnabledCondition;
import com.smartfinance.smart_finance_hub.dto.response.RagStatusDTO;
import com.smartfinance.smart_finance_hub.entity.KnowledgeDocument;
import com.smartfinance.smart_finance_hub.enums.KnowledgeStatus;
import com.smartfinance.smart_finance_hub.repository.KnowledgeDocumentRepository;
import com.smartfinance.smart_finance_hub.service.KnowledgeIngestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

@Service
@Conditional(RagEnabledCondition.class)
@RequiredArgsConstructor
@Slf4j
public class KnowledgeSeedService implements ApplicationRunner {

    private final KnowledgeDocumentRepository documentRepository;
    private final KnowledgeIngestionService ingestionService;

    @Override
    public void run(ApplicationArguments args) {
        try {
            log.info("Starting RAG knowledge seed");
            boolean changed = seedDocuments();
            requestRebuildIfNeeded(changed);
            log.info("RAG knowledge seed completed");
        } catch (Exception e) {
            log.warn("RAG knowledge seed failed; application will keep running: {}", e.getMessage());
        }
    }

    private boolean seedDocuments() {
        boolean changed = false;
        changed |= seedIfNotExists("quy-tac-50-30-20",
            "Quy tac phan bo ngan sach 50/30/20",
            "Tai chinh ca nhan",
            "vi",
            """
            QUY TAC PHAN BO NGAN SACH 50/30/20

            Quy tac 50/30/20 la mot phuong phap quan ly tai chinh ca nhan pho bien:

            1. 50% THU NHAP - NHU CAU THIET YEU
            Bao gom: tien thue nha hoac tra gop, tien an, hoa don dien nuoc,
            bao hiem, di lai va cac chi phi co dinh hang thang.

            2. 30% THU NHAP - MONG MUON
            Bao gom: an nha hang, giai tri, du lich, mua sam khong thiet yeu,
            dich vu streaming, the thao va so thich ca nhan.

            3. 20% THU NHAP - TIET KIEM VA DAU TU
            Bao gom: quy du phong khan cap toi thieu 3-6 thang chi tieu,
            tiet kiem cho muc tieu dai han, dau tu va quy huu tri.

            LUU Y:
            - Ty le co the dieu chinh theo hoan canh ca nhan.
            - Nguoi co thu nhap thap co the can tang ty le nhu cau thiet yeu.
            - Khi thu nhap tang, nen tang ty le tiet kiem.
            """
        );

        changed |= seedIfNotExists("quy-du-phong-khan-cap",
            "Huong dan xay dung quy du phong khan cap",
            "Tai chinh ca nhan",
            "vi",
            """
            QUY DU PHONG KHAN CAP

            Quy du phong khan cap la so tien du tru cho cac tinh huong bat ngo
            nhu mat viec, benh tat hoac sua chua khan cap.

            MUC DE XUAT:
            - Toi thieu: 3 thang chi tieu thiet yeu.
            - Khuyen nghi: 6 thang chi tieu thiet yeu.
            - An toan: 9-12 thang chi tieu thiet yeu cho freelancer hoac tu kinh doanh.

            CACH XAY DUNG:
            1. Tinh tong chi tieu thiet yeu hang thang.
            2. Nhan voi so thang muc tieu.
            3. Trich 10-20% thu nhap hang thang cho quy.
            4. Gui vao tai khoan tiet kiem rieng, de rut.

            NGUYEN TAC:
            - Chi dung cho truong hop khan cap that su.
            - Bo sung lai ngay sau khi su dung.
            - Khong dau tu quy nay vao tai san rui ro cao.
            """
        );

        changed |= seedIfNotExists("quan-ly-no",
            "Chien luoc quan ly no hieu qua",
            "Tai chinh ca nhan",
            "vi",
            """
            CHIEN LUOC QUAN LY NO HIEU QUA

            1. PHUONG PHAP SNOWBALL
            - Tra no nho nhat truoc.
            - Sau khi tra xong, chuyen so tien do sang khoan no tiep theo.
            - Uu diem: tao dong luc tam ly, thay ket qua nhanh.

            2. PHUONG PHAP AVALANCHE
            - Tra no lai suat cao nhat truoc.
            - Tiet kiem tong lai phai tra nhieu hon.
            - Uu diem: toi uu ve mat tai chinh.

            3. NGUYEN TAC CHUNG:
            - Tong no tra hang thang khong nen vuot 36% thu nhap.
            - Luon tra it nhat muc toi thieu cho tat ca khoan no.
            - Tranh vay moi de tra no cu, tru khi tai cau truc lai suat thap hon.
            - No the tin dung nen tra toan bo moi thang.

            4. KHI CAN TU VAN CHUYEN GIA:
            - Tong no vuot 50% thu nhap hang nam.
            - Khong the tra muc toi thieu.
            - Bi goi doi no lien tuc.
            """
        );

        return changed;
    }

    private boolean seedIfNotExists(String sourceKey, String title, String category,
                                    String language, String content) {
        if (documentRepository.findBySourceKey(sourceKey).isPresent()) {
            return false;
        }

        KnowledgeDocument doc = KnowledgeDocument.builder()
            .sourceKey(sourceKey)
            .title(title)
            .category(category)
            .language(language)
            .sourceType("SEED")
            .sourceName("Smart Finance Hub")
            .content(content.trim())
            .status(KnowledgeStatus.APPROVED)
            .version("v1.0")
            .build();
        documentRepository.save(doc);
        log.info("Seeded RAG document: {}", title);
        return true;
    }

    private void requestRebuildIfNeeded(boolean changed) {
        RagStatusDTO status = ingestionService.getStatus();
        if (changed || status == null || !status.isRagAvailable()) {
            boolean accepted = ingestionService.requestRebuild();
            log.info("RAG rebuild requested after seed: accepted={}, changed={}", accepted, changed);
        }
    }
}
