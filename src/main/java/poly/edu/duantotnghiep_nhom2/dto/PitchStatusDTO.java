package poly.edu.duantotnghiep_nhom2.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import poly.edu.duantotnghiep_nhom2.entity.Booking;
import poly.edu.duantotnghiep_nhom2.entity.Pitch;

@Data
@NoArgsConstructor

public class PitchStatusDTO {
    private Pitch pitch;
    private String currentStatus; // "FREE", "OCCUPIED", "MAINTENANCE"
    private Booking currentBooking; // Nếu đang đá thì có booking này
    private long minutesLeft; // Thời gian còn lại (nếu đang đá)

    // Constructor thủ công
    public PitchStatusDTO(Pitch pitch, String currentStatus, Booking currentBooking, long minutesLeft) {
        this.pitch = pitch;
        this.currentStatus = currentStatus;
        this.currentBooking = currentBooking;
        this.minutesLeft = minutesLeft;
    }
    
    // Getters Setters thủ công
    public Pitch getPitch() { return pitch; }
    public void setPitch(Pitch pitch) { this.pitch = pitch; }
    public String getCurrentStatus() { return currentStatus; }
    public void setCurrentStatus(String currentStatus) { this.currentStatus = currentStatus; }
    public Booking getCurrentBooking() { return currentBooking; }
    public void setCurrentBooking(Booking currentBooking) { this.currentBooking = currentBooking; }
    public long getMinutesLeft() { return minutesLeft; }
    public void setMinutesLeft(long minutesLeft) { this.minutesLeft = minutesLeft; }
}
