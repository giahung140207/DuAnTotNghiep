package poly.edu.duantotnghiep_nhom2.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import poly.edu.duantotnghiep_nhom2.dto.BookingDTO;
import poly.edu.duantotnghiep_nhom2.entity.Booking;

import java.util.List;

@Mapper(config = BaseMapperConfig.class)
public interface BookingMapper {

    // Lấy tên User
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.fullName", target = "userFullName")

    // Lấy thông tin Sân và Cơ sở
    @Mapping(source = "pitch.id", target = "pitchId")
    @Mapping(source = "pitch.name", target = "pitchName")
    @Mapping(source = "pitch.facility.name", target = "facilityName")
    @Mapping(source = "pitch.facility.address", target = "facilityAddress")
    BookingDTO toDTO(Booking booking);

    Booking toEntity(BookingDTO bookingDTO);

    List<BookingDTO> toDTOs(List<Booking> bookings);
}