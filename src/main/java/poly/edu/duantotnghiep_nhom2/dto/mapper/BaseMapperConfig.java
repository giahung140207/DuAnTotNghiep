package poly.edu.duantotnghiep_nhom2.dto.mapper;
import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;

// Cấu hình chung cho tất cả các Mapper:
// - componentModel = "spring": Để @Autowired được vào Service
// - unmappedTargetPolicy = IGNORE: Bỏ qua cảnh báo nếu DTO thiếu trường so với Entity
@MapperConfig(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BaseMapperConfig {
}
