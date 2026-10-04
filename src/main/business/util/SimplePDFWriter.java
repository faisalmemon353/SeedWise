package business.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * A minimal PDF writer to generate basic text-only PDF files without external libraries.
 */
public class SimplePDFWriter {

    public static void writePDF(String filePath, String title, String content) throws IOException {
        try (FileOutputStream out = new FileOutputStream(filePath)) {
            writePDF(out, title, content);
        }
    }

    public static void writePDF(OutputStream out, String title, String content) throws IOException {
        List<Long> offsets = new ArrayList<>();
        
        // 1. Header
        out.write("%PDF-1.1\n".getBytes(StandardCharsets.US_ASCII));
        
        // 2. Objects
        
        // Catalog
        long currentOffset = 9; // Length of "%PDF-1.1\n"
        String obj1 = "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n";
        offsets.add(currentOffset);
        out.write(obj1.getBytes(StandardCharsets.US_ASCII));
        currentOffset += obj1.getBytes(StandardCharsets.US_ASCII).length;
        
        // Pages
        String obj2 = "2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n";
        offsets.add(currentOffset);
        out.write(obj2.getBytes(StandardCharsets.US_ASCII));
        currentOffset += obj2.getBytes(StandardCharsets.US_ASCII).length;
        
        // Page
        String obj3 = "3 0 obj\n<< /Type /Page /Parent 2 0 R /Resources << /Font << /F1 4 0 R >> >> /MediaBox [0 0 612 792] /Contents 5 0 R >>\nendobj\n";
        offsets.add(currentOffset);
        out.write(obj3.getBytes(StandardCharsets.US_ASCII));
        currentOffset += obj3.getBytes(StandardCharsets.US_ASCII).length;
        
        // Font
        String obj4 = "4 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n";
        offsets.add(currentOffset);
        out.write(obj4.getBytes(StandardCharsets.US_ASCII));
        currentOffset += obj4.getBytes(StandardCharsets.US_ASCII).length;
        
        // Contents (The Stream)
        StringBuilder stream = new StringBuilder();
        stream.append("BT\n/F1 14 Tf\n100 700 Td\n");
        
        // Add Title
        stream.append("(").append(escapePDF(title)).append(") Tj\n");
        stream.append("0 -20 Td\n"); // Move down
        stream.append("/F1 10 Tf\n"); // Switch to smaller font for content
        
        // Split content by lines and add
        String[] lines = content.split("\n");
        int linesOnPage = 0;
        for (String line : lines) {
            if (line.trim().isEmpty()) {
                stream.append("0 -12 Td\n");
                linesOnPage++;
                continue;
            }
            // Basic line wrapping/clipping for safety (PDF doesn't auto-wrap)
            String safeLine = line.length() > 80 ? line.substring(0, 77) + "..." : line;
            stream.append("(").append(escapePDF(safeLine)).append(") Tj\n");
            stream.append("0 -12 Td\n");
            linesOnPage++;
            
            // If page is full, we'd need multiple pages, but let's keep it simple for now (approx 50 lines)
            if (linesOnPage > 55) break; 
        }
        stream.append("ET");
        
        byte[] streamBytes = stream.toString().getBytes(StandardCharsets.US_ASCII);
        String obj5Head = "5 0 obj\n<< /Length " + streamBytes.length + " >>\nstream\n";
        String obj5Foot = "\nendstream\nendobj\n";
        
        offsets.add(currentOffset);
        out.write(obj5Head.getBytes(StandardCharsets.US_ASCII));
        out.write(streamBytes);
        out.write(obj5Foot.getBytes(StandardCharsets.US_ASCII));
        currentOffset += obj5Head.getBytes(StandardCharsets.US_ASCII).length + streamBytes.length + obj5Foot.getBytes(StandardCharsets.US_ASCII).length;
        
        // 3. Cross-Reference Table
        long xrefOffset = currentOffset;
        out.write("xref\n".getBytes(StandardCharsets.US_ASCII));
        out.write("0 6\n".getBytes(StandardCharsets.US_ASCII));
        out.write("0000000000 65535 f \n".getBytes(StandardCharsets.US_ASCII));
        for (Long offset : offsets) {
            out.write(String.format("%010d 00000 n \n", offset).getBytes(StandardCharsets.US_ASCII));
        }
        
        // 4. Trailer
        out.write("trailer\n<< /Size 6 /Root 1 0 R >>\n".getBytes(StandardCharsets.US_ASCII));
        out.write("startxref\n".getBytes(StandardCharsets.US_ASCII));
        out.write(String.valueOf(xrefOffset).getBytes(StandardCharsets.US_ASCII));
        out.write("\n%%EOF\n".getBytes(StandardCharsets.US_ASCII));
    }

    private static String escapePDF(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }
}
