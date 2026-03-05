package poly.edu.duantotnghiep_nhom2.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import poly.edu.duantotnghiep_nhom2.dto.UserDTO;
import poly.edu.duantotnghiep_nhom2.entity.User;

@Mapper(config = BaseMapperConfig.class)
public interface UserMapper {

    // Entity -> DTO
    UserDTO toDTO(User user);

    // DTO -> Entity (Bỏ qua password và role để bảo mật, thường update profile không đổi role)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    User toEntity(UserDTO userDTO);
}