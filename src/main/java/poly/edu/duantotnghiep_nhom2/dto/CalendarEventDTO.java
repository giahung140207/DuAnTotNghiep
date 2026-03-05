package poly.edu.duantotnghiep_nhom2.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CalendarEventDTO {
    private String title;
    private String start;
    private String end;
    private String backgroundColor;
    private String borderColor;
    private String textColor;
    private String description;

    // Constructor thủ công
    public CalendarEventDTO(String title, String start, String end, String backgroundColor, String borderColor, String textColor, String description) {
        this.title = title;
        this.start = start;
        this.end = end;
        this.backgroundColor = backgroundColor;
        this.borderColor = borderColor;
        this.textColor = textColor;
        this.description = description;
    }

    // --- GETTERS AND SETTERS THỦ CÔNG (Fix lỗi Jackson) ---

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public String getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(String borderColor) {
        this.borderColor = borderColor;
    }

    public String getTextColor() {
        return textColor;
    }

    public void setTextColor(String textColor) {
        this.textColor = textColor;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
