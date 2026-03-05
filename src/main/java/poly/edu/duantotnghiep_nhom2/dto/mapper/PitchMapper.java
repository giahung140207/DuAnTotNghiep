package poly.edu.duantotnghiep_nhom2.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import poly.edu.duantotnghiep_nhom2.dto.PitchDTO;
import poly.edu.duantotnghiep_nhom2.entity.Pitch;

import java.util.List;

@Mapper(config = BaseMapperConfig.class)
public interface PitchMapper {

    // Map các trường lồng nhau từ Facility ra ngoài DTO
    @Mapping(source = "facility.id", target = "facilityId")
    @Mapping(source = "facility.name", target = "facilityName")
    @Mapping(source = "facility.address", target = "facilityAddress")
    PitchDTO toDTO(Pitch pitch);

    // Map ngược lại (thường ít dùng hơn)
    Pitch toEntity(PitchDTO pitchDTO);

    // Map danh sách
    List<PitchDTO> toDTOs(List<Pitch> pitches);
}