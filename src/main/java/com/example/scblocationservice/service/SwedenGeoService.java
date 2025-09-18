package com.example.scblocationservice.service;

import com.example.scblocationservice.dto.KommunDto;
import com.example.scblocationservice.dto.LanDto;
import jakarta.annotation.PostConstruct;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;


@Service
public class SwedenGeoService {

    private final RestClient http;
    private final String scbXlsUrl;

    private final Map<String, LanDto> lanByCode = new TreeMap<>();
    private final Map<String, List<KommunDto>> kommunByLan = new HashMap<>();

    public SwedenGeoService(RestClient.Builder builder,
                            @Value("${scb.kommun-lan-xls-url}") String scbXlsUrl) {
        this.http = builder.build();
        this.scbXlsUrl = scbXlsUrl;
    }

    @PostConstruct
    public void init() {
        byte[] xls = download(scbXlsUrl);
        parseScbWorkbook(xls);

        if (lanByCode.isEmpty()) {
            throw new IllegalStateException("No län loaded from SCB file.");
        }

        for (Map.Entry<String, List<KommunDto>> e : kommunByLan.entrySet()) {
            String lanCode = e.getKey();
            String lanName = Optional.ofNullable(lanByCode.get(lanCode)).map(LanDto::getLanName).orElse(null);
            if (lanName != null) {
                List<KommunDto> updated = e.getValue().stream()
                        .map(m -> new KommunDto(m.getKommunCode(), m.getKommunName(), lanCode, lanName))
                        .sorted(Comparator.comparing(KommunDto::getKommunName))
                        .toList();
                kommunByLan.put(lanCode, updated);
            }
        }
    }

    public List<LanDto> getAllLan() {
        return new ArrayList<>(lanByCode.values());
    }

    public List<KommunDto> getKommunerByLan(String lanCode) {
        String normalized = normalizeLanCode(lanCode);
        return kommunByLan.getOrDefault(normalized, Collections.emptyList());
    }

    private byte[] download(String url) {
        ByteArrayResource res = http.get().uri(url).retrieve().body(ByteArrayResource.class);
        if (res == null || res.getByteArray().length == 0) {
            throw new IllegalStateException("Empty response from: " + url);
        }
        return res.getByteArray();
    }

    private void parseScbWorkbook(byte[] data) {
        try (Workbook wb = openWorkbook(data)) {
            DataFormatter fmt = new DataFormatter(Locale.ROOT);
            Pattern digits = Pattern.compile("^\\d+$");

            scanWorkbook(wb, fmt, digits, 0, 1);
            if (lanByCode.size() < 21) {
                scanWorkbook(wb, fmt, digits, 1, 2);
            }

            if (lanByCode.isEmpty()) {
                throw new IllegalStateException("No län found in SCB workbook.");
            }

        } catch (Exception e) {
            throw new RuntimeException("SCB parse failed: " + e.getMessage(), e);
        }
    }

    private void scanWorkbook(Workbook wb, DataFormatter fmt, Pattern digits, int codeCol, int nameCol) {
        for (int s = 0; s < wb.getNumberOfSheets(); s++) {
            Sheet sheet = wb.getSheetAt(s);
            if (sheet == null) continue;

            for (int r = 0; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                String code = fmt.formatCellValue(row.getCell(codeCol)).trim();
                String name = fmt.formatCellValue(row.getCell(nameCol)).trim();
                if (code.isEmpty() || name.isEmpty()) continue;
                if (!digits.matcher(code).matches()) continue;

                if (code.length() <= 2) {
                    String lanCode = leftPad(code, 2);
                    lanByCode.putIfAbsent(lanCode, new LanDto(lanCode, name));
                } else if (code.length() == 4) {
                    String komCode = leftPad(code, 4);
                    String lanCode = komCode.substring(0, 2);
                    kommunByLan.computeIfAbsent(lanCode, k -> new ArrayList<>())
                            .add(new KommunDto(komCode, name, lanCode, null));
                }
            }
        }
    }

    private Workbook openWorkbook(byte[] data) {
        try {
            return new HSSFWorkbook(new ByteArrayInputStream(data));
        } catch (Exception e) {
            try {
                return new XSSFWorkbook(new ByteArrayInputStream(data));
            } catch (Exception ex) {
                throw new RuntimeException("Failed to open SCB workbook as .xls or .xlsx", ex);
            }
        }
    }


    private String normalizeLanCode(String lanCode) {
        if (lanCode == null) return null;
        String t = lanCode.trim();
        return t.matches("\\d") ? "0" + t : t;
    }

    private String leftPad(String s, int len) {
        if (s == null || !s.matches("\\d+")) return s;
        return s.length() >= len ? s : "0".repeat(len - s.length()) + s;
    }
}
